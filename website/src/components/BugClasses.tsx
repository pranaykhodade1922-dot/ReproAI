import React from 'react';

export const BugClasses: React.FC = () => {
  return (
    <section id="bug-classes" className="py-20 border-t border-[#1a2130] bg-[#07090d]">
      <div className="max-w-[1200px] mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Header */}
        <div className="text-center max-w-3xl mx-auto mb-16">
          <div className="text-xs font-mono text-emerald-400 uppercase tracking-widest font-semibold mb-3">
            Empirical Validation
          </div>
          <h2 className="text-3xl sm:text-4xl font-bold tracking-tight text-white">
            Validated across two independent failure classes.
          </h2>
          <p className="mt-3 text-sm sm:text-base text-slate-400">
            ReproAI is exercised against two fundamentally distinct mobile failure mechanisms to prove scenario generality.
          </p>
        </div>

        {/* 2 Large Cards */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          
          {/* CARD 1: Network / Authentication Retry */}
          <div className="rounded-2xl bg-[#0c1017] border border-[#1e273a] p-6 sm:p-8 flex flex-col justify-between hover:border-slate-500 transition-all shadow-xl">
            <div>
              {/* Header Badge & Title */}
              <div className="flex items-center justify-between gap-3 mb-4">
                <span className="text-xs font-mono px-2.5 py-1 rounded bg-sky-950/70 border border-sky-800/60 text-sky-300 font-medium">
                  Class 1 · Network & Auth
                </span>
                <div className="flex items-center gap-1.5">
                  <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-rose-950/60 border border-rose-800/50 text-rose-300 font-semibold">
                    BUG REPRODUCED
                  </span>
                  <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-emerald-950/60 border border-emerald-800/50 text-emerald-300 font-semibold">
                    FIX VERIFIED
                  </span>
                </div>
              </div>

              <h3 className="text-xl font-bold text-white mb-2">
                Network / Authentication Retry
              </h3>
              
              <div className="mb-6 p-3 rounded-lg bg-[#080c13] border border-[#192233]">
                <div className="text-[11px] font-mono text-slate-400 uppercase tracking-wider mb-1">Trigger</div>
                <div className="text-sm text-slate-200 font-medium">
                  Controlled network transition during payment
                </div>
              </div>

              {/* Comparison Split: Failure vs Fixed Evidence */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-6">
                
                {/* Failure Evidence */}
                <div className="p-4 rounded-xl bg-[#140e13] border border-rose-900/40">
                  <div className="text-[11px] font-mono text-rose-400 font-semibold uppercase tracking-wider mb-2.5 flex items-center gap-1.5">
                    <span className="w-1.5 h-1.5 rounded-full bg-rose-500" />
                    Failure evidence
                  </div>
                  <div className="space-y-1.5 font-mono text-xs text-rose-300/90">
                    <div className="p-1 rounded bg-rose-950/40 border border-rose-900/30">TOKEN_EXPIRED</div>
                    <div className="p-1 rounded bg-rose-950/40 border border-rose-900/30">HTTP 401</div>
                    <div className="p-1 rounded bg-rose-950/40 border border-rose-900/30 font-bold">PAYMENT_FAILED</div>
                  </div>
                </div>

                {/* Fixed Evidence */}
                <div className="p-4 rounded-xl bg-[#091512] border border-emerald-900/40">
                  <div className="text-[11px] font-mono text-emerald-400 font-semibold uppercase tracking-wider mb-2.5 flex items-center gap-1.5">
                    <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" />
                    Fixed evidence
                  </div>
                  <div className="space-y-1.5 font-mono text-xs text-emerald-300/90">
                    <div className="p-1 rounded bg-emerald-950/40 border border-emerald-900/30">TOKEN_REFRESHED</div>
                    <div className="p-1 rounded bg-emerald-950/40 border border-emerald-900/30">HTTP 200</div>
                    <div className="p-1 rounded bg-emerald-950/40 border border-emerald-900/30 font-bold">PAYMENT_SUCCESS</div>
                  </div>
                </div>

              </div>
            </div>

            {/* Bottom Label */}
            <div className="pt-4 border-t border-[#182030] flex items-center justify-between text-xs font-mono text-slate-400">
              <span className="text-slate-300 font-medium">Deterministic network/payment hook + real ADB execution</span>
            </div>
          </div>

          {/* CARD 2: Android Rotation / State Restoration */}
          <div className="rounded-2xl bg-[#0c1017] border border-[#1e273a] p-6 sm:p-8 flex flex-col justify-between hover:border-slate-500 transition-all shadow-xl">
            <div>
              {/* Header Badge & Title */}
              <div className="flex items-center justify-between gap-3 mb-4">
                <span className="text-xs font-mono px-2.5 py-1 rounded bg-cyan-950/70 border border-cyan-800/60 text-cyan-300 font-medium">
                  Class 2 · Lifecycle & State
                </span>
                <div className="flex items-center gap-1.5">
                  <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-rose-950/60 border border-rose-800/50 text-rose-300 font-semibold">
                    BUG REPRODUCED
                  </span>
                  <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-emerald-950/60 border border-emerald-800/50 text-emerald-300 font-semibold">
                    FIX VERIFIED
                  </span>
                </div>
              </div>

              <h3 className="text-xl font-bold text-white mb-2">
                Android Rotation / State Restoration
              </h3>
              
              <div className="mb-6 p-3 rounded-lg bg-[#080c13] border border-[#192233]">
                <div className="text-[11px] font-mono text-slate-400 uppercase tracking-wider mb-1">Trigger</div>
                <div className="text-sm text-slate-200 font-medium">
                  Portrait → Landscape during Checkout
                </div>
              </div>

              {/* Comparison Split: Failure vs Fixed Evidence */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-6">
                
                {/* Failure Evidence */}
                <div className="p-4 rounded-xl bg-[#140e13] border border-rose-900/40">
                  <div className="text-[11px] font-mono text-rose-400 font-semibold uppercase tracking-wider mb-2.5 flex items-center gap-1.5">
                    <span className="w-1.5 h-1.5 rounded-full bg-rose-500" />
                    Failure evidence
                  </div>
                  <div className="space-y-1.5 font-mono text-xs text-rose-300/90">
                    <div className="p-1 rounded bg-rose-950/40 border border-rose-900/30">CHECKOUT_STATE_LOST</div>
                    <div className="p-1 rounded bg-rose-950/40 border border-rose-900/30">CHECKOUT_INVALID</div>
                    <div className="p-1 rounded bg-rose-950/40 border border-rose-900/30 font-bold">PAYMENT_BLOCKED</div>
                  </div>
                </div>

                {/* Fixed Evidence */}
                <div className="p-4 rounded-xl bg-[#091512] border border-emerald-900/40">
                  <div className="text-[11px] font-mono text-emerald-400 font-semibold uppercase tracking-wider mb-2.5 flex items-center gap-1.5">
                    <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" />
                    Fixed evidence
                  </div>
                  <div className="space-y-1.5 font-mono text-xs text-emerald-300/90">
                    <div className="p-1 rounded bg-emerald-950/40 border border-emerald-900/30">CHECKOUT_STATE_RESTORED</div>
                    <div className="p-1 rounded bg-emerald-950/40 border border-emerald-900/30">CHECKOUT_VALID</div>
                    <div className="p-1 rounded bg-emerald-950/40 border border-emerald-900/30 font-bold">PAYMENT_AVAILABLE</div>
                  </div>
                </div>

              </div>
            </div>

            {/* Bottom Label */}
            <div className="pt-4 border-t border-[#182030] flex items-center justify-between text-xs font-mono text-slate-400">
              <span className="text-slate-300 font-medium">Real device rotation + real Activity recreation</span>
            </div>
          </div>

        </div>

        {/* Small Honesty Statement */}
        <div className="mt-8 text-center">
          <p className="text-xs text-slate-400 font-mono">
            These scenarios validate the architecture across multiple failure mechanisms; they do not imply support for every Android bug.
          </p>
        </div>

      </div>
    </section>
  );
};
