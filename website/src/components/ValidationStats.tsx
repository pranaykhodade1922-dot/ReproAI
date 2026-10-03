import React from 'react';

export const ValidationStats: React.FC = () => {
  const stats = [
    { value: '51', label: 'Backend tests', sub: 'pytest suite' },
    { value: '31', label: 'Android unit tests', sub: 'JUnit 4 / Robolectric' },
    { value: '33', label: 'Phone UI tests', sub: 'On-device instrumentation' },
    { value: '2', label: 'Independent failure classes', sub: 'Network & Rotation' },
  ];

  return (
    <section id="validation" className="py-20 border-t border-[#1a2130] bg-[#07090d]">
      <div className="max-w-[1200px] mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Header */}
        <div className="text-center max-w-3xl mx-auto mb-16">
          <div className="text-xs font-mono text-sky-400 uppercase tracking-widest font-semibold mb-3">
            Validation Metrics
          </div>
          <h2 className="text-3xl sm:text-4xl font-bold tracking-tight text-white">
            115 automated and on-device tests.
          </h2>
          <p className="mt-3 text-slate-400 text-sm sm:text-base">
            Empirically measured test counts verifying contract compliance, safety boundaries, and end-to-end device execution.
          </p>
        </div>

        {/* 4 Stats Cards Strip */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6">
          {stats.map((stat) => (
            <div
              key={stat.label}
              className="rounded-2xl bg-[#0c1017] border border-[#1e273a] p-6 text-center hover:border-slate-500 transition-colors shadow-lg"
            >
              <div className="text-4xl sm:text-5xl font-extrabold font-mono text-white tracking-tight">
                {stat.value}
              </div>
              <div className="mt-2 text-sm font-semibold text-slate-200">
                {stat.label}
              </div>
              <div className="mt-1 text-xs font-mono text-slate-500">
                {stat.sub}
              </div>
            </div>
          ))}
        </div>

        {/* Verification Statements Below */}
        <div className="mt-10 rounded-xl bg-[#090d14] border border-[#1b2538] p-6 text-center space-y-2">
          <div className="flex items-center justify-center gap-2 text-emerald-400 font-medium text-sm sm:text-base">
            <span className="w-2 h-2 rounded-full bg-emerald-400" />
            <span>Both ReproAI and DemoShop debug builds verified.</span>
          </div>
          <div className="flex items-center justify-center gap-2 text-slate-300 text-xs sm:text-sm">
            <span className="w-1.5 h-1.5 rounded-full bg-sky-400" />
            <span>Both failure scenarios reproduced and fixes verified using unchanged scenarios.</span>
          </div>
        </div>

      </div>
    </section>
  );
};
