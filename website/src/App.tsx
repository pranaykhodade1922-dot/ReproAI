import React from 'react';
import { Navbar } from './components/Navbar';
import { Hero } from './components/Hero';
import { HeroVisual } from './components/HeroVisual';
import { Problem } from './components/Problem';
import { HowItWorks } from './components/HowItWorks';
import { Screenshots } from './components/Screenshots';
import { BugClasses } from './components/BugClasses';
import { Differentiator } from './components/Differentiator';
import { Architecture } from './components/Architecture';
import { TechStack } from './components/TechStack';
import { AiHonesty } from './components/AiHonesty';
import { RealVsDeterministic } from './components/RealVsDeterministic';
import { ValidationStats } from './components/ValidationStats';
import { Security } from './components/Security';
import { DeveloperReport } from './components/DeveloperReport';
import { TryReproAI } from './components/TryReproAI';
import { FinalCta } from './components/FinalCta';
import { Footer } from './components/Footer';

export const App: React.FC = () => {
  return (
    <div className="min-h-screen bg-[#07090d] text-slate-100 flex flex-col selection:bg-sky-500/20 selection:text-sky-300">
      {/* 1. Navbar */}
      <Navbar />

      <main className="flex-1">
        {/* 2. Hero */}
        <Hero />

        {/* 3. Hero Visual */}
        <HeroVisual />

        {/* 4. Problem */}
        <Problem />

        {/* 5. Solution / How It Works */}
        <HowItWorks />

        {/* 6. Screenshots */}
        <Screenshots />

        {/* 7. Validated Bug Classes */}
        <BugClasses />

        {/* 8. Differentiator */}
        <Differentiator />

        {/* 9. Architecture */}
        <Architecture />

        {/* 10. Tech Stack */}
        <TechStack />

        {/* 11. AI / Analysis Honesty */}
        <AiHonesty />

        {/* 12. Real vs Deterministic */}
        <RealVsDeterministic />

        {/* 13. Validation Numbers */}
        <ValidationStats />

        {/* 14. Security */}
        <Security />

        {/* 15. Developer Report */}
        <DeveloperReport />

        {/* 16. Try ReproAI */}
        <TryReproAI />

        {/* 17. Final CTA */}
        <FinalCta />
      </main>

      {/* 17. Footer */}
      <Footer />
    </div>
  );
};

export default App;
