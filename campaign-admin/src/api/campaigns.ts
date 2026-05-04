import apiClient from './client';
import type {
  Campaign,
  CampaignAnalytics,
  CampaignFormData,
  DashboardSummary,
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

export async function loginUser(
  email: string,
  password: string
): Promise<{ token: string; username: string }> {
  const response = await apiClient.post('/users/login', {
    user: { email, password },
  });
  return response.data.user;
}
