import React from 'react';

export const AiHonesty: React.FC = () => {
  return (
    <section className="py-16 border-t border-[#1a2130] bg-[#07090d]">
      <div className="max-w-[1200px] mx-auto px-4 sm:px-6 lg:px-8">
        <div className="rounded-2xl bg-[#0c1017] border border-[#232d42] p-6 sm:p-8 relative overflow-hidden shadow-xl">
          
          <div className="flex flex-col md:flex-row md:items-start gap-6">
            
            {/* Icon / Badge */}
            <div className="w-12 h-12 rounded-xl bg-sky-950/60 border border-sky-700/50 flex items-center justify-center text-sky-400 shrink-0">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="12" cy="12" r="10" />
                <path d="M12 16v-4" />
                <path d="M12 8h.01" />
              </svg>
            </div>

            {/* Content */}
            <div className="flex-1">
              <div className="flex items-center gap-2 mb-2">
                <span className="text-xs font-mono uppercase tracking-wider text-sky-400 font-semibold">
                  Engineering Honesty & Roadmap
                </span>
                <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-slate-800 text-slate-300 border border-slate-700">
                  Deterministic Baseline
                </span>
              </div>

              <h3 className="text-2xl font-bold text-white tracking-tight">
                Designed for verified AI-assisted reproduction.
              </h3>

              <div className="mt-4 space-y-3 text-sm text-slate-300 leading-relaxed max-w-3xl">
                <p>
                  The current validated prototype uses deterministic local analysis rules for the supported failure patterns.
                </p>
                <p>
                  The analysis layer uses a provider abstraction designed for future local/LLM scenario proposal.
                </p>
                <p className="font-medium text-slate-200 bg-[#121824] p-3.5 rounded-lg border border-[#1e2a3d]">
                  ReproAI’s execution engine remains the source of truth: a proposed scenario only counts if the runner reproduces the measured failure.
                </p>
              </div>

              <div className="mt-5 flex flex-wrap items-center gap-4 text-xs font-mono text-slate-400">
                <span className="flex items-center gap-1.5">
                  <span className="w-1.5 h-1.5 rounded-full bg-emerald-400" />
                  Deterministic rule engine active
                </span>
                <span className="flex items-center gap-1.5">
                  <span className="w-1.5 h-1.5 rounded-full bg-sky-400" />
                  Provider interface ready for LLM plug-in
                </span>
                <span className="flex items-center gap-1.5">
                  <span className="w-1.5 h-1.5 rounded-full bg-slate-500" />
                  No unverified arbitrary claims
                </span>
              </div>
            </div>

          </div>

        </div>
      </div>
    </section>
  );
};
