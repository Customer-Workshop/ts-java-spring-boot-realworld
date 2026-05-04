import type { CampaignStatus } from '../types/campaign';

const statusStyles: Record<CampaignStatus, { bg: string; text: string }> = {
  DRAFT: { bg: '#e2e8f0', text: '#475569' },
  ACTIVE: { bg: '#dcfce7', text: '#166534' },
  PAUSED: { bg: '#fef9c3', text: '#854d0e' },
  ENDED: { bg: '#fee2e2', text: '#991b1b' },
};

export function StatusBadge({ status }: { status: CampaignStatus }) {
  const style = statusStyles[status];
  return (
    <span
      style={{
        display: 'inline-block',
        padding: '4px 12px',
        borderRadius: '9999px',
        fontSize: '12px',
        fontWeight: 600,
        backgroundColor: style.bg,
        color: style.text,
      }}
    >
      {status}
    </span>
  );
}
