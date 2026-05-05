# Market/Competitor Intelligence Agent

An agentic evidence-pack builder that pulls competitor messaging, pricing/positioning, landing page patterns, reviews/complaints, and produces structured evidence packs with actionable campaign angles.

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    React Frontend (5173)                  │
│  Research Form → Evidence Pack View → Report Download     │
└──────────────────────┬──────────────────────────────────┘
                       │ /api (proxy)
┌──────────────────────▼──────────────────────────────────┐
│                  FastAPI Backend (8000)                   │
│                                                          │
│  ┌──────────────────────────────────────────────────┐   │
│  │              Intelligence Agent Loop               │   │
│  │                                                    │   │
│  │  1. Scrape competitor pages (httpx + BS4)          │   │
│  │  2. Extract evidence (messaging, pricing, etc.)    │   │
│  │  3. Analyze gaps (missing evidence categories)     │   │
│  │  4. If gaps → fetch suggested sources (iterate)    │   │
│  │  5. Generate campaign angles from evidence         │   │
│  │  6. Produce evidence pack + Markdown report        │   │
│  └──────────────────────────────────────────────────┘   │
│                                                          │
│  Models → Scraper → Analyzer → Agent → Report → API      │
│                                                          │
│  SQLite persistence for evidence packs                   │
└──────────────────────────────────────────────────────────┘
```

## Why It's Agentic

The agent **iterates** autonomously:
- After initial scraping, it identifies evidence gaps ("missing proof for claim X")
- It fetches additional sources to fill those gaps
- It repeats up to N iterations (configurable, default 3)
- It outputs citations + remaining gaps to resolve manually

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Python 3.12, FastAPI, Pydantic v2 |
| Scraping | httpx (async HTTP), BeautifulSoup4, lxml |
| Analysis | Custom NLP heuristics (keyword extraction, pattern matching, sentiment signals) |
| Frontend | React 18, TypeScript, Vite |
| Database | SQLite (via sqlite3) |
| Output | Structured JSON + Markdown reports |

## Evidence Categories

| Category | What it captures |
|----------|-----------------|
| Messaging | Headlines, taglines, value propositions, hero copy |
| Pricing | Plans, tiers, price points, billing models |
| Positioning | Market claims, differentiators, competitive statements |
| Landing Page | CTA patterns, conversion elements, page structure |
| Reviews | Customer testimonials, ratings, success stories |
| Complaints | Pain points, issues, negative signals |
| Features | Capabilities, integrations, platform tools |
| Social Proof | Customer logos, case studies, trust signals |

## Quick Start

### Backend
```bash
cd intel-agent/backend
pip install -r requirements.txt
python -m uvicorn app.main:app --host 0.0.0.0 --port 8000
```

### Frontend
```bash
cd intel-agent/frontend
npm install
npx vite --port 5173
```

### API Usage
```bash
# Start research
curl -X POST http://localhost:8000/api/research \
  -H "Content-Type: application/json" \
  -d '{
    "competitors": [
      {"name": "Stripe", "url": "https://stripe.com"},
      {"name": "Square", "url": "https://square.com"}
    ],
    "focus_areas": ["messaging", "pricing", "features", "positioning"],
    "campaign_context": "Payment processing for SMBs",
    "max_iterations": 2
  }'

# Check status (poll until COMPLETED)
curl http://localhost:8000/api/research/{id}

# Download Markdown report
curl http://localhost:8000/api/research/{id}/report

# List all research packs
curl http://localhost:8000/api/research
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/research` | Start new competitor research |
| GET | `/api/research` | List all research packs |
| GET | `/api/research/{id}` | Get evidence pack details |
| GET | `/api/research/{id}/report` | Get Markdown report |
| DELETE | `/api/research/{id}` | Delete research pack |
| GET | `/api/health` | Health check |

## Campaign+ Integration

This agent enhances the Campaign Management System's market research step by:
1. Providing evidence-backed competitor insights before campaign creation
2. Generating specific campaign angles with channel recommendations
3. Identifying competitor weaknesses to exploit in messaging
4. Tracking evidence confidence levels to prioritize reliable data
