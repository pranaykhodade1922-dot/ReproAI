import React from 'react';

export const HowItWorks: React.FC = () => {
  const steps = [
    {
      number: '01',
      title: 'Capture',
      desc: 'Record device, lifecycle and instrumented app events.',
      detail: 'Broadcast intent ingestion via repro-sdk & Room persistence',
      tag: 'On-Device',
    },
    {
      number: '02',
      title: 'Diagnose',
      desc: 'Correlate the captured evidence and identify a supported failure pattern.',
      detail: 'Local rule engine correlates events, network errors & orientation',
      tag: 'Analysis',
    },
    {
      number: '03',
      title: 'Generate',
      desc: 'Create reproduction steps and a typed TestScenario.',
      detail: 'Deterministic scenario model with strict schema validation',
      tag: 'Typed Spec',
    },
    {
      number: '04',
      title: 'Reproduce',
      desc: 'Execute the scenario through the laptop Repro Runner and ADB.',
      detail: 'FastAPI runner issues safe ADB commands and asserts failure',
      tag: 'ADB Runner',
    },
    {
      number: '05',
      title: 'Verify',
      desc: 'Rerun the exact same scenario after the fix and require positive healthy-state evidence.',
      detail: 'Ensures healthy state assertions pass on identical test JSON',
      tag: 'Verification',
    },
  ];

  return (
    <section id="how-it-works" className="py-20 border-t border-[#1a2130] bg-[#07090d]">
      <div className="max-w-[1200px] mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Header */}
        <div className="text-center max-w-3xl mx-auto">
          <div className="text-xs font-mono text-sky-400 uppercase tracking-widest font-semibold mb-3">
            Workflow Pipeline
          </div>
          <h2 className="text-3xl sm:text-4xl font-bold tracking-tight text-white">
            From failure to verified fix.
          </h2>
          <p className="mt-3 text-slate-400 text-base">
            An automated, closed-loop pipeline connecting mobile evidence collection to repeatable verification.
          </p>
        </div>

        {/* 5-Step Flow Grid */}
        <div className="mt-16 grid grid-cols-1 md:grid-cols-5 gap-4 relative">
          {steps.map((step, idx) => (
            <div
              key={step.number}
              className="rounded-xl bg-[#0d121c] border border-[#1e273a] p-5 flex flex-col justify-between hover:border-sky-500/40 transition-all group relative"
            >
              <div>
                <div className="flex items-center justify-between mb-4">
                  <span className="text-2xl font-mono font-bold text-sky-400/80 group-hover:text-sky-300 transition-colors">
                    {step.number}
                  </span>
                  <span className="text-[10px] font-mono uppercase px-2 py-0.5 rounded bg-[#161f30] text-slate-300 border border-[#23314c]">
                    {step.tag}
                  </span>
                </div>
                <h3 className="text-lg font-semibold text-white group-hover:text-sky-200 transition-colors">
                  {step.title}
                </h3>
                <p className="mt-2 text-xs sm:text-sm text-slate-400 leading-relaxed">
                  {step.desc}
                </p>
              </div>

              <div className="mt-6 pt-3 border-t border-[#182030] text-[11px] font-mono text-slate-400">
                {step.detail}
              </div>

              {/* Connecting line arrow for desktop */}
              {idx < steps.length - 1 && (
                <div className="hidden lg:block absolute -right-3 top-1/2 -translate-y-1/2 z-10 text-slate-400 pointer-events-none">
                  →
                </div>
              )}
            </div>
          ))}
        </div>

        {/* Important Highlighted Statement */}
        <div className="mt-12 rounded-xl bg-gradient-to-r from-sky-950/40 via-[#0e1726] to-emerald-950/40 border border-sky-500/30 p-5 sm:p-6 text-center shadow-lg">
          <div className="flex items-center justify-center gap-2 mb-2">
            <span className="w-2 h-2 rounded-full bg-emerald-400" />
            <span className="text-xs font-mono uppercase tracking-wider text-emerald-300 font-semibold">
              Core Architectural Invariant
            </span>
          </div>
          <p className="text-base sm:text-lg font-semibold text-white">
            The same scenario is used for reproduction and fix verification.
          </p>
          <p className="mt-1 text-xs sm:text-sm text-slate-400 max-w-2xl mx-auto">
            ReproAI does not write a separate assertion suite for regression testing. The identical scenario that proved the defect is re-executed to prove the resolution.
          </p>
        </div>

      </div>
    </section>
  );
};
