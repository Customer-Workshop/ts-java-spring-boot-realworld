"""FastAPI application for the Market Intelligence Agent."""

from __future__ import annotations

import asyncio
import logging
from contextlib import asynccontextmanager
from typing import Optional

from fastapi import BackgroundTasks, FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import PlainTextResponse

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

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


async def _run_research(pack_id: str, request: ResearchRequest) -> None:
    try:
        def on_status(p: EvidencePack):
            save_pack(p)

        pack = await agent.run(request, on_status=on_status)
        pack.id = pack_id
        save_pack(pack)
        logger.info("Research %s completed with %d evidence items", pack_id, len(pack.evidence))
    except Exception as e:
        logger.error("Research %s failed: %s", pack_id, e)
        pack = load_pack(pack_id)
        if pack:
            pack.status = ResearchStatus.FAILED
            pack.error = str(e)
            save_pack(pack)
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
    save_pack(pack)

    task = asyncio.create_task(_run_research(pack.id, request))
    _running_tasks[pack.id] = task

    return {"id": pack.id, "status": pack.status.value}


@app.get("/api/research/{pack_id}", response_model=dict)
async def get_research(pack_id: str):
    """Get the current state of a research task."""
    pack = load_pack(pack_id)
    if not pack:
        raise HTTPException(status_code=404, detail="Research pack not found")
    return pack.model_dump(mode="json")


@app.get("/api/research/{pack_id}/report", response_class=PlainTextResponse)
async def get_report(pack_id: str):
    """Get the Markdown report for a completed research pack."""
    pack = load_pack(pack_id)
    if not pack:
        raise HTTPException(status_code=404, detail="Research pack not found")
    return generate_markdown_report(pack)


@app.get("/api/research", response_model=list)
async def list_research():
    """List all research packs."""
    return list_packs()


@app.delete("/api/research/{pack_id}")
async def delete_research(pack_id: str):
    """Delete a research pack."""
    if pack_id in _running_tasks:
        _running_tasks[pack_id].cancel()
        _running_tasks.pop(pack_id, None)
    if not delete_pack(pack_id):
        raise HTTPException(status_code=404, detail="Research pack not found")
    return {"deleted": True}


@app.get("/api/health")
async def health():
    return {"status": "ok", "agent": "Market Intelligence Agent v1.0.0"}
