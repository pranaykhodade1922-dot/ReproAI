import React from 'react';

export const Differentiator: React.FC = () => {
  const features = [
    {
      name: 'Captures failure evidence',
      traditional: true,
      replay: true,
      automation: false,
      reproai: true,
    },
    {
      name: 'Shows user sequence',
      traditional: false,
      replay: true,
      automation: false,
      reproai: true,
    },
    {
      name: 'Executes a test scenario',
      traditional: false,
      replay: false,
      automation: true,
      reproai: true,
    },
    {
      name: 'Generates reusable reproduction',
      traditional: false,
      replay: false,
      automation: false,
      reproai: true,
    },
    {
      name: 'Reruns same scenario after fix',
      traditional: false,
      replay: false,
      automation: 'Partial',
      reproai: true,
    },
    {
      name: 'Requires positive fix evidence',
      traditional: false,
      replay: false,
      automation: false,
      reproai: true,
    },
  ];

  return (
    <section className="py-20 border-t border-[#1a2130] bg-[#080b10]">
      <div className="max-w-[1200px] mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Header */}
        <div className="text-center max-w-3xl mx-auto mb-16">
          <div className="text-xs font-mono text-sky-400 uppercase tracking-widest font-semibold mb-3">
            Ecosystem Comparison
          </div>
          <h2 className="text-3xl sm:text-4xl font-bold tracking-tight text-white">
            Capture is only the beginning.
          </h2>
          <p className="mt-3 text-slate-400 text-sm sm:text-base">
            ReproAI connects failure capture with executable reproduction and regression verification.
          </p>
        </div>

        {/* Comparison Table Container */}
        <div className="overflow-x-auto rounded-2xl border border-[#1e273a] bg-[#0c1017] shadow-xl">
          <table className="w-full text-left border-collapse min-w-[640px]">
            <thead>
              <tr className="border-b border-[#1b2333] bg-[#090d14] text-xs font-mono">
                <th className="py-4 px-6 text-slate-400 font-medium">Capability</th>
                <th className="py-4 px-4 text-slate-400 font-medium text-center">
                  Traditional crash reporting
                </th>
                <th className="py-4 px-4 text-slate-400 font-medium text-center">
                  Session replay
                </th>
                <th className="py-4 px-4 text-slate-400 font-medium text-center">
                  Test automation
                </th>
                <th className="py-4 px-6 text-sky-300 font-bold text-center bg-sky-950/40 border-x border-sky-800/40">
                  <div className="flex items-center justify-center gap-1.5">
                    <span className="w-1.5 h-1.5 rounded-full bg-emerald-400" />
                    ReproAI
                  </div>
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#171f2d] text-sm">
              {features.map((item) => (
                <tr key={item.name} className="hover:bg-[#101622]/50 transition-colors">
                  <td className="py-4 px-6 text-slate-200 font-medium text-xs sm:text-sm">
                    {item.name}
                  </td>
                  
                  {/* Traditional */}
                  <td className="py-4 px-4 text-center">
                    {item.traditional ? (
                      <span className="text-emerald-400 font-mono text-base">✓</span>
                    ) : (
                      <span className="text-slate-600 font-mono text-sm">✕</span>
                    )}
                  </td>

                  {/* Session Replay */}
                  <td className="py-4 px-4 text-center">
                    {item.replay ? (
                      <span className="text-emerald-400 font-mono text-base">✓</span>
                    ) : (
                      <span className="text-slate-600 font-mono text-sm">✕</span>
                    )}
                  </td>

                  {/* Test Automation */}
                  <td className="py-4 px-4 text-center">
                    {item.automation === 'Partial' ? (
                      <span className="text-amber-400 text-xs font-mono px-2 py-0.5 rounded bg-amber-950/40 border border-amber-800/30">
                        Partial
                      </span>
                    ) : item.automation ? (
                      <span className="text-emerald-400 font-mono text-base">✓</span>
                    ) : (
                      <span className="text-slate-600 font-mono text-sm">✕</span>
                    )}
                  </td>

                  {/* ReproAI */}
                  <td className="py-4 px-6 text-center bg-sky-950/20 border-x border-sky-800/30 font-semibold text-emerald-400">
                    <div className="flex items-center justify-center gap-1 font-mono">
                      <span>✓</span>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Core Message Callout */}
        <div className="mt-8 p-4 rounded-xl bg-[#0d121c] border border-[#1e273a] text-center">
          <p className="text-xs sm:text-sm text-slate-300">
            <span className="text-sky-400 font-semibold font-mono">ReproAI Advantage:</span> Traditional tools stop at recording the past. ReproAI transforms that trace into an active test that validates the future.
          </p>
        </div>

      </div>
    </section>
  );
};
