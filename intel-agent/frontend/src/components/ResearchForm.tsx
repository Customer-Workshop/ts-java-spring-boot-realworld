import { useState } from 'react';
import { startResearch, EVIDENCE_CATEGORIES } from '../api';

interface Props {
  onCreated: (id: string) => void;
}

export default function ResearchForm({ onCreated }: Props) {
  const [competitors, setCompetitors] = useState([{ name: '', url: '' }]);
  const [focusAreas, setFocusAreas] = useState<string[]>([...EVIDENCE_CATEGORIES]);
  const [campaignContext, setCampaignContext] = useState('');
  const [maxIterations, setMaxIterations] = useState(3);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const addCompetitor = () => {
    setCompetitors([...competitors, { name: '', url: '' }]);
  };

  const removeCompetitor = (index: number) => {
    setCompetitors(competitors.filter((_, i) => i !== index));
  };

  const updateCompetitor = (index: number, field: 'name' | 'url', value: string) => {
    const updated = [...competitors];
    updated[index] = { ...updated[index], [field]: value };
    setCompetitors(updated);
  };

  const toggleFocus = (cat: string) => {
    setFocusAreas(prev =>
      prev.includes(cat) ? prev.filter(c => c !== cat) : [...prev, cat]
    );
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    const validCompetitors = competitors.filter(c => c.name.trim());
    if (validCompetitors.length === 0) {
      setError('Add at least one competitor');
      return;
    }
    setLoading(true);
    try {
      const result = await startResearch({
        competitors: validCompetitors.map(c => ({
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

  return (
    <div className="card research-form">
      <h2>New Research</h2>
      <p className="subtitle">
        Add competitors to analyze. The agent will iteratively gather evidence,
        identify gaps, and generate campaign angles.
      </p>

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Competitors</label>
          {competitors.map((comp, i) => (
            <div key={i} className="competitor-row">
              <input
                type="text"
                placeholder="Competitor name"
                value={comp.name}
                onChange={e => updateCompetitor(i, 'name', e.target.value)}
              />
              <input
                type="text"
                placeholder="Website URL (optional)"
                value={comp.url}
                onChange={e => updateCompetitor(i, 'url', e.target.value)}
              />
              {competitors.length > 1 && (
                <button
                  type="button"
                  className="btn-icon"
                  onClick={() => removeCompetitor(i)}
                >
                  x
                </button>
              )}
            </div>
          ))}
          <button type="button" className="btn-secondary" onClick={addCompetitor}>
            + Add Competitor
          </button>
        </div>

        <div className="form-group">
          <label>Focus Areas</label>
          <div className="focus-grid">
            {EVIDENCE_CATEGORIES.map(cat => (
              <label key={cat} className="focus-chip">
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

        <div className="form-group">
          <label>Campaign Context (optional)</label>
          <textarea
            placeholder="Describe your campaign goals, target market, or product positioning..."
            value={campaignContext}
            onChange={e => setCampaignContext(e.target.value)}
            rows={3}
          />
        </div>

        <div className="form-group">
          <label>Max Agent Iterations: {maxIterations}</label>
          <input
            type="range"
            min={1}
            max={5}
            value={maxIterations}
            onChange={e => setMaxIterations(Number(e.target.value))}
          />
          <span className="hint">
            More iterations = deeper research but longer wait
          </span>
        </div>

        {error && <div className="error-msg">{error}</div>}

        <button type="submit" className="btn-primary" disabled={loading}>
          {loading ? 'Starting Research...' : 'Start Intelligence Gathering'}
        </button>
      </form>
    </div>
  );
}
