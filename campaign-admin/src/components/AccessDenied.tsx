export function AccessDenied() {
  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '60vh',
        textAlign: 'center',
      }}
    >
      <div
        style={{
          width: '80px',
          height: '80px',
          borderRadius: '50%',
          background: '#fee2e2',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: '36px',
          marginBottom: '24px',
        }}
      >
        <span role="img" aria-label="Lock">&#128274;</span>
      </div>
      <h2
        style={{
          fontSize: '24px',
          fontWeight: 700,
          color: '#1e293b',
          margin: '0 0 8px',
        }}
      >
        Access Denied
      </h2>
      <p
        style={{
          fontSize: '16px',
          color: '#64748b',
          maxWidth: '400px',
          lineHeight: 1.5,
        }}
      >
        You do not have the required &quot;Marketing&quot; entitlement to access
        campaign management. Please contact your administrator to request
        access.
      </p>
    </div>
  );
}
