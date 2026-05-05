import apiClient from './client';
import type {
  Campaign,
  CampaignAnalytics,
  CampaignFormData,
  DashboardSummary,
  ABTestVariant,
  AuditLogEntry,
} from '../types/campaign';

export async function fetchCampaigns(
  status?: string,
  includeArchived = false
): Promise<Campaign[]> {
  const params: Record<string, string | boolean> = { includeArchived };
  if (status) params.status = status;
  const response = await apiClient.get('/api/campaigns', { params });
  return response.data.campaigns;
}

export async function fetchCampaign(id: string): Promise<Campaign> {
  const response = await apiClient.get(`/api/campaigns/${id}`);
  return response.data.campaign;
}

export async function createCampaign(
  data: CampaignFormData
): Promise<Campaign> {
  const response = await apiClient.post('/api/campaigns', {
    campaign: data,
  });
  return response.data.campaign;
}

export async function updateCampaign(
  id: string,
  data: Partial<CampaignFormData> & { status?: string }
): Promise<Campaign> {
  const response = await apiClient.put(`/api/campaigns/${id}`, {
    campaign: data,
  });
  return response.data.campaign;
}

export async function deleteCampaign(id: string): Promise<void> {
  await apiClient.delete(`/api/campaigns/${id}`);
}

export async function cloneCampaign(
  id: string,
  name?: string
): Promise<Campaign> {
  const response = await apiClient.post(`/api/campaigns/${id}/clone`, {
    name,
  });
  return response.data.campaign;
}

export async function bulkUpdateStatus(
  campaignIds: string[],
  status: string
): Promise<{ updated: number }> {
  const response = await apiClient.post('/api/campaigns/bulk/status', {
    campaignIds,
    status,
  });
  return response.data;
}

export async function fetchABTestVariants(
  campaignId: string
): Promise<ABTestVariant[]> {
  const response = await apiClient.get(
    `/api/campaigns/${campaignId}/variants`
  );
  return response.data.variants;
}

export async function createABTestVariant(
  campaignId: string,
  variant: Omit<ABTestVariant, 'id' | 'campaignId' | 'impressions' | 'conversions' | 'conversionRate' | 'winner' | 'createdAt'>
): Promise<ABTestVariant> {
  const response = await apiClient.post(
    `/api/campaigns/${campaignId}/variants`,
    { variant }
  );
  return response.data.variant;
}

export async function declareABTestWinner(
  campaignId: string,
  variantId: string
): Promise<ABTestVariant> {
  const response = await apiClient.post(
    `/api/campaigns/${campaignId}/variants/${variantId}/winner`
  );
  return response.data.variant;
}

export async function fetchCampaignTags(
  campaignId: string
): Promise<string[]> {
  const response = await apiClient.get(
    `/api/campaigns/${campaignId}/tags`
  );
  return response.data.tags;
}

export async function fetchAllTags(): Promise<string[]> {
  const response = await apiClient.get('/api/campaigns/tags/all');
  return response.data.tags;
}

export async function fetchAuditLog(
  campaignId: string
): Promise<AuditLogEntry[]> {
  const response = await apiClient.get(
    `/api/campaigns/${campaignId}/audit`
  );
  return response.data.auditLog;
}

export async function fetchCampaignAnalytics(
  id: string
): Promise<CampaignAnalytics> {
  const response = await apiClient.get(`/api/campaigns/${id}/analytics`);
  return response.data;
}

export async function fetchDashboard(): Promise<DashboardSummary> {
  const response = await apiClient.get('/api/campaigns/dashboard');
  return response.data;
}

export async function exportDashboardCsv(): Promise<void> {
  const response = await apiClient.get('/api/campaigns/dashboard/export', {
    responseType: 'blob',
  });
  const blob = new Blob([response.data], { type: 'text/csv' });
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = 'campaigns_export.csv';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
}

export async function loginUser(
  email: string,
  password: string
): Promise<{ token: string; username: string }> {
  const response = await apiClient.post('/users/login', {
    user: { email, password },
  });
  return response.data.user;
}
