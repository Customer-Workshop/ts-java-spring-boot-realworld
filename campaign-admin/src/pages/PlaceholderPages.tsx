import React from 'react';

function PlaceholderPage({ title, icon, description }: { title: string; icon: string; description: string }) {
  return (
    <div style={{ maxWidth: '720px' }}>
      <h2 style={{ fontSize: '22px', fontWeight: 600, marginBottom: '8px', color: '#1a2744' }}>{title}</h2>
      <div style={{
        background: '#fff', padding: '48px 32px', borderRadius: '8px',
        boxShadow: '0 1px 3px rgba(0,0,0,0.06)', border: '1px solid #e5e7eb',
        textAlign: 'center',
      }}>
        <p style={{ fontSize: '48px', margin: '0 0 16px' }}>{icon}</p>
        <p style={{ fontSize: '16px', color: '#6b7280', margin: 0 }}>{description}</p>
      </div>
    </div>
  );
}

export function CampaignJourneyPage() {
  return <PlaceholderPage title="Campaign Journey" icon="🗺️" description="Define multi-step campaign journeys with branching logic, triggers, and automated follow-ups." />;
}

export function LocationsPage() {
  return <PlaceholderPage title="Locations" icon="📍" description="Manage campaign placement locations across web and mobile platforms." />;
}

export function UserSegmentPage() {
  return <PlaceholderPage title="User Segment" icon="👥" description="Create and manage user segments based on customer attributes, behaviors, and demographics." />;
}

export function SegmentCriteriaPage() {
  return <PlaceholderPage title="Segment Criteria" icon="🎯" description="Define and configure criteria rules for audience segmentation." />;
}

export function McmMediaPage() {
  return <PlaceholderPage title="MCM Media" icon="🖼️" description="Manage media assets (images, HTML templates, videos) for campaign content delivery." />;
}

export function InternalPreviewPage() {
  return <PlaceholderPage title="Internal Preview" icon="👁️" description="Preview campaign messages across channels before publishing to end users." />;
}
