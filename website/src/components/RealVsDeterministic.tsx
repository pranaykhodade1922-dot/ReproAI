import React from 'react';

export const RealVsDeterministic: React.FC = () => {
  const realItems = [
    'physical Android device',
    'ADB execution',
    'actual device rotation',
    'Activity recreation',
    'event capture',
    'runner communication',
    'Room persistence',
    'failure assertions',
    'fix verification',
    'incident reports',
  ];

  const controlledItems = [
    'DemoShop payment service',
    'network-transition demo hook',
    'deliberately buggy/fixed modes',
  ];

  const notImplemented = [
    'Physical Wi-Fi → cellular modem switching',
    'Production payment gateway validation',
    'Arbitrary third-party app reproduction',
    'Actual iQOO hardware validation',
  ];

  return (
    <section className="py-20 border-t border-[#1a2130] bg-[#080b10]">
      <div className="max-w-[1200px] mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Header */}
        <div className="text-center max-w-3xl mx-auto mb-16">
          <div className="text-xs font-mono text-emerald-400 uppercase tracking-widest font-semibold mb-3">
            Architectural Transparency
          </div>
          <h2 className="text-3xl sm:text-4xl font-bold tracking-tight text-white">
            What is real?
          </h2>
          <p className="mt-3 text-slate-400 text-sm sm:text-base">
            A precise breakdown of physical device automation versus controlled prototype fixtures.
          </p>
        </div>

        {/* Two Columns Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          
          {/* Column 1: REAL */}
          <div className="rounded-2xl bg-[#0c1017] border border-emerald-900/40 p-6 sm:p-8 flex flex-col justify-between shadow-xl">
            <div>
              <div className="flex items-center justify-between pb-3 mb-6 border-b border-[#182329]">
                <div className="flex items-center gap-2.5">
                  <span className="w-2.5 h-2.5 rounded-full bg-emerald-400 animate-pulse" />
                  <h3 className="text-lg font-bold text-white tracking-wide">REAL</h3>
                </div>
                <span className="text-xs font-mono px-2.5 py-0.5 rounded bg-emerald-950/60 text-emerald-300 border border-emerald-800/40">
                  Physical Execution
                </span>
              </div>

              <ul className="space-y-3 font-mono text-xs sm:text-sm">
                {realItems.map((item) => (
                  <li key={item} className="flex items-center gap-3 text-slate-200">
                    <span className="text-emerald-400 font-bold">✓</span>
                    <span>{item}</span>
                  </li>
                ))}
              </ul>
            </div>

            <div className="mt-8 pt-4 border-t border-[#182030] text-xs font-mono text-slate-400">
              Exercised on connected physical Android hardware
            </div>
          </div>

          {/* Column 2: CONTROLLED / DETERMINISTIC */}
          <div className="rounded-2xl bg-[#0c1017] border border-sky-900/40 p-6 sm:p-8 flex flex-col justify-between shadow-xl">
            <div>
              <div className="flex items-center justify-between pb-3 mb-6 border-b border-[#1b2536]">
                <div className="flex items-center gap-2.5">
                  <span className="w-2.5 h-2.5 rounded-full bg-sky-400" />
                  <h3 className="text-lg font-bold text-white tracking-wide">CONTROLLED / DETERMINISTIC</h3>
                </div>
                <span className="text-xs font-mono px-2.5 py-0.5 rounded bg-sky-950/60 text-sky-300 border border-sky-800/40">
                  Demo Environment
                </span>
              </div>

              <ul className="space-y-3 font-mono text-xs sm:text-sm">
                {controlledItems.map((item) => (
                  <li key={item} className="flex items-center gap-3 text-slate-200">
                    <span className="text-sky-400 font-bold">●</span>
                    <span>{item}</span>
                  </li>
                ))}
              </ul>

              <p className="mt-6 text-xs text-slate-400 leading-relaxed bg-[#080d15] p-3.5 rounded-lg border border-[#1b2538]">
                Deterministic hooks ensure predictable demonstration of network state transitions and reproducible failure conditions without requiring flaky external network manipulation.
              </p>
            </div>

            <div className="mt-8 pt-4 border-t border-[#182030] text-xs font-mono text-slate-400">
              Predictable prototype instrumentation
            </div>
          </div>

        </div>

        {/* Small: Not currently implemented */}
        <div className="mt-8 rounded-xl bg-[#0b0e14] border border-[#1c2333] p-5">
          <div className="flex items-center gap-2 mb-3">
            <span className="text-xs font-mono uppercase tracking-wider text-slate-400 font-semibold">
              Not currently implemented:
            </span>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 text-xs font-mono text-slate-400">
            {notImplemented.map((item) => (
              <div key={item} className="flex items-center gap-2 p-2 rounded bg-[#0f141f] border border-[#1a2130]">
                <span className="text-slate-500">—</span>
                <span>{item}</span>
              </div>
            ))}
          </div>
          <p className="mt-3 text-[11px] text-slate-500">
            Scope is explicitly bounded for transparent evaluation during hackathon judging.
          </p>
        </div>

      </div>
    </section>
  );
};
