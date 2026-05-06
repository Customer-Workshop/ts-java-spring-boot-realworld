import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

interface NavItem {
  path: string;
  label: string;
}

interface NavSection {
  label: string;
  icon: string;
  collapsible?: boolean;
  items: NavItem[];
}

const navSections: NavSection[] = [
  {
    label: 'Marketing',
    icon: '\u{1F4E2}',
    collapsible: true,
    items: [
      { path: '/campaigns/new', label: 'Create campaign' },
      { path: '/campaigns', label: 'Campaigns' },
      { path: '/journey', label: 'Campaign journey' },
      { path: '/locations', label: 'Locations' },
      { path: '/dashboard', label: 'Analytics' },
      { path: '/user-segment', label: 'User segment' },
      { path: '/segment-criteria', label: 'Segment criteria' },
      { path: '/mcm-media', label: 'MCM media' },
    ],
  },
  {
    label: 'Intelligence',
    icon: '\u{1F50D}',
    collapsible: true,
    items: [
      { path: '/intelligence', label: 'Market Research' },
      { path: '/internal-preview', label: 'Internal preview' },
    ],
  },
  {
    label: 'Administration',
    icon: '\u{2699}\u{FE0F}',
    collapsible: true,
    items: [],
  },
  {
    label: 'Configuration',
    icon: '\u{1F527}',
    collapsible: true,
    items: [],
  },
  {
    label: 'Reports',
    icon: '\u{1F4CA}',
    collapsible: true,
    items: [
      { path: '/calendar', label: 'Calendar view' },
    ],
  },
];

const breadcrumbMap: Record<string, string> = {
  '/dashboard': 'Analytics',
  '/campaigns/new': 'Create campaign',
  '/campaigns': 'Campaigns',
  '/calendar': 'Calendar',
  '/journey': 'Campaign journey',
  '/locations': 'Locations',
  '/user-segment': 'User segment',
  '/segment-criteria': 'Segment criteria',
  '/mcm-media': 'MCM media',
  '/intelligence': 'Market Research',
  '/internal-preview': 'Internal preview',
};

