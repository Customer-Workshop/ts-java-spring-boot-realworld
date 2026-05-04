import { useEffect, useState, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { fetchCampaigns, deleteCampaign } from '../api/campaigns';
import { StatusBadge } from '../components/StatusBadge';
import type { Campaign } from '../types/campaign';

const statusFilters: Array<{ label: string; value: string }> = [
  { label: 'All', value: '' },
  { label: 'Draft', value: 'DRAFT' },
  { label: 'Active', value: 'ACTIVE' },
  { label: 'Paused', value: 'PAUSED' },
  { label: 'Ended', value: 'ENDED' },
];

export function CampaignListPage() {
  const [campaigns, setCampaigns] = useState<Campaign[]>([]);
  const [statusFilter, setStatusFilter] = useState('');
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  const loadCampaigns = useCallback(async () => {
    setLoading(true);
    try {
      const data = await fetchCampaigns(statusFilter || undefined);
      setCampaigns(data);
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
    await deleteCampaign(campaign.id);
    loadCampaigns();
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
        <h2 style={{ fontSize: '24px', fontWeight: 700, margin: 0 }}>
          Campaigns
        </h2>
        <Link
          to="/campaigns/new"
          style={{
            background: '#2563eb',
            color: '#fff',
            padding: '10px 20px',
            borderRadius: '8px',
            textDecoration: 'none',
            fontSize: '14px',
            fontWeight: 600,
          }}
        >
          + New Campaign
        </Link>
      </div>

      <div style={{ display: 'flex', gap: '8px', marginBottom: '20px' }}>
        {statusFilters.map((f) => (
          <button
            key={f.value}
            onClick={() => setStatusFilter(f.value)}
            style={{
              padding: '8px 16px',
              border:
                statusFilter === f.value
                  ? '2px solid #2563eb'
                  : '1px solid #d1d5db',
              borderRadius: '8px',
              background: statusFilter === f.value ? '#eff6ff' : '#fff',
              color: statusFilter === f.value ? '#2563eb' : '#374151',
              cursor: 'pointer',
              fontSize: '13px',
              fontWeight: statusFilter === f.value ? 600 : 400,
            }}
          >
            {f.label}
          </button>
        ))}
      </div>

      {loading ? (
        <p style={{ color: '#64748b' }}>Loading campaigns...</p>
      ) : campaigns.length === 0 ? (
        <div
          style={{
            textAlign: 'center',
            padding: '60px',
            background: '#fff',
            borderRadius: '12px',
          }}
        >
          <p style={{ color: '#64748b', fontSize: '16px' }}>
            No campaigns found. Create your first campaign to get started.
          </p>
        </div>
      ) : (
        <div
          style={{
            background: '#fff',
            borderRadius: '12px',
            overflow: 'hidden',
            boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
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
                <th style={thStyle}>Campaign Name</th>
                <th style={thStyle}>Status</th>
                <th style={thStyle}>Audience</th>
                <th style={thStyle}>Start Date</th>
                <th style={thStyle}>End Date</th>
                <th style={thStyle}>Action Type</th>
                <th style={thStyle}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {campaigns.map((c) => (
                <tr
                  key={c.id}
                  style={{ borderBottom: '1px solid #f1f5f9' }}
                >
                  <td style={tdStyle}>
                    <Link
                      to={`/campaigns/${c.id}`}
                      style={{
                        color: '#2563eb',
                        textDecoration: 'none',
                        fontWeight: 500,
                      }}
                    >
                      {c.name}
                    </Link>
                  </td>
                  <td style={tdStyle}>
                    <StatusBadge status={c.status} />
                  </td>
                  <td style={tdStyle}>{c.targetAudienceSegment}</td>
                  <td style={tdStyle}>
                    {c.startDate
                      ? new Date(c.startDate).toLocaleDateString()
                      : '-'}
                  </td>
                  <td style={tdStyle}>
                    {c.endDate
                      ? new Date(c.endDate).toLocaleDateString()
                      : '-'}
                  </td>
                  <td style={tdStyle}>{c.fulfillmentActionType}</td>
                  <td style={tdStyle}>
                    <div style={{ display: 'flex', gap: '8px' }}>
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
                      <Link
                        to={`/campaigns/${c.id}/analytics`}
                        style={{
                          color: '#7c3aed',
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
  color: '#64748b',
  fontSize: '12px',
  textTransform: 'uppercase',
  letterSpacing: '0.05em',
};

const tdStyle: React.CSSProperties = {
  padding: '12px 16px',
  color: '#334155',
};
