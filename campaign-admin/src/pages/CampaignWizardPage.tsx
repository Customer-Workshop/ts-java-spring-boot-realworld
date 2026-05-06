import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createCampaign } from '../api/campaigns';
import type { CampaignFormData } from '../types/campaign';

/* ─── Types ─── */
interface Asset {
  id: number;
  type: 'image' | 'text' | 'html';
  imageTag: string;
  imageAlt: string;
  ctaLink: string;
  textContent: string;
  htmlContent: string;
  fileName: string;
}

interface LocationSection {
  name: string;
  expanded: boolean;
  website: { topBanner: boolean; bottomBanner: boolean; rightColTop: boolean; rightColBottom: boolean; interstitial: boolean };
  mobile: { topBanner: boolean; bottomBanner: boolean; interstitial: boolean };
}

interface UserSegment {
  id: string;
  name: string;
  totalUsers: number;
}

/* ─── Sample Data ─── */
const SAMPLE_SEGMENTS: UserSegment[] = [
  { id: 's1', name: 'High balance checking', totalUsers: 12678 },
  { id: 's2', name: 'Young professional in CA', totalUsers: 12678 },
  { id: 's3', name: 'Digital users only', totalUsers: 12678 },
  { id: 's4', name: 'Banksegment', totalUsers: 12678 },
  { id: 's5', name: 'BU4CIBC bank', totalUsers: 12678 },
  { id: 's6', name: 'Business', totalUsers: 12678 },
  { id: 's7', name: 'Business123', totalUsers: 12678 },
  { id: 's8', name: 'Business@Jan1', totalUsers: 12678 },
  { id: 's9', name: 'Commercial', totalUsers: 12678 },
  { id: 's10', name: 'Commercial22/12', totalUsers: 12678 },
  { id: 's11', name: 'Credit Union', totalUsers: 12678 },
  { id: 's12', name: 'CU@Nov12', totalUsers: 12678 },
  { id: 's13', name: 'Long name segment', totalUsers: 12678 },
  { id: 's14', name: 'MyNewSegmentforC...', totalUsers: 12678 },
  { id: 's15', name: 'NewSegment', totalUsers: 12678 },
  { id: 's16', name: 'PNC', totalUsers: 12678 },
  { id: 's17', name: 'PNC@Business', totalUsers: 12678 },
  { id: 's18', name: 'Retail', totalUsers: 12678 },
  { id: 's19', name: 'Retail$123', totalUsers: 12678 },
  { id: 's20', name: 'Retail456', totalUsers: 12678 },
  { id: 's21', name: 'SegmentforBank', totalUsers: 12678 },
  { id: 's22', name: 'SegmentNewNew', totalUsers: 12678 },
  { id: 's23', name: 'Short name', totalUsers: 12678 },
  { id: 's24', name: 'Premium Members', totalUsers: 8432 },
];

const PRODUCT_CATEGORIES = [
  'Savings', 'Checking', 'Credit Card', 'Loans', 'Mortgage',
  'Investment', 'Insurance', 'Wealth Management',
];

const PRIORITY_OPTIONS = ['Low', 'Medium', 'High', 'Critical'];

const STEPS = ['Setup', 'Content', 'Segment', 'Location', 'Review'] as const;

const DEFAULT_LOCATIONS: LocationSection[] = [
  {
    name: 'Account summary',
    expanded: true,
    website: { topBanner: false, bottomBanner: false, rightColTop: false, rightColBottom: false, interstitial: false },
    mobile: { topBanner: false, bottomBanner: false, interstitial: false },
  },
  {
    name: 'Make a transfer',
    expanded: false,
    website: { topBanner: false, bottomBanner: false, rightColTop: false, rightColBottom: false, interstitial: false },
    mobile: { topBanner: false, bottomBanner: false, interstitial: false },
  },
  {
    name: 'Payments',
    expanded: false,
    website: { topBanner: false, bottomBanner: false, rightColTop: false, rightColBottom: false, interstitial: false },
    mobile: { topBanner: false, bottomBanner: false, interstitial: false },
  },
  {
    name: 'Bill pay',
    expanded: false,
    website: { topBanner: false, bottomBanner: false, rightColTop: false, rightColBottom: false, interstitial: false },
    mobile: { topBanner: false, bottomBanner: false, interstitial: false },
  },
];

/* ─── Channel config ─── */
const CHANNELS = [
  { key: 'IN_APP', label: 'In-app', icon: '📱' },
  { key: 'EMAIL', label: 'Email', icon: '✉️' },
  { key: 'SMS', label: 'SMS', icon: '💬' },
  { key: 'SOCIAL', label: 'Social media', icon: '📣' },
  { key: 'ADS', label: 'Ads', icon: '💎' },
] as const;

