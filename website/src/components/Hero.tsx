import React from 'react';
import { links } from '../config';

export const Hero: React.FC = () => {
  return (
    <section className="relative pt-16 pb-12 sm:pt-24 sm:pb-16 overflow-hidden">
      {/* Background subtle radial gradient */}
      <div className="absolute top-0 left-1/2 -translate-x-1/2 w-[800px] h-[350px] bg-gradient-to-b from-sky-500/10 via-emerald-500/5 to-transparent blur-3xl pointer-events-none -z-10" />

      <div className="max-w-[1200px] mx-auto px-4 sm:px-6 lg:px-8 text-center">
        {/* Credibility pill */}
        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-medium bg-[#111622] border border-[#242e42] text-slate-300 mb-8 shadow-sm">
          <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
          <span>Validated end-to-end on a physical Android device.</span>
        </div>

        {/* Main headline */}
        <h1 className="text-4xl sm:text-5xl lg:text-6xl font-bold tracking-tight text-white max-w-4xl mx-auto leading-[1.12]">
          Turn real mobile failures{' '}
          <span className="text-transparent bg-clip-text bg-gradient-to-r from-sky-400 via-teal-300 to-emerald-400">
            into reproducible tests.
          </span>
        </h1>

        {/* Subtitle */}
        <p className="mt-6 text-base sm:text-lg text-slate-400 max-w-2xl mx-auto leading-relaxed">
          ReproAI captures device and application evidence around a mobile failure, converts it into an executable{' '}
          <code className="text-xs sm:text-sm font-mono text-sky-300 bg-sky-950/40 px-1.5 py-0.5 rounded border border-sky-800/40">TestScenario</code>
          , reproduces it through ADB, and reruns the same scenario to verify the fix.
        </p>

        {/* Action buttons */}
        <div className="mt-8 flex flex-wrap items-center justify-center gap-4">
          <a
            href="#how-it-works"
            className="inline-flex items-center justify-center px-6 py-3 rounded-lg text-sm font-semibold text-slate-950 bg-gradient-to-r from-sky-400 to-teal-300 hover:from-sky-300 hover:to-teal-200 transition-all shadow-sm shadow-sky-500/20 focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-400"
          >
            Explore ReproAI
          </a>
          <a
            href={links.github}
            target="_blank"
            rel="noreferrer"
            className="inline-flex items-center justify-center gap-2 px-6 py-3 rounded-lg text-sm font-medium text-slate-300 bg-[#121824] border border-[#232c3f] hover:bg-[#182132] hover:text-white hover:border-slate-500 transition-all focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-500"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <path fillRule="evenodd" clipRule="evenodd" d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.53 1.032 1.53 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z"/>
            </svg>
            <span>View Source</span>
          </a>
        </div>

        {/* Small status badges */}
        <div className="mt-12 pt-8 border-t border-[#1a2130] flex flex-wrap items-center justify-center gap-3 text-xs font-mono text-slate-400">
          <div className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-md bg-[#0f141f] border border-[#1e2738]">
            <span className="w-1.5 h-1.5 rounded-full bg-sky-400" />
            <span>Physical Android Device</span>
          </div>
          <div className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-md bg-[#0f141f] border border-[#1e2738]">
            <span className="w-1.5 h-1.5 rounded-full bg-cyan-400" />
            <span>Real ADB Execution</span>
          </div>
          <div className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-md bg-[#0f141f] border border-[#1e2738]">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-400" />
            <span>Same-Scenario Verification</span>
          </div>
          <div className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-md bg-[#0f141f] border border-[#1e2738]">
            <span className="w-1.5 h-1.5 rounded-full bg-teal-400" />
            <span className="text-slate-200 font-semibold">115 automated/device tests passed</span>
          </div>
        </div>
      </div>
    </section>
  );
};
