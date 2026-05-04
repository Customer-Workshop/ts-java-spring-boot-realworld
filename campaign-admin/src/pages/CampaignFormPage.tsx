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
          });
        })
        .catch(() => setError('Failed to load campaign'))
        .finally(() => setLoading(false));
    }
  }, [id]);

  const isActiveOnly = campaignStatus === 'ACTIVE';

  const handleChange = (
    e: React.ChangeEvent<
      HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement
    >
  ) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
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

  return (
    <div style={{ maxWidth: '720px' }}>
      <h2 style={{ fontSize: '24px', fontWeight: 700, marginBottom: '24px' }}>
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
          This campaign is Active. Only message content fields can be edited.
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
          borderRadius: '12px',
          boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
        }}
      >
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

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', ...fieldGroup }}>
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

        <hr style={{ border: 'none', borderTop: '1px solid #e2e8f0', margin: '24px 0' }} />
        <h3 style={{ fontSize: '16px', fontWeight: 600, margin: '0 0 16px', color: '#1e293b' }}>
          Message Content
        </h3>

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
            placeholder="Enter message body content"
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
              borderRadius: '8px',
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
              borderRadius: '8px',
              background: '#2563eb',
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
  borderRadius: '8px',
  fontSize: '14px',
  boxSizing: 'border-box',
};
