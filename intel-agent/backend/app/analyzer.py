"""Evidence gap analysis and campaign angle generation."""

from __future__ import annotations

import re
from collections import Counter
from typing import Optional

from .models import (
    CampaignAngle,
    ConfidenceLevel,
    EvidenceCategory,
    EvidenceGap,
    EvidenceItem,
    EvidencePack,
)

CATEGORY_SEARCH_HINTS: dict[EvidenceCategory, list[str]] = {
    EvidenceCategory.MESSAGING: ["/about", "/", "/company"],
    EvidenceCategory.PRICING: ["/pricing", "/plans", "/packages"],
    EvidenceCategory.POSITIONING: ["/about", "/why-us", "/compare"],
    EvidenceCategory.LANDING_PAGE: ["/", "/product", "/solutions"],
    EvidenceCategory.REVIEWS: ["/reviews", "/testimonials", "/customers"],
    EvidenceCategory.COMPLAINTS: ["/support", "/help", "/faq"],
    EvidenceCategory.FEATURES: ["/features", "/product", "/platform"],
    EvidenceCategory.SOCIAL_PROOF: ["/customers", "/case-studies", "/testimonials"],
}


def analyze_gaps(pack: EvidencePack) -> list[EvidenceGap]:
    gaps: list[EvidenceGap] = []

    existing = set()
    for item in pack.evidence:
        existing.add((item.category, item.competitor))

    for competitor in pack.competitors:
        for category in pack.focus_areas:
            if (category, competitor.name) not in existing:
                base_url = competitor.url or f"https://www.{competitor.name.lower().replace(' ', '')}.com"
                hints = CATEGORY_SEARCH_HINTS.get(category, ["/"])
                suggested = [f"{base_url.rstrip('/')}{path}" for path in hints]

                gaps.append(EvidenceGap(
                    category=category,
                    competitor=competitor.name,
                    description=f"No {category.value} evidence found for {competitor.name}",
                    suggested_sources=suggested,
                ))

    return gaps


def _extract_keywords(text: str) -> list[str]:
    words = re.findall(r"\b[a-z]{4,}\b", text.lower())
    stop = {
        "this", "that", "with", "from", "your", "have", "been", "they",
        "their", "will", "more", "than", "also", "what", "when", "which",
        "about", "into", "each", "make", "like", "just", "over", "such",
        "only", "very", "some", "most", "other", "after", "before",
    }
    return [w for w in words if w not in stop]


def _find_weaknesses(
    evidence: list[EvidenceItem], competitor: str
) -> list[dict[str, str]]:
    weaknesses = []
    comp_evidence = [e for e in evidence if e.competitor == competitor]

    complaint_evidence = [
        e for e in comp_evidence if e.category == EvidenceCategory.COMPLAINTS
    ]
    for item in complaint_evidence:
        weaknesses.append({
            "area": "Customer Pain Points",
            "detail": item.supporting_text[:200],
            "evidence_id": item.id,
        })

    pricing_evidence = [
        e for e in comp_evidence if e.category == EvidenceCategory.PRICING
    ]
    for item in pricing_evidence:
        text = item.supporting_text.lower()
        if any(kw in text for kw in ["expensive", "enterprise only", "contact sales"]):
            weaknesses.append({
                "area": "Pricing Barriers",
                "detail": item.supporting_text[:200],
                "evidence_id": item.id,
            })

    missing_categories = set(EvidenceCategory) - {e.category for e in comp_evidence}
    if EvidenceCategory.SOCIAL_PROOF in missing_categories:
        weaknesses.append({
            "area": "Limited Social Proof",
            "detail": f"{competitor} lacks visible customer testimonials or case studies",
            "evidence_id": "",
        })

    return weaknesses


