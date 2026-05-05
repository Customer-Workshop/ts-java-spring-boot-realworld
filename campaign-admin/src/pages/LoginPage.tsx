import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(email, password);
      navigate('/dashboard');
    } catch {
      setError('Invalid credentials. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
      }}
    >
      {/* Header */}
      <div
        style={{
          background: 'linear-gradient(90deg, #1a2744 0%, #243b6e 100%)',
          padding: '0 32px',
          height: '56px',
          display: 'flex',
          alignItems: 'center',
          gap: '12px',
        }}
      >
        <div
          style={{
            width: '32px',
            height: '32px',
            background: '#fff',
            borderRadius: '6px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '16px',
          }}
        >
          {'\u{1F3E6}'}
        </div>
        <span
          style={{
            color: '#fff',
            fontSize: '18px',
            fontWeight: 700,
            fontStyle: 'italic',
          }}
        >
          Campaign
        </span>
        <span
          style={{
            color: '#fff',
            fontSize: '18px',
            fontWeight: 400,
          }}
        >
          Admin Tool
        </span>
      </div>

      {/* Content */}
      <div
        style={{
          flex: 1,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          background: '#f3f4f6',
        }}
      >
        <div
          style={{
            background: '#fff',
            borderRadius: '8px',
            padding: '40px 48px',
            width: '420px',
            boxShadow: '0 4px 24px rgba(0,0,0,0.08)',
            border: '1px solid #e5e7eb',
          }}
        >
          <h1
            style={{
              fontSize: '22px',
              fontWeight: 600,
              color: '#1a2744',
              margin: '0 0 6px',
              textAlign: 'center',
            }}
          >
            Welcome Back
          </h1>
          <p
            style={{
              fontSize: '13px',
              color: '#6b7280',
              textAlign: 'center',
              margin: '0 0 28px',
            }}
          >
            Sign in to Campaign Admin Tool
          </p>
          {error && (
            <div
              style={{
                background: '#fef2f2',
                color: '#b91c1c',
                padding: '10px 14px',
                borderRadius: '6px',
                fontSize: '13px',
                marginBottom: '16px',
                border: '1px solid #fecaca',
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
              }}
            >
              <span style={{ fontSize: '14px' }}>{'\u26A0\uFE0F'}</span>
              {error}
              <button
                onClick={() => setError('')}
                style={{
                  marginLeft: 'auto',
                  background: 'none',
                  border: 'none',
                  color: '#b91c1c',
                  cursor: 'pointer',
                  fontSize: '16px',
                  padding: '0 2px',
                  lineHeight: 1,
                }}
                aria-label="Dismiss error"
              >
                {'\u00D7'}
              </button>
            </div>
          )}
          <form onSubmit={handleSubmit}>
            <div style={{ marginBottom: '16px' }}>
              <label
                style={{
                  display: 'block',
                  fontSize: '13px',
                  fontWeight: 500,
                  color: '#374151',
                  marginBottom: '6px',
                }}
              >
                Email
              </label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  border: '1px solid #d1d5db',
                  borderRadius: '6px',
                  fontSize: '14px',
                  boxSizing: 'border-box',
                  transition: 'border-color 0.15s',
                }}
                placeholder="john@example.com"
              />
            </div>
            <div style={{ marginBottom: '24px' }}>
              <label
                style={{
                  display: 'block',
                  fontSize: '13px',
                  fontWeight: 500,
                  color: '#374151',
                  marginBottom: '6px',
                }}
              >
                Password
              </label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  border: '1px solid #d1d5db',
                  borderRadius: '6px',
                  fontSize: '14px',
                  boxSizing: 'border-box',
                  transition: 'border-color 0.15s',
                }}
                placeholder="Enter your password"
              />
            </div>
            <button
              type="submit"
              disabled={loading}
              style={{
                width: '100%',
                padding: '11px',
                background: '#1d4ed8',
                color: '#fff',
                border: 'none',
                borderRadius: '6px',
                fontSize: '14px',
                fontWeight: 600,
                cursor: loading ? 'not-allowed' : 'pointer',
                opacity: loading ? 0.7 : 1,
                transition: 'background 0.15s',
              }}
            >
              {loading ? 'Signing in...' : 'Sign In'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
