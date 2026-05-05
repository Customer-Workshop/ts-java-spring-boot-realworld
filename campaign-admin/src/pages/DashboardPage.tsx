import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { fetchDashboard, fetchCampaigns } from '../api/campaigns';
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

const DECISION_COLORS = ['#22c55e', '#ef4444', '#f59e0b'];

export function DashboardPage() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [campaigns, setCampaigns] = useState<Campaign[]>([]);
  const [loading, setLoading] = useState(true);
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

  if (loading) return <p style={{ color: '#64748b' }}>Loading dashboard...</p>;
  if (!summary) return <p style={{ color: '#dc2626' }}>Failed to load dashboard.</p>;

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
  ];

  return (
    <div>
      <h2 style={{ fontSize: '24px', fontWeight: 700, margin: '0 0 24px' }}>
        Campaign &amp; Promotion Dashboard
      </h2>

      {/* KPI Row */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(5, 1fr)',
          gap: '16px',
          marginBottom: '32px',
        }}
      >
        <KpiCard label="Total Campaigns" value={summary.totalCampaigns} color="#2563eb" />
        <KpiCard label="Total Targeted" value={summary.totalTargetedPopulation} color="#7c3aed" />
        <KpiCard label="Accepted" value={summary.totalAccepted} color="#22c55e" />
        <KpiCard label="Declined" value={summary.totalDeclined} color="#ef4444" />
        <KpiCard label="Unfinished" value={summary.totalClickedUnfinished} color="#f59e0b" />
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
                  <Cell key={`cell-${index}`} fill={DECISION_COLORS[index]} />
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
          borderRadius: '12px',
          padding: '32px',
          boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
          marginBottom: '32px',
        }}
      >
        <h3 style={{ fontSize: '16px', fontWeight: 600, margin: '0 0 20px', color: '#1e293b' }}>
          Performance Metrics
        </h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '24px' }}>
          <MetricBar
            label="Acceptance Rate"
            value={
              summary.totalTargetedPopulation > 0
                ? (summary.totalAccepted / summary.totalTargetedPopulation) * 100
                : 0
            }
            color="#22c55e"
          />
          <MetricBar
            label="Decline Rate"
            value={
              summary.totalTargetedPopulation > 0
                ? (summary.totalDeclined / summary.totalTargetedPopulation) * 100
                : 0
            }
            color="#ef4444"
          />
          <MetricBar
            label="Drop-off Rate"
            value={
              summary.totalTargetedPopulation > 0
                ? (summary.totalClickedUnfinished / summary.totalTargetedPopulation) * 100
                : 0
            }
            color="#f59e0b"
          />
        </div>
      </div>

      {/* Recent Campaigns Table */}
      <div
        style={{
          background: '#fff',
          borderRadius: '12px',
          padding: '24px',
          boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
        }}
      >
        <h3 style={{ fontSize: '16px', fontWeight: 600, margin: '0 0 16px', color: '#1e293b' }}>
          Recent Campaigns
        </h3>
        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
          <thead>
            <tr style={{ background: '#f8fafc' }}>
              <th style={thStyle}>Name</th>
              <th style={thStyle}>Status</th>
              <th style={thStyle}>Audience</th>
              <th style={thStyle}>Created</th>
            </tr>
          </thead>
          <tbody>
            {campaigns.slice(0, 5).map((c) => (
              <tr key={c.id} style={{ borderBottom: '1px solid #f1f5f9' }}>
                <td style={tdStyle}>
                  <Link to={`/campaigns/${c.id}`} style={{ color: '#2563eb', textDecoration: 'none' }}>
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
                      background: STATUS_COLORS[c.status.charAt(0) + c.status.slice(1).toLowerCase()] || '#e2e8f0',
                      color: '#fff',
                    }}
                  >
                    {c.status}
                  </span>
                </td>
                <td style={tdStyle}>{c.targetAudienceSegment}</td>
                <td style={tdStyle}>{new Date(c.createdAt).toLocaleDateString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function KpiCard({ label, value, color }: { label: string; value: number; color: string }) {
  return (
    <div
      style={{
        background: '#fff',
        borderRadius: '12px',
        padding: '20px',
        boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
        borderTop: `4px solid ${color}`,
      }}
    >
      <p style={{ fontSize: '11px', color: '#64748b', margin: '0 0 6px', textTransform: 'uppercase', fontWeight: 600, letterSpacing: '0.05em' }}>
        {label}
      </p>
      <p style={{ fontSize: '28px', fontWeight: 700, color: '#1e293b', margin: 0 }}>
        {value.toLocaleString()}
      </p>
    </div>
  );
}

function ChartCard({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div style={{ background: '#fff', borderRadius: '12px', padding: '24px', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
      <h3 style={{ fontSize: '16px', fontWeight: 600, color: '#1e293b', margin: '0 0 16px' }}>{title}</h3>
      {children}
    </div>
  );
}

function MetricBar({ label, value, color }: { label: string; value: number; color: string }) {
  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '6px' }}>
        <span style={{ fontSize: '13px', color: '#475569', fontWeight: 500 }}>{label}</span>
        <span style={{ fontSize: '13px', color: '#1e293b', fontWeight: 700 }}>{value.toFixed(1)}%</span>
      </div>
      <div style={{ background: '#e2e8f0', borderRadius: '4px', height: '8px', overflow: 'hidden' }}>
        <div style={{ width: `${Math.min(value, 100)}%`, background: color, height: '100%', borderRadius: '4px', transition: 'width 0.5s ease' }} />
      </div>
    </div>
  );
}

const thStyle: React.CSSProperties = {
  textAlign: 'left',
  padding: '10px 14px',
  fontWeight: 600,
  color: '#64748b',
  fontSize: '12px',
  textTransform: 'uppercase',
  letterSpacing: '0.05em',
};

const tdStyle: React.CSSProperties = {
  padding: '10px 14px',
  color: '#334155',
};
