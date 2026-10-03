import React from 'react';

export const DeveloperReport: React.FC = () => {
  const preservedItems = [
    'environment',
    'captured evidence',
    'inferred diagnosis',
    'reproduction steps',
    'TestScenario',
    'original execution',
    'verification execution',
  ];

  const exportsList = [
    { format: 'JSON', desc: 'Typed scenario & execution schemas' },
    { format: 'Markdown', desc: 'Renderable post-mortem document' },
    { format: 'Text', desc: 'Plain-text bug report digest' },
    { format: 'Developer ZIP', desc: 'Sanitized full evidence bundle' },
  ];

  return (
    <section className="py-20 border-t border-[#1a2130] bg-[#07090d]">
      <div className="max-w-[1200px] mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Section Shell */}
        <div className="rounded-2xl border border-[#1e273a] bg-[#0c1017] p-6 sm:p-10 shadow-2xl">
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 items-center">
            
            {/* Left Column: Description & Preserved Elements */}
            <div className="lg:col-span-7">
              <div className="text-xs font-mono text-sky-400 uppercase tracking-widest font-semibold mb-3">
                Post-Mortem Artifacts
              </div>
              <h2 className="text-3xl font-bold tracking-tight text-white mb-4">
                Structured Developer Incident Reports
              </h2>
              <p className="text-sm sm:text-base text-slate-300 leading-relaxed mb-6">
                Every completed incident can preserve:
              </p>

              {/* Preserved List */}
              <ul className="grid grid-cols-1 sm:grid-cols-2 gap-2.5 mb-8 font-mono text-xs sm:text-sm">
                {preservedItems.map((item) => (
                  <li key={item} className="flex items-center gap-2.5 p-2 rounded-lg bg-[#090d14] border border-[#1b2538] text-slate-200">
                    <span className="w-1.5 h-1.5 rounded-full bg-sky-400" />
                    <span>{item}</span>
                  </li>
                ))}
              </ul>

              {/* Exports Formats Strip */}
              <div className="pt-6 border-t border-[#182030]">
                <div className="text-xs font-mono uppercase text-slate-400 font-semibold mb-3">
                  Export Formats:
                </div>
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                  {exportsList.map((exp) => (
                    <div key={exp.format} className="p-3 rounded-lg bg-[#0f1522] border border-[#1e2a3f]">
                      <div className="text-xs font-mono font-bold text-sky-300">{exp.format}</div>
                      <div className="text-[10px] text-slate-400 mt-0.5">{exp.desc}</div>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            {/* Right Column: Real Incident Report Screenshot */}
            <div className="lg:col-span-5 flex justify-center">
              <div className="rounded-2xl border-2 border-[#202b3d] bg-black p-2.5 shadow-2xl max-w-[280px] w-full group">
                <div className="relative rounded-xl overflow-hidden">
                  <img
                    src="/screenshots/09-incident-report.png"
                    alt="Real Incident Report captured from phone screen"
                    loading="lazy"
                    className="w-full h-auto block object-cover group-hover:scale-105 transition-transform duration-300"
                  />
                  <div className="absolute bottom-2 left-2 right-2 px-2.5 py-1.5 rounded-lg bg-black/80 backdrop-blur-sm border border-slate-700/60 text-center">
                    <span className="text-[11px] font-mono text-slate-300">09-incident-report.png</span>
                  </div>
                </div>
              </div>
            </div>

          </div>
        </div>

      </div>
    </section>
  );
};
