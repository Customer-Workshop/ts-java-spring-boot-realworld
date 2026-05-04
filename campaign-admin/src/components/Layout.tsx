import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const navItems = [
  { path: '/dashboard', label: 'Dashboard' },
  { path: '/campaigns', label: 'Campaigns' },
];

export function Layout({ children }: { children: React.ReactNode }) {
  const location = useLocation();
  const { username, logout } = useAuth();

  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      <aside
        style={{
          width: '240px',
          background: '#1e293b',
          color: '#f8fafc',
          padding: '24px 0',
          display: 'flex',
          flexDirection: 'column',
        }}
      >
        <div
          style={{
            padding: '0 24px 24px',
            borderBottom: '1px solid #334155',
            marginBottom: '16px',
          }}
        >
          <h1 style={{ fontSize: '20px', fontWeight: 700, margin: 0 }}>
            Campaign Manager
          </h1>
          <p
            style={{
              fontSize: '12px',
              color: '#94a3b8',
              margin: '4px 0 0',
            }}
          >
            Marketing Admin Tool
          </p>
        </div>
        <nav>
          {navItems.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              style={{
                display: 'block',
                padding: '12px 24px',
                color:
                  location.pathname === item.path ? '#38bdf8' : '#cbd5e1',
                textDecoration: 'none',
                fontSize: '14px',
                fontWeight: location.pathname === item.path ? 600 : 400,
                background:
                  location.pathname === item.path
                    ? 'rgba(56,189,248,0.1)'
                    : 'transparent',
                borderLeft:
                  location.pathname === item.path
                    ? '3px solid #38bdf8'
                    : '3px solid transparent',
              }}
            >
              {item.label}
            </Link>
          ))}
        </nav>
        <div style={{ marginTop: 'auto', padding: '16px 24px' }}>
          <p
            style={{
              fontSize: '13px',
              color: '#94a3b8',
              margin: '0 0 8px',
            }}
          >
            {username}
          </p>
          <button
            onClick={logout}
            style={{
              background: 'none',
              border: '1px solid #475569',
              color: '#94a3b8',
              padding: '6px 16px',
              borderRadius: '6px',
              cursor: 'pointer',
              fontSize: '12px',
              width: '100%',
            }}
          >
            Sign Out
          </button>
        </div>
      </aside>

      <main
        style={{
          flex: 1,
          background: '#f1f5f9',
          padding: '32px',
          overflow: 'auto',
        }}
      >
        {children}
      </main>
    </div>
  );
}
