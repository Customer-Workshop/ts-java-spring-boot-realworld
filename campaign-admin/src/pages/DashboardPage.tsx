import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  fetchDashboard,
  fetchCampaigns,
  exportDashboardCsv,
} from '../api/campaigns';
import {
  PieChart,
  Pie,
  Cell,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import type { Campaign, DashboardSummary } from '../types/campaign';

const STATUS_COLORS: Record<string, string> = {
  Active: '#22c55e',
  Draft: '#94a3b8',
  Paused: '#f59e0b',
  Ended: '#ef4444',
};

const DECISION_COLORS = ['#22c55e', '#ef4444', '#f59e0b', '#6366f1'];

export function DashboardPage() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [campaigns, setCampaigns] = useState<Campaign[]>([]);
  const [loading, setLoading] = useState(true);
  const [exporting, setExporting] = useState(false);
  const [filterName, setFilterName] = useState('');
  const [filterSegment, setFilterSegment] = useState('');
  const [filterDecision, setFilterDecision] = useState('');
  const [filterDateFrom, setFilterDateFrom] = useState('');
  const [filterDateTo, setFilterDateTo] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    Promise.all([fetchDashboard(), fetchCampaigns()])
      .then(([s, c]) => {
        setSummary(s);
        setCampaigns(c);
      })
      .catch((err) => {
        if (err?.response?.status === 403) navigate('/access-denied');
      })
      .finally(() => setLoading(false));
  }, [navigate]);

  const handleExport = async () => {
    setExporting(true);
    try {
      await exportDashboardCsv();
    } catch {
      alert('Failed to export data.');
    } finally {
      setExporting(false);
    }
  };

  if (loading)
    return <p style={{ color: '#64748b' }}>Loading dashboard...</p>;
  if (!summary)
    return <p style={{ color: '#dc2626' }}>Failed to load dashboard.</p>;

  const statusData = [
    { name: 'Active', value: summary.activeCampaigns },
    { name: 'Draft', value: summary.draftCampaigns },
    { name: 'Paused', value: summary.pausedCampaigns },
    { name: 'Ended', value: summary.endedCampaigns },
  ];

  const decisionData = [
    { name: 'Accepted', value: summary.totalAccepted },
    { name: 'Declined', value: summary.totalDeclined },
    { name: 'Unfinished', value: summary.totalClickedUnfinished },
    { name: 'Remind Later', value: summary.totalRemindLater },
  ];

  const filteredCampaigns = campaigns.filter((c) => {
    if (filterName && !c.name.toLowerCase().includes(filterName.toLowerCase()))
      return false;
    if (
      filterSegment &&
      !c.targetAudienceSegment
        ?.toLowerCase()
        .includes(filterSegment.toLowerCase())
    )
      return false;
    if (filterDecision && c.fulfillmentActionType !== filterDecision)
      return false;
    if (filterDateFrom && c.startDate && c.startDate.slice(0, 10) < filterDateFrom)
      return false;
    if (filterDateTo && c.endDate && c.endDate.slice(0, 10) > filterDateTo)
      return false;
    return true;
  });

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
          Campaign &amp; Promotion Dashboard
        </h2>
        <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
          {summary.lastUpdated && (
            <span style={{ fontSize: '12px', color: '#6b7280' }}>
              Last updated:{' '}
              {new Date(summary.lastUpdated).toLocaleString()}
            </span>
          )}
          <button
            onClick={handleExport}
            disabled={exporting}
            style={{
              padding: '8px 16px',
              background: '#1d4ed8',
              color: '#fff',
              border: 'none',
              borderRadius: '6px',
              cursor: 'pointer',
              fontSize: '13px',
              fontWeight: 600,
              opacity: exporting ? 0.6 : 1,
            }}
          >
            {exporting ? 'Exporting...' : 'Export CSV'}
          </button>
        </div>
      </div>

      {/* Filters */}
      <div
        style={{
          background: '#fff',
          borderRadius: '8px',
          padding: '16px 20px',
          boxShadow: '0 1px 3px rgba(0,0,0,0.06)',
          border: '1px solid #e5e7eb',
          marginBottom: '24px',
          display: 'flex',
          gap: '12px',
          flexWrap: 'wrap',
          alignItems: 'flex-end',
        }}
      >
        <FilterInput
          label="Campaign Name"
          value={filterName}
          onChange={setFilterName}
        />
        <FilterInput
          label="Target Segment"
          value={filterSegment}
          onChange={setFilterSegment}
        />
        <FilterSelect
          label="Fulfillment Type"
          value={filterDecision}
          onChange={setFilterDecision}
          options={[
            { label: 'All', value: '' },
            { label: 'Accept', value: 'ACCEPT' },
            { label: 'Decline', value: 'DECLINE' },
            { label: 'Remind Later', value: 'REMIND_LATER' },
          ]}
        />
        <div>
          <label style={filterLabelStyle}>Date From</label>
          <input
            type="date"
            value={filterDateFrom}
            onChange={(e) => setFilterDateFrom(e.target.value)}
            style={filterInputStyle}
          />
        </div>
        <div>
          <label style={filterLabelStyle}>Date To</label>
          <input
            type="date"
            value={filterDateTo}
            onChange={(e) => setFilterDateTo(e.target.value)}
            style={filterInputStyle}
          />
        </div>
        <button
          onClick={() => {
            setFilterName('');
            setFilterSegment('');
            setFilterDecision('');
            setFilterDateFrom('');
            setFilterDateTo('');
          }}
          style={{
            padding: '8px 14px',
            border: '1px solid #d1d5db',
            borderRadius: '6px',
            background: '#f9fafb',
            color: '#374151',
            cursor: 'pointer',
            fontSize: '13px',
            marginTop: '18px',
          }}
        >
          Clear Filters
        </button>
      </div>

      {/* KPI Row */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(6, 1fr)',
          gap: '16px',
          marginBottom: '32px',
        }}
      >
        <KpiCard
          label="Total Campaigns"
          value={summary.totalCampaigns}
          color="#1d4ed8"
        />
        <KpiCard
          label="Total Targeted"
          value={summary.totalTargetedPopulation}
          color="#6366f1"
        />
        <KpiCard
          label="Accepted"
          value={summary.totalAccepted}
          color="#22c55e"
        />
        <KpiCard
          label="Declined"
          value={summary.totalDeclined}
          color="#ef4444"
        />
        <KpiCard
          label="Unfinished"
          value={summary.totalClickedUnfinished}
          color="#f59e0b"
        />
        <KpiCard
          label="Remind Later"
          value={summary.totalRemindLater}
          color="#6366f1"
        />
      </div>

      {/* Charts */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: '1fr 1fr',
          gap: '24px',
          marginBottom: '32px',
        }}
      >
        <ChartCard title="Campaign Status Distribution">
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={statusData}
                cx="50%"
                cy="50%"
                outerRadius={110}
                fill="#8884d8"
                dataKey="value"
                label={({ name, value }) => `${name}: ${value}`}
              >
                {statusData.map((entry) => (
                  <Cell key={entry.name} fill={STATUS_COLORS[entry.name]} />
                ))}
              </Pie>
              <Tooltip />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        </ChartCard>

        <ChartCard title="Overall Decision Breakdown">
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={decisionData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" fontSize={12} />
              <YAxis fontSize={12} />
              <Tooltip />
              <Bar dataKey="value" name="Count">
                {decisionData.map((_, index) => (
                  <Cell
                    key={`cell-${index}`}
                    fill={DECISION_COLORS[index]}
                  />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </ChartCard>
      </div>

      {/* Conversion Rate */}
      <div
        style={{
          background: '#fff',
          borderRadius: '8px',
          padding: '32px',
          boxShadow: '0 1px 3px rgba(0,0,0,0.06)',
          border: '1px solid #e5e7eb',
          marginBottom: '32px',
        }}
      >
        <h3
          style={{
            fontSize: '15px',
            fontWeight: 600,
            margin: '0 0 20px',
            color: '#1a2744',
          }}
        >
          Performance Metrics
        </h3>
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(4, 1fr)',
            gap: '24px',
          }}
        >
          <MetricBar
            label="Acceptance Rate"
            value={
              summary.totalTargetedPopulation > 0
                ? (summary.totalAccepted /
                    summary.totalTargetedPopulation) *
                  100
                : 0
            }
            color="#22c55e"
          />
          <MetricBar
            label="Decline Rate"
            value={
              summary.totalTargetedPopulation > 0
                ? (summary.totalDeclined /
                    summary.totalTargetedPopulation) *
                  100
                : 0
            }
            color="#ef4444"
          />
          <MetricBar
            label="Drop-off Rate"
            value={
              summary.totalTargetedPopulation > 0
                ? (summary.totalClickedUnfinished /
                    summary.totalTargetedPopulation) *
                  100
                : 0
            }
            color="#f59e0b"
          />
          <MetricBar
            label="Deferral Rate"
            value={
              summary.totalTargetedPopulation > 0
                ? (summary.totalRemindLater /
                    summary.totalTargetedPopulation) *
                  100
                : 0
            }
            color="#6366f1"
          />
        </div>
      </div>

      {/* Recent Campaigns Table */}
      <div
        style={{
          background: '#fff',
          borderRadius: '8px',
          padding: '24px',
          boxShadow: '0 1px 3px rgba(0,0,0,0.06)',
          border: '1px solid #e5e7eb',
        }}
      >
        <h3
          style={{
            fontSize: '15px',
            fontWeight: 600,
            margin: '0 0 16px',
            color: '#1a2744',
          }}
        >
          Recent Campaigns
          {(filterName || filterSegment || filterDecision || filterDateFrom || filterDateTo) && (
            <span
              style={{ fontSize: '12px', color: '#6b7280', fontWeight: 400 }}
            >
              {' '}
              ({filteredCampaigns.length} results)
            </span>
          )}
        </h3>
        <table
          style={{
            width: '100%',
            borderCollapse: 'collapse',
            fontSize: '14px',
          }}
        >
          <thead>
            <tr style={{ background: '#f8fafc' }}>
              <th style={thStyle}>Name</th>
              <th style={thStyle}>Status</th>
              <th style={thStyle}>Audience</th>
              <th style={thStyle}>Placement</th>
              <th style={thStyle}>Created</th>
            </tr>
          </thead>
          <tbody>
            {filteredCampaigns.slice(0, 10).map((c) => (
              <tr
                key={c.id}
                style={{ borderBottom: '1px solid #f1f5f9' }}
              >
                <td style={tdStyle}>
                  <Link
                    to={`/campaigns/${c.id}`}
                    style={{ color: '#1d4ed8', textDecoration: 'none' }}
                  >
                    {c.name}
                  </Link>
                </td>
                <td style={tdStyle}>
                  <span
                    style={{
                      display: 'inline-block',
                      padding: '2px 8px',
                      borderRadius: '9999px',
                      fontSize: '11px',
                      fontWeight: 600,
                      background:
                        STATUS_COLORS[
                          c.status.charAt(0) +
                            c.status.slice(1).toLowerCase()
                        ] || '#e2e8f0',
                      color: '#fff',
                    }}
                  >
                    {c.status}
                  </span>
                </td>
                <td style={tdStyle}>{c.targetAudienceSegment}</td>
                <td style={tdStyle}>
                  {c.displayPlacement || '-'}
                </td>
                <td style={tdStyle}>
                  {new Date(c.createdAt).toLocaleDateString()}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function FilterInput({
  label,
  value,
  onChange,
}: {
  label: string;
  value: string;
  onChange: (v: string) => void;
}) {
  return (
    <div>
      <label style={filterLabelStyle}>{label}</label>
      <input
        type="text"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={`Filter by ${label.toLowerCase()}`}
        style={filterInputStyle}
      />
    </div>
  );
}

function FilterSelect({
  label,
  value,
  onChange,
  options,
}: {
  label: string;
  value: string;
  onChange: (v: string) => void;
  options: { label: string; value: string }[];
}) {
  return (
    <div>
      <label style={filterLabelStyle}>{label}</label>
      <select
        value={value}
        onChange={(e) => onChange(e.target.value)}
        style={filterInputStyle}
      >
        {options.map((o) => (
          <option key={o.value} value={o.value}>
            {o.label}
          </option>
        ))}
      </select>
    </div>
  );
}

function KpiCard({
  label,
  value,
  color,
}: {
  label: string;
  value: number;
  color: string;
}) {
  return (
    <div
      style={{
        background: '#fff',
        borderRadius: '8px',
        padding: '20px',
        boxShadow: '0 1px 3px rgba(0,0,0,0.06)',
        border: '1px solid #e5e7eb',
        borderTop: `3px solid ${color}`,
      }}
    >
      <p
        style={{
          fontSize: '11px',
          color: '#6b7280',
          margin: '0 0 6px',
          textTransform: 'uppercase',
          fontWeight: 600,
          letterSpacing: '0.05em',
        }}
      >
        {label}
      </p>
      <p
        style={{
          fontSize: '28px',
          fontWeight: 700,
          color: '#1a2744',
          margin: 0,
        }}
      >
        {value.toLocaleString()}
      </p>
    </div>
  );
}

function ChartCard({
  title,
  children,
}: {
  title: string;
  children: React.ReactNode;
}) {
  return (
    <div
      style={{
        background: '#fff',
        borderRadius: '8px',
        padding: '24px',
        boxShadow: '0 1px 3px rgba(0,0,0,0.06)',
        border: '1px solid #e5e7eb',
      }}
    >
      <h3
        style={{
          fontSize: '15px',
          fontWeight: 600,
          color: '#1a2744',
          margin: '0 0 16px',
        }}
      >
        {title}
      </h3>
      {children}
    </div>
  );
}

function MetricBar({
  label,
  value,
  color,
}: {
  label: string;
  value: number;
  color: string;
}) {
  return (
    <div>
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          marginBottom: '6px',
        }}
      >
        <span
          style={{ fontSize: '13px', color: '#475569', fontWeight: 500 }}
        >
          {label}
        </span>
        <span
          style={{ fontSize: '13px', color: '#1a2744', fontWeight: 700 }}
        >
          {value.toFixed(1)}%
        </span>
      </div>
      <div
        style={{
          background: '#e2e8f0',
          borderRadius: '4px',
          height: '8px',
          overflow: 'hidden',
        }}
      >
        <div
          style={{
            width: `${Math.min(value, 100)}%`,
            background: color,
            height: '100%',
            borderRadius: '4px',
            transition: 'width 0.5s ease',
          }}
        />
      </div>
    </div>
  );
}

const filterLabelStyle: React.CSSProperties = {
  display: 'block',
  fontSize: '11px',
  color: '#6b7280',
  fontWeight: 600,
  textTransform: 'uppercase',
  letterSpacing: '0.05em',
  marginBottom: '4px',
};

const filterInputStyle: React.CSSProperties = {
  padding: '8px 10px',
  border: '1px solid #d1d5db',
  borderRadius: '6px',
  fontSize: '13px',
  minWidth: '140px',
};

const thStyle: React.CSSProperties = {
  textAlign: 'left',
  padding: '10px 14px',
  fontWeight: 600,
  color: '#6b7280',
  fontSize: '12px',
  textTransform: 'uppercase',
  letterSpacing: '0.05em',
};

const tdStyle: React.CSSProperties = {
  padding: '10px 14px',
  color: '#334155',
};
