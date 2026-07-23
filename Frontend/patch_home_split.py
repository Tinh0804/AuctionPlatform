import os

filepath = 'src/pages/public/Home.jsx'
with open(filepath, 'r') as f:
    lines = f.readlines()

gsap_start = -1
gsap_end = -1
for i, line in enumerate(lines):
    if "useEffect(() => {" in line and "gsap.context" in lines[i+1]:
        gsap_start = i
    if gsap_start != -1 and "return () => ctx.revert();" in line:
        gsap_end = i + 2
        break

gsap_effect = """    useEffect(() => {
        const ctx = gsap.context(() => {
            gsap.fromTo('.hero-text-stagger', 
                { opacity: 0, x: -30 },
                { opacity: 1, x: 0, duration: 1.2, ease: 'power3.out', stagger: 0.15, delay: 0.2 }
            );
            
            // Subtle slow zoom on background
            gsap.to('.hero-bg-zoom', {
                scale: 1.15,
                duration: 25,
                ease: 'sine.inOut',
                yoyo: true,
                repeat: -1
            });
        }, heroRef);
        return () => ctx.revert();
    }, []);
"""

if gsap_start != -1 and gsap_end != -1:
    lines[gsap_start:gsap_end] = [gsap_effect]

start_idx = -1
end_idx = -1
for i, line in enumerate(lines):
    if "══════════ HERO " in line:
        start_idx = i
    if "{/* ══════════ FEATURES BAR ══════════ */}" in line:
        end_idx = i
        break

hero_jsx = """            {/* ══════════ HERO (Editorial Split Screen) ══════════ */}
            <section ref={heroRef} className="relative bg-[#faf7f1] w-full min-h-[90vh] md:h-screen flex flex-col md:flex-row pt-[72px] md:pt-0 overflow-hidden border-b border-[#1c1815]/10">
                {/* Left Side: Editorial Typography */}
                <div className="w-full md:w-[45%] lg:w-[40%] h-full flex flex-col justify-center px-8 md:px-12 lg:px-20 py-16 md:py-0 z-10 shrink-0">
                    <div className="hero-text-stagger mb-8">
                        <div className="flex items-center gap-4">
                            <span className="w-12 h-[1px] bg-[#1c1815]"></span>
                            <span className="text-[10px] md:text-xs font-bold uppercase tracking-[0.3em] text-[#1c1815]">Đấu giá nghệ thuật</span>
                        </div>
                    </div>
                    
                    <h1 className="hero-text-stagger font-serif text-[4.5rem] md:text-[5rem] lg:text-[7rem] font-bold text-[#1c1815] leading-[0.85] tracking-tighter mb-8 uppercase">
                        Nghệ Thuật<br />
                        <span className="italic font-light text-[#746b62] lowercase tracking-normal pl-4 md:pl-12 block mt-2">độc bản</span>
                    </h1>
                    
                    <p className="hero-text-stagger text-base md:text-lg text-[#746b62] leading-relaxed max-w-sm mb-12 font-light">
                        Tuyển tập những kiệt tác dành cho giới mộ điệu. Nơi hội tụ của di sản và sự tinh tế vượt thời gian.
                    </p>
                    
                    <div className="hero-text-stagger flex items-center gap-8">
                        <a href="#auction-floor" className="group relative inline-flex items-center justify-center w-28 h-28 md:w-32 md:h-32 rounded-full border border-[#1c1815]/20 text-[#1c1815] hover:bg-[#1c1815] hover:text-[#faf7f1] transition-all duration-500 overflow-hidden">
                            <span className="text-[10px] font-bold uppercase tracking-[0.2em] relative z-10 text-center leading-loose">Khám phá<br/>Sàn đấu</span>
                        </a>

                        {/* Slide Controls */}
                        <div className="flex gap-4">
                            <button onClick={() => setHeroIdx(prev => (prev - 1 + heroShowcaseItems.length) % heroShowcaseItems.length)} className="w-12 h-12 rounded-full border border-[#1c1815]/20 flex items-center justify-center text-[#1c1815] hover:bg-[#1c1815] hover:text-[#faf7f1] transition-colors">
                                <ArrowRight className="w-5 h-5 rotate-180" />
                            </button>
                            <button onClick={() => setHeroIdx(prev => (prev + 1) % heroShowcaseItems.length)} className="w-12 h-12 rounded-full border border-[#1c1815]/20 flex items-center justify-center text-[#1c1815] hover:bg-[#1c1815] hover:text-[#faf7f1] transition-colors">
                                <ArrowRight className="w-5 h-5" />
                            </button>
                        </div>
                    </div>
                </div>

                {/* Right Side: Massive Image */}
                <div className="w-full md:w-[55%] lg:w-[60%] h-[60vh] md:h-full relative overflow-hidden flex-grow">
                    {heroShowcaseItems.map((item, index) => (
                        <div 
                            key={item.id} 
                            className={`absolute inset-0 transition-opacity duration-1000 ease-in-out ${index === heroIdx ? 'opacity-100 z-10' : 'opacity-0 z-0 pointer-events-none'}`}
                        >
                            <img 
                                src={item.image} 
                                alt={item.name} 
                                className="w-full h-full object-cover scale-105 hero-bg-zoom" 
                            />
                            {/* Subtle dark gradient overlay at bottom for the caption */}
                            <div className="absolute inset-0 bg-gradient-to-t from-[#1c1815]/90 via-[#1c1815]/10 to-transparent opacity-80" />
                            
                            <div className="absolute bottom-10 right-10 md:bottom-16 md:right-16 text-right">
                                <span className="block text-[10px] font-bold uppercase tracking-[0.3em] text-[#faf7f1]/70 mb-3">Tác phẩm nổi bật</span>
                                <span className="font-serif text-3xl md:text-5xl text-[#faf7f1]">{item.name}</span>
                            </div>
                        </div>
                    ))}
                </div>
            </section>
"""

if start_idx != -1 and end_idx != -1:
    lines[start_idx:end_idx] = [hero_jsx]

with open(filepath, 'w') as f:
    f.writelines(lines)
