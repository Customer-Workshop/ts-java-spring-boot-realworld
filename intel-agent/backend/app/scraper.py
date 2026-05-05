"""Web scraping tools for competitor intelligence gathering."""

from __future__ import annotations

import asyncio
import ipaddress
import logging
import re
import socket
from dataclasses import dataclass, field
from datetime import datetime
from typing import Optional
from urllib.parse import urljoin, urlparse

import httpx
from bs4 import BeautifulSoup, Tag

from .models import Citation, ConfidenceLevel, EvidenceCategory, EvidenceItem

logger = logging.getLogger(__name__)

HEADERS = {
    "User-Agent": (
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
        "AppleWebKit/537.36 (KHTML, like Gecko) "
        "Chrome/120.0.0.0 Safari/537.36"
    ),
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    "Accept-Language": "en-US,en;q=0.9",
}

PRICING_KEYWORDS = [
    "price", "pricing", "cost", "plan", "tier", "subscription",
    "free", "premium", "enterprise", "starter", "pro", "basic",
    "per month", "/mo", "/yr", "per year", "annually", "monthly",
    "discount", "save", "off", "deal", "offer",
]

MESSAGING_KEYWORDS = [
    "tagline", "hero", "headline", "value prop", "mission",
    "why choose", "how it works", "benefits", "features",
    "trusted by", "loved by", "used by",
]

REVIEW_KEYWORDS = [
    "review", "testimonial", "rating", "star", "feedback",
    "customer says", "what people say", "success story",
]

COMPLAINT_KEYWORDS = [
    "complaint", "issue", "problem", "bug", "broken",
    "disappointed", "frustrated", "poor", "bad experience",
    "doesn't work", "not working", "missing",
]

SOCIAL_PROOF_KEYWORDS = [
    "customers", "users", "companies", "trusted by",
    "case study", "success", "results", "roi",
    "fortune 500", "enterprise", "startup",
]


@dataclass
class ScrapedPage:
    url: str
    title: str
    meta_description: str
    headings: list[str]
    paragraphs: list[str]
    links: list[str]
    images: list[str]
    pricing_sections: list[str]
    testimonials: list[str]
    cta_buttons: list[str]
    fetched_at: datetime = field(default_factory=datetime.utcnow)
    status_code: int = 200
    error: Optional[str] = None


def _extract_text_blocks(soup: BeautifulSoup) -> list[str]:
    blocks = []
    for tag in soup.find_all(["p", "li", "span", "div"]):
        text = tag.get_text(strip=True)
        if len(text) > 20 and len(text) < 2000:
            blocks.append(text)
    return blocks


def _extract_headings(soup: BeautifulSoup) -> list[str]:
    headings = []
    for level in range(1, 7):
        for h in soup.find_all(f"h{level}"):
            text = h.get_text(strip=True)
            if text:
                headings.append(text)
    return headings


def _extract_pricing(soup: BeautifulSoup) -> list[str]:
    sections = []
    pricing_containers = soup.find_all(
        class_=re.compile(r"pric|plan|tier|package", re.I)
    )
    for container in pricing_containers:
        text = container.get_text(separator=" ", strip=True)
        if len(text) > 10:
            sections.append(text[:1000])

    for block in _extract_text_blocks(soup):
        lower = block.lower()
        if any(kw in lower for kw in ["$", "€", "£", "/mo", "/yr", "per month"]):
            sections.append(block)
    return sections[:20]


def _extract_testimonials(soup: BeautifulSoup) -> list[str]:
    testimonials = []
    containers = soup.find_all(
        class_=re.compile(r"testimon|review|quote|feedback", re.I)
    )
    for container in containers:
        text = container.get_text(separator=" ", strip=True)
        if len(text) > 20:
            testimonials.append(text[:500])

    for blockquote in soup.find_all("blockquote"):
        text = blockquote.get_text(strip=True)
        if text:
            testimonials.append(text[:500])
    return testimonials[:20]


def _extract_cta_buttons(soup: BeautifulSoup) -> list[str]:
    ctas = []
    for btn in soup.find_all(["button", "a"]):
        classes = " ".join(btn.get("class", []))
        text = btn.get_text(strip=True)
        if not text or len(text) > 50:
            continue
        lower = text.lower()
        if any(
            kw in lower
            for kw in [
                "start", "try", "get", "sign up", "demo", "buy",
                "subscribe", "contact", "learn more", "free",
            ]
        ) or "cta" in classes.lower() or "btn" in classes.lower():
            ctas.append(text)
    return list(set(ctas))[:15]


def _check_ip(ip_str: str) -> bool:
    """Return True if the IP is public (safe to connect to)."""
    ip = ipaddress.ip_address(ip_str)
    return not (ip.is_private or ip.is_loopback or ip.is_link_local or ip.is_reserved)


