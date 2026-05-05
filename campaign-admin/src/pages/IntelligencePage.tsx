import { useCallback, useEffect, useState } from 'react';
import {
  startResearch,
  getResearch,
  getReport,
  listResearch,
  deleteResearch,
  EVIDENCE_CATEGORIES,
} from '../api/intelligence';
import type {
  EvidencePack,
  PackSummary,
} from '../api/intelligence';
import './IntelligencePage.css';

type View = 'home' | 'pack';

export function IntelligencePage() {
  const [view, setView] = useState<View>('home');
  const [selectedPackId, setSelectedPackId] = useState('');

  const handleViewPack = (id: string) => {
    setSelectedPackId(id);
    setView('pack');
  };

  return (
    <div>
      {view === 'home' && (
        <>
          <h1 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: 20, color: '#1a2744' }}>
            Market Intelligence Agent
          </h1>
          <div className="intel-home-layout">
            <div className="intel-form-section">
              <ResearchForm onCreated={handleViewPack} />
            </div>
            <div className="intel-list-section">
              <ResearchList onSelect={handleViewPack} />
            </div>
          </div>
        </>
      )}
      {view === 'pack' && (
        <EvidencePackView
          packId={selectedPackId}
          onBack={() => setView('home')}
        />
      )}
    </div>
  );
}

/* ── Research Form ─────────────────────────────────────── */

const categoryLabels: Record<string, string> = {
  messaging: 'Messaging',
  pricing: 'Pricing',
  positioning: 'Positioning',
  landing_page: 'Landing Pages',
  reviews: 'Reviews',
  complaints: 'Complaints',
  features: 'Features',
  social_proof: 'Social Proof',
};

function ResearchForm({ onCreated }: { onCreated: (id: string) => void }) {
  const [competitors, setCompetitors] = useState([{ name: '', url: '' }]);
  const [focusAreas, setFocusAreas] = useState<string[]>([...EVIDENCE_CATEGORIES]);
  const [campaignContext, setCampaignContext] = useState('');
  const [maxIterations, setMaxIterations] = useState(3);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const addCompetitor = () =>
    setCompetitors([...competitors, { name: '', url: '' }]);

  const removeCompetitor = (index: number) =>
    setCompetitors(competitors.filter((_, i) => i !== index));

  const updateCompetitor = (
    index: number,
    field: 'name' | 'url',
    value: string
  ) => {
    const updated = [...competitors];
    updated[index] = { ...updated[index], [field]: value };
    setCompetitors(updated);
  };

  const toggleFocus = (cat: string) =>
    setFocusAreas((prev) =>
      prev.includes(cat) ? prev.filter((c) => c !== cat) : [...prev, cat]
    );

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    const validCompetitors = competitors.filter((c) => c.name.trim());
    if (validCompetitors.length === 0) {
      setError('Add at least one competitor');
      return;
    }
    setLoading(true);
    try {
      const result = await startResearch({
        competitors: validCompetitors.map((c) => ({
          name: c.name.trim(),
          url: c.url.trim() || undefined,
        })),
        focus_areas: focusAreas,
        campaign_context: campaignContext || undefined,
        max_iterations: maxIterations,
      });
      onCreated(result.id);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Research failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="intel-card intel-research-form">
      <h2>New Research</h2>
      <p className="intel-subtitle">
        Add competitors to analyze. The agent will iteratively gather evidence,
        identify gaps, and generate campaign angles.
      </p>

      <form onSubmit={handleSubmit}>
        <div className="intel-form-group">
          <label>Competitors</label>
          {competitors.map((comp, i) => (
            <div key={i} className="intel-competitor-row">
              <input
                type="text"
                placeholder="Competitor name"
                value={comp.name}
                onChange={(e) => updateCompetitor(i, 'name', e.target.value)}
              />
              <input
                type="text"
                placeholder="Website URL (optional)"
                value={comp.url}
                onChange={(e) => updateCompetitor(i, 'url', e.target.value)}
              />
              {competitors.length > 1 && (
                <button
                  type="button"
                  className="intel-btn-icon"
                  onClick={() => removeCompetitor(i)}
                >
                  x
                </button>
              )}
            </div>
          ))}
          <button
            type="button"
            className="intel-btn-secondary"
            onClick={addCompetitor}
          >
            + Add Competitor
          </button>
        </div>

        <div className="intel-form-group">
          <label>Focus Areas</label>
          <div className="intel-focus-grid">
            {EVIDENCE_CATEGORIES.map((cat) => (
              <label key={cat} className="intel-focus-chip">
                <input
                  type="checkbox"
                  checked={focusAreas.includes(cat)}
                  onChange={() => toggleFocus(cat)}
                />
                <span>{categoryLabels[cat] || cat}</span>
              </label>
            ))}
          </div>
        </div>

        <div className="intel-form-group">
          <label>Campaign Context (optional)</label>
          <textarea
            placeholder="Describe your campaign goals, target market, or product positioning..."
            value={campaignContext}
            onChange={(e) => setCampaignContext(e.target.value)}
            rows={3}
          />
        </div>

        <div className="intel-form-group">
          <label>Max Agent Iterations: {maxIterations}</label>
          <input
            type="range"
            min={1}
            max={5}
            value={maxIterations}
            onChange={(e) => setMaxIterations(Number(e.target.value))}
          />
          <span className="intel-hint">
            More iterations = deeper research but longer wait
          </span>
        </div>

        {error && <div className="intel-error-msg">{error}</div>}

        <button type="submit" className="intel-btn-primary" disabled={loading}>
          {loading ? 'Starting Research...' : 'Start Intelligence Gathering'}
        </button>
      </form>
    </div>
  );
}

