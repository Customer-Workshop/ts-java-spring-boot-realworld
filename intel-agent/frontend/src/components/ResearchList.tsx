import { useCallback, useEffect, useState } from 'react';
import { listResearch, deleteResearch } from '../api';
import type { PackSummary } from '../api';

interface Props {
  onSelect: (id: string) => void;
}

export default function ResearchList({ onSelect }: Props) {
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
      .then(data => { if (active) { setPacks(data); setLoading(false); } })
      .catch(() => { if (active) setLoading(false); });
    const interval = setInterval(refresh, 5000);
    return () => { active = false; clearInterval(interval); };
  }, [refresh]);

  const handleDelete = async (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    if (!confirm('Delete this research pack?')) return;
    await deleteResearch(id);
    refresh();
  };

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

  if (loading) return <div className="card"><p>Loading research history...</p></div>;

  return (
    <div className="card">
      <div className="list-header">
        <h2>Research History</h2>
        <button className="btn-secondary" onClick={refresh}>Refresh</button>
      </div>
      {packs.length === 0 ? (
        <p className="empty-state">
          No research packs yet. Start a new research above.
        </p>
      ) : (
        <div className="pack-list">
          {packs.map(pack => (
            <div
              key={pack.id}
              className="pack-item"
              onClick={() => onSelect(pack.id)}
            >
              <div className="pack-info">
                <span className="pack-status">
                  {statusIcon(pack.status)} {pack.status}
                </span>
                <span className="pack-id">{pack.id.slice(0, 8)}...</span>
                <span className="pack-date">
                  {new Date(pack.created_at).toLocaleDateString()}
                </span>
              </div>
              <button
                className="btn-icon btn-danger"
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
