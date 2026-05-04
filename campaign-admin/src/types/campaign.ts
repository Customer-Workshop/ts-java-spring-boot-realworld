export type CampaignStatus = 'DRAFT' | 'ACTIVE' | 'PAUSED' | 'ENDED';
export type FulfillmentActionType = 'ACCEPT' | 'DECLINE' | 'REMIND_LATER';
export type DecisionType = 'ACCEPTED' | 'DECLINED' | 'CLICKED_UNFINISHED';

export interface Campaign {
  id: string;
  name: string;
  status: CampaignStatus;
  targetAudienceSegment: string;
  startDate: string | null;
  endDate: string | null;
  messageTitle: string;
  messageBody: string;
  messageImageUrl: string | null;
  messageCtaText: string | null;
  fulfillmentActionType: FulfillmentActionType;
  createdBy: string;
  archived: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CampaignFormData {
  name: string;
  targetAudienceSegment: string;
  startDate: string;
  endDate: string;
  messageTitle: string;
  messageBody: string;
  messageImageUrl: string;
  messageCtaText: string;
  fulfillmentActionType: FulfillmentActionType;
}

export interface CampaignAnalytics {
  totalTargetedPopulation: number;
  acceptedCount: number;
  declinedCount: number;
  clickedUnfinishedCount: number;
  commonalityBySegment: Record<string, Record<string, number>>;
  commonalityByAgeGroup: Record<string, Record<string, number>>;
  commonalityByRegion: Record<string, Record<string, number>>;
}

export interface DashboardSummary {
  totalCampaigns: number;
  activeCampaigns: number;
  draftCampaigns: number;
  pausedCampaigns: number;
  endedCampaigns: number;
  totalTargetedPopulation: number;
  totalAccepted: number;
  totalDeclined: number;
  totalClickedUnfinished: number;
}

export interface User {
  id: string;
  email: string;
  username: string;
  token: string;
}
