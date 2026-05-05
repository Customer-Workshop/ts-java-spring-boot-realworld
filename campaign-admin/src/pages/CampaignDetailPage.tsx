import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import {
  fetchCampaign,
  updateCampaign,
  deleteCampaign,
} from '../api/campaigns';
import { StatusBadge } from '../components/StatusBadge';
import type { Campaign } from '../types/campaign';

export function CampaignDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [campaign, setCampaign] = useState<Campaign | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (id) {
      fetchCampaign(id)
        .then(setCampaign)
        .finally(() => setLoading(false));
    }
  }, [id]);

  if (loading)
    return <p style={{ color: '#64748b' }}>Loading...</p>;
  if (!campaign)
    return <p style={{ color: '#dc2626' }}>Campaign not found.</p>;

  const handleStatusChange = async (newStatus: string) => {
    if (!id) return;
    try {
      const updated = await updateCampaign(id, { status: newStatus });
      setCampaign(updated);
    } catch {
      alert('Failed to update campaign status.');
    }
  };

  const handleDelete = async () => {
    if (!id) return;
    const action =
      campaign.status === 'DRAFT' ? 'permanently delete' : 'archive';
    if (
      !window.confirm(
        `Are you sure you want to ${action} this campaign?`
      )
    )
      return;
    try {
      await deleteCampaign(id);
      navigate('/campaigns');
    } catch {
      alert('Failed to delete campaign.');
    }
  };

  const freqCapLabel =
    campaign.frequencyCapType === 'ONCE_PER_SESSION'
      ? 'Once Per Session'
      : campaign.frequencyCapType === 'ONCE_PER_DAY'
        ? 'Once Per Day'
        : 'Once Per Campaign';

  return (
    <div style={{ maxWidth: '800px' }}>
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '24px',
        }}
      >
        <div>
          <Link
            to="/campaigns"
            style={{
              color: '#6b7280',
              textDecoration: 'none',
              fontSize: '13px',
            }}
          >
            &larr; Back to Campaigns
          </Link>
          <h2
            style={{
              fontSize: '22px',
              fontWeight: 600,
              margin: '8px 0 0',
              color: '#1a2744',
            }}
          >
            {campaign.name}
          </h2>
        </div>
        <StatusBadge status={campaign.status} />
      </div>

      {/* Basic Info */}
      <div style={cardStyle}>
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: '1fr 1fr',
            gap: '24px',
          }}
        >
          <DetailField
            label="Target Audience"
            value={campaign.targetAudienceSegment}
          />
          <DetailField
            label="Fulfillment Action"
            value={campaign.fulfillmentActionType}
          />
          <DetailField
            label="Start Date"
            value={
              campaign.startDate
                ? new Date(campaign.startDate).toLocaleDateString()
                : '-'
            }
          />
          <DetailField
            label="End Date"
            value={
              campaign.endDate
                ? new Date(campaign.endDate).toLocaleDateString()
                : '-'
            }
          />
          <DetailField
            label="Created"
            value={new Date(campaign.createdAt).toLocaleString()}
          />
          <DetailField
            label="Last Updated"
            value={new Date(campaign.updatedAt).toLocaleString()}
          />
        </div>

        <hr
          style={{
            border: 'none',
            borderTop: '1px solid #e2e8f0',
            margin: '24px 0',
          }}
        />

        <h3
          style={{
            fontSize: '15px',
            fontWeight: 600,
            margin: '0 0 16px',
            color: '#1a2744',
          }}
        >
          Message Content
        </h3>
        <DetailField label="Title" value={campaign.messageTitle} />
        <DetailField label="Body" value={campaign.messageBody} />
        {campaign.messageImageUrl && (
          <DetailField
            label="Image URL"
            value={campaign.messageImageUrl}
          />
        )}
        {campaign.messageCtaText && (
          <DetailField label="CTA" value={campaign.messageCtaText} />
        )}
        {campaign.personalizationTokens && (
          <DetailField
            label="Personalization Tokens"
            value={campaign.personalizationTokens}
          />
        )}
      </div>

      {/* Targeting & Scheduling */}
      <div style={{ ...cardStyle, marginTop: '16px' }}>
        <h3
          style={{
            fontSize: '15px',
            fontWeight: 600,
            margin: '0 0 16px',
            color: '#1a2744',
          }}
        >
          Targeting & Scheduling
        </h3>
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: '1fr 1fr',
            gap: '24px',
          }}
        >
          <DetailField
            label="Display Placement"
            value={
              campaign.displayPlacement === 'POST_LOGIN'
                ? 'Post-Login Landing Page'
                : campaign.displayPlacement === 'LOGGED_OFF'
                  ? 'Logged-Off Page'
                  : campaign.displayPlacement
            }
          />
          <DetailField label="Frequency Cap" value={freqCapLabel} />
          <DetailField
            label="Max Impressions"
            value={String(campaign.frequencyCapMaxImpressions)}
          />
          <DetailField
            label="Delivery Window"
            value={
              campaign.deliveryStartTime && campaign.deliveryEndTime
                ? `${campaign.deliveryStartTime} - ${campaign.deliveryEndTime}`
                : '-'
            }
          />
          {campaign.audienceRules && (
            <DetailField
              label="Audience Rules"
              value={campaign.audienceRules}
            />
          )}
        </div>
      </div>

      {/* Fulfillment */}
      <div style={{ ...cardStyle, marginTop: '16px' }}>
        <h3
          style={{
            fontSize: '15px',
            fontWeight: 600,
            margin: '0 0 16px',
            color: '#1a2744',
          }}
        >
          Fulfillment Configuration
        </h3>
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: '1fr 1fr',
            gap: '24px',
          }}
        >
          <DetailField
            label="Remind Later Deferral"
            value={`${campaign.remindLaterDeferralDays} day(s)`}
          />
          <DetailField
            label="Decline Suppression"
            value={campaign.declineSuppression ? 'Enabled' : 'Disabled'}
          />
          <DetailField
            label="Confirmation Message"
            value={campaign.confirmationMessage}
          />
          {campaign.fulfillmentWorkflowUrl && (
            <DetailField
              label="Workflow URL"
              value={campaign.fulfillmentWorkflowUrl}
            />
          )}
        </div>
      </div>

      {/* Actions */}
      <div
        style={{
          display: 'flex',
          gap: '12px',
          flexWrap: 'wrap',
          marginTop: '24px',
        }}
      >
        {campaign.status !== 'ENDED' && (
          <Link
            to={`/campaigns/${campaign.id}/edit`}
            style={{
              padding: '10px 20px',
              background: '#1d4ed8',
              color: '#fff',
              borderRadius: '6px',
              textDecoration: 'none',
              fontSize: '14px',
              fontWeight: 600,
            }}
          >
            Edit Campaign
          </Link>
        )}
        <Link
          to={`/campaigns/${campaign.id}/analytics`}
          style={{
            padding: '10px 20px',
            background: '#6366f1',
            color: '#fff',
            borderRadius: '6px',
            textDecoration: 'none',
            fontSize: '14px',
            fontWeight: 600,
          }}
        >
          View Analytics
        </Link>

        {(campaign.status === 'DRAFT' ||
          campaign.status === 'PAUSED') && (
          <button
            onClick={() => handleStatusChange('ACTIVE')}
            style={actionBtn('#16a34a')}
          >
            Activate
          </button>
        )}
        {campaign.status === 'ACTIVE' && (
          <>
            <button
              onClick={() => handleStatusChange('PAUSED')}
              style={actionBtn('#d97706')}
            >
              Pause
            </button>
            <button
              onClick={() => handleStatusChange('ENDED')}
              style={actionBtn('#64748b')}
            >
              End Campaign
            </button>
          </>
        )}
        <button onClick={handleDelete} style={actionBtn('#dc2626')}>
          {campaign.status === 'DRAFT' ? 'Delete' : 'Archive'}
        </button>
      </div>
    </div>
  );
}

const cardStyle: React.CSSProperties = {
  background: '#fff',
  borderRadius: '8px',
  padding: '32px',
  boxShadow: '0 1px 3px rgba(0,0,0,0.06)',
  border: '1px solid #e5e7eb',
};

function DetailField({
  label,
  value,
}: {
  label: string;
  value: string | null | undefined;
}) {
  return (
    <div style={{ marginBottom: '12px' }}>
      <span
        style={{
          fontSize: '12px',
          color: '#6b7280',
          fontWeight: 500,
          textTransform: 'uppercase',
          letterSpacing: '0.05em',
        }}
      >
        {label}
      </span>
      <p
        style={{
          margin: '4px 0 0',
          fontSize: '14px',
          color: '#1a2744',
        }}
      >
        {value || '-'}
      </p>
    </div>
  );
}

function actionBtn(bg: string): React.CSSProperties {
  return {
    padding: '10px 20px',
    background: bg,
    color: '#fff',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
    fontSize: '14px',
    fontWeight: 600,
  };
}
