import React, { useState } from 'react';

interface ScreenshotItem {
  id: string;
  title: string;
  subtitle: string;
  tag: string;
  src: string;
  description: string;
}

const primaryScreenshots: ScreenshotItem[] = [
  {
    id: '03-analysis',
    title: 'Incident Analysis',
    subtitle: 'RPA-F1E6817D · Network Transition',
    tag: 'Automated Diagnosis',
    src: '/screenshots/03-analysis.png',
    description: 'Correlates captured device telemetry, lifecycle shifts, and HTTP 401 errors into a structured root cause hypothesis before test generation.',
  },
  {
    id: '07-bug-reproduced',
    title: 'Bug Reproduced',
    subtitle: 'Scenario Runner · Mode ADB',
    tag: 'Failure Assertion Passed',
    src: '/screenshots/07-bug-reproduced.png',
    description: 'Autonomous runner commands the physical device over ADB, triggering network toggles and asserting the exact payment failure signature.',
  },
  {
    id: '08-fix-verified',
    title: 'Fix Verified',
    subtitle: 'Unchanged Scenario Rerun',
    tag: 'Positive Health Verification',
    src: '/screenshots/08-fix-verified.png',
    description: 'Re-executes the identical TestScenario against the fixed app. Verifies token refresh, HTTP 200, and successful checkout completion.',
  },
];

const secondaryScreenshots: ScreenshotItem[] = [
  {
    id: '01-home',
    title: 'Home & Readiness',
    subtitle: 'Preflight & Device Status',
    tag: 'Setup',
    src: '/screenshots/01-home.png',
    description: 'Preflight readiness checks for ADB daemon, runner connectivity, package installation, and demo reset.',
  },
  {
    id: '02-live-session',
    title: 'Live Telemetry Session',
    subtitle: 'Active SDK Event Capture',
    tag: 'Capture',
    src: '/screenshots/02-live-session.png',
    description: 'Live broadcast receiver collecting instrumented domain events, UI clicks, and lifecycle state changes.',
  },
  {
    id: '04-timeline',
    title: 'Event Timeline',
    subtitle: 'Filtered Telemetry Stream',
    tag: 'Inspection',
    src: '/screenshots/04-timeline.png',
    description: 'Chronological timeline of system events with domain filtering, severity flags, and payload inspection.',
  },
  {
    id: '05-reproduction',
    title: 'Scenario Config',
    subtitle: 'Generated Reproduction Steps',
    tag: 'Generation',
    src: '/screenshots/05-reproduction.png',
    description: 'Generated typed steps (OPEN_SCREEN, CHANGE_NETWORK, TAP, ASSERT) ready for runner execution.',
  },
  {
    id: '06-running-test',
    title: 'Active Runner Execution',
    subtitle: 'WebSocket Step Stream',
    tag: 'Execution',
    src: '/screenshots/06-running-test.png',
    description: 'Step-by-step ADB execution progress streamed via WebSocket with real-time phone state updates.',
  },
  {
    id: '09-incident-report',
    title: 'Incident Report',
    subtitle: 'Exportable Debug Artifact',
    tag: 'Export',
    src: '/screenshots/09-incident-report.png',
    description: 'Preserved post-mortem incident report bundle with environment metadata, scenario JSON, and dual execution results.',
  },
];