export function Layout({ children }: { children: React.ReactNode }) {
  const location = useLocation();
  const { username, logout } = useAuth();
  const [expandedSections, setExpandedSections] = useState<Record<string, boolean>>({
    Marketing: true,
    Intelligence: true,
    Administration: false,
    Configuration: false,
    Reports: false,
  });

  const toggleSection = (label: string) => {
    setExpandedSections(prev => ({ ...prev, [label]: !prev[label] }));
  };

  const getBreadcrumb = () => {
    for (const [path, label] of Object.entries(breadcrumbMap)) {
      if (location.pathname === path) return label;
    }
    if (location.pathname.includes('/analytics')) return 'Analytics';
    if (location.pathname.includes('/edit')) return 'Edit campaign';
    if (location.pathname.startsWith('/campaigns/')) return 'Campaign detail';
    return 'Campaigns';
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      {/* Top Header Bar - Fiserv style */}
      <header
        style={{
          background: 'linear-gradient(90deg, #1a2744 0%, #243b6e 100%)',
          color: '#fff',
          padding: '0 24px',
          height: '56px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          flexShrink: 0,
          zIndex: 10,
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
          <img
            src="/fiserv-logo-white.svg"
            alt="Fiserv"
            style={{ height: '28px' }}
          />
          <div
            style={{
              width: '1px',
              height: '28px',
              background: 'rgba(255,255,255,0.3)',
            }}
          />
          <span
            style={{
              fontSize: '16px',
              fontWeight: 600,
              letterSpacing: '-0.02em',
            }}
          >
            Admin Tool
          </span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          <span style={{ fontSize: '13px', color: '#cbd5e1' }}>{username}</span>
          <button
            onClick={logout}
            style={{
              background: 'rgba(255,255,255,0.15)',
              border: '1px solid rgba(255,255,255,0.3)',
              color: '#fff',
              padding: '5px 14px',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '12px',
              fontWeight: 500,
            }}
          >
            Sign Out
          </button>
        </div>
      </header>

      <div style={{ display: 'flex', flex: 1 }}>
        {/* Left Sidebar */}
        <aside
          style={{
            width: '260px',
            background: '#fff',
            borderRight: '1px solid #e5e7eb',
            display: 'flex',
            flexDirection: 'column',
            flexShrink: 0,
            overflowY: 'auto',
          }}
        >
          {/* Entity selector */}
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '10px',
              padding: '14px 16px',
              borderBottom: '1px solid #e5e7eb',
              cursor: 'pointer',
            }}
          >
            <div
              style={{
                width: '36px',
                height: '36px',
                borderRadius: '8px',
                background: '#1d4ed8',
                color: '#fff',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '16px',
                fontWeight: 600,
              }}
            >
              🏦
            </div>
            <div style={{ flex: 1 }}>
              <div
                style={{
                  fontSize: '13px',
                  fontWeight: 600,
                  color: '#1f2937',
                }}
              >
                First Financial Bank
              </div>
              <div style={{ fontSize: '11px', color: '#9ca3af' }}>
                Entity Info
              </div>
            </div>
            <span style={{ fontSize: '12px', color: '#9ca3af' }}>⬦</span>
          </div>

          {/* Search */}
          <div style={{ padding: '12px 16px' }}>
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                padding: '8px 12px',
                background: '#f9fafb',
                border: '1px solid #e5e7eb',
                borderRadius: '6px',
                fontSize: '13px',
                color: '#9ca3af',
              }}
            >
              {'\u{1F50D}'} Search navigation
            </div>
          </div>

          {/* Navigation */}
          <nav style={{ flex: 1 }}>
            {navSections.map((section) => {
              const isExpanded = expandedSections[section.label] ?? false;
              return (
                <div key={section.label}>
                  <div
                    onClick={() =>
                      section.collapsible && toggleSection(section.label)
                    }
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '10px',
                      padding: '10px 20px',
                      fontSize: '14px',
                      fontWeight: 600,
                      color: '#1f2937',
                      cursor: section.collapsible ? 'pointer' : 'default',
                      userSelect: 'none',
                    }}
                  >
                    <span style={{ fontSize: '16px' }}>{section.icon}</span>
                    <span style={{ flex: 1 }}>{section.label}</span>
                    {section.collapsible && (
                      <span
                        style={{
                          fontSize: '12px',
                          color: '#9ca3af',
                          transition: 'transform 0.2s',
                          transform: isExpanded
                            ? 'rotate(180deg)'
                            : 'rotate(0)',
                        }}
                      >
                        ▾
                      </span>
                    )}
                  </div>
                  {isExpanded &&
                    section.items.map((item) => {
                      const isActive =
                        item.path === '/campaigns'
                          ? location.pathname === '/campaigns'
                          : location.pathname.startsWith(item.path);
                      return (
                        <Link
                          key={item.path}
                          to={item.path}
                          style={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: '10px',
                            padding: '9px 20px 9px 44px',
                            color: isActive ? '#1d4ed8' : '#4b5563',
                            textDecoration: 'none',
                            fontSize: '13px',
                            fontWeight: isActive ? 600 : 400,
                            background: isActive ? '#eff6ff' : 'transparent',
                            borderRight: isActive
                              ? '3px solid #1d4ed8'
                              : '3px solid transparent',
                            transition: 'all 0.15s ease',
                          }}
                        >
                          {item.label}
                        </Link>
                      );
                    })}
                </div>
              );
            })}
          </nav>

          {/* Bottom entity info */}
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '10px',
              padding: '14px 16px',
              borderTop: '1px solid #e5e7eb',
              marginTop: 'auto',
            }}
          >
            <div
              style={{
                width: '32px',
                height: '32px',
                borderRadius: '50%',
                background: '#1d4ed8',
                color: '#fff',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '14px',
              }}
            >
              👤
            </div>
            <div style={{ flex: 1 }}>
              <div
                style={{
                  fontSize: '12px',
                  fontWeight: 600,
                  color: '#1f2937',
                }}
              >
                Entity Name
              </div>
              <div style={{ fontSize: '11px', color: '#9ca3af' }}>
                Entity Info
              </div>
            </div>
            <span style={{ fontSize: '12px', color: '#9ca3af' }}>⬦</span>
          </div>
        </aside>

        {/* Main Content */}
        <main
          style={{
            flex: 1,
            background: '#f3f4f6',
            padding: '24px 32px',
            overflow: 'auto',
          }}
        >
          {/* Breadcrumb */}
          <div
            style={{
              fontSize: '12px',
              color: '#6b7280',
              marginBottom: '16px',
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
            }}
          >
            {'\u{1F3E0}'}{' '}
            <span>Marketing &gt; {getBreadcrumb()}</span>
          </div>
          {children}
        </main>
      </div>
    </div>
  );
}
