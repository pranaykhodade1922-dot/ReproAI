import React from 'react';
import { links } from '../config';

export const Footer: React.FC = () => {
  return (
    <footer className="border-t border-[#1a2130] bg-[#05070a] py-12 text-slate-400 text-xs">
      <div className="max-w-[1200px] mx-auto px-4 sm:px-6 lg:px-8">
        
        <div className="flex flex-col sm:flex-row items-center justify-between gap-6">
          
          {/* Brand & attribution */}
          <div className="flex items-center gap-3">
            <div className="w-6 h-6 rounded bg-[#0d131d] border border-sky-500/30 flex items-center justify-center text-sky-400">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M4 4h7a4 4 0 0 1 4 4v0a4 4 0 0 1-4 4H4" />
                <path d="M4 4v16" />
                <path d="M9 12l5 8" />
              </svg>
            </div>
            <div>
              <span className="font-semibold text-slate-200">ReproAI</span>
              <span className="mx-2 text-slate-600">·</span>
              <span>Built for the iQOO Developer Tools challenge.</span>
            </div>
          </div>

          {/* Real Links Only */}
          <div className="flex items-center gap-6 font-mono">
            <a
              href={links.github}
              target="_blank"
              rel="noreferrer"
              className="text-slate-400 hover:text-white transition-colors"
            >
              GitHub
            </a>
            <a
              href={`${links.github}#readme`}
              target="_blank"
              rel="noreferrer"
              className="text-slate-400 hover:text-white transition-colors"
            >
              Documentation
            </a>
          </div>

        </div>

        <div className="mt-8 pt-6 border-t border-[#141b28] text-center text-slate-400 text-[11px] font-mono">
          Physical Android Device Automation · Deterministic Scenario Runner · Zero Synthetic Metrics
        </div>

      </div>
    </footer>
  );
};