export function CampaignWizardPage() {
  const navigate = useNavigate();
  const [currentStep, setCurrentStep] = useState(0);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  /* Step 1 - Setup state */
  const [campaignName, setCampaignName] = useState('');
  const [description, setDescription] = useState('');
  const [keywords, setKeywords] = useState('');
  const [productCategory, setProductCategory] = useState('');
  const [priority, setPriority] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [selectedChannels, setSelectedChannels] = useState<string[]>(['IN_APP']);

  /* Step 2 - Content state */
  const [assets, setAssets] = useState<Asset[]>([
    { id: 1, type: 'image', imageTag: '', imageAlt: '', ctaLink: '', textContent: '', htmlContent: '', fileName: '' },
  ]);

  /* Step 3 - Segment state */
  const [selectedSegments, setSelectedSegments] = useState<UserSegment[]>([]);
  const [showSegmentModal, setShowSegmentModal] = useState(false);
  const [segmentSearch, setSegmentSearch] = useState('');
  const [tempSelectedSegments, setTempSelectedSegments] = useState<UserSegment[]>([]);

  /* Step 4 - Location state */
  const [locations, setLocations] = useState<LocationSection[]>(DEFAULT_LOCATIONS);

  const canGoNext = (): boolean => {
    if (currentStep === 0) return campaignName.trim().length > 0;
    return true;
  };

  const handleNext = () => {
    if (currentStep < STEPS.length - 1) setCurrentStep(currentStep + 1);
  };

  const handleBack = () => {
    if (currentStep > 0) setCurrentStep(currentStep - 1);
  };

  const goToStep = (step: number) => {
    setCurrentStep(step);
  };

  const handleSubmit = async () => {
    setError('');
    setSaving(true);
    try {
      const formData: CampaignFormData = {
        name: campaignName,
        targetAudienceSegment: selectedSegments.map(s => s.name).join(', '),
        startDate,
        endDate,
        messageTitle: assets.find(a => a.type === 'text')?.textContent.slice(0, 100) || campaignName,
        messageBody: assets.find(a => a.type === 'text')?.textContent || '',
        messageImageUrl: assets.find(a => a.type === 'image')?.fileName || '',
        messageCtaText: assets.find(a => a.type === 'image')?.ctaLink || '',
        fulfillmentActionType: 'ACCEPT',
        displayPlacement: 'POST_LOGIN',
        frequencyCapType: 'ONCE_PER_CAMPAIGN',
        frequencyCapMaxImpressions: 1,
        deliveryStartTime: '',
        deliveryEndTime: '',
        personalizationTokens: '',
        remindLaterDeferralDays: 1,
        fulfillmentWorkflowUrl: '',
        declineSuppression: true,
        confirmationMessage: 'Thank you for your response!',
        audienceRules: '',
        channel: selectedChannels[0] || 'IN_APP',
        priority: priority ? ({ Low: 2, Medium: 5, High: 8, Critical: 10 } as Record<string, number>)[priority] ?? 5 : 5,
        tags: keywords,
        abTestEnabled: false,
      };
      await createCampaign(formData);
      navigate('/campaigns');
    } catch {
      setError('Failed to create campaign. Please check your inputs.');
    } finally {
      setSaving(false);
    }
  };

  /* ─── Asset helpers ─── */
  const addAsset = () => {
    setAssets(prev => [
      ...prev,
      { id: Date.now(), type: 'image', imageTag: '', imageAlt: '', ctaLink: '', textContent: '', htmlContent: '', fileName: '' },
    ]);
  };

  const removeAsset = (id: number) => {
    if (assets.length > 1) setAssets(prev => prev.filter(a => a.id !== id));
  };

  const updateAsset = (id: number, field: keyof Asset, value: string) => {
    setAssets(prev => prev.map(a => a.id === id ? { ...a, [field]: value } : a));
  };

  /* ─── Channel toggle ─── */
  const toggleChannel = (key: string) => {
    setSelectedChannels(prev =>
      prev.includes(key) ? prev.filter(c => c !== key) : [...prev, key]
    );
  };

  /* ─── Location helpers ─── */
  const toggleLocationExpand = (idx: number) => {
    setLocations(prev => prev.map((l, i) => i === idx ? { ...l, expanded: !l.expanded } : l));
  };

  const toggleLocationCheckbox = (
    idx: number,
    platform: 'website' | 'mobile',
    field: string,
  ) => {
    setLocations(prev => prev.map((l, i) => {
      if (i !== idx) return l;
      return {
        ...l,
        [platform]: { ...l[platform], [field]: !(l[platform] as Record<string, boolean>)[field] },
      };
    }));
  };

  /* ─── Segment modal helpers ─── */
  const openSegmentModal = () => {
    setTempSelectedSegments([...selectedSegments]);
    setSegmentSearch('');
    setShowSegmentModal(true);
  };

  const toggleTempSegment = (seg: UserSegment) => {
    setTempSelectedSegments(prev =>
      prev.find(s => s.id === seg.id)
        ? prev.filter(s => s.id !== seg.id)
        : [...prev, seg]
    );
  };

  const confirmSegments = () => {
    setSelectedSegments(tempSelectedSegments);
    setShowSegmentModal(false);
  };

  const removeSegment = (id: string) => {
    setSelectedSegments(prev => prev.filter(s => s.id !== id));
  };

  const filteredSegments = SAMPLE_SEGMENTS.filter(s =>
    s.name.toLowerCase().includes(segmentSearch.toLowerCase())
  );

  const estimatedReach = selectedSegments.reduce((sum, s) => sum + s.totalUsers, 0);

  /* ─── Location summary for review ─── */
  const getLocationSummary = () => {
    const webLocs: string[] = [];
    const mobileLocs: string[] = [];
    locations.forEach(loc => {
      const wFields = loc.website;
      if (wFields.topBanner) webLocs.push(`${loc.name}-top banner`);
      if (wFields.bottomBanner) webLocs.push(`${loc.name}-bottom banner`);
      if (wFields.rightColTop) webLocs.push(`${loc.name}-right column top`);
      if (wFields.rightColBottom) webLocs.push(`${loc.name}-right column bottom`);
      if (wFields.interstitial) webLocs.push(`${loc.name}-interstitial`);
      const mFields = loc.mobile;
      if (mFields.topBanner) mobileLocs.push(`${loc.name}-top banner`);
      if (mFields.bottomBanner) mobileLocs.push(`${loc.name}-bottom banner`);
      if (mFields.interstitial) mobileLocs.push(`${loc.name}-interstitial`);
    });
    return { webLocs, mobileLocs };
  };

  return (
    <div style={{ maxWidth: '960px' }}>
      {/* Step indicator */}
      <StepIndicator currentStep={currentStep} onStepClick={goToStep} />

      {error && (
        <div style={{ background: '#fee2e2', color: '#991b1b', padding: '12px', borderRadius: '8px', fontSize: '14px', marginBottom: '16px' }}>
          {error}
        </div>
      )}

      {/* Step content */}
      <div style={{ background: '#fff', padding: '32px', borderRadius: '8px', boxShadow: '0 1px 3px rgba(0,0,0,0.06)', border: '1px solid #e5e7eb', minHeight: '480px' }}>
        {currentStep === 0 && (
          <StepSetup
            campaignName={campaignName} setCampaignName={setCampaignName}
            description={description} setDescription={setDescription}
            keywords={keywords} setKeywords={setKeywords}
            productCategory={productCategory} setProductCategory={setProductCategory}
            priority={priority} setPriority={setPriority}
            startDate={startDate} setStartDate={setStartDate}
            endDate={endDate} setEndDate={setEndDate}
            selectedChannels={selectedChannels} toggleChannel={toggleChannel}
          />
        )}
        {currentStep === 1 && (
          <StepContent
            assets={assets}
            addAsset={addAsset}
            removeAsset={removeAsset}
            updateAsset={updateAsset}
          />
        )}
        {currentStep === 2 && (
          <StepSegment
            selectedSegments={selectedSegments}
            openSegmentModal={openSegmentModal}
            removeSegment={removeSegment}
            estimatedReach={estimatedReach}
          />
        )}
        {currentStep === 3 && (
          <StepLocation
            locations={locations}
            toggleLocationExpand={toggleLocationExpand}
            toggleLocationCheckbox={toggleLocationCheckbox}
          />
        )}
        {currentStep === 4 && (
          <StepReview
            campaignName={campaignName}
            productCategory={productCategory}
            priority={priority}
            startDate={startDate}
            endDate={endDate}
            selectedChannels={selectedChannels}
            assets={assets}
            selectedSegments={selectedSegments}
            estimatedReach={estimatedReach}
            locationSummary={getLocationSummary()}
            goToStep={goToStep}
          />
        )}
      </div>

      {/* Navigation buttons */}
      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '24px' }}>
        {currentStep > 0 && (
          <button onClick={handleBack} style={navBtnOutline}>Back</button>
        )}
        {currentStep < STEPS.length - 1 && (
          <button onClick={handleNext} disabled={!canGoNext()} style={{ ...navBtnPrimary, opacity: canGoNext() ? 1 : 0.5, cursor: canGoNext() ? 'pointer' : 'not-allowed' }}>
            Next
          </button>
        )}
        {currentStep === STEPS.length - 1 && (
          <>
            <button onClick={() => alert('Campaign sent for approval!')} style={navBtnOutline}>
              Send for approval
            </button>
            <button onClick={handleSubmit} disabled={saving} style={{ ...navBtnPrimary, opacity: saving ? 0.7 : 1 }}>
              {saving ? 'Creating...' : 'Define journey'}
            </button>
          </>
        )}
      </div>

      {/* Segment Modal */}
      {showSegmentModal && (
        <SegmentModal
          segments={filteredSegments}
          tempSelected={tempSelectedSegments}
          toggleTempSegment={toggleTempSegment}
          segmentSearch={segmentSearch}
          setSegmentSearch={setSegmentSearch}
          onConfirm={confirmSegments}
          onCancel={() => setShowSegmentModal(false)}
        />
      )}
    </div>
  );
}

/* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   Step Indicator
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
function StepIndicator({ currentStep, onStepClick }: { currentStep: number; onStepClick: (step: number) => void }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '32px', padding: '24px 0' }}>
      {STEPS.map((step, idx) => {
        const isCompleted = idx < currentStep;
        const isCurrent = idx === currentStep;
        return (
          <React.Fragment key={step}>
            {idx > 0 && (
              <div style={{
                flex: 1, height: '3px', maxWidth: '120px',
                background: isCompleted ? '#1d4ed8' : '#d1d5db',
                margin: '0 4px',
              }} />
            )}
            <div
              onClick={() => onStepClick(idx)}
              style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', cursor: 'pointer', minWidth: '80px' }}
            >
              <div style={{
                width: '40px', height: '40px', borderRadius: '50%',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontWeight: 600, fontSize: '14px',
                background: isCompleted ? '#1d4ed8' : isCurrent ? '#1d4ed8' : '#e5e7eb',
                color: isCompleted || isCurrent ? '#fff' : '#6b7280',
                border: isCurrent ? '3px solid #93c5fd' : 'none',
                transition: 'all 0.2s',
              }}>
                {isCompleted ? '✓' : idx + 1}
              </div>
              <span style={{
                marginTop: '6px', fontSize: '12px', fontWeight: isCurrent ? 600 : 400,
                color: isCurrent ? '#1d4ed8' : isCompleted ? '#1d4ed8' : '#6b7280',
              }}>
                {step}
              </span>
            </div>
          </React.Fragment>
        );
      })}
    </div>
  );
}

/* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   Step 1 – Setup
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
function StepSetup({
  campaignName, setCampaignName, description, setDescription,
  keywords, setKeywords, productCategory, setProductCategory,
  priority, setPriority, startDate, setStartDate, endDate, setEndDate,
  selectedChannels, toggleChannel,
}: {
  campaignName: string; setCampaignName: (v: string) => void;
  description: string; setDescription: (v: string) => void;
  keywords: string; setKeywords: (v: string) => void;
  productCategory: string; setProductCategory: (v: string) => void;
  priority: string; setPriority: (v: string) => void;
  startDate: string; setStartDate: (v: string) => void;
  endDate: string; setEndDate: (v: string) => void;
  selectedChannels: string[]; toggleChannel: (k: string) => void;
}) {
  return (
    <>
      <h2 style={stepTitle}>Campaign details</h2>

      <div style={fieldGroup}>
        <input
          value={campaignName} onChange={e => setCampaignName(e.target.value)}
          style={inputStyle} placeholder="Campaign name" required
        />
      </div>

      <div style={fieldGroup}>
        <textarea
          value={description} onChange={e => setDescription(e.target.value)}
          rows={3} style={{ ...inputStyle, resize: 'vertical' }} placeholder="Description"
        />
        <span style={optionalLabel}>Optional</span>
      </div>

      <div style={fieldGroup}>
        <input
          value={keywords} onChange={e => setKeywords(e.target.value)}
          style={inputStyle} placeholder="Keywords (separated by comma)"
        />
        <span style={optionalLabel}>Optional</span>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', ...fieldGroup }}>
        <div>
          <select value={productCategory} onChange={e => setProductCategory(e.target.value)} style={inputStyle}>
            <option value="">Product category</option>
            {PRODUCT_CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
          </select>
          <span style={optionalLabel}>Optional</span>
        </div>
        <div>
          <select value={priority} onChange={e => setPriority(e.target.value)} style={inputStyle}>
            <option value="">Priority</option>
            {PRIORITY_OPTIONS.map(p => <option key={p} value={p}>{p}</option>)}
          </select>
          <span style={optionalLabel}>Optional</span>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', ...fieldGroup }}>
        <div>
          <label style={labelStyle}>Start date</label>
          <input type="date" value={startDate} onChange={e => setStartDate(e.target.value)} style={inputStyle} />
        </div>
        <div>
          <label style={labelStyle}>End date</label>
          <input type="date" value={endDate} onChange={e => setEndDate(e.target.value)} style={inputStyle} />
        </div>
      </div>

      <h3 style={{ fontSize: '16px', fontWeight: 600, color: '#1a2744', marginTop: '24px', marginBottom: '16px' }}>Channel</h3>
      <div style={{ display: 'flex', gap: '16px', flexWrap: 'wrap' }}>
        {CHANNELS.map(ch => {
          const selected = selectedChannels.includes(ch.key);
          return (
            <div
              key={ch.key}
              onClick={() => toggleChannel(ch.key)}
              style={{
                width: '120px', padding: '20px 16px', borderRadius: '8px', cursor: 'pointer',
                border: selected ? '2px solid #4ade80' : '2px solid #e5e7eb',
                background: selected ? '#f0fdf4' : '#fff',
                display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '10px',
                position: 'relative', transition: 'all 0.15s',
              }}
            >
              {selected && (
                <div style={{
                  position: 'absolute', top: '6px', right: '6px', width: '20px', height: '20px',
                  borderRadius: '4px', background: '#4ade80', color: '#fff',
                  display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '12px',
                }}>✓</div>
              )}
              {!selected && (
                <div style={{
                  position: 'absolute', top: '6px', right: '6px', width: '20px', height: '20px',
                  borderRadius: '4px', border: '2px solid #d1d5db',
                }} />
              )}
              <span style={{ fontSize: '28px' }}>{ch.icon}</span>
              <span style={{ fontSize: '13px', fontWeight: 500, color: '#374151' }}>{ch.label}</span>
            </div>
          );
        })}
      </div>
    </>
  );
}

/* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   Step 2 – Content
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
function StepContent({
  assets, addAsset, removeAsset, updateAsset,
}: {
  assets: Asset[];
  addAsset: () => void;
  removeAsset: (id: number) => void;
  updateAsset: (id: number, field: keyof Asset, value: string) => void;
}) {
  return (
    <>
      <div style={{ display: 'flex', gap: '12px', marginBottom: '24px' }}>
        <button style={{ ...navBtnPrimary, fontSize: '13px', padding: '8px 20px' }}>
          Upload from MCM media
        </button>
        <button style={{ ...navBtnOutline, fontSize: '13px', padding: '8px 20px' }}>
          Upload new
        </button>
      </div>

      {assets.map((asset, idx) => (
        <div key={asset.id} style={{ border: '1px solid #e5e7eb', borderRadius: '8px', padding: '24px', marginBottom: '20px', position: 'relative' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
            <h3 style={{ fontSize: '16px', fontWeight: 600, margin: 0, color: '#1a2744' }}>Asset {idx + 1}</h3>
            {assets.length > 1 && (
              <button onClick={() => removeAsset(asset.id)} style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: '18px', color: '#9ca3af' }} title="Delete asset">🗑️</button>
            )}
          </div>

          {/* Type selector */}
          <div style={{ display: 'flex', gap: '24px', marginBottom: '16px' }}>
            {(['image', 'text', 'html'] as const).map(t => (
              <label key={t} style={{ display: 'flex', alignItems: 'center', gap: '6px', cursor: 'pointer', fontSize: '14px', color: '#374151' }}>
                <input
                  type="radio" name={`asset-type-${asset.id}`} checked={asset.type === t}
                  onChange={() => updateAsset(asset.id, 'type', t)}
                  style={{ accentColor: '#4ade80', width: '16px', height: '16px' }}
                />
                {t.charAt(0).toUpperCase() + t.slice(1)}
              </label>
            ))}
          </div>

          {/* Image type */}
          {asset.type === 'image' && (
            <>
              <p style={{ fontSize: '12px', color: '#6b7280', margin: '0 0 12px' }}>
                ℹ️ Please upload WebP, JPG, or PNG files. Max file size: 2MB.
              </p>
              <div style={{
                border: '2px dashed #d1d5db', borderRadius: '8px', padding: '40px',
                textAlign: 'center', color: '#9ca3af', marginBottom: '16px',
                background: '#fafafa',
              }}>
                <button style={{ ...navBtnOutline, fontSize: '13px', padding: '6px 16px', marginRight: '12px' }}>
                  Select a file
                </button>
                Drop your file here
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', marginBottom: '12px' }}>
                <input value={asset.imageTag} onChange={e => updateAsset(asset.id, 'imageTag', e.target.value)} style={inputStyle} placeholder="Image tag option" />
                <input value={asset.imageAlt} onChange={e => updateAsset(asset.id, 'imageAlt', e.target.value)} style={inputStyle} placeholder="Image alt text" />
              </div>
              <div style={fieldGroup}>
                <input value={asset.ctaLink} onChange={e => updateAsset(asset.id, 'ctaLink', e.target.value)} style={inputStyle} placeholder="CTA link" />
                <span style={optionalLabel}>Optional</span>
              </div>
            </>
          )}

          {/* Text type */}
          {asset.type === 'text' && (
            <>
              <div style={{
                display: 'flex', flexWrap: 'wrap', gap: '2px', padding: '6px 8px',
                background: '#f3f4f6', borderRadius: '6px 6px 0 0', border: '1px solid #d1d5db',
                borderBottom: 'none', fontSize: '13px',
              }}>
                <select style={{ border: 'none', background: 'transparent', fontSize: '12px', padding: '4px', color: '#374151' }}>
                  <option>Paragraph</option>
                  <option>Heading 1</option>
                  <option>Heading 2</option>
                  <option>Heading 3</option>
                </select>
                {['B', 'I', 'U', '⌐', '≡', '⊕', '⊗', 'A', 'Aa', '⊞', '—'].map((btn, i) => (
                  <button key={i} style={{
                    background: 'none', border: 'none', cursor: 'pointer',
                    padding: '4px 8px', fontSize: '13px', fontWeight: btn === 'B' ? 700 : 400,
                    fontStyle: btn === 'I' ? 'italic' : 'normal',
                    textDecoration: btn === 'U' ? 'underline' : 'none',
                    color: '#374151',
                  }}>
                    {btn}
                  </button>
                ))}
                {['≡₁', '≡₂', '≡₃', '☰', '☰₂', '☰₃'].map((btn, i) => (
                  <button key={`t-${i}`} style={{ background: 'none', border: 'none', cursor: 'pointer', padding: '4px 6px', fontSize: '12px', color: '#6b7280' }}>
                    {btn}
                  </button>
                ))}
              </div>
              <textarea
                value={asset.textContent}
                onChange={e => updateAsset(asset.id, 'textContent', e.target.value)}
                rows={6}
                style={{ ...inputStyle, borderRadius: '0 0 6px 6px', resize: 'vertical' }}
                placeholder="Write content here"
              />
            </>
          )}

          {/* HTML type */}
          {asset.type === 'html' && (
            <>
              <p style={{ fontSize: '12px', color: '#6b7280', margin: '0 0 12px' }}>
                ℹ️ Upload your HTML file or drop the file in the HTML editor.
              </p>
              <button style={{ ...navBtnOutline, fontSize: '13px', padding: '6px 16px', marginBottom: '12px' }}>
                Select a file
              </button>
              <textarea
                value={asset.htmlContent}
                onChange={e => updateAsset(asset.id, 'htmlContent', e.target.value)}
                rows={6}
                style={{ ...inputStyle, resize: 'vertical', fontFamily: 'monospace' }}
                placeholder="Paste HTML content or drop your file here"
              />
            </>
          )}
        </div>
      ))}

      <button onClick={addAsset} style={{ ...navBtnOutline, fontSize: '13px' }}>
        + Add asset
      </button>
    </>
  );
}

/* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   Step 3 – Segment
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
function StepSegment({
  selectedSegments, openSegmentModal, removeSegment, estimatedReach,
}: {
  selectedSegments: UserSegment[];
  openSegmentModal: () => void;
  removeSegment: (id: string) => void;
  estimatedReach: number;
}) {
  return (
    <>
      <h2 style={stepTitle}>Segment</h2>

      <div style={{ marginBottom: '20px' }}>
        <button onClick={openSegmentModal} style={{ ...navBtnPrimary, fontSize: '13px', padding: '10px 24px' }}>
          + Select user segment
        </button>
      </div>

      {selectedSegments.length > 0 && (
        <>
          <div style={{ marginBottom: '16px' }}>
            <label style={{ ...labelStyle, fontSize: '13px', textTransform: 'uppercase', letterSpacing: '0.05em', color: '#6b7280' }}>
              User segment selected:
            </label>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px', marginTop: '8px' }}>
              {selectedSegments.map(seg => (
                <span key={seg.id} style={{
                  display: 'inline-flex', alignItems: 'center', gap: '6px',
                  background: '#e0f2fe', color: '#0369a1', padding: '6px 12px',
                  borderRadius: '20px', fontSize: '13px', fontWeight: 500,
                }}>
                  {seg.name}
                  <button onClick={() => removeSegment(seg.id)} style={{
                    background: 'none', border: 'none', cursor: 'pointer',
                    color: '#0369a1', fontSize: '16px', lineHeight: 1, padding: 0,
                  }}>×</button>
                </span>
              ))}
            </div>
          </div>

          <div style={{ background: '#f0fdf4', border: '1px solid #bbf7d0', borderRadius: '8px', padding: '16px', display: 'flex', gap: '12px', alignItems: 'center' }}>
            <span style={{ fontSize: '24px' }}>👥</span>
            <div>
              <div style={{ fontSize: '13px', color: '#6b7280' }}>Estimated reach</div>
              <div style={{ fontSize: '20px', fontWeight: 700, color: '#166534' }}>{estimatedReach.toLocaleString()}</div>
            </div>
          </div>
        </>
      )}

      {selectedSegments.length === 0 && (
        <div style={{ textAlign: 'center', padding: '60px 0', color: '#9ca3af' }}>
          <p style={{ fontSize: '48px', margin: '0 0 16px' }}>👥</p>
          <p style={{ fontSize: '16px' }}>No segments selected yet</p>
          <p style={{ fontSize: '13px' }}>Click the button above to select user segments for this campaign</p>
        </div>
      )}
    </>
  );
}

/* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   Step 4 – Location
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
function StepLocation({
  locations, toggleLocationExpand, toggleLocationCheckbox,
}: {
  locations: LocationSection[];
  toggleLocationExpand: (idx: number) => void;
  toggleLocationCheckbox: (idx: number, platform: 'website' | 'mobile', field: string) => void;
}) {
  return (
    <>
      <h2 style={stepTitle}>Campaign location</h2>
      {locations.map((loc, idx) => (
        <div key={loc.name} style={{ border: '1px solid #e5e7eb', borderRadius: '8px', marginBottom: '16px', overflow: 'hidden' }}>
          <div
            onClick={() => toggleLocationExpand(idx)}
            style={{
              display: 'flex', justifyContent: 'space-between', alignItems: 'center',
              padding: '16px 20px', cursor: 'pointer', background: '#fafafa',
              borderBottom: loc.expanded ? '1px solid #e5e7eb' : 'none',
            }}
          >
            <span style={{ fontSize: '15px', fontWeight: 500, color: '#1a2744' }}>{loc.name}</span>
            <span style={{ fontSize: '18px', color: '#6b7280', transition: 'transform 0.2s', transform: loc.expanded ? 'rotate(180deg)' : 'rotate(0)' }}>
              ▾
            </span>
          </div>
          {loc.expanded && (
            <div style={{ padding: '20px' }}>
              {/* Website */}
              <h4 style={{ fontSize: '14px', fontWeight: 600, color: '#374151', margin: '0 0 12px' }}>Website</h4>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '16px', marginBottom: '20px' }}>
                {[
                  { key: 'topBanner', label: 'Top banner' },
                  { key: 'bottomBanner', label: 'Bottom banner' },
                  { key: 'rightColTop', label: 'Right column top' },
                  { key: 'rightColBottom', label: 'Right column bottom' },
                  { key: 'interstitial', label: 'Interstitial' },
                ].map(item => (
                  <label key={item.key} style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', fontSize: '14px', color: '#374151', minWidth: '180px' }}>
                    <input
                      type="checkbox"
                      checked={(loc.website as Record<string, boolean>)[item.key]}
                      onChange={() => toggleLocationCheckbox(idx, 'website', item.key)}
                      style={{ width: '18px', height: '18px', accentColor: '#1d4ed8' }}
                    />
                    {item.label}
                  </label>
                ))}
              </div>

              {/* Mobile */}
              <h4 style={{ fontSize: '14px', fontWeight: 600, color: '#374151', margin: '0 0 12px' }}>Mobile</h4>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '16px' }}>
                {[
                  { key: 'topBanner', label: 'Top banner' },
                  { key: 'bottomBanner', label: 'Bottom banner' },
                  { key: 'interstitial', label: 'Interstitial' },
                ].map(item => (
                  <label key={item.key} style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', fontSize: '14px', color: '#374151', minWidth: '180px' }}>
                    <input
                      type="checkbox"
                      checked={(loc.mobile as Record<string, boolean>)[item.key]}
                      onChange={() => toggleLocationCheckbox(idx, 'mobile', item.key)}
                      style={{ width: '18px', height: '18px', accentColor: '#1d4ed8' }}
                    />
                    {item.label}
                  </label>
                ))}
              </div>
            </div>
          )}
        </div>
      ))}
    </>
  );
}

/* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   Step 5 – Review
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
function StepReview({
  campaignName, productCategory, priority, startDate, endDate,
  selectedChannels, assets, selectedSegments, estimatedReach,
  locationSummary, goToStep,
}: {
  campaignName: string; productCategory: string; priority: string;
  startDate: string; endDate: string; selectedChannels: string[];
  assets: Asset[]; selectedSegments: UserSegment[]; estimatedReach: number;
  locationSummary: { webLocs: string[]; mobileLocs: string[] };
  goToStep: (step: number) => void;
}) {
  const channelLabels = selectedChannels.map(k => CHANNELS.find(c => c.key === k)?.label || k).join(', ');

  return (
    <>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <h2 style={{ ...stepTitle, marginBottom: 0 }}>Review</h2>
        <button style={{ ...navBtnPrimary, fontSize: '13px', padding: '8px 20px' }}>
          Preview campaign
        </button>
      </div>

      {/* Campaign details */}
      <ReviewSection title="Campaign details" onEdit={() => goToStep(0)}>
        <ReviewRow label="Campaign name" value={campaignName} />
        <ReviewRow label="Product" value={productCategory || 'Not set'} />
        <ReviewRow label="Priority" value={priority || 'Not set'} />
        <ReviewRow label="Start date" value={startDate || 'MM/DD/YYYY'} />
        <ReviewRow label="End date" value={endDate || 'MM/DD/YYYY'} />
        <ReviewRow label="Channel" value={channelLabels || 'None'} />
      </ReviewSection>

      {/* Content */}
      <ReviewSection title="Content" onEdit={() => goToStep(1)}>
        {assets.map((asset, idx) => {
          if (asset.type === 'image') {
            return <ReviewRow key={idx} label="Image" value={asset.fileName || asset.imageTag || '(no file)'} />;
          }
          if (asset.type === 'text') {
            return <ReviewRow key={idx} label="Text" value={asset.textContent.slice(0, 80) || '(empty)'} />;
          }
          return <ReviewRow key={idx} label="HTML" value={asset.htmlContent.slice(0, 80) || '(no content)'} />;
        })}
      </ReviewSection>

      {/* Segment */}
      <ReviewSection title="Segment" onEdit={() => goToStep(2)}>
        <ReviewRow label="User segment" value={selectedSegments.map(s => s.name).join('\n') || 'None'} />
        <ReviewRow label="Estimated reach" value={estimatedReach.toLocaleString()} />
      </ReviewSection>

      {/* Location */}
      <ReviewSection title="Location" onEdit={() => goToStep(3)}>
        <ReviewRow label="Web location" value={locationSummary.webLocs.join('\n') || 'None'} />
        <ReviewRow label="Mobile location" value={locationSummary.mobileLocs.join('\n') || 'None'} />
      </ReviewSection>
    </>
  );
}