async def _validate_url(url: str) -> bool:
    """Validate URL scheme and that all resolved IPs are public.

    Uses asyncio.to_thread to avoid blocking the event loop.
    """
    parsed = urlparse(url)
    if parsed.scheme not in ("http", "https"):
        return False
    hostname = parsed.hostname
    if not hostname:
        return False
    try:
        resolved = await asyncio.to_thread(
            socket.getaddrinfo, hostname, None, socket.AF_UNSPEC, socket.SOCK_STREAM,
        )
    except socket.gaierror:
        return False
    for _, _, _, _, addr in resolved:
        if not _check_ip(addr[0]):
            return False
    return bool(resolved)


async def _safe_fetch(
    url: str, client: httpx.AsyncClient,
) -> httpx.Response:
    """Fetch a URL with SSRF protection: validate DNS, follow redirects safely.

    Uses the original URL (preserving hostname for TLS/SNI) but validates all
    resolved IPs before each request. A short per-request timeout limits the
    DNS rebinding window.
    """
    if not await _validate_url(url):
        raise _SsrfBlocked(url)

    resp = await client.get(url, headers=HEADERS, follow_redirects=False, timeout=5)
    redirects = 0
    while resp.is_redirect and redirects < 5:
        location = resp.headers.get("location", "")
        redirect_url = urljoin(str(resp.url), location)
        if not await _validate_url(redirect_url):
            raise _SsrfBlocked(redirect_url)
        resp = await client.get(redirect_url, headers=HEADERS, follow_redirects=False, timeout=5)
        redirects += 1
    return resp


class _SsrfBlocked(Exception):
    """Raised when a URL fails SSRF validation."""

    def __init__(self, url: str) -> None:
        self.url = url
        super().__init__(f"Blocked: {url} resolves to private/internal address")


async def scrape_page(url: str, client: httpx.AsyncClient) -> ScrapedPage:
    try:
        try:
            resp = await _safe_fetch(url, client)
        except _SsrfBlocked:
            return ScrapedPage(
                url=url, title="", meta_description="", headings=[], paragraphs=[],
                links=[], images=[], pricing_sections=[], testimonials=[],
                cta_buttons=[], status_code=0, error="Blocked: URL resolves to private/internal address",
            )
        resp.raise_for_status()
        soup = BeautifulSoup(resp.text, "lxml")

        title_tag = soup.find("title")
        title = title_tag.get_text(strip=True) if title_tag else ""

        meta = soup.find("meta", attrs={"name": "description"})
        meta_desc = ""
        if meta and isinstance(meta, Tag):
            meta_desc = meta.get("content", "") or ""
            if isinstance(meta_desc, list):
                meta_desc = " ".join(meta_desc)

        links = []
        for a in soup.find_all("a", href=True):
            href = a["href"]
            if isinstance(href, list):
                href = href[0]
            full = urljoin(url, href)
            if full.startswith("http"):
                links.append(full)

        images = []
        for img in soup.find_all("img", src=True):
            src = img["src"]
            if isinstance(src, list):
                src = src[0]
            images.append(urljoin(url, src))

        return ScrapedPage(
            url=url,
            title=title,
            meta_description=meta_desc,
            headings=_extract_headings(soup),
            paragraphs=_extract_text_blocks(soup),
            links=links[:50],
            images=images[:30],
            pricing_sections=_extract_pricing(soup),
            testimonials=_extract_testimonials(soup),
            cta_buttons=_extract_cta_buttons(soup),
            status_code=resp.status_code,
        )
    except Exception as e:
        logger.warning(f"Failed to scrape {url}: {e}")
        return ScrapedPage(
            url=url, title="", meta_description="", headings=[], paragraphs=[],
            links=[], images=[], pricing_sections=[], testimonials=[],
            cta_buttons=[], status_code=0, error=str(e),
        )


def _find_relevant_text(
    page: ScrapedPage, keywords: list[str], max_items: int = 5
) -> list[str]:
    results = []
    all_text = page.headings + page.paragraphs
    for text in all_text:
        lower = text.lower()
        score = sum(1 for kw in keywords if kw in lower)
        if score > 0:
            results.append((score, text))
    results.sort(key=lambda x: x[0], reverse=True)
    return [text for _, text in results[:max_items]]


