import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const navSections = [
  {
    label: 'Marketing',
    icon: '\u{1F4E2}',
    items: [
      { path: '/dashboard', label: 'Dashboard', icon: '\u{1F4CA}' },
      { path: '/campaigns/new', label: 'Create campaign', icon: '\u2795' },
      { path: '/campaigns', label: 'Campaigns', icon: '\u{1F4CB}' },
      { path: '/calendar', label: 'Calendar', icon: '\u{1F4C5}' },
    ],
  },
];

export function Layout({ children }: { children: React.ReactNode }) {
  const location = useLocation();
  const { username, logout } = useAuth();

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
            Campaign Admin Tool
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
        {/* Left Sidebar - White with navigation */}
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
          {/* Search */}
          <div style={{ padding: '16px' }}>
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
            {navSections.map((section) => (
              <div key={section.label}>
                <div
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '10px',
                    padding: '10px 20px',
                    fontSize: '14px',
                    fontWeight: 600,
                    color: '#1f2937',
                    cursor: 'default',
                  }}
                >
                  <span style={{ fontSize: '16px' }}>{section.icon}</span>
                  {section.label}
                </div>
                {section.items.map((item) => {
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
                      <span style={{ fontSize: '14px' }}>{item.icon}</span>
                      {item.label}
                    </Link>
                  );
                })}
              </div>
            ))}
          </nav>
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
            <span>
              Marketing &gt;{' '}
              {location.pathname === '/dashboard'
                ? 'Dashboard'
                : location.pathname === '/campaigns/new'
                  ? 'Create campaign'
                  : location.pathname.includes('/analytics')
                    ? 'Analytics'
                    : location.pathname.includes('/edit')
                      ? 'Edit campaign'
                      : location.pathname.startsWith('/campaigns/')
                        ? 'Campaign detail'
                        : 'Campaigns'}
            </span>
          </div>
          {children}
        </main>
      </div>
    </div>
  );
}