def generate_campaign_angles(pack: EvidencePack) -> list[CampaignAngle]:
    angles: list[CampaignAngle] = []

    for competitor in pack.competitors:
        comp_evidence = [
            e for e in pack.evidence if e.competitor == competitor.name
        ]
        if not comp_evidence:
            continue

        weaknesses = _find_weaknesses(pack.evidence, competitor.name)

        # Angle 1: Feature comparison
        feature_evidence = [
            e for e in comp_evidence if e.category == EvidenceCategory.FEATURES
        ]
        if feature_evidence:
            evidence_ids = [e.id for e in feature_evidence]
            angles.append(CampaignAngle(
                title=f"Feature superiority vs {competitor.name}",
                description=(
                    f"Highlight areas where our solution outperforms "
                    f"{competitor.name}'s feature set. Focus on capabilities "
                    f"they don't mention or underemphasize."
                ),
                target_weakness="Feature gaps in competitor offering",
                supporting_evidence_ids=evidence_ids,
                channel_fit=["Landing Page", "Email", "Comparison Page"],
                confidence=ConfidenceLevel.MEDIUM,
            ))

        # Angle 2: Pricing advantage
        pricing_evidence = [
            e for e in comp_evidence if e.category == EvidenceCategory.PRICING
        ]
        if pricing_evidence:
            evidence_ids = [e.id for e in pricing_evidence]
            text = " ".join(e.supporting_text for e in pricing_evidence).lower()
            has_expensive_signals = any(
                kw in text for kw in ["enterprise", "contact", "custom", "$99", "$199"]
            )
            angles.append(CampaignAngle(
                title=f"Pricing transparency vs {competitor.name}",
                description=(
                    f"Position our transparent/competitive pricing against "
                    f"{competitor.name}'s pricing model. "
                    + ("Their pricing appears enterprise-focused — "
                       "emphasize accessibility and value." if has_expensive_signals
                       else "Compare tier-for-tier value proposition.")
                ),
                target_weakness="Pricing opacity or premium positioning",
                supporting_evidence_ids=evidence_ids,
                channel_fit=["PPC", "Landing Page", "Email"],
                confidence=ConfidenceLevel.HIGH if has_expensive_signals else ConfidenceLevel.MEDIUM,
            ))

        # Angle 3: Customer pain point exploitation
        for weakness in weaknesses:
            if weakness["area"] == "Customer Pain Points":
                angles.append(CampaignAngle(
                    title=f"Address {competitor.name}'s known pain points",
                    description=(
                        f"Target users frustrated with {competitor.name}. "
                        f"Pain point: {weakness['detail'][:150]}"
                    ),
                    target_weakness=weakness["detail"][:100],
                    supporting_evidence_ids=[weakness["evidence_id"]] if weakness["evidence_id"] else [],
                    channel_fit=["Search Ads", "Review Sites", "Social Media"],
                    confidence=ConfidenceLevel.HIGH,
                ))

        # Angle 4: Messaging counter-positioning
        messaging_evidence = [
            e for e in comp_evidence if e.category == EvidenceCategory.MESSAGING
        ]
        if messaging_evidence:
            evidence_ids = [e.id for e in messaging_evidence]
            keywords = []
            for e in messaging_evidence:
                keywords.extend(_extract_keywords(e.supporting_text))
            top_themes = [w for w, _ in Counter(keywords).most_common(5)]
            angles.append(CampaignAngle(
                title=f"Counter-position {competitor.name}'s narrative",
                description=(
                    f"{competitor.name} emphasizes: {', '.join(top_themes)}. "
                    f"Develop messaging that either outperforms on these themes "
                    f"or redirects the conversation to our strengths."
                ),
                target_weakness="Competitor messaging focus areas",
                supporting_evidence_ids=evidence_ids,
                channel_fit=["Content Marketing", "Social Media", "PR"],
                confidence=ConfidenceLevel.MEDIUM,
            ))

        # Angle 5: Social proof gap
        social_evidence = [
            e for e in comp_evidence
            if e.category in (EvidenceCategory.SOCIAL_PROOF, EvidenceCategory.REVIEWS)
        ]
        if not social_evidence:
            angles.append(CampaignAngle(
                title=f"Social proof advantage over {competitor.name}",
                description=(
                    f"{competitor.name} has limited visible social proof. "
                    f"Lead with customer testimonials, case studies, and "
                    f"trust signals in competitive placements."
                ),
                target_weakness="Limited social proof",
                supporting_evidence_ids=[],
                channel_fit=["Landing Page", "Review Sites", "Email"],
                confidence=ConfidenceLevel.MEDIUM,
            ))

    # Cross-competitor angles
    if len(pack.competitors) > 1:
        all_messaging = [
            e for e in pack.evidence if e.category == EvidenceCategory.MESSAGING
        ]
        if all_messaging:
            all_keywords = []
            for e in all_messaging:
                all_keywords.extend(_extract_keywords(e.supporting_text))
            common_themes = [w for w, c in Counter(all_keywords).most_common(5) if c > 1]
            if common_themes:
                angles.append(CampaignAngle(
                    title="Industry-wide messaging gaps",
                    description=(
                        f"All competitors focus on: {', '.join(common_themes)}. "
                        f"Find an underserved angle or differentiate on a dimension "
                        f"no competitor owns."
                    ),
                    target_weakness="Category messaging saturation",
                    supporting_evidence_ids=[e.id for e in all_messaging],
                    channel_fit=["Brand Campaign", "Content", "Thought Leadership"],
                    confidence=ConfidenceLevel.HIGH,
                ))

    return angles
