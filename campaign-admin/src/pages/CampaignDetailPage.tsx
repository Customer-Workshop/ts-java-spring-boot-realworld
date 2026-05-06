import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import {
  fetchCampaign,
  updateCampaign,
  deleteCampaign,
  cloneCampaign,
  fetchABTestVariants,
  createABTestVariant,
  declareABTestWinner,
  fetchAuditLog,
} from '../api/campaigns';
import { StatusBadge } from '../components/StatusBadge';
import type { Campaign, ABTestVariant, AuditLogEntry } from '../types/campaign';

type DetailTab = 'details' | 'ab-testing' | 'audit';

const channelLabels: Record<string, string> = {
  IN_APP: 'In-App',
  EMAIL: 'Email',
  SMS: 'SMS',
  PUSH: 'Push Notification',
  SOCIAL: 'Social Media',
  ADS: 'Ads',
};

export function CampaignDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [campaign, setCampaign] = useState<Campaign | null>(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<DetailTab>('details');
  const [variants, setVariants] = useState<ABTestVariant[]>([]);
  const [auditLogs, setAuditLogs] = useState<AuditLogEntry[]>([]);
  const [showVariantForm, setShowVariantForm] = useState(false);
  const [variantForm, setVariantForm] = useState({
    variantName: '',
    splitPercentage: 50,
    messageTitle: '',
    messageBody: '',
    messageCtaText: '',
    messageImageUrl: '',
  });

  useEffect(() => {
    if (id) {
      fetchCampaign(id)
        .then(setCampaign)
        .finally(() => setLoading(false));
    }
  }, [id]);

  useEffect(() => {
    if (id && activeTab === 'ab-testing') {
      fetchABTestVariants(id).then(setVariants).catch(() => {/* ignore */});
    }
    if (id && activeTab === 'audit') {
      fetchAuditLog(id).then(setAuditLogs).catch(() => {/* ignore */});
    }
  }, [id, activeTab]);

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

  const handleClone = async () => {
    if (!id) return;
    try {
      const cloned = await cloneCampaign(id, campaign.name + ' (Copy)');
      navigate(`/campaigns/${cloned.id}`);
    } catch {
      alert('Failed to clone campaign.');
    }
  };

  const handleAddVariant = async () => {
    if (!id) return;
    try {
      await createABTestVariant(id, variantForm);
      const updatedVariants = await fetchABTestVariants(id);
      setVariants(updatedVariants);
      setShowVariantForm(false);
      setVariantForm({
        variantName: '',
        splitPercentage: 50,
        messageTitle: '',
        messageBody: '',
        messageCtaText: '',
        messageImageUrl: '',
      });
    } catch {
      alert('Failed to create variant.');
    }
  };

  const handleDeclareWinner = async (variantId: string) => {
    if (!id) return;
    if (!window.confirm('Declare this variant as the winner?')) return;
    try {
      await declareABTestWinner(id, variantId);
      const updatedVariants = await fetchABTestVariants(id);
      setVariants(updatedVariants);
    } catch {
      alert('Failed to declare winner.');
    }
  };

  const freqCapLabel =
    campaign.frequencyCapType === 'ONCE_PER_SESSION'
      ? 'Once Per Session'
      : campaign.frequencyCapType === 'ONCE_PER_DAY'
        ? 'Once Per Day'
        : 'Once Per Campaign';

  const tabs: Array<{ key: DetailTab; label: string }> = [
    { key: 'details', label: 'Details' },
    { key: 'ab-testing', label: 'A/B Testing' },
    { key: 'audit', label: 'Audit Trail' },
  ];

  return (
    <div style={{ maxWidth: '900px' }}>
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
            {campaign.abTestEnabled && (
              <span
                style={{
                  marginLeft: '10px',
                  background: '#eef2ff',
                  color: '#4f46e5',
                  padding: '4px 10px',
                  borderRadius: '6px',
                  fontSize: '12px',
                  fontWeight: 600,
                }}
              >
                A/B Test
              </span>
            )}
          </h2>
        </div>
        <StatusBadge status={campaign.status} />
      </div>

      {/* Tab Navigation */}
      <div
        style={{
          display: 'flex',
          gap: '0',
          marginBottom: '20px',
          borderBottom: '2px solid #e5e7eb',
        }}
      >
        {tabs.map((tab) => (
          <button
            key={tab.key}
            onClick={() => setActiveTab(tab.key)}
            style={{
              padding: '10px 20px',
              border: 'none',
              background: 'none',
              cursor: 'pointer',
              fontSize: '14px',
              fontWeight: activeTab === tab.key ? 600 : 400,
              color:
                activeTab === tab.key ? '#1d4ed8' : '#6b7280',
              borderBottom:
                activeTab === tab.key
                  ? '2px solid #1d4ed8'
                  : '2px solid transparent',
              marginBottom: '-2px',
            }}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Details Tab */}
      {activeTab === 'details' && (
        <>
          {/* Basic Info */}
          <div style={cardStyle}>
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: '1fr 1fr 1fr',
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
                label="Channel"
                value={channelLabels[campaign.channel || 'IN_APP'] || campaign.channel}
              />
              <DetailField
                label="Priority"
                value={String(campaign.priority)}
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

            {campaign.tags && (
              <div style={{ marginTop: '16px' }}>
                <span
                  style={{
                    fontSize: '12px',
                    color: '#6b7280',
                    fontWeight: 500,
                    textTransform: 'uppercase',
                    letterSpacing: '0.05em',
                  }}
                >
                  Tags
                </span>
                <div
                  style={{
                    display: 'flex',
                    gap: '6px',
                    flexWrap: 'wrap',
                    marginTop: '6px',
                  }}
                >
                  {campaign.tags.split(',').map((tag) => (
                    <span
                      key={tag}
                      style={{
                        background: '#e0e7ff',
                        color: '#3730a3',
                        padding: '4px 10px',
                        borderRadius: '12px',
                        fontSize: '12px',
                        fontWeight: 500,
                      }}
                    >
                      {tag.trim()}
                    </span>
                  ))}
                </div>
              </div>
            )}

            <hr
              style={{
                border: 'none',
                borderTop: '1px solid #e2e8f0',
                margin: '24px 0',
              }}
            />

            <h3 style={sectionTitle}>Message Content</h3>
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
            <h3 style={sectionTitle}>Targeting & Scheduling</h3>
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
            <h3 style={sectionTitle}>Fulfillment Configuration</h3>
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
        </>
      )}

      {/* A/B Testing Tab */}
      {activeTab === 'ab-testing' && (
        <div style={cardStyle}>
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginBottom: '20px',
            }}
          >
            <h3 style={{ ...sectionTitle, margin: 0 }}>
              A/B Test Variants
            </h3>
            {campaign.abTestEnabled && (
              <button
                onClick={() => setShowVariantForm(true)}
                style={{
                  padding: '8px 16px',
                  border: 'none',
                  borderRadius: '6px',
                  background: '#1d4ed8',
                  color: '#fff',
                  cursor: 'pointer',
                  fontSize: '13px',
                  fontWeight: 600,
                }}
              >
                + Add Variant
              </button>
            )}
          </div>

          {!campaign.abTestEnabled && (
            <p style={{ color: '#6b7280', fontSize: '14px' }}>
              A/B Testing is not enabled for this campaign. Enable it in the
              campaign settings to create test variants.
            </p>
          )}

          {campaign.abTestEnabled && variants.length === 0 && (
            <p style={{ color: '#6b7280', fontSize: '14px' }}>
              No variants created yet. Add variants to start A/B testing.
            </p>
          )}

          {showVariantForm && (
            <div
              style={{
                background: '#f8fafc',
                padding: '20px',
                borderRadius: '8px',
                marginBottom: '20px',
                border: '1px solid #e5e7eb',
              }}
            >
              <h4
                style={{
                  fontSize: '14px',
                  fontWeight: 600,
                  margin: '0 0 16px',
                  color: '#1a2744',
                }}
              >
                New Variant
              </h4>
              <div
                style={{
                  display: 'grid',
                  gridTemplateColumns: '1fr 1fr',
                  gap: '12px',
                }}
              >
                <div>
                  <label style={formLabel}>Variant Name</label>
                  <input
                    value={variantForm.variantName}
                    onChange={(e) =>
                      setVariantForm({
                        ...variantForm,
                        variantName: e.target.value,
                      })
                    }
                    style={formInput}
                    placeholder="e.g. Variant B"
                  />
                </div>
                <div>
                  <label style={formLabel}>Split %</label>
                  <input
                    type="number"
                    min={1}
                    max={100}
                    value={variantForm.splitPercentage}
                    onChange={(e) =>
                      setVariantForm({
                        ...variantForm,
                        splitPercentage: Number(e.target.value),
                      })
                    }
                    style={formInput}
                  />
                </div>
                <div>
                  <label style={formLabel}>Message Title</label>
                  <input
                    value={variantForm.messageTitle}
                    onChange={(e) =>
                      setVariantForm({
                        ...variantForm,
                        messageTitle: e.target.value,
                      })
                    }
                    style={formInput}
                  />
                </div>
                <div>
                  <label style={formLabel}>CTA Text</label>
                  <input
                    value={variantForm.messageCtaText}
                    onChange={(e) =>
                      setVariantForm({
                        ...variantForm,
                        messageCtaText: e.target.value,
                      })
                    }
                    style={formInput}
                  />
                </div>
                <div style={{ gridColumn: '1 / -1' }}>
                  <label style={formLabel}>Message Body</label>
                  <textarea
                    value={variantForm.messageBody}
                    onChange={(e) =>
                      setVariantForm({
                        ...variantForm,
                        messageBody: e.target.value,
                      })
                    }
                    style={{ ...formInput, minHeight: '80px' }}
                  />
                </div>
              </div>
              <div
                style={{
                  display: 'flex',
                  gap: '8px',
                  marginTop: '12px',
                  justifyContent: 'flex-end',
                }}
              >
                <button
                  onClick={() => setShowVariantForm(false)}
                  style={{
                    padding: '8px 16px',
                    border: '1px solid #d1d5db',
                    borderRadius: '6px',
                    background: '#fff',
                    cursor: 'pointer',
                    fontSize: '13px',
                  }}
                >
                  Cancel
                </button>
                <button
                  onClick={handleAddVariant}
                  style={{
                    padding: '8px 16px',
                    border: 'none',
                    borderRadius: '6px',
                    background: '#1d4ed8',
                    color: '#fff',
                    cursor: 'pointer',
                    fontSize: '13px',
                    fontWeight: 600,
                  }}
                >
                  Save Variant
                </button>
              </div>
            </div>
          )}

          {variants.length > 0 && (
            <div style={{ display: 'grid', gap: '12px' }}>
              {variants.map((v) => (
                <div
                  key={v.id}
                  style={{
                    padding: '16px',
                    borderRadius: '8px',
                    border: v.winner
                      ? '2px solid #16a34a'
                      : '1px solid #e5e7eb',
                    background: v.winner ? '#f0fdf4' : '#fff',
                  }}
                >
                  <div
                    style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                    }}
                  >
                    <div>
                      <strong style={{ color: '#1a2744' }}>
                        {v.variantName}
                      </strong>
                      {v.winner && (
                        <span
                          style={{
                            marginLeft: '8px',
                            background: '#16a34a',
                            color: '#fff',
                            padding: '2px 8px',
                            borderRadius: '4px',
                            fontSize: '11px',
                            fontWeight: 600,
                          }}
                        >
                          WINNER
                        </span>
                      )}
                      <span
                        style={{
                          marginLeft: '8px',
                          color: '#6b7280',
                          fontSize: '13px',
                        }}
                      >
                        Split: {v.splitPercentage}%
                      </span>
                    </div>
                    {!v.winner &&
                      !variants.some((vr) => vr.winner) && (
                        <button
                          onClick={() => handleDeclareWinner(v.id)}
                          style={{
                            padding: '5px 12px',
                            border: '1px solid #16a34a',
                            borderRadius: '6px',
                            background: '#fff',
                            color: '#16a34a',
                            cursor: 'pointer',
                            fontSize: '12px',
                            fontWeight: 600,
                          }}
                        >
                          Declare Winner
                        </button>
                      )}
                  </div>
                  <div
                    style={{
                      display: 'grid',
                      gridTemplateColumns: '1fr 1fr 1fr 1fr',
                      gap: '16px',
                      marginTop: '12px',
                    }}
                  >
                    <div>
                      <span style={metricLabel}>Impressions</span>
                      <p style={metricValue}>
                        {v.impressions.toLocaleString()}
                      </p>
                    </div>
                    <div>
                      <span style={metricLabel}>Conversions</span>
                      <p style={metricValue}>
                        {v.conversions.toLocaleString()}
                      </p>
                    </div>
                    <div>
                      <span style={metricLabel}>Conversion Rate</span>
                      <p style={metricValue}>
                        {v.conversionRate.toFixed(1)}%
                      </p>
                    </div>
                    <div>
                      <span style={metricLabel}>Title</span>
                      <p style={metricValue}>
                        {v.messageTitle || '-'}
                      </p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Audit Trail Tab */}
      {activeTab === 'audit' && (
        <div style={cardStyle}>
          <h3 style={{ ...sectionTitle, marginBottom: '20px' }}>
            Change History
          </h3>
          {auditLogs.length === 0 ? (
            <p style={{ color: '#6b7280', fontSize: '14px' }}>
              No audit log entries found.
            </p>
          ) : (
            <div
              style={{
                borderLeft: '2px solid #e5e7eb',
                paddingLeft: '20px',
              }}
            >
              {auditLogs.map((log) => (
                <div
                  key={log.id}
                  style={{
                    position: 'relative',
                    marginBottom: '20px',
                    paddingBottom: '16px',
                    borderBottom: '1px solid #f1f5f9',
                  }}
                >
                  <div
                    style={{
                      position: 'absolute',
                      left: '-27px',
                      top: '4px',
                      width: '12px',
                      height: '12px',
                      borderRadius: '50%',
                      background:
                        log.action === 'CREATED'
                          ? '#16a34a'
                          : log.action === 'STATUS_CHANGE'
                            ? '#2563eb'
                            : log.action === 'DELETED'
                              ? '#dc2626'
                              : '#6b7280',
                    }}
                  />
                  <div
                    style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'flex-start',
                    }}
                  >
                    <div>
                      <span
                        style={{
                          fontSize: '13px',
                          fontWeight: 600,
                          color: '#1a2744',
                        }}
                      >
                        {log.action.replace('_', ' ')}
                      </span>
                      {log.fieldName && (
                        <span
                          style={{
                            marginLeft: '8px',
                            fontSize: '12px',
                            color: '#6b7280',
                          }}
                        >
                          ({log.fieldName})
                        </span>
                      )}
                      {log.oldValue && log.newValue && (
                        <div
                          style={{
                            marginTop: '4px',
                            fontSize: '12px',
                            color: '#6b7280',
                          }}
                        >
                          <span
                            style={{
                              textDecoration: 'line-through',
                              color: '#dc2626',
                            }}
                          >
                            {log.oldValue}
                          </span>
                          {' → '}
                          <span style={{ color: '#16a34a' }}>
                            {log.newValue}
                          </span>
                        </div>
                      )}
                    </div>
                    <span
                      style={{
                        fontSize: '12px',
                        color: '#94a3b8',
                        whiteSpace: 'nowrap',
                      }}
                    >
                      {new Date(log.timestamp).toLocaleString()}
                    </span>
                  </div>
                  <div
                    style={{
                      fontSize: '12px',
                      color: '#94a3b8',
                      marginTop: '4px',
                    }}
                  >
                    by {log.userId}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

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
        <button onClick={handleClone} style={actionBtn('#0891b2')}>
          Clone Campaign
        </button>
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

const sectionTitle: React.CSSProperties = {
  fontSize: '15px',
  fontWeight: 600,
  margin: '0 0 16px',
  color: '#1a2744',
};

const formLabel: React.CSSProperties = {
  display: 'block',
  fontSize: '12px',
  fontWeight: 500,
  color: '#6b7280',
  marginBottom: '4px',
};

const formInput: React.CSSProperties = {
  width: '100%',
  padding: '8px 12px',
  border: '1px solid #d1d5db',
  borderRadius: '6px',
  fontSize: '13px',
  boxSizing: 'border-box',
};

const metricLabel: React.CSSProperties = {
  fontSize: '11px',
  color: '#6b7280',
  textTransform: 'uppercase',
  fontWeight: 500,
};

const metricValue: React.CSSProperties = {
  margin: '2px 0 0',
  fontSize: '14px',
  fontWeight: 600,
  color: '#1a2744',
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
