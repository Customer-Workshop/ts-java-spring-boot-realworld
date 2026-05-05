import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { fetchCampaignAnalytics, fetchCampaign } from '../api/campaigns';
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
import type { Campaign, CampaignAnalytics } from '../types/campaign';

const COLORS = ['#22c55e', '#ef4444', '#f59e0b', '#6366f1'];

export function CampaignAnalyticsPage() {
  const { id } = useParams<{ id: string }>();
  const [analytics, setAnalytics] = useState<CampaignAnalytics | null>(
    null
  );
  const [campaign, setCampaign] = useState<Campaign | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (id) {
      Promise.all([fetchCampaign(id), fetchCampaignAnalytics(id)])
        .then(([c, a]) => {
          setCampaign(c);
          setAnalytics(a);
        })
        .finally(() => setLoading(false));
    }
  }, [id]);

  if (loading)
    return <p style={{ color: '#64748b' }}>Loading analytics...</p>;
  if (!analytics || !campaign)
    return (
      <p style={{ color: '#dc2626' }}>Failed to load analytics.</p>
    );

  const decisionData = [
    { name: 'Accepted', value: analytics.acceptedCount },
    { name: 'Declined', value: analytics.declinedCount },
    {
      name: 'Clicked (Unfinished)',
      value: analytics.clickedUnfinishedCount,
    },
    { name: 'Remind Later', value: analytics.remindLaterCount },
  ];

  const buildBarData = (
    commonality: Record<string, Record<string, number>>
  ) => {
    return Object.entries(commonality).map(([key, decisions]) => ({
      name: key,
      ACCEPTED: decisions['ACCEPTED'] || 0,
      DECLINED: decisions['DECLINED'] || 0,
      CLICKED_UNFINISHED: decisions['CLICKED_UNFINISHED'] || 0,
      REMIND_LATER: decisions['REMIND_LATER'] || 0,
    }));
  };

  return (
    <div>
      <Link
        to={`/campaigns/${id}`}
        style={{
          color: '#6b7280',
          textDecoration: 'none',
          fontSize: '13px',
        }}
      >
        &larr; Back to Campaign
      </Link>
      <h2
        style={{
          fontSize: '22px',
          fontWeight: 600,
          margin: '8px 0 24px',
          color: '#1a2744',
        }}
      >
        Analytics: {campaign.name}
      </h2>

      {/* KPI Cards */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(5, 1fr)',
          gap: '16px',
          marginBottom: '32px',
        }}
      >
        <KpiCard
          label="Total Targeted"
          value={analytics.totalTargetedPopulation}
          color="#1d4ed8"
        />
        <KpiCard
          label="Accepted / Fulfilled"
          value={analytics.acceptedCount}
          color="#22c55e"
        />
        <KpiCard
          label="Declined"
          value={analytics.declinedCount}
          color="#ef4444"
        />
        <KpiCard
          label="Clicked (Unfinished)"
          value={analytics.clickedUnfinishedCount}
          color="#f59e0b"
        />
        <KpiCard
          label="Remind Later"
          value={analytics.remindLaterCount}
          color="#6366f1"
        />
      </div>

      {/* Charts Row */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: '1fr 1fr',
          gap: '24px',
          marginBottom: '32px',
        }}
      >
        <ChartCard title="Decision Distribution">
          <ResponsiveContainer width="100%" height={280}>
            <PieChart>
              <Pie
                data={decisionData}
                cx="50%"
                cy="50%"
                outerRadius={100}
                fill="#8884d8"
                dataKey="value"
                label={({ name, percent }) =>
                  `${name}: ${(percent * 100).toFixed(0)}%`
                }
              >
                {decisionData.map((_, index) => (
                  <Cell
                    key={`cell-${index}`}
                    fill={COLORS[index % COLORS.length]}
                  />
                ))}
              </Pie>
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </ChartCard>

        <ChartCard title="Commonality by Age Group">
          <ResponsiveContainer width="100%" height={280}>
            <BarChart
              data={buildBarData(analytics.commonalityByAgeGroup)}
            >
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" fontSize={12} />
              <YAxis fontSize={12} />
              <Tooltip />
              <Legend />
              <Bar
                dataKey="ACCEPTED"
                fill="#22c55e"
                name="Accepted"
              />
              <Bar
                dataKey="DECLINED"
                fill="#ef4444"
                name="Declined"
              />
              <Bar
                dataKey="CLICKED_UNFINISHED"
                fill="#f59e0b"
                name="Unfinished"
              />
              <Bar
                dataKey="REMIND_LATER"
                fill="#6366f1"
                name="Remind Later"
              />
            </BarChart>
          </ResponsiveContainer>
        </ChartCard>
      </div>

      <div
        style={{
          display: 'grid',
          gridTemplateColumns: '1fr 1fr',
          gap: '24px',
        }}
      >
        <ChartCard title="Commonality by Region">
          <ResponsiveContainer width="100%" height={280}>
            <BarChart
              data={buildBarData(analytics.commonalityByRegion)}
            >
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" fontSize={12} />
              <YAxis fontSize={12} />
              <Tooltip />
              <Legend />
              <Bar
                dataKey="ACCEPTED"
                fill="#22c55e"
                name="Accepted"
              />
              <Bar
                dataKey="DECLINED"
                fill="#ef4444"
                name="Declined"
              />
              <Bar
                dataKey="CLICKED_UNFINISHED"
                fill="#f59e0b"
                name="Unfinished"
              />
              <Bar
                dataKey="REMIND_LATER"
                fill="#6366f1"
                name="Remind Later"
              />
            </BarChart>
          </ResponsiveContainer>
        </ChartCard>

        <ChartCard title="Commonality by Segment">
          <ResponsiveContainer width="100%" height={280}>
            <BarChart
              data={buildBarData(analytics.commonalityBySegment)}
            >
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" fontSize={12} />
              <YAxis fontSize={12} />
              <Tooltip />
              <Legend />
              <Bar
                dataKey="ACCEPTED"
                fill="#22c55e"
                name="Accepted"
              />
              <Bar
                dataKey="DECLINED"
                fill="#ef4444"
                name="Declined"
              />
              <Bar
                dataKey="CLICKED_UNFINISHED"
                fill="#f59e0b"
                name="Unfinished"
              />
              <Bar
                dataKey="REMIND_LATER"
                fill="#6366f1"
                name="Remind Later"
              />
            </BarChart>
          </ResponsiveContainer>
        </ChartCard>
      </div>
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
        padding: '24px',
        boxShadow: '0 1px 3px rgba(0,0,0,0.06)',
        border: '1px solid #e5e7eb',
        borderTop: `3px solid ${color}`,
      }}
    >
      <p
        style={{
          fontSize: '12px',
          color: '#6b7280',
          margin: '0 0 8px',
          textTransform: 'uppercase',
          fontWeight: 600,
          letterSpacing: '0.05em',
        }}
      >
        {label}
      </p>
      <p
        style={{
          fontSize: '32px',
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
          fontSize: '16px',
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
