import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  createCampaign,
  fetchCampaign,
  updateCampaign,
} from '../api/campaigns';
import type { CampaignFormData } from '../types/campaign';

const defaultForm: CampaignFormData = {
  name: '',
  targetAudienceSegment: '',
  startDate: '',
  endDate: '',
  messageTitle: '',
  messageBody: '',
  messageImageUrl: '',
  messageCtaText: '',
  fulfillmentActionType: 'ACCEPT',
  displayPlacement: 'POST_LOGIN',
  frequencyCapType: 'ONCE_PER_CAMPAIGN',
  frequencyCapMaxImpressions: 1,
  deliveryStartTime: '',
  deliveryEndTime: '',
  personalizationTokens: '',
  remindLaterDeferralDays: 1,
  fulfillmentWorkflowUrl: '',
  declineSuppression: true,
  confirmationMessage: 'Thank you for your response!',
  audienceRules: '',
  channel: 'IN_APP',
  priority: 5,
  tags: '',
  abTestEnabled: false,
};

export function CampaignFormPage() {
  const { id } = useParams<{ id: string }>();
  const isEditing = !!id;
  const navigate = useNavigate();
  const [form, setForm] = useState<CampaignFormData>(defaultForm);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [campaignStatus, setCampaignStatus] = useState('');

  useEffect(() => {
    if (id) {
      setLoading(true);
      fetchCampaign(id)
        .then((c) => {
          setCampaignStatus(c.status);
          setForm({
            name: c.name,
            targetAudienceSegment: c.targetAudienceSegment || '',
            startDate: c.startDate
              ? new Date(c.startDate).toISOString().slice(0, 16)
              : '',
            endDate: c.endDate
              ? new Date(c.endDate).toISOString().slice(0, 16)
              : '',
            messageTitle: c.messageTitle || '',
            messageBody: c.messageBody || '',
            messageImageUrl: c.messageImageUrl || '',
            messageCtaText: c.messageCtaText || '',
            fulfillmentActionType: c.fulfillmentActionType,
            displayPlacement: c.displayPlacement || 'POST_LOGIN',
            frequencyCapType: c.frequencyCapType || 'ONCE_PER_CAMPAIGN',
            frequencyCapMaxImpressions: c.frequencyCapMaxImpressions ?? 1,
            deliveryStartTime: c.deliveryStartTime || '',
            deliveryEndTime: c.deliveryEndTime || '',
            personalizationTokens: c.personalizationTokens || '',
            remindLaterDeferralDays: c.remindLaterDeferralDays ?? 1,
            fulfillmentWorkflowUrl: c.fulfillmentWorkflowUrl || '',
            declineSuppression: c.declineSuppression ?? true,
            confirmationMessage:
              c.confirmationMessage || 'Thank you for your response!',
            audienceRules: c.audienceRules || '',
            channel: c.channel || 'IN_APP',
            priority: c.priority ?? 5,
            tags: c.tags || '',
            abTestEnabled: c.abTestEnabled ?? false,
          });
        })
        .catch(() => setError('Failed to load campaign'))
        .finally(() => setLoading(false));
    }
  }, [id]);

  const isActiveOnly = campaignStatus === 'ACTIVE';
  const isEnded = campaignStatus === 'ENDED';

  const handleChange = (
    e: React.ChangeEvent<
      HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement
    >
  ) => {
    const { name, value, type } = e.target;
    if (type === 'checkbox') {
      setForm((prev) => ({
        ...prev,
        [name]: (e.target as HTMLInputElement).checked,
      }));
    } else if (type === 'number') {
      setForm((prev) => ({
        ...prev,
        [name]: value === '' ? 0 : Number(value),
      }));
    } else {
      setForm((prev) => ({ ...prev, [name]: value }));
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSaving(true);
    try {
      if (isEditing && id) {
        if (isActiveOnly) {
          await updateCampaign(id, {
            messageTitle: form.messageTitle,
            messageBody: form.messageBody,
            messageCtaText: form.messageCtaText,
          });
        } else {
          await updateCampaign(id, form);
        }
      } else {
        await createCampaign(form);
      }
      navigate('/campaigns');
    } catch {
      setError('Failed to save campaign. Please check your inputs.');
    } finally {
      setSaving(false);
    }
  };

  if (loading)
    return <p style={{ color: '#64748b' }}>Loading campaign...</p>;

  if (isEnded) {
    return (
      <div style={{ maxWidth: '720px' }}>
        <div
          style={{
            background: '#fef2f2',
            color: '#991b1b',
            padding: '24px',
            borderRadius: '8px',
            border: '1px solid #fecaca',
            textAlign: 'center',
          }}
        >
          <h2 style={{ margin: '0 0 8px', fontSize: '20px' }}>
            Cannot Edit
          </h2>
          <p style={{ margin: '0 0 16px' }}>
            ENDED campaigns cannot be edited. They are preserved for
            reporting history.
          </p>
          <button
            onClick={() => navigate(`/campaigns/${id}`)}
            style={{
              padding: '10px 24px',
              border: 'none',
              borderRadius: '8px',
              background: '#1d4ed8',
              color: '#fff',
              cursor: 'pointer',
              fontSize: '14px',
              fontWeight: 600,
            }}
          >
            Back to Campaign
          </button>
        </div>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '720px' }}>
      <h2
        style={{
          fontSize: '22px',
          fontWeight: 600,
          marginBottom: '24px',
          color: '#1a2744',
        }}
      >
        {isEditing ? 'Edit Campaign' : 'Create New Campaign'}
      </h2>

      {isActiveOnly && (
        <div
          style={{
            background: '#fef9c3',
            color: '#854d0e',
            padding: '12px 16px',
            borderRadius: '8px',
            fontSize: '14px',
            marginBottom: '20px',
          }}
        >
          This campaign is Active. Only message content fields can be
          edited.
        </div>
      )}

      {error && (
        <div
          style={{
            background: '#fee2e2',
            color: '#991b1b',
            padding: '12px',
            borderRadius: '8px',
            fontSize: '14px',
            marginBottom: '16px',
          }}
        >
          {error}
        </div>
      )}

      <form
        onSubmit={handleSubmit}
        style={{
          background: '#fff',
          padding: '32px',
          borderRadius: '8px',
          boxShadow: '0 1px 3px rgba(0,0,0,0.06)',
          border: '1px solid #e5e7eb',
        }}
      >
        {/* Basic Info */}
        <div style={fieldGroup}>
          <label style={labelStyle}>Campaign Name *</label>
          <input
            name="name"
            value={form.name}
            onChange={handleChange}
            required
            disabled={isActiveOnly}
            style={inputStyle}
            placeholder="Enter campaign name"
          />
        </div>

        <div style={fieldGroup}>
          <label style={labelStyle}>Target Audience Segment</label>
          <input
            name="targetAudienceSegment"
            value={form.targetAudienceSegment}
            onChange={handleChange}
            disabled={isActiveOnly}
            style={inputStyle}
            placeholder="e.g., High-Value Customers"
          />
        </div>

        <div
          style={{
            display: 'grid',
            gridTemplateColumns: '1fr 1fr',
            gap: '16px',
            ...fieldGroup,
          }}
        >
          <div>
            <label style={labelStyle}>Start Date</label>
            <input
              name="startDate"
              type="datetime-local"
              value={form.startDate}
              onChange={handleChange}
              disabled={isActiveOnly}
              style={inputStyle}
            />
          </div>
          <div>
            <label style={labelStyle}>End Date</label>
            <input
              name="endDate"
              type="datetime-local"
              value={form.endDate}
              onChange={handleChange}
              disabled={isActiveOnly}
              style={inputStyle}
            />
          </div>
        </div>

        {/* Audience Segmentation Rules */}
        <SectionHeader title="Audience Segmentation" />
        <div style={fieldGroup}>
          <label style={labelStyle}>
            Audience Rules (JSON attributes)
          </label>
          <textarea
            name="audienceRules"
            value={form.audienceRules}
            onChange={handleChange}
            disabled={isActiveOnly}
            rows={3}
            style={{ ...inputStyle, resize: 'vertical' }}
            placeholder='{"attributes":["accountType","segment","geography"]}'
          />
          <p style={helpText}>
            Define target audience using customer attributes like account
            type, product holdings, and behavioral signals.
          </p>
        </div>

        {/* Message Content */}
        <SectionHeader title="Message Content" />

        <div style={fieldGroup}>
          <label style={labelStyle}>Message Title</label>
          <input
            name="messageTitle"
            value={form.messageTitle}
            onChange={handleChange}
            style={inputStyle}
            placeholder="Enter message title"
          />
        </div>

        <div style={fieldGroup}>
          <label style={labelStyle}>Message Body</label>
          <textarea
            name="messageBody"
            value={form.messageBody}
            onChange={handleChange}
            rows={4}
            style={{ ...inputStyle, resize: 'vertical' }}
            placeholder="Enter message body content. Use tokens like {{firstName}} for personalization."
          />
        </div>

        <div style={fieldGroup}>
          <label style={labelStyle}>Image URL (optional)</label>
          <input
            name="messageImageUrl"
            value={form.messageImageUrl}
            onChange={handleChange}
            disabled={isActiveOnly}
            style={inputStyle}
            placeholder="https://example.com/image.png"
          />
        </div>

        <div style={fieldGroup}>
          <label style={labelStyle}>CTA Text (optional)</label>
          <input
            name="messageCtaText"
            value={form.messageCtaText}
            onChange={handleChange}
            style={inputStyle}
            placeholder="e.g., Shop Now"
          />
        </div>

        <div style={fieldGroup}>
          <label style={labelStyle}>
            Personalization Tokens (JSON, up to 5)
          </label>
          <textarea
            name="personalizationTokens"
            value={form.personalizationTokens}
            onChange={handleChange}
            disabled={isActiveOnly}
            rows={3}
            style={{ ...inputStyle, resize: 'vertical' }}
            placeholder='[{"token":"{{firstName}}","field":"First Name"},{"token":"{{balance}}","field":"Account Balance"}]'
          />
          <p style={helpText}>
            Up to 5 personalized data fields (e.g., first name, account
            balance range) using placeholder tokens.
          </p>
        </div>

        {/* Scheduling & Display */}
        <SectionHeader title="Scheduling & Display" />

        <div
          style={{
            display: 'grid',
            gridTemplateColumns: '1fr 1fr',
            gap: '16px',
            ...fieldGroup,
          }}
        >
          <div>
            <label style={labelStyle}>Display Placement</label>
            <select
              name="displayPlacement"
              value={form.displayPlacement}
              onChange={handleChange}
              disabled={isActiveOnly}
              style={inputStyle}
            >
              <option value="POST_LOGIN">Post-Login Landing Page</option>
              <option value="LOGGED_OFF">Logged-Off Page</option>
            </select>
          </div>
          <div>
            <label style={labelStyle}>Frequency Cap</label>
            <select
              name="frequencyCapType"
              value={form.frequencyCapType}
              onChange={handleChange}
              disabled={isActiveOnly}
              style={inputStyle}
            >
              <option value="ONCE_PER_SESSION">Once Per Session</option>
              <option value="ONCE_PER_DAY">Once Per Day</option>
              <option value="ONCE_PER_CAMPAIGN">
                Once Per Campaign Lifetime
              </option>
            </select>
          </div>
        </div>

        <div
          style={{
            display: 'grid',
            gridTemplateColumns: '1fr 1fr 1fr',
            gap: '16px',
            ...fieldGroup,
          }}
        >
          <div>
            <label style={labelStyle}>Max Impressions</label>
            <input
              name="frequencyCapMaxImpressions"
              type="number"
              min={1}
              value={form.frequencyCapMaxImpressions}
              onChange={handleChange}
              disabled={isActiveOnly}
              style={inputStyle}
            />
          </div>
          <div>
            <label style={labelStyle}>Delivery Start Time</label>
            <input
              name="deliveryStartTime"
              type="time"
              value={form.deliveryStartTime}
              onChange={handleChange}
              disabled={isActiveOnly}
              style={inputStyle}
            />
          </div>
          <div>
            <label style={labelStyle}>Delivery End Time</label>
            <input
              name="deliveryEndTime"
              type="time"
              value={form.deliveryEndTime}
              onChange={handleChange}
              disabled={isActiveOnly}
              style={inputStyle}
            />
          </div>
        </div>

        {/* Fulfillment */}
        <SectionHeader title="Fulfillment Actions" />

        <div
          style={{
            display: 'grid',
            gridTemplateColumns: '1fr 1fr',
            gap: '16px',
            ...fieldGroup,
          }}
        >
          <div>
            <label style={labelStyle}>Fulfillment Action Type *</label>
            <select
              name="fulfillmentActionType"
              value={form.fulfillmentActionType}
              onChange={handleChange}
              disabled={isActiveOnly}
              style={inputStyle}
            >
              <option value="ACCEPT">Accept</option>
              <option value="DECLINE">Decline</option>
              <option value="REMIND_LATER">Remind Later</option>
            </select>
          </div>
          <div>
            <label style={labelStyle}>
              Remind Later Deferral (days)
            </label>
            <input
              name="remindLaterDeferralDays"
              type="number"
              min={1}
              value={form.remindLaterDeferralDays}
              onChange={handleChange}
              disabled={isActiveOnly}
              style={inputStyle}
            />
          </div>
        </div>

        <div style={fieldGroup}>
          <label style={labelStyle}>
            Fulfillment Workflow URL (optional)
          </label>
          <input
            name="fulfillmentWorkflowUrl"
            value={form.fulfillmentWorkflowUrl}
            onChange={handleChange}
            disabled={isActiveOnly}
            style={inputStyle}
            placeholder="https://api.example.com/enroll"
          />
          <p style={helpText}>
            Upon acceptance, the system triggers this downstream workflow
            (product application, redirect URL, or API callback).
          </p>
        </div>

        <div style={fieldGroup}>
          <label style={labelStyle}>Confirmation Message</label>
          <input
            name="confirmationMessage"
            value={form.confirmationMessage}
            onChange={handleChange}
            disabled={isActiveOnly}
            style={inputStyle}
            placeholder="Thank you for your response!"
          />
          <p style={helpText}>
            On-screen confirmation shown to end users after completing any
            fulfillment action.
          </p>
        </div>

        <div style={{ ...fieldGroup, display: 'flex', alignItems: 'center', gap: '8px' }}>
          <input
            name="declineSuppression"
            type="checkbox"
            checked={form.declineSuppression}
            onChange={handleChange}
            disabled={isActiveOnly}
            style={{ width: '18px', height: '18px' }}
          />
          <label style={{ ...labelStyle, marginBottom: 0 }}>
            Suppress campaign for users who decline (no re-display for
            campaign lifetime)
          </label>
        </div>

        {/* Industry Features Section */}
        <h3
          style={{
            fontSize: '17px',
            fontWeight: 600,
            color: '#1a2744',
            marginTop: '32px',
            marginBottom: '16px',
            paddingBottom: '8px',
            borderBottom: '2px solid #e5e7eb',
          }}
        >
          Channel & Priority
        </h3>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
          <div style={fieldGroup}>
            <label style={labelStyle}>Delivery Channel</label>
            <select
              name="channel"
              value={form.channel}
              onChange={handleChange}
              disabled={isActiveOnly}
              style={inputStyle}
            >
              <option value="IN_APP">In-App</option>
              <option value="EMAIL">Email</option>
              <option value="SMS">SMS</option>
              <option value="PUSH">Push Notification</option>
              <option value="SOCIAL">Social Media</option>
              <option value="ADS">Ads</option>
            </select>
            <p style={helpText}>
              Primary channel for campaign delivery.
            </p>
          </div>

          <div style={fieldGroup}>
            <label style={labelStyle}>Priority (1-10)</label>
            <input
              name="priority"
              type="number"
              min={1}
              max={10}
              value={form.priority}
              onChange={handleChange}
              disabled={isActiveOnly}
              style={inputStyle}
            />
            <p style={helpText}>
              Higher priority campaigns are shown first when multiple campaigns target the same user.
            </p>
          </div>
        </div>

        <div style={fieldGroup}>
          <label style={labelStyle}>Tags (comma-separated)</label>
          <input
            name="tags"
            value={form.tags}
            onChange={handleChange}
            disabled={isActiveOnly}
            style={inputStyle}
            placeholder="e.g. rewards, premium, seasonal"
          />
          <p style={helpText}>
            Organize campaigns with tags for easy filtering and categorization.
          </p>
        </div>

        <div style={{ ...fieldGroup, display: 'flex', alignItems: 'center', gap: '8px' }}>
          <input
            name="abTestEnabled"
            type="checkbox"
            checked={form.abTestEnabled}
            onChange={handleChange}
            disabled={isActiveOnly}
            style={{ width: '18px', height: '18px' }}
          />
          <label style={{ ...labelStyle, marginBottom: 0 }}>
            Enable A/B Testing for this campaign
          </label>
        </div>
        {form.abTestEnabled && (
          <p style={helpText}>
            After creating the campaign, go to the campaign detail page to configure A/B test variants with different message content and split percentages.
          </p>
        )}

        <div
          style={{
            display: 'flex',
            gap: '12px',
            justifyContent: 'flex-end',
            marginTop: '24px',
          }}
        >
          <button
            type="button"
            onClick={() => navigate('/campaigns')}
            style={{
              padding: '10px 24px',
              border: '1px solid #d1d5db',
              borderRadius: '6px',
              background: '#fff',
              cursor: 'pointer',
              fontSize: '14px',
            }}
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={saving}
            style={{
              padding: '10px 24px',
              border: 'none',
              borderRadius: '6px',
              background: '#1d4ed8',
              color: '#fff',
              cursor: saving ? 'not-allowed' : 'pointer',
              fontSize: '14px',
              fontWeight: 600,
              opacity: saving ? 0.7 : 1,
            }}
          >
            {saving
              ? 'Saving...'
              : isEditing
                ? 'Update Campaign'
                : 'Create Campaign'}
          </button>
        </div>
      </form>
    </div>
  );
}

function SectionHeader({ title }: { title: string }) {
  return (
    <>
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
        {title}
      </h3>
    </>
  );
}

const fieldGroup: React.CSSProperties = { marginBottom: '16px' };

const labelStyle: React.CSSProperties = {
  display: 'block',
  fontSize: '14px',
  fontWeight: 500,
  color: '#374151',
  marginBottom: '6px',
};

const inputStyle: React.CSSProperties = {
  width: '100%',
  padding: '10px 12px',
  border: '1px solid #d1d5db',
  borderRadius: '6px',
  fontSize: '14px',
  boxSizing: 'border-box',
};

const helpText: React.CSSProperties = {
  fontSize: '12px',
  color: '#6b7280',
  margin: '4px 0 0',
};
