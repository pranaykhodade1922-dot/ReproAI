import React, { useState } from 'react';
import { links } from '../config';

export const Navbar: React.FC = () => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  return (
    <nav className="sticky top-0 z-50 w-full border-b border-[#1e2532] bg-[#07090d]/85 backdrop-blur-md transition-colors">
      <div className="max-w-[1200px] mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        {/* Brand */}
        <a href="#" className="flex items-center gap-3 group focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-500 rounded-md">
          <div className="w-8 h-8 rounded-lg bg-[#0d131d] border border-sky-500/30 flex items-center justify-center text-sky-400 shadow-sm shadow-sky-500/10 group-hover:border-sky-400/60 transition-colors">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M4 4h7a4 4 0 0 1 4 4v0a4 4 0 0 1-4 4H4" />
              <path d="M4 4v16" />
              <path d="M9 12l5 8" />
              <circle cx="19" cy="19" r="2.5" className="fill-emerald-400 stroke-emerald-400" />
            </svg>
          </div>
          <div className="flex items-baseline gap-1.5">
            <span className="font-semibold text-base tracking-tight text-white group-hover:text-sky-300 transition-colors">ReproAI</span>
            <span className="text-[10px] font-mono uppercase tracking-widest text-slate-400 bg-slate-800/80 px-1.5 py-0.5 rounded border border-slate-700/60">ADB</span>
          </div>
        </a>

        {/* Desktop Links */}
        <div className="hidden md:flex items-center gap-7 text-sm">
          <a href="#how-it-works" className="text-slate-400 hover:text-slate-200 transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-500 rounded">
            How it works
          </a>
          <a href="#bug-classes" className="text-slate-400 hover:text-slate-200 transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-500 rounded">
            Validation
          </a>
          <a href="#architecture" className="text-slate-400 hover:text-slate-200 transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-500 rounded">
            Architecture
          </a>
          <a href="#try-reproai" className="text-sky-400 hover:text-sky-300 transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-500 rounded font-medium">
            Try Demo
          </a>
          <a
            href={links.github}
            target="_blank"
            rel="noreferrer"
            className="text-slate-400 hover:text-slate-200 transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-500 rounded"
          >
            GitHub
          </a>
        </div>

        {/* Primary CTA */}
        <div className="hidden md:flex items-center">
          <a
            href={links.github}
            target="_blank"
            rel="noreferrer"
            className="inline-flex items-center gap-2 px-3.5 py-1.5 text-xs font-medium rounded-lg text-slate-200 bg-[#161c28] border border-[#2b354a] hover:bg-[#1f2738] hover:border-slate-500 transition-all focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-500 shadow-sm"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor" className="text-slate-300">
              <path fillRule="evenodd" clipRule="evenodd" d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.53 1.032 1.53 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z"/>
            </svg>
            <span>View on GitHub</span>
          </a>
        </div>

        {/* Mobile menu button */}
        <div className="flex md:hidden">
          <button
            type="button"
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="p-2 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800/60 focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-500"
            aria-label="Toggle navigation menu"
            aria-expanded={mobileMenuOpen}
          >
            {mobileMenuOpen ? (
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M18 6L6 18M6 6l12 12" />
              </svg>
            ) : (
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            )}
          </button>
        </div>
      </div>

      {/* Mobile drawer */}
      {mobileMenuOpen && (
        <div className="md:hidden border-t border-[#1e2532] bg-[#090c12] px-4 pt-3 pb-5 space-y-3">
          <a
            href="#how-it-works"
            onClick={() => setMobileMenuOpen(false)}
            className="block px-3 py-2 text-sm font-medium text-slate-300 hover:text-white hover:bg-slate-800/40 rounded-md"
          >
            How it works
          </a>
          <a
            href="#bug-classes"
            onClick={() => setMobileMenuOpen(false)}
            className="block px-3 py-2 text-sm font-medium text-slate-300 hover:text-white hover:bg-slate-800/40 rounded-md"
          >
            Validation
          </a>
          <a
            href="#architecture"
            onClick={() => setMobileMenuOpen(false)}
            className="block px-3 py-2 text-sm font-medium text-slate-300 hover:text-white hover:bg-slate-800/40 rounded-md"
          >
            Architecture
          </a>
          <a
            href="#try-reproai"
            onClick={() => setMobileMenuOpen(false)}
            className="block px-3 py-2 text-sm font-medium text-sky-400 hover:bg-slate-800/40 rounded-md"
          >
            Try Demo
          </a>
          <a
            href={links.github}
            target="_blank"
            rel="noreferrer"
            onClick={() => setMobileMenuOpen(false)}
            className="block px-3 py-2 text-sm font-medium text-slate-300 hover:text-white hover:bg-slate-800/40 rounded-md"
          >
            View on GitHub →
          </a>
        </div>
      )}
    </nav>
  );
};