export const Screenshots: React.FC = () => {
  const [selectedImage, setSelectedImage] = useState<ScreenshotItem | null>(null);

  return (
    <section id="screenshots" className="py-20 border-t border-[#1a2130] bg-[#080b10]">
      <div className="max-w-[1200px] mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Section Header */}
        <div className="text-center max-w-3xl mx-auto mb-16">
          <div className="text-xs font-mono text-sky-400 uppercase tracking-widest font-semibold mb-3">
            Real Handset Evidence
          </div>
          <h2 className="text-3xl sm:text-4xl font-bold tracking-tight text-white">
            Unedited physical device captures.
          </h2>
          <p className="mt-3 text-sm sm:text-base text-slate-400">
            Real screenshots captured on the connected Android handset during Phase 8 validation. No mock fixtures or synthetic renders.
          </p>
        </div>

        {/* Primary Large Screenshots (3 Columns) */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mb-16">
          {primaryScreenshots.map((item) => (
            <div
              key={item.id}
              className="flex flex-col rounded-2xl bg-[#0c1017] border border-[#1e273a] hover:border-slate-500/80 transition-all overflow-hidden group shadow-lg"
            >
              {/* Card Header */}
              <div className="p-4 sm:p-5 border-b border-[#182030] bg-[#090d14]">
                <div className="flex items-center justify-between gap-2 mb-1.5">
                  <span className="text-[11px] font-mono px-2 py-0.5 rounded bg-sky-950/60 border border-sky-800/50 text-sky-300 font-medium">
                    {item.tag}
                  </span>
                  <span className="text-[10px] font-mono text-slate-400">{item.subtitle}</span>
                </div>
                <h3 className="text-base font-semibold text-white group-hover:text-sky-300 transition-colors">
                  {item.title}
                </h3>
              </div>

              {/* Phone Frame Wrapper (Realistic phone aspect ratio, no browser chrome) */}
              <div
                className="relative bg-[#05070a] p-3 flex items-center justify-center cursor-pointer"
                onClick={() => setSelectedImage(item)}
              >
                <div className="relative rounded-2xl overflow-hidden border-2 border-[#1f283d] bg-black shadow-inner max-w-[280px] w-full">
                  <img
                    src={item.src}
                    alt={`${item.title} screen capture from physical Android device`}
                    loading="lazy"
                    className="w-full h-auto block object-cover group-hover:scale-[1.02] transition-transform duration-300"
                  />
                  <div className="absolute inset-0 bg-sky-500/0 group-hover:bg-sky-500/10 transition-colors flex items-center justify-center">
                    <span className="opacity-0 group-hover:opacity-100 transition-opacity px-3 py-1.5 rounded-full bg-slate-950/80 text-white text-xs font-mono border border-slate-700 shadow-md">
                      Inspect High-Res ↗
                    </span>
                  </div>
                </div>
              </div>

              {/* Card Footer Caption */}
              <div className="p-4 text-xs text-slate-400 leading-relaxed border-t border-[#182030] bg-[#0a0e16] flex-1">
                {item.description}
              </div>
            </div>
          ))}
        </div>

        {/* Secondary Gallery Header */}
        <div className="pt-8 border-t border-[#1a2130] mb-8 flex items-center justify-between flex-wrap gap-4">
          <div>
            <h3 className="text-lg font-semibold text-white">Full Application Workflow Gallery</h3>
            <p className="text-xs text-slate-400 mt-0.5">Complementary screens across the complete debugging lifecycle.</p>
          </div>
          <span className="text-xs font-mono text-slate-400 bg-[#0d121c] px-2.5 py-1 rounded border border-[#1e273a]">
            6 additional screens
          </span>
        </div>

        {/* Secondary 6-Grid Gallery */}
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-4">
          {secondaryScreenshots.map((item) => (
            <div
              key={item.id}
              onClick={() => setSelectedImage(item)}
              className="flex flex-col rounded-xl bg-[#0c1017] border border-[#1b2333] hover:border-sky-500/50 transition-all overflow-hidden cursor-pointer group"
            >
              <div className="p-2.5 bg-[#090d14] border-b border-[#182030]">
                <span className="text-[10px] font-mono text-sky-400 block truncate">{item.tag}</span>
                <h4 className="text-xs font-medium text-slate-200 group-hover:text-white truncate mt-0.5">
                  {item.title}
                </h4>
              </div>
              
              <div className="p-2 bg-[#05070a] flex items-center justify-center">
                <div className="rounded-lg overflow-hidden border border-[#1c2436] bg-black max-w-[130px] w-full">
                  <img
                    src={item.src}
                    alt={item.title}
                    loading="lazy"
                    className="w-full h-auto block object-cover group-hover:scale-105 transition-transform duration-200"
                  />
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* Modal / Lightbox for Full Inspection */}
        {selectedImage && (
          <div
            className="fixed inset-0 z-50 bg-black/85 backdrop-blur-md flex items-center justify-center p-4 sm:p-6"
            onClick={() => setSelectedImage(null)}
          >
            <div
              className="relative max-w-lg w-full max-h-[92vh] bg-[#0c1017] border border-[#26334a] rounded-2xl p-4 flex flex-col items-center shadow-2xl overflow-y-auto"
              onClick={(e) => e.stopPropagation()}
            >
              <div className="w-full flex items-center justify-between pb-3 mb-3 border-b border-[#1e273a]">
                <div>
                  <span className="text-xs font-mono text-sky-400 uppercase">{selectedImage.tag}</span>
                  <h3 className="text-base font-semibold text-white">{selectedImage.title}</h3>
                </div>
                <button
                  type="button"
                  onClick={() => setSelectedImage(null)}
                  className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition-colors focus:outline-none"
                  aria-label="Close modal"
                >
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <line x1="18" y1="6" x2="6" y2="18"></line>
                    <line x1="6" y1="6" x2="18" y2="18"></line>
                  </svg>
                </button>
              </div>

              <div className="rounded-xl overflow-hidden border-2 border-slate-700 bg-black max-w-[320px] shadow-2xl my-2">
                <img
                  src={selectedImage.src}
                  alt={selectedImage.title}
                  className="w-full h-auto block"
                />
              </div>

              <p className="mt-3 text-xs sm:text-sm text-slate-300 text-center leading-relaxed">
                {selectedImage.description}
              </p>
            </div>
          </div>
        )}

      </div>
    </section>
  );
};
