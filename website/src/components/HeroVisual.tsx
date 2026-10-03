import React from 'react';

export const HeroVisual: React.FC = () => {
  return (
    <section className="pb-20 max-w-[1200px] mx-auto px-4 sm:px-6 lg:px-8">
      {/* Dev Container Shell */}
      <div className="rounded-2xl border border-[#202738] bg-[#0c1017]/90 shadow-2xl shadow-sky-950/20 overflow-hidden">
        {/* Terminal / Tool Header */}
        <div className="px-4 py-3 bg-[#090d14] border-b border-[#1b2333] flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="w-3 h-3 rounded-full bg-slate-700/60 inline-block" />
            <span className="w-3 h-3 rounded-full bg-slate-700/60 inline-block" />
            <span className="w-3 h-3 rounded-full bg-slate-700/60 inline-block" />
            <span className="ml-2 text-xs font-mono text-slate-400">reproai-inspector // execution-pipeline</span>
          </div>
          <div className="flex items-center gap-3">
            <span className="text-[11px] font-mono text-emerald-400/90 flex items-center gap-1.5 bg-emerald-950/40 px-2 py-0.5 rounded border border-emerald-800/40">
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />
              ADB: CONNECTED
            </span>
            <span className="text-[11px] font-mono text-slate-400 hidden sm:inline">DEVICE: physical-android</span>
          </div>
        </div>

        {/* Inner Content Grid */}
        <div className="p-4 sm:p-6 lg:p-8 grid grid-cols-1 lg:grid-cols-12 gap-6 items-center">
          
          {/* LEFT: Captured Event Timeline */}
          <div className="lg:col-span-5 rounded-xl bg-[#090d13] border border-[#1d2537] p-4 sm:p-5 flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between pb-3 mb-3 border-b border-[#182030]">
                <div className="flex items-center gap-2">
                  <div className="w-2 h-2 rounded-full bg-amber-400" />
                  <span className="text-xs font-semibold tracking-wider uppercase text-slate-300">Captured Event Timeline</span>
                </div>
                <span className="text-[11px] font-mono text-slate-400">5 events recorded</span>
              </div>

              {/* Event Stack */}
              <div className="space-y-2.5 font-mono text-xs">
                <div className="flex items-center justify-between p-2 rounded bg-[#0f141f] border border-[#1b2333]">
                  <span className="text-slate-400">12:04:31</span>
                  <span className="text-slate-200 font-medium">PAY_BUTTON_CLICKED</span>
                  <span className="text-[10px] text-slate-400 px-1.5 py-0.5 rounded bg-slate-800">UI</span>
                </div>

                <div className="flex items-center justify-between p-2 rounded bg-[#0f141f] border border-[#1b2333]">
                  <span className="text-slate-400">12:04:32</span>
                  <span className="text-amber-300 font-medium">PAYMENT_RETRY</span>
                  <span className="text-[10px] text-amber-500/80 px-1.5 py-0.5 rounded bg-amber-950/30">RETRY</span>
                </div>

                <div className="flex items-center justify-between p-2 rounded bg-[#0f141f] border border-[#1b2333]">
                  <span className="text-slate-400">12:04:32</span>
                  <span className="text-rose-400 font-medium">TOKEN_EXPIRED</span>
                  <span className="text-[10px] text-rose-400/80 px-1.5 py-0.5 rounded bg-rose-950/30">AUTH</span>
                </div>

                <div className="flex items-center justify-between p-2 rounded bg-[#13111b] border border-rose-900/40">
                  <span className="text-slate-400">12:04:33</span>
                  <span className="text-rose-300 font-bold">HTTP 401</span>
                  <span className="text-[10px] text-rose-300 px-1.5 py-0.5 rounded bg-rose-900/50">NETWORK</span>
                </div>

                <div className="flex items-center justify-between p-2 rounded bg-[#170e14] border border-rose-700/50 shadow-sm shadow-rose-950/30">
                  <span className="text-rose-400">12:04:33</span>
                  <span className="text-rose-200 font-bold">PAYMENT_FAILED</span>
                  <span className="text-[10px] text-rose-400 px-1.5 py-0.5 rounded bg-rose-950 font-bold">FAILURE</span>
                </div>
              </div>
            </div>

            <div className="mt-4 pt-3 border-t border-[#182030] flex items-center justify-between text-[11px] text-slate-400">
              <span>Source: <code className="text-slate-300">repro-sdk</code></span>
              <span>Session: <code className="text-slate-300">SES-4091A</code></span>
            </div>
          </div>

          {/* CENTER: Pipeline Arrow & Transformation */}
          <div className="lg:col-span-2 flex flex-col items-center justify-center py-2 lg:py-0">
            <div className="flex flex-col items-center text-center gap-2">
              <span className="text-xs font-medium text-slate-400 uppercase tracking-wider">Captured failure</span>
              
              <div className="w-9 h-9 rounded-full bg-[#131a26] border border-sky-500/30 flex items-center justify-center text-sky-400 shadow-sm shadow-sky-500/10">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" className="rotate-90 lg:rotate-0">
                  <line x1="5" y1="12" x2="19" y2="12"></line>
                  <polyline points="12 5 19 12 12 19"></polyline>
                </svg>
              </div>

              <div className="px-2.5 py-1 rounded bg-sky-950/40 border border-sky-800/40 text-[11px] font-mono text-sky-300">
                TestScenario
              </div>
            </div>
          </div>

          {/* RIGHT: Reproduction & Same-Scenario Fix Verification */}
          <div className="lg:col-span-5 space-y-4">
            
            {/* Box 1: Reproduction Run */}
            <div className="rounded-xl bg-[#090d13] border border-[#1d2537] p-4 sm:p-5">
              <div className="flex items-center justify-between pb-2 mb-3 border-b border-[#182030]">
                <div className="flex items-center gap-2">
                  <span className="text-xs font-semibold tracking-wider uppercase text-slate-300">1. Runner Execution (ADB)</span>
                </div>
                <span className="text-[10px] font-mono font-semibold px-2 py-0.5 rounded bg-rose-950/70 border border-rose-700/50 text-rose-300">
                  BUG REPRODUCED
                </span>
              </div>

              <div className="space-y-1.5 font-mono text-xs">
                <div className="flex items-center gap-2 text-slate-300">
                  <span className="text-slate-400">1.</span>
                  <span className="text-sky-300 font-semibold">OPEN_SCREEN</span>
                  <span className="text-slate-400">Checkout</span>
                </div>
                <div className="flex items-center gap-2 text-slate-300">
                  <span className="text-slate-400">2.</span>
                  <span className="text-sky-300 font-semibold">CHANGE_NETWORK</span>
                  <span className="text-slate-400">CELLULAR</span>
                </div>
                <div className="flex items-center gap-2 text-slate-300">
                  <span className="text-slate-400">3.</span>
                  <span className="text-sky-300 font-semibold">TAP</span>
                  <span className="text-slate-400">PAY</span>
                </div>
                <div className="flex items-center gap-2 text-rose-300">
                  <span className="text-slate-400">4.</span>
                  <span className="font-semibold text-rose-400">ASSERT_API_STATUS</span>
                  <span className="px-1 bg-rose-950/60 rounded border border-rose-900/60">401</span>
                </div>
                <div className="flex items-center gap-2 text-rose-300">
                  <span className="text-slate-400">5.</span>
                  <span className="font-semibold text-rose-400">ASSERT_EVENT</span>
                  <span className="px-1 bg-rose-950/60 rounded border border-rose-900/60">PAYMENT_FAILED</span>
                </div>
              </div>
            </div>

            {/* Same Scenario Divider */}
            <div className="flex items-center justify-center gap-2 text-center">
              <div className="h-px bg-slate-800 flex-1" />
              <div className="inline-flex items-center gap-1.5 px-3 py-0.5 rounded-full bg-[#111724] border border-[#212c41] text-[11px] font-mono text-slate-300">
                <span className="text-sky-400 font-bold">↓</span> SAME SCENARIO
              </div>
              <div className="h-px bg-slate-800 flex-1" />
            </div>

            {/* Box 2: Fix Verification Run */}
            <div className="rounded-xl bg-[#081014] border border-[#16302e] p-4 sm:p-5">
              <div className="flex items-center justify-between pb-2 mb-3 border-b border-[#14302c]">
                <div className="flex items-center gap-2">
                  <span className="text-xs font-semibold tracking-wider uppercase text-emerald-200">2. Fix Verification</span>
                </div>
                <span className="text-[10px] font-mono font-semibold px-2 py-0.5 rounded bg-emerald-950/80 border border-emerald-600/60 text-emerald-300">
                  FIX VERIFIED
                </span>
              </div>

              <div className="space-y-1.5 font-mono text-xs">
                <div className="flex items-center justify-between text-slate-300">
                  <span className="text-emerald-300 font-semibold">TOKEN_REFRESHED</span>
                  <span className="text-[10px] text-emerald-400">OK</span>
                </div>
                <div className="flex items-center justify-between text-slate-300">
                  <span className="text-emerald-300 font-semibold">HTTP 200</span>
                  <span className="text-[10px] text-emerald-400">OK</span>
                </div>
                <div className="flex items-center justify-between text-slate-300">
                  <span className="text-emerald-300 font-semibold">PAYMENT_SUCCESS</span>
                  <span className="text-[10px] text-emerald-400 font-bold">HEALTHY STATE</span>
                </div>
              </div>
            </div>

          </div>

        </div>
      </div>
    </section>
  );
};
