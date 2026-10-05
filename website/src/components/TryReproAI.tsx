import React from 'react';
import { links } from '../config';

export const TryReproAI: React.FC = () => {
  const packageItems = [
    {
      name: 'ReproAI',
      file: 'reproai-demo-v1.0.apk',
      role: 'Main Android debugging application',
      desc: 'Captures domain & lifecycle events, displays scenario diagnosis, and renders execution results.',
    },
    {
      name: 'DemoShop',
      file: 'demoshop-demo-v1.0.apk',
      role: 'Instrumented demonstration target app',
      desc: 'Sample mobile commerce application featuring controlled failure hooks for end-to-end validation.',
    },
  ];

  const requirements = [
    { title: 'ReproAI Android app', desc: 'Installed on device' },
    { title: 'DemoShop target app', desc: 'Installed on device' },
    { title: 'Repro Runner running on a laptop', desc: 'Python 3.12 + FastAPI daemon' },
    { title: 'ADB-authorized Android device', desc: 'USB or wireless debugging' },
  ];

  return (
    <section id="try-reproai" className="py-20 border-t border-[#1a2130] bg-[#07090d]">
      <div className="max-w-[1200px] mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Header */}
        <div className="text-center max-w-3xl mx-auto mb-16">
          <div className="text-xs font-mono text-sky-400 uppercase tracking-widest font-semibold mb-3">
            Hands-On Evaluation
          </div>
          <h2 className="text-3xl sm:text-4xl font-bold tracking-tight text-white">
            Try ReproAI
          </h2>
          <p className="mt-3 text-slate-400 text-sm sm:text-base leading-relaxed">
            Install the Android demo build and connect it to the laptop-side Repro Runner to reproduce and verify mobile failures end-to-end.
          </p>
        </div>

        {/* Primary Download Card */}
        <div className="rounded-2xl border border-[#1e273a] bg-[#0c1017] p-6 sm:p-10 shadow-2xl relative overflow-hidden">
          {/* Subtle accent gradient */}
          <div className="absolute top-0 right-0 w-80 h-80 bg-gradient-to-bl from-sky-500/10 via-emerald-500/5 to-transparent blur-3xl pointer-events-none -z-10" />

          <div>
            {/* Badges */}
            <div className="flex flex-wrap items-center gap-2 mb-4">
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-mono bg-sky-950/60 border border-sky-800/50 text-sky-300 font-medium">
                <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />
                <span>Hackathon Demo Build</span>
              </div>
              <span className="text-[11px] font-mono text-slate-400 px-2.5 py-0.5 rounded bg-[#101622] border border-[#1e273a]">
                Demo release: v1.0.0-demo
              </span>
            </div>

            {/* Title & Description */}
            <h3 className="text-2xl sm:text-3xl font-bold tracking-tight text-white mb-3">
              Run ReproAI on Android
            </h3>
            <p className="text-sm sm:text-base text-slate-300 leading-relaxed max-w-3xl mb-8">
              The complete prototype uses two Android applications plus the laptop-side Repro Runner. Both APKs are available directly from the official GitHub Release.
            </p>

            {/* Actions */}
            <div className="flex flex-wrap items-center gap-4 mb-10">
              <a
                href={links.release}
                target="_blank"
                rel="noreferrer"
                className="inline-flex items-center gap-2.5 px-6 py-3.5 rounded-xl text-sm font-semibold text-slate-950 bg-gradient-to-r from-sky-400 to-teal-300 hover:from-sky-300 hover:to-teal-200 transition-all shadow-md shadow-sky-500/20 focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-400"
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                  <polyline points="7 10 12 15 17 10" />
                  <line x1="12" y1="15" x2="12" y2="3" />
                </svg>
                <span>Download Android Demo</span>
              </a>

              <a
                href={links.setupGuide}
                target="_blank"
                rel="noreferrer"
                className="inline-flex items-center gap-2 px-5 py-3.5 rounded-xl text-sm font-medium text-slate-200 bg-[#121824] border border-[#233148] hover:bg-[#182132] hover:text-white hover:border-slate-500 transition-all focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-500"
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z" />
                  <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z" />
                </svg>
                <span>Setup Guide</span>
              </a>

              <a
                href={links.github}
                target="_blank"
                rel="noreferrer"
                className="inline-flex items-center gap-2 px-5 py-3.5 rounded-xl text-sm font-medium text-slate-200 bg-[#121824] border border-[#233148] hover:bg-[#182132] hover:text-white hover:border-slate-500 transition-all focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-500"
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                  <path fillRule="evenodd" clipRule="evenodd" d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.53 1.032 1.53 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z"/>
                </svg>
                <span>View Source</span>
              </a>
              <a
                href={links.demoVideo}
                target="_blank"
                rel="noopener noreferrer"
                aria-label="Watch ReproAI demo video on YouTube"
                className="inline-flex items-center gap-2 px-5 py-3.5 rounded-xl text-sm font-medium text-slate-200 bg-[#121824] border border-[#233148] hover:bg-[#182132] hover:text-white hover:border-slate-500 transition-all focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-500"
              >
                <svg aria-hidden="true" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <polygon points="5 3 19 12 5 21 5 3" />
                </svg>
                <span>Watch Demo</span>
              </a>
            </div>

            {/* Package Contents Breakdown */}
            <div className="pt-8 border-t border-[#182030] mb-8">
              <div className="text-xs font-mono uppercase tracking-wider text-slate-300 font-semibold mb-4">
                The GitHub Release contains:
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {packageItems.map((pkg) => (
                  <div key={pkg.name} className="p-4 rounded-xl bg-[#090d14] border border-[#1b2538]">
                    <div className="flex items-center justify-between gap-2 mb-1.5">
                      <span className="text-sm font-bold text-white">{pkg.name}</span>
                      <code className="text-[11px] font-mono text-sky-400 bg-sky-950/40 px-2 py-0.5 rounded border border-sky-800/40">
                        {pkg.file}
                      </code>
                    </div>
                    <div className="text-xs text-slate-300 font-medium mb-1">{pkg.role}</div>
                    <p className="text-[11px] text-slate-400 leading-relaxed">{pkg.desc}</p>
                  </div>
                ))}
              </div>
            </div>

            {/* Requirements Section */}
            <div className="pt-6 border-t border-[#182030]">
              <div className="text-xs font-mono uppercase tracking-wider text-slate-300 font-semibold mb-4">
                Full demo requires:
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3.5 mb-4">
                {requirements.map((req) => (
                  <div key={req.title} className="p-3 rounded-lg bg-[#090d14] border border-[#182234]">
                    <div className="flex items-center gap-2 text-slate-200 font-medium text-xs">
                      <span className="w-1.5 h-1.5 rounded-full bg-sky-400" />
                      <span>{req.title}</span>
                    </div>
                    <p className="mt-1 text-[11px] text-slate-400">
                      {req.desc}
                    </p>
                  </div>
                ))}
              </div>

              <p className="text-xs text-slate-400 font-mono">
                Both APKs are available from the GitHub Release. The complete reproduction and fix-verification workflow requires both Android applications and the laptop-side runner.
              </p>
            </div>

            {/* Distribution & Honesty Footnotes */}
            <div className="mt-8 pt-6 border-t border-[#182030] flex flex-col sm:flex-row sm:items-center justify-between gap-4 text-xs text-slate-400">
              <div className="flex items-center gap-2">
                <span className="w-1.5 h-1.5 rounded-full bg-slate-500" />
                <span>APK binaries are distributed through GitHub Releases and are excluded from normal source history.</span>
              </div>

              <div className="flex items-center gap-2 text-slate-400 font-mono text-[11px]">
                <span>Validated end-to-end on a physical Android device. Actual iQOO hardware validation remains pending.</span>
              </div>
            </div>

          </div>

        </div>

      </div>
    </section>
  );
};
