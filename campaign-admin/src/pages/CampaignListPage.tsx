import { useEffect, useState, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  fetchCampaigns,
  deleteCampaign,
  cloneCampaign,
  bulkUpdateStatus,
} from '../api/campaigns';
import { StatusBadge } from '../components/StatusBadge';
import type { Campaign } from '../types/campaign';

const statusFilters: Array<{ label: string; value: string }> = [
  { label: 'All', value: '' },
  { label: 'Draft', value: 'DRAFT' },
  { label: 'Active', value: 'ACTIVE' },
  { label: 'Paused', value: 'PAUSED' },
  { label: 'Ended', value: 'ENDED' },
];

const channelLabels: Record<string, string> = {
  IN_APP: 'In-App',
  EMAIL: 'Email',
  SMS: 'SMS',
  PUSH: 'Push',
};

export function CampaignListPage() {
  const [campaigns, setCampaigns] = useState<Campaign[]>([]);
  const [statusFilter, setStatusFilter] = useState('');
  const [loading, setLoading] = useState(true);
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());
  const [bulkAction, setBulkAction] = useState('');
  const navigate = useNavigate();

  const loadCampaigns = useCallback(async () => {
    setLoading(true);
    try {
      const data = await fetchCampaigns(statusFilter || undefined);
      setCampaigns(data);
      setSelectedIds(new Set());
    } catch (err: unknown) {
      if (
        err &&
        typeof err === 'object' &&
        'response' in err &&
        (err as { response?: { status?: number } }).response?.status === 403
      ) {
        navigate('/access-denied');
      }
    } finally {
      setLoading(false);
    }
  }, [statusFilter, navigate]);

  useEffect(() => {
    loadCampaigns();
  }, [loadCampaigns]);

  const handleDelete = async (campaign: Campaign) => {
    const action =
      campaign.status === 'DRAFT' ? 'permanently delete' : 'archive';
    if (!window.confirm(`Are you sure you want to ${action} "${campaign.name}"?`))
      return;
    try {
      await deleteCampaign(campaign.id);
      loadCampaigns();
    } catch {
      alert('Failed to delete campaign.');
    }
  };

  const handleClone = async (campaign: Campaign) => {
    try {
      await cloneCampaign(campaign.id, campaign.name + ' (Copy)');
      loadCampaigns();
    } catch {
      alert('Failed to clone campaign.');
    }
  };

  const toggleSelection = (id: string) => {
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const toggleSelectAll = () => {
    if (selectedIds.size === campaigns.length) {
      setSelectedIds(new Set());
    } else {
      setSelectedIds(new Set(campaigns.map((c) => c.id)));
    }
  };

  const handleBulkAction = async () => {
    if (!bulkAction || selectedIds.size === 0) return;
    if (
      !window.confirm(
        `Apply "${bulkAction}" to ${selectedIds.size} campaign(s)?`
      )
    )
      return;
    try {
      const result = await bulkUpdateStatus(
        Array.from(selectedIds),
        bulkAction
      );
      alert(`${result.updated} campaign(s) updated.`);
      loadCampaigns();
    } catch {
      alert('Bulk action failed.');
    }
    setBulkAction('');
  };

  return (
    <div>
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '24px',
        }}
      >
        <h2
          style={{
            fontSize: '22px',
            fontWeight: 600,
            margin: 0,
            color: '#1a2744',
          }}
        >
          Campaigns
        </h2>
        <Link
          to="/campaigns/new"
          style={{
            background: '#1d4ed8',
            color: '#fff',
            padding: '10px 20px',
            borderRadius: '6px',
            textDecoration: 'none',
            fontSize: '14px',
            fontWeight: 600,
          }}
        >
          + New Campaign
        </Link>
      </div>

      <div
        style={{
          display: 'flex',
          gap: '8px',
          marginBottom: '20px',
          alignItems: 'center',
          flexWrap: 'wrap',
        }}
      >
        {statusFilters.map((f) => (
          <button
            key={f.value}
            onClick={() => setStatusFilter(f.value)}
            style={{
              padding: '8px 16px',
              border:
                statusFilter === f.value
                  ? '2px solid #1d4ed8'
                  : '1px solid #d1d5db',
              borderRadius: '6px',
              background: statusFilter === f.value ? '#eff6ff' : '#fff',
              color: statusFilter === f.value ? '#1d4ed8' : '#374151',
              cursor: 'pointer',
              fontSize: '13px',
              fontWeight: statusFilter === f.value ? 600 : 400,
            }}
          >
            {f.label}
          </button>
        ))}

        {selectedIds.size > 0 && (
          <div
            style={{
              marginLeft: 'auto',
              display: 'flex',
              gap: '8px',
              alignItems: 'center',
            }}
          >
            <span
              style={{ fontSize: '13px', color: '#6b7280' }}
            >
              {selectedIds.size} selected
            </span>
            <select
              value={bulkAction}
              onChange={(e) => setBulkAction(e.target.value)}
              style={{
                padding: '6px 12px',
                border: '1px solid #d1d5db',
                borderRadius: '6px',
                fontSize: '13px',
              }}
            >
              <option value="">Bulk Action...</option>
              <option value="ACTIVE">Activate</option>
              <option value="PAUSED">Pause</option>
              <option value="ENDED">End</option>
            </select>
            <button
              onClick={handleBulkAction}
              disabled={!bulkAction}
              style={{
                padding: '6px 14px',
                border: 'none',
                borderRadius: '6px',
                background: bulkAction ? '#1d4ed8' : '#94a3b8',
                color: '#fff',
                cursor: bulkAction ? 'pointer' : 'not-allowed',
                fontSize: '13px',
                fontWeight: 600,
              }}
            >
              Apply
            </button>
          </div>
        )}
      </div>

      {loading ? (
        <p style={{ color: '#64748b' }}>Loading campaigns...</p>
      ) : campaigns.length === 0 ? (
        <div
          style={{
            textAlign: 'center',
            padding: '60px',
            background: '#fff',
            borderRadius: '8px',
            border: '1px solid #e5e7eb',
          }}
        >
          <p style={{ color: '#6b7280', fontSize: '16px' }}>
            No campaigns found. Create your first campaign to get started.
          </p>
        </div>
      ) : (
        <div
          style={{
            background: '#fff',
            borderRadius: '8px',
            overflow: 'auto',
            boxShadow: '0 1px 3px rgba(0,0,0,0.06)',
            border: '1px solid #e5e7eb',
          }}
        >
          <table
            style={{
              width: '100%',
              borderCollapse: 'collapse',
              fontSize: '14px',
            }}
          >
            <thead>
              <tr style={{ background: '#f8fafc' }}>
                <th style={{ ...thStyle, width: '40px' }}>
                  <input
                    type="checkbox"
                    checked={
                      selectedIds.size === campaigns.length &&
                      campaigns.length > 0
                    }
                    onChange={toggleSelectAll}
                  />
                </th>
                <th style={thStyle}>Campaign Name</th>
                <th style={thStyle}>Status</th>
                <th style={thStyle}>Channel</th>
                <th style={thStyle}>Priority</th>
                <th style={thStyle}>Audience</th>
                <th style={thStyle}>Tags</th>
                <th style={thStyle}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {campaigns.map((c) => (
                <tr
                  key={c.id}
                  style={{
                    borderBottom: '1px solid #f1f5f9',
                    background: selectedIds.has(c.id)
                      ? '#f0f9ff'
                      : 'transparent',
                  }}
                >
                  <td style={tdStyle}>
                    <input
                      type="checkbox"
                      checked={selectedIds.has(c.id)}
                      onChange={() => toggleSelection(c.id)}
                    />
                  </td>
                  <td style={tdStyle}>
                    <Link
                      to={`/campaigns/${c.id}`}
                      style={{
                        color: '#1d4ed8',
                        textDecoration: 'none',
                        fontWeight: 500,
                      }}
                    >
                      {c.name}
                    </Link>
                    {c.abTestEnabled && (
                      <span
                        style={{
                          marginLeft: '6px',
                          background: '#eef2ff',
                          color: '#4f46e5',
                          padding: '2px 6px',
                          borderRadius: '4px',
                          fontSize: '10px',
                          fontWeight: 600,
                        }}
                      >
                        A/B
                      </span>
                    )}
                  </td>
                  <td style={tdStyle}>
                    <StatusBadge status={c.status} />
                  </td>
                  <td style={tdStyle}>
                    <span
                      style={{
                        background: '#f1f5f9',
                        padding: '3px 8px',
                        borderRadius: '4px',
                        fontSize: '12px',
                        fontWeight: 500,
                      }}
                    >
                      {channelLabels[c.channel || 'IN_APP'] || c.channel}
                    </span>
                  </td>
                  <td style={tdStyle}>
                    <span
                      style={{
                        display: 'inline-flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        width: '28px',
                        height: '28px',
                        borderRadius: '50%',
                        background:
                          c.priority >= 8
                            ? '#fee2e2'
                            : c.priority >= 5
                            ? '#fef9c3'
                            : '#f0fdf4',
                        color:
                          c.priority >= 8
                            ? '#dc2626'
                            : c.priority >= 5
                            ? '#ca8a04'
                            : '#16a34a',
                        fontSize: '12px',
                        fontWeight: 700,
                      }}
                    >
                      {c.priority}
                    </span>
                  </td>
                  <td style={tdStyle}>{c.targetAudienceSegment}</td>
                  <td style={tdStyle}>
                    <div style={{ display: 'flex', gap: '4px', flexWrap: 'wrap' }}>
                      {c.tags &&
                        c.tags.split(',').map((tag: string) => (
                          <span
                            key={tag}
                            style={{
                              background: '#e0e7ff',
                              color: '#3730a3',
                              padding: '2px 8px',
                              borderRadius: '12px',
                              fontSize: '11px',
                              fontWeight: 500,
                            }}
                          >
                            {tag.trim()}
                          </span>
                        ))}
                    </div>
                  </td>
                  <td style={tdStyle}>
                    <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                      <Link
                        to={`/campaigns/${c.id}/edit`}
                        style={{
                          color: '#2563eb',
                          textDecoration: 'none',
                          fontSize: '13px',
                        }}
                      >
                        Edit
                      </Link>
                      <button
                        onClick={() => handleClone(c)}
                        style={{
                          background: 'none',
                          border: 'none',
                          color: '#059669',
                          cursor: 'pointer',
                          fontSize: '13px',
                          padding: 0,
                        }}
                      >
                        Clone
                      </button>
                      <Link
                        to={`/campaigns/${c.id}/analytics`}
                        style={{
                          color: '#6366f1',
                          textDecoration: 'none',
                          fontSize: '13px',
                        }}
                      >
                        Analytics
                      </Link>
                      <button
                        onClick={() => handleDelete(c)}
                        style={{
                          background: 'none',
                          border: 'none',
                          color: '#dc2626',
                          cursor: 'pointer',
                          fontSize: '13px',
                          padding: 0,
                        }}
                      >
                        {c.status === 'DRAFT' ? 'Delete' : 'Archive'}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

const thStyle: React.CSSProperties = {
  textAlign: 'left',
  padding: '12px 16px',
  fontWeight: 600,
  color: '#6b7280',
  fontSize: '12px',
  textTransform: 'uppercase',
  letterSpacing: '0.05em',
};

const tdStyle: React.CSSProperties = {
  padding: '12px 16px',
  color: '#334155',
};
