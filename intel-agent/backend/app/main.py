"""FastAPI application for the Market Intelligence Agent."""

from __future__ import annotations

import asyncio
import logging
import os
import time
from collections import defaultdict
from contextlib import asynccontextmanager
from typing import Optional

from fastapi import BackgroundTasks, FastAPI, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse, PlainTextResponse

from .agent import IntelligenceAgent
from .database import delete_pack, init_db, list_packs, load_pack, save_pack
from .models import EvidencePack, ResearchRequest, ResearchStatus
from .report import generate_markdown_report

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

agent = IntelligenceAgent()
_running_tasks: dict[str, asyncio.Task] = {}


@asynccontextmanager
async def lifespan(app: FastAPI):
    init_db()
    yield
    for task in _running_tasks.values():
        task.cancel()


app = FastAPI(
    title="Market Intelligence Agent",
    description=(
        "Agentic competitor intelligence system that iteratively gathers evidence "
        "on competitor messaging, pricing, positioning, landing page patterns, "
        "reviews, and complaints — then produces an evidence pack with campaign angles."
    ),
    version="1.0.0",
    lifespan=lifespan,
)

ALLOWED_ORIGINS = os.getenv(
    "CORS_ORIGINS",
    "http://localhost:4200,http://localhost:5173,http://127.0.0.1:4200,http://127.0.0.1:5173",
).split(",")

app.add_middleware(
    CORSMiddleware,
    allow_origins=ALLOWED_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Simple in-memory rate limiter: max 10 research requests per minute per IP
_rate_limit: dict[str, list[float]] = defaultdict(list)
RATE_LIMIT_MAX = 10
RATE_LIMIT_WINDOW = 60  # seconds
_SWEEP_THRESHOLD = 1000


def _sweep_stale_entries(now: float) -> None:
    """Remove all IP entries with no recent timestamps to bound memory."""
    stale = [ip for ip, ts in _rate_limit.items() if not any(now - t < RATE_LIMIT_WINDOW for t in ts)]
    for ip in stale:
        del _rate_limit[ip]


@app.middleware("http")
async def rate_limit_middleware(request: Request, call_next):
    if request.url.path == "/api/research" and request.method == "POST":
        client_ip = request.client.host if request.client else "unknown"
        now = time.time()
        if len(_rate_limit) > _SWEEP_THRESHOLD:
            _sweep_stale_entries(now)
        recent = [t for t in _rate_limit[client_ip] if now - t < RATE_LIMIT_WINDOW]
        if not recent:
            _rate_limit.pop(client_ip, None)
        else:
            _rate_limit[client_ip] = recent
        if len(recent) >= RATE_LIMIT_MAX:
            return JSONResponse(
                status_code=429,
                content={"detail": "Too many research requests. Please try again later."},
            )
        _rate_limit[client_ip].append(now)
    return await call_next(request)


async def _run_research(pack_id: str, request: ResearchRequest) -> None:
    try:
        async def on_status(p: EvidencePack):
            p.id = pack_id
            await asyncio.to_thread(save_pack, p)

        pack = await agent.run(request, on_status=on_status)
        pack.id = pack_id
        await asyncio.to_thread(save_pack, pack)
        logger.info("Research %s completed with %d evidence items", pack_id, len(pack.evidence))
    except Exception as e:
        logger.error("Research %s failed: %s", pack_id, e)
        pack = await asyncio.to_thread(load_pack, pack_id)
        if pack:
            pack.status = ResearchStatus.FAILED
            pack.error = str(e)
            await asyncio.to_thread(save_pack, pack)
    finally:
        _running_tasks.pop(pack_id, None)


@app.post("/api/research", response_model=dict)
async def start_research(request: ResearchRequest):
    """Start a new competitor research task.

    The agent will iteratively scrape competitor sites, extract evidence,
    analyze gaps, and generate campaign angles. Returns immediately with
    the pack ID — poll /api/research/{id} for status updates.
    """
    pack = EvidencePack(
        competitors=request.competitors,
        focus_areas=request.focus_areas,
        campaign_context=request.campaign_context,
        status=ResearchStatus.SCRAPING,
    )
    await asyncio.to_thread(save_pack, pack)

    task = asyncio.create_task(_run_research(pack.id, request))
    _running_tasks[pack.id] = task

    return {"id": pack.id, "status": pack.status.value}


@app.get("/api/research/{pack_id}", response_model=dict)
async def get_research(pack_id: str):
    """Get the current state of a research task."""
    pack = await asyncio.to_thread(load_pack, pack_id)
    if not pack:
        raise HTTPException(status_code=404, detail="Research pack not found")
    return pack.model_dump(mode="json")


@app.get("/api/research/{pack_id}/report", response_class=PlainTextResponse)
async def get_report(pack_id: str):
    """Get the Markdown report for a completed research pack."""
    pack = await asyncio.to_thread(load_pack, pack_id)
    if not pack:
        raise HTTPException(status_code=404, detail="Research pack not found")
    return await asyncio.to_thread(generate_markdown_report, pack)


@app.get("/api/research", response_model=list)
async def list_research():
    """List all research packs."""
    return await asyncio.to_thread(list_packs)


@app.delete("/api/research/{pack_id}")
async def delete_research(pack_id: str):
    """Delete a research pack."""
    if pack_id in _running_tasks:
        _running_tasks[pack_id].cancel()
        _running_tasks.pop(pack_id, None)
    if not await asyncio.to_thread(delete_pack, pack_id):
        raise HTTPException(status_code=404, detail="Research pack not found")
    return {"deleted": True}


@app.get("/api/health")
async def health():
    return {"status": "ok", "agent": "Market Intelligence Agent v1.0.0"}
