const BASE = '';

export interface CompetitorInput {
  name: string;
  url?: string;
  description?: string;
}

export interface ResearchRequest {
  competitors: CompetitorInput[];
  focus_areas: string[];
  campaign_context?: string;
  max_iterations: number;
}

export interface Citation {
  url: string;
  title: string;
  snippet: string;
  fetched_at: string;
}

export interface EvidenceItem {
  id: string;
  category: string;
  competitor: string;
  claim: string;
  supporting_text: string;
  citations: Citation[];
  confidence: string;
  tags: string[];
}

export interface EvidenceGap {
  category: string;
  competitor: string;
  description: string;
  suggested_sources: string[];
}

export interface CampaignAngle {
  title: string;
  description: string;
  target_weakness: string;
  supporting_evidence_ids: string[];
  channel_fit: string[];
  confidence: string;
}

export interface ResearchIteration {
  iteration: number;
  gaps_found: EvidenceGap[];
  evidence_added: number;
  gaps_resolved: number;
  timestamp: string;
}

export interface EvidencePack {
  id: string;
  status: string;
  competitors: CompetitorInput[];
  focus_areas: string[];
  campaign_context?: string;
  evidence: EvidenceItem[];
  gaps: EvidenceGap[];
  campaign_angles: CampaignAngle[];
  iterations: ResearchIteration[];
  created_at: string;
  completed_at?: string;
  error?: string;
  coverage_score?: number;
}

export interface PackSummary {
  id: string;
  status: string;
  created_at: string;
  completed_at?: string;
}

export async function startResearch(req: ResearchRequest): Promise<{ id: string; status: string }> {
  const res = await fetch(`${BASE}/api/research`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(req),
  });
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

export async function getResearch(id: string): Promise<EvidencePack> {
  const res = await fetch(`${BASE}/api/research/${id}`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

export async function getReport(id: string): Promise<string> {
  const res = await fetch(`${BASE}/api/research/${id}/report`);
  if (!res.ok) throw new Error(await res.text());
  return res.text();
}

export async function listResearch(): Promise<PackSummary[]> {
  const res = await fetch(`${BASE}/api/research`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

export async function deleteResearch(id: string): Promise<void> {
  const res = await fetch(`${BASE}/api/research/${id}`, { method: 'DELETE' });
  if (!res.ok) throw new Error(await res.text());
}

export const EVIDENCE_CATEGORIES = [
  'messaging', 'pricing', 'positioning', 'landing_page',
  'reviews', 'complaints', 'features', 'social_proof',
] as const;
