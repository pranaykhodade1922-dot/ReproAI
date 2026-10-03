import React from 'react';

export const Problem: React.FC = () => {
  return (
    <section className="py-20 border-t border-[#1a2130] bg-[#080b10]">
      <div className="max-w-[1200px] mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Section Heading */}
        <div className="max-w-3xl">
          <div className="text-xs font-mono text-rose-400 uppercase tracking-widest font-semibold mb-3">
            The Reproduction Gap
          </div>
          <h2 className="text-3xl sm:text-4xl font-bold tracking-tight text-white leading-tight">
            Bug reports tell you what failed.<br className="hidden sm:inline" /> Not how to reproduce it.
          </h2>
          <p className="mt-4 text-base text-slate-400 leading-relaxed">
            Mobile failures often depend on combinations of user actions, device state, lifecycle changes, network conditions, API responses and timing.
          </p>
          <p className="mt-2 text-base text-slate-400 leading-relaxed">
            Traditional bug reports may preserve a screenshot or stack trace, but developers still have to manually reconstruct the sequence that caused the failure.
          </p>
        </div>

        {/* 3 Compact Cards */}
        <div className="mt-12 grid grid-cols-1 md:grid-cols-3 gap-6">
          
          {/* Card 1 */}
          <div className="rounded-xl bg-[#0d121c] border border-[#1e273a] p-6 flex flex-col justify-between hover:border-slate-600 transition-colors">
            <div>
              <div className="w-10 h-10 rounded-lg bg-rose-950/40 border border-rose-800/40 flex items-center justify-center text-rose-400 mb-5">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <circle cx="12" cy="12" r="10" />
                  <line x1="12" y1="8" x2="12" y2="12" />
                  <line x1="12" y1="16" x2="12.01" y2="16" />
                </svg>
              </div>
              <h3 className="text-lg font-semibold text-white">Missing context</h3>
              <p className="mt-3 text-sm text-slate-400 leading-relaxed">
                “Payment failed” does not explain what happened before the failure.
              </p>
            </div>
            <div className="mt-6 pt-4 border-t border-[#182030] text-xs font-mono text-slate-400">
              Isolated errors lack sequence
            </div>
          </div>

          {/* Card 2 */}
          <div className="rounded-xl bg-[#0d121c] border border-[#1e273a] p-6 flex flex-col justify-between hover:border-slate-600 transition-colors">
            <div>
              <div className="w-10 h-10 rounded-lg bg-amber-950/40 border border-amber-800/40 flex items-center justify-center text-amber-400 mb-5">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M12 20h9" />
                  <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z" />
                </svg>
              </div>
              <h3 className="text-lg font-semibold text-white">Manual reproduction</h3>
              <p className="mt-3 text-sm text-slate-400 leading-relaxed">
                Developers repeatedly attempt to recreate the issue by hand.
              </p>
            </div>
            <div className="mt-6 pt-4 border-t border-[#182030] text-xs font-mono text-slate-400">
              Unreliable trial-and-error
            </div>
          </div>

          {/* Card 3 */}
          <div className="rounded-xl bg-[#0d121c] border border-[#1e273a] p-6 flex flex-col justify-between hover:border-slate-600 transition-colors">
            <div>
              <div className="w-10 h-10 rounded-lg bg-sky-950/40 border border-sky-800/40 flex items-center justify-center text-sky-400 mb-5">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M18 6L6 18" />
                  <path d="M6 6l12 12" />
                </svg>
              </div>
              <h3 className="text-lg font-semibold text-white">Weak verification</h3>
              <p className="mt-3 text-sm text-slate-400 leading-relaxed">
                A disappearing error does not prove the fix actually works.
              </p>
            </div>
            <div className="mt-6 pt-4 border-t border-[#182030] text-xs font-mono text-slate-400">
              No healthy-state proof
            </div>
          </div>

        </div>

      </div>
    </section>
  );
};