/* ── Research List ─────────────────────────────────────── */

function ResearchList({ onSelect }: { onSelect: (id: string) => void }) {
  const [packs, setPacks] = useState<PackSummary[]>([]);
  const [loading, setLoading] = useState(true);

  const refresh = useCallback(async () => {
    try {
      const data = await listResearch();
      setPacks(data);
    } catch {
      // ignore
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    let active = true;
    listResearch()
      .then((data) => {
        if (active) {
          setPacks(data);
          setLoading(false);
        }
      })
      .catch(() => {
        if (active) setLoading(false);
      });
    const interval = setInterval(refresh, 5000);
    return () => {
      active = false;
      clearInterval(interval);
    };
  }, [refresh]);

  const handleDelete = async (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    if (!confirm('Delete this research pack?')) return;
    await deleteResearch(id);
    refresh();
  };

  const statusIcon = (status: string) => {
    switch (status) {
      case 'COMPLETED':
        return '\u{1F7E2}';
      case 'FAILED':
        return '\u{1F534}';
      case 'SCRAPING':
        return '\u{1F535}';
      case 'ANALYZING':
        return '\u{1F7E1}';
      case 'ITERATING':
        return '\u{1F504}';
      default:
        return '\u26AA';
    }
  };

  if (loading)
    return (
      <div className="intel-card">
        <p>Loading research history...</p>
      </div>
    );

  return (
    <div className="intel-card">
      <div className="intel-list-header">
        <h2>Research History</h2>
        <button className="intel-btn-secondary" onClick={refresh}>
          Refresh
        </button>
      </div>
      {packs.length === 0 ? (
        <p className="intel-empty-state">
          No research packs yet. Start a new research above.
        </p>
      ) : (
        <div className="intel-pack-list">
          {packs.map((pack) => (
            <div
              key={pack.id}
              className="intel-pack-item"
              onClick={() => onSelect(pack.id)}
            >
              <div className="intel-pack-info">
                <span className="intel-pack-status">
                  {statusIcon(pack.status)} {pack.status}
                </span>
                <span className="intel-pack-id">{pack.id.slice(0, 8)}...</span>
                <span className="intel-pack-date">
                  {new Date(pack.created_at).toLocaleDateString()}
                </span>
              </div>
              <button
                className="intel-btn-icon intel-btn-danger"
                onClick={(e) => handleDelete(pack.id, e)}
              >
                Delete
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

/* ── Evidence Pack View ────────────────────────────────── */

type Tab = 'overview' | 'evidence' | 'gaps' | 'angles' | 'report';

function EvidencePackView({
  packId,
  onBack,
}: {
  packId: string;
  onBack: () => void;
}) {
  const [pack, setPack] = useState<EvidencePack | null>(null);
  const [report, setReport] = useState('');
  const [tab, setTab] = useState<Tab>('overview');
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    const poll = async () => {
      try {
        const data = await getResearch(packId);
        if (!cancelled) {
          setPack(data);
          if (data.status === 'COMPLETED' || data.status === 'FAILED') return;
          setTimeout(poll, 3000);
        }
      } catch (err: unknown) {
        if (!cancelled)
          setError(err instanceof Error ? err.message : 'Failed to load');
      }
    };
    poll();
    return () => {
      cancelled = true;
    };
  }, [packId]);

  const loadReport = async () => {
    setTab('report');
    if (!report) {
      const md = await getReport(packId);
      setReport(md);
    }
  };

  const downloadReport = () => {
    const blob = new Blob([report], { type: 'text/markdown' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `evidence-pack-${packId.slice(0, 8)}.md`;
    a.click();
    URL.revokeObjectURL(url);
  };

  if (error) {
    return (
      <div className="intel-card">
        <button className="intel-btn-secondary" onClick={onBack}>
          Back
        </button>
        <p className="intel-error-msg">{error}</p>
      </div>
    );
  }

  if (!pack) {
    return (
      <div className="intel-card">
        <p>Loading research pack...</p>
      </div>
    );
  }

  const statusIcon = (status: string) => {
    switch (status) {
      case 'COMPLETED':
        return '\u{1F7E2}';
      case 'FAILED':
        return '\u{1F534}';
      case 'SCRAPING':
        return '\u{1F535}';
      case 'ANALYZING':
        return '\u{1F7E1}';
      case 'ITERATING':
        return '\u{1F504}';
      default:
        return '\u26AA';
    }
  };

  const confidenceIcon = (c: string) => {
    switch (c) {
      case 'high':
        return '\u{1F7E2}';
      case 'medium':
        return '\u{1F7E1}';
      case 'low':
        return '\u{1F534}';
      default:
        return '\u26AA';
    }
  };

  const groupByCompetitor = () => {
    const grouped: Record<string, typeof pack.evidence> = {};
    for (const item of pack.evidence) {
      if (!grouped[item.competitor]) grouped[item.competitor] = [];
      grouped[item.competitor].push(item);
    }
    return grouped;
  };

  const coverageScore = () => {
    if (!pack.focus_areas.length || !pack.competitors.length) return 0;
    const total = pack.focus_areas.length * pack.competitors.length;
    const covered = new Set(
      pack.evidence.map((e) => `${e.category}::${e.competitor}`)
    );
    return Math.round((covered.size / total) * 100);
  };

  return (
    <div className="intel-evidence-pack">
      <div className="intel-pack-header">
        <button className="intel-btn-secondary" onClick={onBack}>
          Back
        </button>
        <h2>{statusIcon(pack.status)} Evidence Pack</h2>
        <span className="intel-pack-meta">
          {pack.status}
          {pack.status !== 'COMPLETED' && pack.status !== 'FAILED' && (
            <span className="intel-spinner" />
          )}
        </span>
      </div>

      {pack.status !== 'COMPLETED' && pack.status !== 'FAILED' && (
        <div className="intel-progress-bar">
          <div className="intel-progress-status">
            Agent is {pack.status.toLowerCase()}...
            {pack.iterations.length > 0 && (
              <span> (Iteration {pack.iterations.length})</span>
            )}
          </div>
          <div className="intel-progress-track">
            <div
              className="intel-progress-fill"
              style={{
                width: `${Math.min(
                  ((pack.iterations.length + 1) / 4) * 100,
                  90
                )}%`,
              }}
            />
          </div>
        </div>
      )}

      <div className="intel-tabs">
        <button
          className={tab === 'overview' ? 'intel-tab active' : 'intel-tab'}
          onClick={() => setTab('overview')}
        >
          Overview
        </button>
        <button
          className={tab === 'evidence' ? 'intel-tab active' : 'intel-tab'}
          onClick={() => setTab('evidence')}
        >
          Evidence ({pack.evidence.length})
        </button>
        <button
          className={tab === 'gaps' ? 'intel-tab active' : 'intel-tab'}
          onClick={() => setTab('gaps')}
        >
          Gaps ({pack.gaps.length})
        </button>
        <button
          className={tab === 'angles' ? 'intel-tab active' : 'intel-tab'}
          onClick={() => setTab('angles')}
        >
          Campaign Angles ({pack.campaign_angles.length})
        </button>
        <button
          className={tab === 'report' ? 'intel-tab active' : 'intel-tab'}
          onClick={loadReport}
        >
          Report
        </button>
      </div>

      {tab === 'overview' && (
        <div className="intel-tab-content">
          <div className="intel-stats-grid">
            <div className="intel-stat-card">
              <span className="intel-stat-value">{pack.evidence.length}</span>
              <span className="intel-stat-label">Evidence Items</span>
            </div>
            <div className="intel-stat-card">
              <span className="intel-stat-value">{pack.gaps.length}</span>
              <span className="intel-stat-label">Remaining Gaps</span>
            </div>
            <div className="intel-stat-card">
              <span className="intel-stat-value">
                {pack.campaign_angles.length}
              </span>
              <span className="intel-stat-label">Campaign Angles</span>
            </div>
            <div className="intel-stat-card">
              <span className="intel-stat-value">{coverageScore()}%</span>
              <span className="intel-stat-label">Coverage Score</span>
            </div>
          </div>

          <div className="intel-card">
            <h3>Competitors</h3>
            <div className="intel-competitor-chips">
              {pack.competitors.map((c, i) => (
                <span key={i} className="intel-chip">
                  {c.name}
                  {c.url && (
                    <a href={c.url} target="_blank" rel="noreferrer">
                      {' '}
                      &uarr;
                    </a>
                  )}
                </span>
              ))}
            </div>
          </div>

          {pack.iterations.length > 0 && (
            <div className="intel-card">
              <h3>Agent Iterations</h3>
              <div className="intel-iteration-timeline">
                {pack.iterations.map((it) => (
                  <div key={it.iteration} className="intel-iteration-item">
                    <span className="intel-iteration-num">
                      #{it.iteration}
                    </span>
                    <span>
                      Found {it.gaps_found.length} gaps, added{' '}
                      {it.evidence_added} evidence, resolved{' '}
                      {it.gaps_resolved}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      {tab === 'evidence' && (
        <div className="intel-tab-content">
          {Object.entries(groupByCompetitor()).map(([competitor, items]) => (
            <div key={competitor} className="intel-card">
              <h3>{competitor}</h3>
              <p className="intel-subtitle">{items.length} evidence items</p>
              {items.map((item) => (
                <div key={item.id} className="intel-evidence-item">
                  <div className="intel-evidence-header">
                    <span className="intel-category-badge">
                      {item.category}
                    </span>
                    <span>
                      {confidenceIcon(item.confidence)} {item.confidence}
                    </span>
                  </div>
                  <p className="intel-evidence-claim">{item.claim}</p>
                  <p className="intel-evidence-text">
                    {item.supporting_text.slice(0, 300)}
                  </p>
                  {item.citations.length > 0 && (
                    <div className="intel-citations">
                      {item.citations.map((cit, i) => (
                        <a
                          key={i}
                          href={cit.url}
                          target="_blank"
                          rel="noreferrer"
                          className="intel-citation-link"
                        >
                          {cit.title || cit.url}
                        </a>
                      ))}
                    </div>
                  )}
                  {item.tags.length > 0 && (
                    <div className="intel-tag-list">
                      {item.tags.map((t) => (
                        <span key={t} className="intel-tag">
                          {t}
                        </span>
                      ))}
                    </div>
                  )}
                </div>
              ))}
            </div>
          ))}
          {pack.evidence.length === 0 && (
            <p className="intel-empty-state">No evidence collected yet.</p>
          )}
        </div>
      )}

      {tab === 'gaps' && (
        <div className="intel-tab-content">
          {pack.gaps.length === 0 ? (
            <div className="intel-card">
              <p className="intel-empty-state">
                No remaining gaps — full coverage achieved!
              </p>
            </div>
          ) : (
            <div className="intel-card">
              <h3>Evidence Gaps to Resolve</h3>
              <p className="intel-subtitle">
                These areas need further research or manual verification.
              </p>
              {pack.gaps.map((gap, i) => (
                <div key={i} className="intel-gap-item">
                  <div className="intel-gap-header">
                    <span className="intel-category-badge">{gap.category}</span>
                    <span className="intel-gap-competitor">
                      {gap.competitor}
                    </span>
                  </div>
                  <p>{gap.description}</p>
                  {gap.suggested_sources.length > 0 && (
                    <div className="intel-suggested-sources">
                      <span>Suggested:</span>
                      {gap.suggested_sources.slice(0, 2).map((s, j) => (
                        <a key={j} href={s} target="_blank" rel="noreferrer">
                          {s}
                        </a>
                      ))}
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {tab === 'angles' && (
        <div className="intel-tab-content">
          {pack.campaign_angles.length === 0 ? (
            <div className="intel-card">
              <p className="intel-empty-state">
                Campaign angles will be generated when research completes.
              </p>
            </div>
          ) : (
            pack.campaign_angles.map((angle, i) => (
              <div key={i} className="intel-card intel-angle-card">
                <div className="intel-angle-header">
                  <span className="intel-angle-num">#{i + 1}</span>
                  <h3>
                    {confidenceIcon(angle.confidence)} {angle.title}
                  </h3>
                </div>
                <div className="intel-angle-body">
                  <p>
                    <strong>Target Weakness:</strong> {angle.target_weakness}
                  </p>
                  <p>{angle.description}</p>
                  <div className="intel-channel-fit">
                    <strong>Channel Fit:</strong>
                    {angle.channel_fit.map((ch) => (
                      <span key={ch} className="intel-chip">
                        {ch}
                      </span>
                    ))}
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {tab === 'report' && (
        <div className="intel-tab-content">
          <div className="intel-card">
            <div className="intel-report-actions">
              <button className="intel-btn-primary" onClick={downloadReport}>
                Download Markdown Report
              </button>
            </div>
            <pre className="intel-report-content">
              {report || 'Loading report...'}
            </pre>
          </div>
        </div>
      )}
    </div>
  );
}
