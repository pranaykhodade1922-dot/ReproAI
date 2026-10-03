import React from 'react';

export const TechStack: React.FC = () => {
  const stack = [
    {
      category: 'Android Host App',
      tag: 'Client Core',
      techs: ['Kotlin', 'Jetpack Compose', 'Room Database', 'Kotlin Coroutines', 'StateFlow', 'Retrofit / OkHttp'],
      desc: 'Modern Android client with unidirectional state flow, real-time UI, and persistent incident tracking.',
    },
    {
      category: 'repro-sdk',
      tag: 'Instrumentation',
      techs: ['Reusable Android library', 'Package-restricted event transport', 'Broadcast Intents', 'Zero-overhead telemetry'],
      desc: 'Embeddable SDK with secure intent broadcasting and zero runtime disruption to the host app.',
    },
    {
      category: 'Analysis Engine',
      tag: 'Diagnosis',
      techs: ['Typed analysis models', 'Provider abstraction', 'Deterministic local rules', 'Multi-signal correlation'],
      desc: 'Pluggable provider architecture with deterministic rule evaluation for robust failure pattern recognition.',
    },
    {
      category: 'Repro Runner',
      tag: 'Test Execution',
      techs: ['Python 3.12', 'FastAPI', 'Pydantic v2', 'WebSocket streaming', 'ADB Client (shell=False)'],
      desc: 'High-performance local orchestrator commanding the phone via Android Debug Bridge with strict safety constraints.',
    },
    {
      category: 'Testing Suite',
      tag: 'Quality Gates',
      techs: ['pytest (51 passed)', 'JUnit (31 passed)', 'Android UI instrumentation (33 passed)', 'Contract validation'],
      desc: '115 total verified tests spanning unit, integration, mock-runner contracts, and on-device UI flows.',
    },
  ];

  return (
    <section className="py-20 border-t border-[#1a2130] bg-[#080b10]">
      <div className="max-w-[1200px] mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Header */}
        <div className="text-center max-w-3xl mx-auto mb-16">
          <div className="text-xs font-mono text-sky-400 uppercase tracking-widest font-semibold mb-3">
            Engineering Foundation
          </div>
          <h2 className="text-3xl sm:text-4xl font-bold tracking-tight text-white">
            Built with modern developer infrastructure.
          </h2>
          <p className="mt-3 text-slate-400 text-sm sm:text-base">
            Type-safe, robust, and cleanly separated across client telemetry, scenario typing, and test execution.
          </p>
        </div>

        {/* 5 Stack Cards Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {stack.map((item) => (
            <div
              key={item.category}
              className="rounded-2xl bg-[#0c1017] border border-[#1e273a] p-6 flex flex-col justify-between hover:border-slate-500 transition-colors shadow-lg"
            >
              <div>
                <div className="flex items-center justify-between mb-3">
                  <span className="text-[11px] font-mono px-2 py-0.5 rounded bg-sky-950/60 border border-sky-800/50 text-sky-300">
                    {item.tag}
                  </span>
                </div>
                <h3 className="text-lg font-bold text-white mb-2">{item.category}</h3>
                <p className="text-xs text-slate-400 leading-relaxed mb-4">{item.desc}</p>
                
                {/* Tech Pills */}
                <div className="flex flex-wrap gap-1.5">
                  {item.techs.map((tech) => (
                    <span
                      key={tech}
                      className="text-xs font-mono px-2.5 py-1 rounded-md bg-[#121824] border border-[#202b3d] text-slate-300"
                    >
                      {tech}
                    </span>
                  ))}
                </div>
              </div>

              <div className="mt-6 pt-3 border-t border-[#182030] text-[11px] font-mono text-slate-400">
                Production-grade tooling
              </div>
            </div>
          ))}
        </div>

      </div>
    </section>
  );
};