function ReviewSection({ title, onEdit, children }: { title: string; onEdit: () => void; children: React.ReactNode }) {
  return (
    <div style={{ border: '1px solid #e5e7eb', borderRadius: '8px', padding: '20px', marginBottom: '16px', position: 'relative' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
        <h3 style={{ fontSize: '16px', fontWeight: 600, margin: 0, color: '#1a2744' }}>{title}</h3>
        <button onClick={onEdit} style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: '16px', color: '#6b7280' }} title="Edit">✏️</button>
      </div>
      {children}
    </div>
  );
}

function ReviewRow({ label, value }: { label: string; value: string }) {
  return (
    <div style={{ display: 'flex', gap: '24px', padding: '6px 0', fontSize: '14px' }}>
      <span style={{ color: '#6b7280', minWidth: '140px', fontWeight: 500 }}>{label}</span>
      <span style={{ color: '#1f2937', whiteSpace: 'pre-line' }}>{value}</span>
    </div>
  );
}

/* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   Segment Modal
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
function SegmentModal({
  segments, tempSelected, toggleTempSegment,
  segmentSearch, setSegmentSearch, onConfirm, onCancel,
}: {
  segments: UserSegment[];
  tempSelected: UserSegment[];
  toggleTempSegment: (seg: UserSegment) => void;
  segmentSearch: string;
  setSegmentSearch: (v: string) => void;
  onConfirm: () => void;
  onCancel: () => void;
}) {
  return (
    <div style={{
      position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
      background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center',
      zIndex: 1000,
    }}>
      <div style={{
        background: '#fff', borderRadius: '12px', width: '780px', maxHeight: '80vh',
        display: 'flex', flexDirection: 'column', boxShadow: '0 20px 60px rgba(0,0,0,0.3)',
      }}>
        {/* Header */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '24px 28px 0' }}>
          <h2 style={{ fontSize: '20px', fontWeight: 600, margin: 0, color: '#1a2744' }}>Select user segment</h2>
          <button onClick={onCancel} style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: '24px', color: '#6b7280' }}>×</button>
        </div>

        {/* Search */}
        <div style={{ padding: '16px 28px', display: 'flex', gap: '12px' }}>
          <input
            value={segmentSearch} onChange={e => setSegmentSearch(e.target.value)}
            style={{ ...inputStyle, flex: 1 }} placeholder="Search by description"
          />
          <button style={{ ...navBtnPrimary, fontSize: '13px', padding: '8px 24px' }}>Search</button>
        </div>

        {/* Selected chips */}
        {tempSelected.length > 0 && (
          <div style={{ padding: '0 28px 8px', display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
            <span style={{ fontSize: '12px', color: '#6b7280', textTransform: 'uppercase', letterSpacing: '0.05em', alignSelf: 'center' }}>
              User segment selected:
            </span>
            {tempSelected.map(seg => (
              <span key={seg.id} style={{
                display: 'inline-flex', alignItems: 'center', gap: '4px',
                background: '#e0f2fe', color: '#0369a1', padding: '4px 12px',
                borderRadius: '16px', fontSize: '12px',
              }}>
                {seg.name}
                <button onClick={() => toggleTempSegment(seg)} style={{
                  background: 'none', border: 'none', cursor: 'pointer', color: '#0369a1', fontSize: '14px', padding: 0,
                }}>×</button>
              </span>
            ))}
          </div>
        )}

        {/* Grid */}
        <div style={{ flex: 1, overflow: 'auto', padding: '8px 28px' }}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '12px' }}>
            {segments.map(seg => {
              const checked = tempSelected.some(s => s.id === seg.id);
              return (
                <label key={seg.id} style={{
                  display: 'flex', alignItems: 'flex-start', gap: '8px', cursor: 'pointer',
                  padding: '12px', borderRadius: '6px', border: '1px solid #e5e7eb',
                  background: checked ? '#f0fdf4' : '#fff',
                  transition: 'all 0.15s',
                }}>
                  <input
                    type="checkbox" checked={checked} onChange={() => toggleTempSegment(seg)}
                    style={{ marginTop: '2px', width: '18px', height: '18px', accentColor: '#4ade80', flexShrink: 0 }}
                  />
                  <div>
                    <div style={{ fontSize: '13px', fontWeight: 500, color: '#1f2937' }}>{seg.name}</div>
                    <div style={{ fontSize: '11px', color: '#6b7280' }}>Total users: {seg.totalUsers.toLocaleString()}</div>
                  </div>
                </label>
              );
            })}
          </div>
        </div>

        {/* Footer */}
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', padding: '16px 28px', borderTop: '1px solid #e5e7eb' }}>
          <button onClick={onCancel} style={navBtnOutline}>Cancel</button>
          <button onClick={onConfirm} style={navBtnPrimary}>Add</button>
        </div>
      </div>
    </div>
  );
}

/* ─── Shared Styles ─── */
const stepTitle: React.CSSProperties = {
  fontSize: '22px', fontWeight: 600, marginBottom: '24px', color: '#1a2744',
};

const fieldGroup: React.CSSProperties = { marginBottom: '16px' };

const labelStyle: React.CSSProperties = {
  display: 'block', fontSize: '14px', fontWeight: 500, color: '#374151', marginBottom: '6px',
};

const inputStyle: React.CSSProperties = {
  width: '100%', padding: '10px 12px', border: '1px solid #d1d5db',
  borderRadius: '6px', fontSize: '14px', boxSizing: 'border-box' as const,
};

const optionalLabel: React.CSSProperties = {
  fontSize: '11px', color: '#9ca3af', marginTop: '2px', display: 'block',
};

const navBtnPrimary: React.CSSProperties = {
  padding: '10px 24px', border: 'none', borderRadius: '6px',
  background: '#4a7c3f', color: '#fff', cursor: 'pointer',
  fontSize: '14px', fontWeight: 600,
};

const navBtnOutline: React.CSSProperties = {
  padding: '10px 24px', border: '1px solid #d1d5db', borderRadius: '6px',
  background: '#fff', cursor: 'pointer', fontSize: '14px', color: '#374151',
};
