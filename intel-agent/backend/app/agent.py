"""Agentic research loop with iterative gap resolution."""

from __future__ import annotations

import asyncio
import logging
from datetime import datetime
from typing import Optional

import httpx

from .analyzer import analyze_gaps, generate_campaign_angles
from .models import (
    CompetitorInput,
    EvidenceCategory,
    EvidenceGap,
    EvidenceItem,
    EvidencePack,
    ResearchIteration,
    ResearchRequest,
    ResearchStatus,
)
from .scraper import discover_subpages, extract_evidence_from_page, scrape_page

logger = logging.getLogger(__name__)


class IntelligenceAgent:
    """Iterative competitor intelligence agent.

    The agent follows this loop:
      1. Scrape competitor pages
      2. Extract evidence
      3. Analyze gaps
      4. If gaps remain and iterations left, fetch suggested sources
      5. Repeat until coverage is sufficient or max iterations reached
      6. Generate campaign angles from collected evidence
    """

    def __init__(self, max_concurrent: int = 5):
        self._max_concurrent = max_concurrent
        self._semaphore = asyncio.Semaphore(max_concurrent)

    async def _fetch_with_limit(
        self, url: str, client: httpx.AsyncClient
    ) -> tuple[str, object]:
        async with self._semaphore:
            page = await scrape_page(url, client)
            return url, page

    async def run(
        self,
        request: ResearchRequest,
        on_status: Optional[callable] = None,
    ) -> EvidencePack:
        pack = EvidencePack(
            competitors=request.competitors,
            focus_areas=request.focus_areas,
            campaign_context=request.campaign_context,
            status=ResearchStatus.SCRAPING,
        )

        if on_status:
            on_status(pack)

        async with httpx.AsyncClient() as client:
            # Phase 1: Initial scraping
            logger.info("Phase 1: Scraping %d competitors", len(request.competitors))
            for competitor in request.competitors:
                urls = await self._get_competitor_urls(competitor, client)
                evidence = await self._scrape_urls(urls, competitor.name, client)
                pack.evidence.extend(evidence)

            # Phase 2: Iterative gap resolution
            for iteration in range(1, request.max_iterations + 1):
                pack.status = ResearchStatus.ANALYZING
                if on_status:
                    on_status(pack)

                gaps = analyze_gaps(pack)
                if not gaps:
                    logger.info("Iteration %d: No gaps found, done", iteration)
                    break

                logger.info(
                    "Iteration %d: Found %d gaps, fetching more sources",
                    iteration, len(gaps),
                )
                pack.status = ResearchStatus.ITERATING
                if on_status:
                    on_status(pack)

                evidence_before = len(pack.evidence)
                resolved = 0

                for gap in gaps:
                    new_evidence = await self._resolve_gap(gap, client)
                    if new_evidence:
                        pack.evidence.extend(new_evidence)
                        resolved += 1

                pack.iterations.append(ResearchIteration(
                    iteration=iteration,
                    gaps_found=gaps,
                    evidence_added=len(pack.evidence) - evidence_before,
                    gaps_resolved=resolved,
                ))

                if resolved == 0:
                    logger.info("Iteration %d: No gaps resolved, stopping", iteration)
                    break

            # Phase 3: Final gap analysis and angle generation
            pack.gaps = analyze_gaps(pack)
            pack.campaign_angles = generate_campaign_angles(pack)
            pack.status = ResearchStatus.COMPLETED
            pack.completed_at = datetime.utcnow()

            if on_status:
                on_status(pack)

        return pack

    async def _get_competitor_urls(
        self, competitor: CompetitorInput, client: httpx.AsyncClient
    ) -> list[str]:
        if competitor.url:
            base = competitor.url.rstrip("/")
            if not base.startswith("http"):
                base = f"https://{base}"
            return await discover_subpages(base, client, max_pages=5)

        guessed = f"https://www.{competitor.name.lower().replace(' ', '')}.com"
        return await discover_subpages(guessed, client, max_pages=5)

    async def _scrape_urls(
        self,
        urls: list[str],
        competitor_name: str,
        client: httpx.AsyncClient,
    ) -> list[EvidenceItem]:
        evidence: list[EvidenceItem] = []
        tasks = [self._fetch_with_limit(url, client) for url in urls]
        results = await asyncio.gather(*tasks, return_exceptions=True)

        for result in results:
            if isinstance(result, Exception):
                logger.warning("Scrape error: %s", result)
                continue
            url, page = result
            items = extract_evidence_from_page(page, competitor_name)
            evidence.extend(items)

        return evidence

    async def _resolve_gap(
        self, gap: EvidenceGap, client: httpx.AsyncClient
    ) -> list[EvidenceItem]:
        evidence: list[EvidenceItem] = []
        for url in gap.suggested_sources[:3]:
            try:
                page = await scrape_page(url, client)
                items = extract_evidence_from_page(page, gap.competitor)
                relevant = [
                    item for item in items if item.category == gap.category
                ]
                evidence.extend(relevant)
                if relevant:
                    break
            except Exception as e:
                logger.warning("Gap resolution failed for %s: %s", url, e)
                continue
        return evidence
