import { useState } from 'react';
import './App.css';
import ResearchForm from './components/ResearchForm';
import ResearchList from './components/ResearchList';
import EvidencePackView from './components/EvidencePackView';

type View = 'home' | 'pack';

function App() {
  const [view, setView] = useState<View>('home');
  const [selectedPackId, setSelectedPackId] = useState<string>('');

  const handleViewPack = (id: string) => {
    setSelectedPackId(id);
    setView('pack');
  };

  return (
    <div className="app">
      <header className="app-header">
        <div className="header-content">
          <div className="header-left">
            <span className="logo-icon">🔍</span>
            <h1 onClick={() => setView('home')} style={{ cursor: 'pointer' }}>
              Market Intelligence Agent
            </h1>
          </div>
          <span className="header-badge">Campaign+ Enhancement</span>
        </div>
      </header>

      <main className="app-main">
        {view === 'home' && (
          <div className="home-layout">
            <div className="form-section">
              <ResearchForm onCreated={handleViewPack} />
            </div>
            <div className="list-section">
              <ResearchList onSelect={handleViewPack} />
            </div>
          </div>
        )}
        {view === 'pack' && (
          <EvidencePackView
            packId={selectedPackId}
            onBack={() => setView('home')}
          />
        )}
      </main>
    </div>
  );
}

export default App;