def extract_evidence_from_page(
    page: ScrapedPage, competitor_name: str
) -> list[EvidenceItem]:
    if page.error:
        return []

    items: list[EvidenceItem] = []
    citation = Citation(
        url=page.url,
        title=page.title,
        snippet=page.meta_description[:200] if page.meta_description else "",
    )

    # Messaging evidence
    hero_texts = _find_relevant_text(page, MESSAGING_KEYWORDS)
    if page.headings:
        hero_texts = page.headings[:3] + hero_texts
    if hero_texts:
        items.append(EvidenceItem(
            category=EvidenceCategory.MESSAGING,
            competitor=competitor_name,
            claim=f"Key messaging from {competitor_name}",
            supporting_text=" | ".join(hero_texts[:5]),
            citations=[citation],
            confidence=ConfidenceLevel.HIGH if len(hero_texts) > 2 else ConfidenceLevel.MEDIUM,
            tags=["headline", "value-prop"],
        ))

    # Pricing evidence
    if page.pricing_sections:
        items.append(EvidenceItem(
            category=EvidenceCategory.PRICING,
            competitor=competitor_name,
            claim=f"Pricing information from {competitor_name}",
            supporting_text=" | ".join(page.pricing_sections[:5]),
            citations=[citation],
            confidence=ConfidenceLevel.HIGH,
            tags=["pricing", "plans"],
        ))

    # Landing page patterns
    if page.cta_buttons:
        items.append(EvidenceItem(
            category=EvidenceCategory.LANDING_PAGE,
            competitor=competitor_name,
            claim=f"CTA patterns: {', '.join(page.cta_buttons[:5])}",
            supporting_text=f"Landing page uses {len(page.cta_buttons)} CTAs. "
            f"Meta: {page.meta_description[:200]}",
            citations=[citation],
            confidence=ConfidenceLevel.HIGH,
            tags=["cta", "landing-page", "conversion"],
        ))

    # Social proof
    social = _find_relevant_text(page, SOCIAL_PROOF_KEYWORDS)
    if social or page.testimonials:
        combined = social + page.testimonials
        items.append(EvidenceItem(
            category=EvidenceCategory.SOCIAL_PROOF,
            competitor=competitor_name,
            claim=f"Social proof from {competitor_name}",
            supporting_text=" | ".join(combined[:5]),
            citations=[citation],
            confidence=ConfidenceLevel.MEDIUM,
            tags=["social-proof", "testimonials"],
        ))

    # Reviews from on-page testimonials
    if page.testimonials:
        items.append(EvidenceItem(
            category=EvidenceCategory.REVIEWS,
            competitor=competitor_name,
            claim=f"Customer testimonials from {competitor_name}",
            supporting_text=" | ".join(page.testimonials[:5]),
            citations=[citation],
            confidence=ConfidenceLevel.MEDIUM,
            tags=["reviews", "testimonials"],
        ))

    # Feature evidence
    feature_texts = _find_relevant_text(
        page, ["feature", "capability", "integration", "api", "tool", "platform"]
    )
    if feature_texts:
        items.append(EvidenceItem(
            category=EvidenceCategory.FEATURES,
            competitor=competitor_name,
            claim=f"Key features of {competitor_name}",
            supporting_text=" | ".join(feature_texts[:5]),
            citations=[citation],
            confidence=ConfidenceLevel.MEDIUM,
            tags=["features", "capabilities"],
        ))

    # Positioning evidence
    positioning = _find_relevant_text(
        page, ["leader", "best", "#1", "only", "first", "unique", "unlike"]
    )
    if positioning:
        items.append(EvidenceItem(
            category=EvidenceCategory.POSITIONING,
            competitor=competitor_name,
            claim=f"Market positioning of {competitor_name}",
            supporting_text=" | ".join(positioning[:5]),
            citations=[citation],
            confidence=ConfidenceLevel.MEDIUM,
            tags=["positioning", "differentiation"],
        ))

    # Complaint signals (from their own site, lower confidence)
    complaints = _find_relevant_text(page, COMPLAINT_KEYWORDS)
    if complaints:
        items.append(EvidenceItem(
            category=EvidenceCategory.COMPLAINTS,
            competitor=competitor_name,
            claim=f"Potential pain points mentioned on {competitor_name} site",
            supporting_text=" | ".join(complaints[:5]),
            citations=[citation],
            confidence=ConfidenceLevel.LOW,
            tags=["complaints", "pain-points"],
        ))

    return items


async def discover_subpages(
    base_url: str, client: httpx.AsyncClient, max_pages: int = 5
) -> list[str]:
    page = await scrape_page(base_url, client)
    if page.error:
        return [base_url]

    parsed = urlparse(base_url)
    base_domain = parsed.netloc
    interesting = []

    priority_paths = ["pricing", "features", "about", "product", "solutions",
                      "customers", "testimonials", "reviews", "plans", "compare"]

    for link in page.links:
        link_parsed = urlparse(link)
        if link_parsed.netloc != base_domain:
            continue
        path = link_parsed.path.lower().strip("/")
        score = sum(1 for kw in priority_paths if kw in path)
        if score > 0:
            interesting.append((score, link))

    interesting.sort(key=lambda x: x[0], reverse=True)
    urls = [base_url] + [url for _, url in interesting[:max_pages - 1]]
    return list(dict.fromkeys(urls))
