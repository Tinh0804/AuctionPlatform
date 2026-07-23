import os

filepath = 'src/pages/public/Home.jsx'
with open(filepath, 'r') as f:
    lines = f.readlines()

# Replace GSAP
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
                { opacity: 0, y: 40 },
                { opacity: 1, y: 0, duration: 1.2, ease: 'power3.out', stagger: 0.15, delay: 0.2 }
            );
            
            gsap.to('.hero-bg-zoom', {
                scale: 1.1,
                duration: 20,
                ease: 'none',
                yoyo: true,
                repeat: -1
            });
        }, heroRef);
        return () => ctx.revert();
    }, []);
"""

if gsap_start != -1 and gsap_end != -1:
    lines[gsap_start:gsap_end] = [gsap_effect]

# Replace Hero
start_idx = -1
end_idx = -1
for i, line in enumerate(lines):
    if "{/* ══════════ HERO " in line:
        start_idx = i
    if "{/* ══════════ FEATURES BAR ══════════ */}" in line:
        end_idx = i
        break

hero_jsx = """            {/* ══════════ HERO (Cinematic Full-Screen) ══════════ */}
            <section ref={heroRef} className="relative w-full h-screen flex items-center overflow-hidden">
                
                {/* Background Carousel */}
                {heroShowcaseItems.map((item, index) => (
                    <div 
                        key={item.id || index} 
                        className={`absolute inset-0 transition-opacity duration-[1500ms] ease-in-out ${index === heroIdx ? 'opacity-100 z-0' : 'opacity-0 -z-10'}`}
                    >
                        <img 
                            src={item.image} 
                            alt={item.name} 
                            className="hero-bg-zoom w-full h-full object-cover origin-center" 
                        />
                        {/* Elegant Dark Overlay for text legibility */}
                        <div className="absolute inset-0 bg-gradient-to-r from-[#050505]/90 via-[#050505]/50 to-transparent" />
                        <div className="absolute inset-0 bg-black/20" />
                    </div>
                ))}

                {/* Content Overlay */}
                <div className="relative z-10 max-w-[1400px] w-full mx-auto px-6 md:px-12 lg:px-20 flex flex-col justify-center h-full">
                    
                    <div className="hero-text-stagger mb-6">
                        <div className="flex items-center gap-4">
                            <span className="w-12 h-[1px] bg-[#faf7f1]/60"></span>
                            <span className="text-xs font-bold uppercase tracking-[0.4em] text-[#faf7f1]/80">Bộ sưu tập độc bản</span>
                        </div>
                    </div>
                    
                    <h1 className="hero-text-stagger font-serif text-[4.5rem] md:text-[6.5rem] lg:text-[8.5rem] font-bold text-[#faf7f1] leading-[0.9] tracking-tighter mb-6 uppercase drop-shadow-2xl">
                        Nghệ Thuật<br />
                        <span className="italic font-light text-[#e8e2d5] lowercase tracking-normal pl-4 md:pl-16 block mt-2">& Di Sản</span>
                    </h1>
                    
                    <p className="hero-text-stagger text-lg md:text-xl text-[#faf7f1]/90 leading-relaxed max-w-xl mb-12 font-light drop-shadow-md">
                        Nơi giao thoa giữa thời gian và cái đẹp vĩnh cửu. Khám phá những kiệt tác dành riêng cho giới mộ điệu.
                    </p>
                    
                    <div className="hero-text-stagger flex items-center gap-6">
                        <a href="#auction-floor" className="group relative inline-flex items-center justify-center px-10 py-5 bg-[#faf7f1] text-[#1c1815] hover:bg-transparent hover:text-[#faf7f1] border border-[#faf7f1] transition-all duration-300">
                            <span className="text-xs font-bold uppercase tracking-[0.2em]">Tham gia Phiên Đấu</span>
                        </a>

                        {/* Slide Controls */}
                        <div className="flex gap-4 ml-4">
                            <button onClick={() => setHeroIdx(prev => (prev - 1 + heroShowcaseItems.length) % heroShowcaseItems.length)} className="w-12 h-12 rounded-full border border-[#faf7f1]/30 flex items-center justify-center text-[#faf7f1] hover:bg-[#faf7f1] hover:text-[#1c1815] hover:border-[#faf7f1] transition-all backdrop-blur-md">
                                <ArrowRight className="w-5 h-5 rotate-180" />
                            </button>
                            <button onClick={() => setHeroIdx(prev => (prev + 1) % heroShowcaseItems.length)} className="w-12 h-12 rounded-full border border-[#faf7f1]/30 flex items-center justify-center text-[#faf7f1] hover:bg-[#faf7f1] hover:text-[#1c1815] hover:border-[#faf7f1] transition-all backdrop-blur-md">
                                <ArrowRight className="w-5 h-5" />
                            </button>
                        </div>
                    </div>

                    {/* Current Item Name Indicator */}
                    <div className="hero-text-stagger absolute bottom-12 left-6 md:left-12 lg:left-20 flex items-end gap-6">
                        <div className="text-[#faf7f1]/50 font-serif text-5xl italic leading-none">
                            0{heroIdx + 1}
                        </div>
                        <div className="pb-1">
                            <span className="block text-[10px] font-bold uppercase tracking-[0.3em] text-[#faf7f1]/50 mb-1">Đang hiển thị</span>
                            <span className="font-serif text-xl md:text-2xl text-[#faf7f1]">{heroShowcaseItems[heroIdx]?.name}</span>
                        </div>
                    </div>

                </div>
            </section>
"""

if start_idx != -1 and end_idx != -1:
    lines[start_idx:end_idx] = [hero_jsx]

with open(filepath, 'w') as f:
    f.writelines(lines)
