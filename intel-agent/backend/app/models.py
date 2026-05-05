"""Domain models for the Market Intelligence Agent."""

from __future__ import annotations

import uuid
from datetime import datetime
from enum import Enum
from typing import Optional

from pydantic import BaseModel, Field


class ResearchStatus(str, Enum):
    PENDING = "PENDING"
    SCRAPING = "SCRAPING"
    ANALYZING = "ANALYZING"
    ITERATING = "ITERATING"
    COMPLETED = "COMPLETED"
    FAILED = "FAILED"


class EvidenceCategory(str, Enum):
    MESSAGING = "messaging"
    PRICING = "pricing"
    POSITIONING = "positioning"
    LANDING_PAGE = "landing_page"
    REVIEWS = "reviews"
    COMPLAINTS = "complaints"
    FEATURES = "features"
    SOCIAL_PROOF = "social_proof"


class ConfidenceLevel(str, Enum):
    HIGH = "high"
    MEDIUM = "medium"
    LOW = "low"


class CompetitorInput(BaseModel):
    name: str
    url: Optional[str] = None
    description: Optional[str] = None


class ResearchRequest(BaseModel):
    competitors: list[CompetitorInput]
    focus_areas: list[EvidenceCategory] = Field(
        default_factory=lambda: list(EvidenceCategory)
    )
    campaign_context: Optional[str] = None
    max_iterations: int = Field(default=3, ge=1, le=5)


class Citation(BaseModel):
    url: str
    title: str
    snippet: str
    fetched_at: datetime = Field(default_factory=datetime.utcnow)


class EvidenceItem(BaseModel):
    id: str = Field(default_factory=lambda: str(uuid.uuid4())[:8])
    category: EvidenceCategory
    competitor: str
    claim: str
    supporting_text: str
    citations: list[Citation] = Field(default_factory=list)
    confidence: ConfidenceLevel = ConfidenceLevel.MEDIUM
    tags: list[str] = Field(default_factory=list)


class EvidenceGap(BaseModel):
    category: EvidenceCategory
    competitor: str
    description: str
    suggested_sources: list[str] = Field(default_factory=list)


class CampaignAngle(BaseModel):
    title: str
    description: str
    target_weakness: str
    supporting_evidence_ids: list[str] = Field(default_factory=list)
    channel_fit: list[str] = Field(default_factory=list)
    confidence: ConfidenceLevel = ConfidenceLevel.MEDIUM


class ResearchIteration(BaseModel):
    iteration: int
    gaps_found: list[EvidenceGap]
    evidence_added: int
    gaps_resolved: int
    timestamp: datetime = Field(default_factory=datetime.utcnow)


class EvidencePack(BaseModel):
    id: str = Field(default_factory=lambda: str(uuid.uuid4()))
    status: ResearchStatus = ResearchStatus.PENDING
    competitors: list[CompetitorInput] = Field(default_factory=list)
    focus_areas: list[EvidenceCategory] = Field(default_factory=list)
    campaign_context: Optional[str] = None
    evidence: list[EvidenceItem] = Field(default_factory=list)
    gaps: list[EvidenceGap] = Field(default_factory=list)
    campaign_angles: list[CampaignAngle] = Field(default_factory=list)
    iterations: list[ResearchIteration] = Field(default_factory=list)
    created_at: datetime = Field(default_factory=datetime.utcnow)
    completed_at: Optional[datetime] = None
    error: Optional[str] = None

    @property
    def evidence_by_competitor(self) -> dict[str, list[EvidenceItem]]:
        result: dict[str, list[EvidenceItem]] = {}
        for item in self.evidence:
            result.setdefault(item.competitor, []).append(item)
        return result

    @property
    def evidence_by_category(self) -> dict[EvidenceCategory, list[EvidenceItem]]:
        result: dict[EvidenceCategory, list[EvidenceItem]] = {}
        for item in self.evidence:
            result.setdefault(item.category, []).append(item)
        return result

    @property
    def coverage_score(self) -> float:
        if not self.focus_areas or not self.competitors:
            return 0.0
        total_cells = len(self.focus_areas) * len(self.competitors)
        covered = set()
        for item in self.evidence:
            covered.add((item.category, item.competitor))
        return len(covered) / total_cells * 100
