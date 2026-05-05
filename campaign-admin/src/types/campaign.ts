export type CampaignStatus = 'DRAFT' | 'ACTIVE' | 'PAUSED' | 'ENDED';
export type FulfillmentActionType = 'ACCEPT' | 'DECLINE' | 'REMIND_LATER';
export type DecisionType =
  | 'ACCEPTED'
  | 'DECLINED'
  | 'CLICKED_UNFINISHED'
  | 'REMIND_LATER';
export type DisplayPlacement = 'POST_LOGIN' | 'LOGGED_OFF';
export type FrequencyCapType =
  | 'ONCE_PER_SESSION'
  | 'ONCE_PER_DAY'
  | 'ONCE_PER_CAMPAIGN';

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
  displayPlacement: string | null;
  frequencyCapType: string | null;
  frequencyCapMaxImpressions: number;
  deliveryStartTime: string | null;
  deliveryEndTime: string | null;
  personalizationTokens: string | null;
  remindLaterDeferralDays: number;
  fulfillmentWorkflowUrl: string | null;
  declineSuppression: boolean;
  confirmationMessage: string | null;
  audienceRules: string | null;
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
  displayPlacement: string;
  frequencyCapType: string;
  frequencyCapMaxImpressions: number;
  deliveryStartTime: string;
  deliveryEndTime: string;
  personalizationTokens: string;
  remindLaterDeferralDays: number;
  fulfillmentWorkflowUrl: string;
  declineSuppression: boolean;
  confirmationMessage: string;
  audienceRules: string;
}

export interface CampaignAnalytics {
  totalTargetedPopulation: number;
  acceptedCount: number;
  declinedCount: number;
  clickedUnfinishedCount: number;
  remindLaterCount: number;
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
  totalRemindLater: number;
  lastUpdated: string;
}

export interface User {
  id: string;
  email: string;
  username: string;
  token: string;
}
