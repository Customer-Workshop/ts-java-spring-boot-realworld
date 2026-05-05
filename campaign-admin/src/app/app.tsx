import { Route, Routes, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Layout } from '../components/Layout';
import { AccessDenied } from '../components/AccessDenied';
import { LoginPage } from '../pages/LoginPage';
import { DashboardPage } from '../pages/DashboardPage';
import { CampaignListPage } from '../pages/CampaignListPage';
import { CampaignFormPage } from '../pages/CampaignFormPage';
import { CampaignDetailPage } from '../pages/CampaignDetailPage';
import { CampaignAnalyticsPage } from '../pages/CampaignAnalyticsPage';
import { CampaignCalendarPage } from '../pages/CampaignCalendarPage';
import { IntelligencePage } from '../pages/IntelligencePage';

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuth();
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return <Layout>{children}</Layout>;
}

export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/access-denied" element={<Layout><AccessDenied /></Layout>} />

      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <DashboardPage />
          </ProtectedRoute>
        }
      />

      <Route
        path="/campaigns"
        element={
          <ProtectedRoute>
            <CampaignListPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/campaigns/new"
        element={
          <ProtectedRoute>
            <CampaignFormPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/campaigns/:id"
        element={
          <ProtectedRoute>
            <CampaignDetailPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/campaigns/:id/edit"
        element={
          <ProtectedRoute>
            <CampaignFormPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/campaigns/:id/analytics"
        element={
          <ProtectedRoute>
            <CampaignAnalyticsPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/calendar"
        element={
          <ProtectedRoute>
            <CampaignCalendarPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/intelligence"
        element={
          <ProtectedRoute>
            <IntelligencePage />
          </ProtectedRoute>
        }
      />

      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}

export default App;
