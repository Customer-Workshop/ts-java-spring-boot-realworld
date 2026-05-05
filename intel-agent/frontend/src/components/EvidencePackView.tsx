import { useEffect, useState } from 'react';
import { getResearch, getReport, EvidencePack } from '../api';

interface Props {
  packId: string;
  onBack: () => void;
}

type Tab = 'overview' | 'evidence' | 'gaps' | 'angles' | 'report';

export default function EvidencePackView({ packId, onBack }: Props) {
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
        if (!cancelled) setError(err instanceof Error ? err.message : 'Failed to load');
      }
    };
    poll();
    return () => { cancelled = true; };
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
      <div className="card">
        <button className="btn-secondary" onClick={onBack}>Back</button>
        <p className="error-msg">{error}</p>
      </div>
    );
  }

  if (!pack) {
    return <div className="card"><p>Loading research pack...</p></div>;
  }

  const statusIcon = (status: string) => {
    switch (status) {
      case 'COMPLETED': return '🟢';
      case 'FAILED': return '🔴';
      case 'SCRAPING': return '🔵';
      case 'ANALYZING': return '🟡';
      case 'ITERATING': return '🔄';
      default: return '⚪';
    }
  };

  const confidenceIcon = (c: string) => {
    switch (c) {
      case 'high': return '🟢';
      case 'medium': return '🟡';
      case 'low': return '🔴';
      default: return '⚪';
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
      pack.evidence.map(e => `${e.category}::${e.competitor}`)
    );
    return Math.round((covered.size / total) * 100);
  };

  return (
    <div className="evidence-pack">
      <div className="pack-header">
        <button className="btn-secondary" onClick={onBack}>Back</button>
        <h2>
          {statusIcon(pack.status)} Evidence Pack
        </h2>
        <span className="pack-meta">
          {pack.status}
          {pack.status !== 'COMPLETED' && pack.status !== 'FAILED' && (
            <span className="spinner" />
          )}
        </span>
      </div>

      {/* Status bar for in-progress */}
      {pack.status !== 'COMPLETED' && pack.status !== 'FAILED' && (
        <div className="progress-bar">
          <div className="progress-status">
            Agent is {pack.status.toLowerCase()}...
            {pack.iterations.length > 0 && (
              <span> (Iteration {pack.iterations.length})</span>
            )}
          </div>
          <div className="progress-track">
            <div
              className="progress-fill"
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

      {/* Tabs */}
      <div className="tabs">
        <button
          className={tab === 'overview' ? 'tab active' : 'tab'}
          onClick={() => setTab('overview')}
        >
          Overview
        </button>
        <button
          className={tab === 'evidence' ? 'tab active' : 'tab'}
          onClick={() => setTab('evidence')}
        >
          Evidence ({pack.evidence.length})
        </button>
        <button
          className={tab === 'gaps' ? 'tab active' : 'tab'}
          onClick={() => setTab('gaps')}
        >
          Gaps ({pack.gaps.length})
        </button>
        <button
          className={tab === 'angles' ? 'tab active' : 'tab'}
          onClick={() => setTab('angles')}
        >
          Campaign Angles ({pack.campaign_angles.length})
        </button>
        <button
          className={tab === 'report' ? 'tab active' : 'tab'}
          onClick={loadReport}
        >
          Report
        </button>
      </div>

      {/* Overview Tab */}
      {tab === 'overview' && (
        <div className="tab-content">
          <div className="stats-grid">
            <div className="stat-card">
              <span className="stat-value">{pack.evidence.length}</span>
              <span className="stat-label">Evidence Items</span>
            </div>
            <div className="stat-card">
              <span className="stat-value">{pack.gaps.length}</span>
              <span className="stat-label">Remaining Gaps</span>
            </div>
            <div className="stat-card">
              <span className="stat-value">{pack.campaign_angles.length}</span>
              <span className="stat-label">Campaign Angles</span>
            </div>
            <div className="stat-card">
              <span className="stat-value">{coverageScore()}%</span>
              <span className="stat-label">Coverage Score</span>
            </div>
          </div>

          <div className="card">
            <h3>Competitors</h3>
            <div className="competitor-chips">
              {pack.competitors.map((c, i) => (
                <span key={i} className="chip">
                  {c.name}
                  {c.url && <a href={c.url} target="_blank" rel="noreferrer"> ↗</a>}
                </span>
              ))}
            </div>
          </div>

          {pack.iterations.length > 0 && (
            <div className="card">
              <h3>Agent Iterations</h3>
              <div className="iteration-timeline">
                {pack.iterations.map(it => (
                  <div key={it.iteration} className="iteration-item">
                    <span className="iteration-num">#{it.iteration}</span>
                    <span>
                      Found {it.gaps_found.length} gaps,
                      added {it.evidence_added} evidence,
                      resolved {it.gaps_resolved}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      {/* Evidence Tab */}
      {tab === 'evidence' && (
        <div className="tab-content">
          {Object.entries(groupByCompetitor()).map(([competitor, items]) => (
            <div key={competitor} className="card">
              <h3>{competitor}</h3>
              <p className="subtitle">{items.length} evidence items</p>
              {items.map(item => (
                <div key={item.id} className="evidence-item">
                  <div className="evidence-header">
                    <span className="category-badge">{item.category}</span>
                    <span>{confidenceIcon(item.confidence)} {item.confidence}</span>
                  </div>
                  <p className="evidence-claim">{item.claim}</p>
                  <p className="evidence-text">{item.supporting_text.slice(0, 300)}</p>
                  {item.citations.length > 0 && (
                    <div className="citations">
                      {item.citations.map((cit, i) => (
                        <a key={i} href={cit.url} target="_blank" rel="noreferrer" className="citation-link">
                          {cit.title || cit.url}
                        </a>
                      ))}
                    </div>
                  )}
                  {item.tags.length > 0 && (
                    <div className="tag-list">
                      {item.tags.map(t => <span key={t} className="tag">{t}</span>)}
                    </div>
                  )}
                </div>
              ))}
            </div>
          ))}
          {pack.evidence.length === 0 && (
            <p className="empty-state">No evidence collected yet.</p>
          )}
        </div>
      )}

      {/* Gaps Tab */}
      {tab === 'gaps' && (
        <div className="tab-content">
          {pack.gaps.length === 0 ? (
            <div className="card">
              <p className="empty-state">
                No remaining gaps — full coverage achieved!
              </p>
            </div>
          ) : (
            <div className="card">
              <h3>Evidence Gaps to Resolve</h3>
              <p className="subtitle">
                These areas need further research or manual verification.
              </p>
              {pack.gaps.map((gap, i) => (
                <div key={i} className="gap-item">
                  <div className="gap-header">
                    <span className="category-badge">{gap.category}</span>
                    <span className="gap-competitor">{gap.competitor}</span>
                  </div>
                  <p>{gap.description}</p>
                  {gap.suggested_sources.length > 0 && (
                    <div className="suggested-sources">
                      <span>Suggested:</span>
                      {gap.suggested_sources.slice(0, 2).map((s, j) => (
                        <a key={j} href={s} target="_blank" rel="noreferrer">{s}</a>
                      ))}
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Campaign Angles Tab */}
      {tab === 'angles' && (
        <div className="tab-content">
          {pack.campaign_angles.length === 0 ? (
            <div className="card">
              <p className="empty-state">
                Campaign angles will be generated when research completes.
              </p>
            </div>
          ) : (
            pack.campaign_angles.map((angle, i) => (
              <div key={i} className="card angle-card">
                <div className="angle-header">
                  <span className="angle-num">#{i + 1}</span>
                  <h3>{confidenceIcon(angle.confidence)} {angle.title}</h3>
                </div>
                <div className="angle-body">
                  <p><strong>Target Weakness:</strong> {angle.target_weakness}</p>
                  <p>{angle.description}</p>
                  <div className="channel-fit">
                    <strong>Channel Fit:</strong>
                    {angle.channel_fit.map(ch => (
                      <span key={ch} className="chip">{ch}</span>
                    ))}
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* Report Tab */}
      {tab === 'report' && (
        <div className="tab-content">
          <div className="card">
            <div className="report-actions">
              <button className="btn-primary" onClick={downloadReport}>
                Download Markdown Report
              </button>
            </div>
            <pre className="report-content">{report || 'Loading report...'}</pre>
          </div>
        </div>
      )}
    </div>
  );
}
