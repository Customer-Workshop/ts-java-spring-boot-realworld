import { useEffect, useState, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { fetchCampaigns } from '../api/campaigns';
import { StatusBadge } from '../components/StatusBadge';
import type { Campaign } from '../types/campaign';

const channelLabels: Record<string, string> = {
  IN_APP: 'In-App',
  EMAIL: 'Email',
  SMS: 'SMS',
  PUSH: 'Push',
  SOCIAL: 'Social',
  ADS: 'Ads',
};

const statusColors: Record<string, string> = {
  DRAFT: '#94a3b8',
  ACTIVE: '#16a34a',
  PAUSED: '#d97706',
  ENDED: '#6b7280',
};

export function CampaignCalendarPage() {
  const [campaigns, setCampaigns] = useState<Campaign[]>([]);
  const [loading, setLoading] = useState(true);
  const [currentMonth, setCurrentMonth] = useState(() => {
    const now = new Date();
    return new Date(now.getFullYear(), now.getMonth(), 1);
  });
  const navigate = useNavigate();

  const loadCampaigns = useCallback(async () => {
    setLoading(true);
    try {
      const data = await fetchCampaigns();
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
  }, [navigate]);

  useEffect(() => {
    loadCampaigns();
  }, [loadCampaigns]);

  const year = currentMonth.getFullYear();
  const month = currentMonth.getMonth();
  const firstDay = new Date(year, month, 1).getDay();
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const monthName = currentMonth.toLocaleString('default', {
    month: 'long',
    year: 'numeric',
  });

  const prevMonth = () =>
    setCurrentMonth(new Date(year, month - 1, 1));
  const nextMonth = () =>
    setCurrentMonth(new Date(year, month + 1, 1));

  const getCampaignsForDay = (day: number): Campaign[] => {
    const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
    return campaigns.filter((c) => {
      const start = c.startDate ? c.startDate.slice(0, 10) : null;
      const end = c.endDate ? c.endDate.slice(0, 10) : null;
      if (!start) return false;
      if (start <= dateStr && (!end || end >= dateStr)) return true;
      return false;
    });
  };

  const days: (number | null)[] = [];
  for (let i = 0; i < firstDay; i++) days.push(null);
  for (let d = 1; d <= daysInMonth; d++) days.push(d);

  if (loading)
    return <p style={{ color: '#64748b' }}>Loading calendar...</p>;

  return (
    <div>
      <h2
        style={{
          fontSize: '22px',
          fontWeight: 600,
          marginBottom: '24px',
          color: '#1a2744',
        }}
      >
        Campaign Calendar
      </h2>

      {/* Month Navigation */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          marginBottom: '20px',
          background: '#fff',
          padding: '12px 20px',
          borderRadius: '8px',
          border: '1px solid #e5e7eb',
        }}
      >
        <button onClick={prevMonth} style={navBtn}>
          &larr; Previous
        </button>
        <h3
          style={{
            fontSize: '18px',
            fontWeight: 600,
            color: '#1a2744',
            margin: 0,
          }}
        >
          {monthName}
        </h3>
        <button onClick={nextMonth} style={navBtn}>
          Next &rarr;
        </button>
      </div>

      {/* Calendar Grid */}
      <div
        style={{
          background: '#fff',
          borderRadius: '8px',
          border: '1px solid #e5e7eb',
          overflow: 'hidden',
        }}
      >
        {/* Day Headers */}
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(7, 1fr)',
            background: '#f8fafc',
            borderBottom: '1px solid #e5e7eb',
          }}
        >
          {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map(
            (day) => (
              <div
                key={day}
                style={{
                  padding: '10px',
                  textAlign: 'center',
                  fontSize: '12px',
                  fontWeight: 600,
                  color: '#6b7280',
                  textTransform: 'uppercase',
                }}
              >
                {day}
              </div>
            )
          )}
        </div>

        {/* Calendar Cells */}
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(7, 1fr)',
          }}
        >
          {days.map((day, idx) => {
            const dayCampaigns = day ? getCampaignsForDay(day) : [];
            const isToday =
              day &&
              new Date().getDate() === day &&
              new Date().getMonth() === month &&
              new Date().getFullYear() === year;
            return (
              <div
                key={idx}
                style={{
                  minHeight: '100px',
                  padding: '4px',
                  borderRight:
                    (idx + 1) % 7 !== 0
                      ? '1px solid #f1f5f9'
                      : 'none',
                  borderBottom: '1px solid #f1f5f9',
                  background: isToday ? '#eff6ff' : 'transparent',
                }}
              >
                {day && (
                  <>
                    <div
                      style={{
                        fontSize: '12px',
                        fontWeight: isToday ? 700 : 400,
                        color: isToday ? '#1d4ed8' : '#6b7280',
                        padding: '2px 4px',
                      }}
                    >
                      {day}
                    </div>
                    <div
                      style={{
                        display: 'flex',
                        flexDirection: 'column',
                        gap: '2px',
                      }}
                    >
                      {dayCampaigns.slice(0, 3).map((c) => (
                        <Link
                          key={c.id}
                          to={`/campaigns/${c.id}`}
                          title={`${c.name} (${c.channel || 'IN_APP'}) - Priority: ${c.priority}`}
                          style={{
                            display: 'block',
                            padding: '2px 4px',
                            fontSize: '10px',
                            fontWeight: 500,
                            color: '#fff',
                            background:
                              statusColors[c.status] || '#6b7280',
                            borderRadius: '3px',
                            textDecoration: 'none',
                            overflow: 'hidden',
                            textOverflow: 'ellipsis',
                            whiteSpace: 'nowrap',
                          }}
                        >
                          {c.name}
                        </Link>
                      ))}
                      {dayCampaigns.length > 3 && (
                        <span
                          style={{
                            fontSize: '10px',
                            color: '#6b7280',
                            padding: '0 4px',
                          }}
                        >
                          +{dayCampaigns.length - 3} more
                        </span>
                      )}
                    </div>
                  </>
                )}
              </div>
            );
          })}
        </div>
      </div>

      {/* Legend */}
      <div
        style={{
          display: 'flex',
          gap: '16px',
          marginTop: '16px',
          padding: '12px 20px',
          background: '#fff',
          borderRadius: '8px',
          border: '1px solid #e5e7eb',
          flexWrap: 'wrap',
        }}
      >
        <span
          style={{ fontSize: '12px', fontWeight: 600, color: '#6b7280' }}
        >
          Legend:
        </span>
        {Object.entries(statusColors).map(([status, color]) => (
          <div
            key={status}
            style={{ display: 'flex', alignItems: 'center', gap: '4px' }}
          >
            <div
              style={{
                width: '12px',
                height: '12px',
                borderRadius: '2px',
                background: color,
              }}
            />
            <span style={{ fontSize: '12px', color: '#6b7280' }}>
              {status}
            </span>
          </div>
        ))}
      </div>

      {/* Upcoming Campaigns List */}
      <div
        style={{
          marginTop: '24px',
          background: '#fff',
          borderRadius: '8px',
          padding: '24px',
          border: '1px solid #e5e7eb',
        }}
      >
        <h3
          style={{
            fontSize: '16px',
            fontWeight: 600,
            marginBottom: '16px',
            color: '#1a2744',
          }}
        >
          Campaigns This Month
        </h3>
        {campaigns
          .filter((c) => {
            const start = c.startDate ? c.startDate.slice(0, 10) : null;
            const end = c.endDate ? c.endDate.slice(0, 10) : null;
            const monthStart = `${year}-${String(month + 1).padStart(2, '0')}-01`;
            const monthEnd = `${year}-${String(month + 1).padStart(2, '0')}-${String(daysInMonth).padStart(2, '0')}`;
            if (!start) return false;
            return start <= monthEnd && (!end || end >= monthStart);
          })
          .sort((a, b) => b.priority - a.priority)
          .map((c) => (
            <div
              key={c.id}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '12px',
                padding: '10px 0',
                borderBottom: '1px solid #f1f5f9',
              }}
            >
              <div
                style={{
                  width: '4px',
                  height: '32px',
                  borderRadius: '2px',
                  background: statusColors[c.status] || '#6b7280',
                }}
              />
              <div style={{ flex: 1 }}>
                <Link
                  to={`/campaigns/${c.id}`}
                  style={{
                    color: '#1d4ed8',
                    textDecoration: 'none',
                    fontSize: '14px',
                    fontWeight: 500,
                  }}
                >
                  {c.name}
                </Link>
                <div
                  style={{
                    fontSize: '12px',
                    color: '#6b7280',
                    marginTop: '2px',
                  }}
                >
                  {c.startDate
                    ? new Date(c.startDate).toLocaleDateString()
                    : '-'}{' '}
                  -{' '}
                  {c.endDate
                    ? new Date(c.endDate).toLocaleDateString()
                    : 'Ongoing'}
                </div>
              </div>
              <span
                style={{
                  background: '#f1f5f9',
                  padding: '3px 8px',
                  borderRadius: '4px',
                  fontSize: '11px',
                  fontWeight: 500,
                }}
              >
                {channelLabels[c.channel || 'IN_APP'] || c.channel || 'IN_APP'}
              </span>
              <StatusBadge status={c.status} />
              <span
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  width: '24px',
                  height: '24px',
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
                  fontSize: '11px',
                  fontWeight: 700,
                }}
                title={`Priority: ${c.priority}`}
              >
                {c.priority}
              </span>
            </div>
          ))}
      </div>
    </div>
  );
}

const navBtn: React.CSSProperties = {
  padding: '8px 16px',
  border: '1px solid #d1d5db',
  borderRadius: '6px',
  background: '#fff',
  cursor: 'pointer',
  fontSize: '13px',
  fontWeight: 500,
  color: '#374151',
};
