import React from 'react';

export const Security: React.FC = () => {
  const cards = [
    {
      title: 'Sanitized telemetry',
      desc: 'Sensitive tokens, credentials, emails and phone numbers are redacted from selected events/exports.',
      tag: 'Data Privacy',
      icon: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
          <path d="M7 11V7a5 5 0 0 1 10 0v4" />
        </svg>
      ),
    },
    {
      title: 'Allow-listed execution',
      desc: 'TestScenario actions cannot execute arbitrary shell commands.',
      tag: 'Bounded Actions',
      icon: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
        </svg>
      ),
    },
    {
      title: 'Safe subprocess execution',
      desc: 'ADB wrappers use argument arrays and shell=False.',
      tag: 'Subprocess Guard',
      icon: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <polyline points="4 17 10 11 4 5" />
          <line x1="12" y1="19" x2="20" y2="19" />
        </svg>
      ),
    },
    {
      title: 'Debug-only automation',
      desc: 'Demo automation components are excluded from the release manifest.',
      tag: 'Manifest Safety',
      icon: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <polygon points="12 2 2 7 12 12 22 7 12 2" />
          <polyline points="2 17 12 22 22 17" />
          <polyline points="2 12 12 17 22 12" />
        </svg>
      ),
    },
  ];

  return (
    <section className="py-20 border-t border-[#1a2130] bg-[#080b10]">
      <div className="max-w-[1200px] mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Header */}
        <div className="text-center max-w-3xl mx-auto mb-16">
          <div className="text-xs font-mono text-emerald-400 uppercase tracking-widest font-semibold mb-3">
            Safety Guardrails
          </div>
          <h2 className="text-3xl sm:text-4xl font-bold tracking-tight text-white">
            Built with controlled execution.
          </h2>
          <p className="mt-3 text-slate-400 text-sm sm:text-base">
            Engineered with strict bounds on phone commands, process invocation, and sensitive telemetry sanitization.
          </p>
        </div>

        {/* 4 Cards Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {cards.map((card) => (
            <div
              key={card.title}
              className="rounded-2xl bg-[#0c1017] border border-[#1e273a] p-6 flex flex-col justify-between hover:border-slate-500 transition-colors shadow-lg"
            >
              <div>
                <div className="w-10 h-10 rounded-lg bg-[#141b28] border border-[#233148] flex items-center justify-center text-sky-400 mb-5">
                  {card.icon}
                </div>
                <div className="text-[10px] font-mono uppercase text-sky-400 font-semibold mb-1">
                  {card.tag}
                </div>
                <h3 className="text-base font-bold text-white mb-2">{card.title}</h3>
                <p className="text-xs text-slate-400 leading-relaxed">{card.desc}</p>
              </div>

              <div className="mt-6 pt-3 border-t border-[#182030] text-[10px] font-mono text-slate-400">
                Enforced by design
              </div>
            </div>
          ))}
        </div>

        {/* Small Disclaimer */}
        <div className="mt-8 text-center">
          <p className="text-xs text-slate-400 font-mono">
            Prototype security controls are not a complete production security model.
          </p>
        </div>

      </div>
    </section>
  );
};
