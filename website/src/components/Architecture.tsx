import React from 'react';

export const Architecture: React.FC = () => {
  const mainPipeline = [
    { title: 'DemoShop', role: 'Target Application', badge: 'App Under Test', color: 'border-slate-700 bg-slate-900/60' },
    { title: 'repro-sdk', role: 'Instrumentation Client', badge: 'Broadcast Transport', color: 'border-sky-800/60 bg-sky-950/30 text-sky-300' },
    { title: 'ReproAI Android', role: 'Telemetry Coordinator', badge: 'Local Host App', color: 'border-sky-700/60 bg-sky-950/40 text-sky-200' },
    { title: 'Captured Event Timeline', role: 'Structured History', badge: 'Chronological Trace', color: 'border-teal-800/60 bg-teal-950/30 text-teal-300' },
    { title: 'Analyzer', role: 'Deterministic Rule Engine', badge: 'Pattern Matcher', color: 'border-indigo-800/60 bg-indigo-950/30 text-indigo-300' },
    { title: 'Typed TestScenario', role: 'Validated Test Spec', badge: 'Pydantic Model', color: 'border-cyan-800/60 bg-cyan-950/30 text-cyan-300' },
    { title: 'FastAPI Repro Runner', role: 'Local Test Server', badge: 'Python 3.12 / WS', color: 'border-emerald-800/60 bg-emerald-950/30 text-emerald-300' },
    { title: 'ADB', role: 'Android Debug Bridge', badge: 'Command Dispatcher', color: 'border-amber-800/60 bg-amber-950/30 text-amber-300' },
    { title: 'Android Device', role: 'Physical Phone Target', badge: 'Real Execution', color: 'border-sky-800/60 bg-sky-950/30 text-sky-300' },
    { title: 'ExecutionResult', role: 'Structured Outcome', badge: 'Assertions & Proof', color: 'border-emerald-700/60 bg-emerald-950/40 text-emerald-200' },
    { title: 'Incident Report', role: 'Exportable Bundle', badge: 'ZIP / Markdown / JSON', color: 'border-purple-800/60 bg-purple-950/30 text-purple-300' },
  ];

  const persistenceEntities = [
    { name: 'Sessions', desc: 'Active capture runs & hardware metadata' },
    { name: 'Events', desc: 'Raw & sanitized telemetry stream' },
    { name: 'Analysis', desc: 'Diagnostic hypotheses & rule hits' },
    { name: 'Reports', desc: 'Exportable post-mortem records' },
  ];

  return (
    <section id="architecture" className="py-20 border-t border-[#1a2130] bg-[#07090d]">
      <div className="max-w-[1200px] mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Header */}
        <div className="text-center max-w-3xl mx-auto mb-16">
          <div className="text-xs font-mono text-sky-400 uppercase tracking-widest font-semibold mb-3">
            System Topology
          </div>
          <h2 className="text-3xl sm:text-4xl font-bold tracking-tight text-white">
            Built as a complete debugging pipeline.
          </h2>
          <p className="mt-3 text-slate-400 text-sm sm:text-base">
            End-to-end data flow spanning Android client instrumentation, local rule diagnosis, laptop test orchestration, and persistent local storage.
          </p>
        </div>

        {/* Main Flow Container */}
        <div className="rounded-2xl border border-[#1e273a] bg-[#0c1017] p-6 sm:p-8 shadow-xl">
          
          <div className="mb-6 flex items-center justify-between pb-4 border-b border-[#182030]">
            <div className="flex items-center gap-2">
              <span className="w-2.5 h-2.5 rounded-full bg-sky-400" />
              <h3 className="text-sm font-mono uppercase tracking-wider text-slate-200 font-semibold">
                Execution Pipeline Sequence
              </h3>
            </div>
            <span className="text-xs font-mono text-slate-400">11 Pipeline Stages</span>
          </div>

          {/* Sequential Grid Layout */}
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            {mainPipeline.map((node, index) => (
              <div
                key={node.title}
                className={`p-4 rounded-xl border ${node.color} flex flex-col justify-between relative group hover:border-slate-400 transition-colors`}
              >
                <div>
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-[10px] font-mono text-slate-400">Stage {String(index + 1).padStart(2, '0')}</span>
                    <span className="text-[9px] font-mono uppercase px-1.5 py-0.5 rounded bg-black/40 border border-white/10 text-slate-300">
                      {node.badge}
                    </span>
                  </div>
                  <h4 className="text-sm font-bold text-white tracking-tight">{node.title}</h4>
                  <p className="text-xs text-slate-400 mt-1">{node.role}</p>
                </div>

                {index < mainPipeline.length - 1 && (
                  <div className="mt-3 pt-2 border-t border-white/5 flex items-center justify-end text-slate-400 font-mono text-xs">
                    <span>next</span>
                    <span className="ml-1 text-sky-400">→</span>
                  </div>
                )}
              </div>
            ))}
          </div>

          {/* Room Persistence Subsystem Box */}
          <div className="mt-10 pt-8 border-t border-[#182030]">
            <div className="rounded-xl bg-[#090d14] border border-[#1b2538] p-5 sm:p-6">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-6 pb-4 border-b border-[#161f30]">
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 rounded-lg bg-emerald-950/40 border border-emerald-800/40 flex items-center justify-center text-emerald-400">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <ellipse cx="12" cy="5" rx="9" ry="3"></ellipse>
                      <path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"></path>
                      <path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"></path>
                    </svg>
                  </div>
                  <div>
                    <h4 className="text-base font-bold text-white">Room Persistence Layer</h4>
                    <p className="text-xs text-slate-400">Embedded SQLite storage with type-safe schema migrations</p>
                  </div>
                </div>
                <div className="flex items-center gap-2 text-xs font-mono text-emerald-400 bg-emerald-950/30 px-3 py-1 rounded border border-emerald-800/30 self-start sm:self-auto">
                  <span>Persistent State</span>
                </div>
              </div>

              {/* 4 Connected Entities */}
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                {persistenceEntities.map((entity) => (
                  <div key={entity.name} className="p-3.5 rounded-lg bg-[#0e1420] border border-[#1d273a]">
                    <div className="flex items-center gap-2 text-sky-300 font-mono text-xs font-semibold">
                      <span className="w-1.5 h-1.5 rounded-full bg-emerald-400" />
                      {entity.name}
                    </div>
                    <p className="mt-1.5 text-xs text-slate-400 leading-relaxed">
                      {entity.desc}
                    </p>
                  </div>
                ))}
              </div>
            </div>
          </div>

        </div>

      </div>
    </section>
  );
};
