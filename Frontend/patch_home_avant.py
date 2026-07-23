import os

filepath = 'src/pages/public/Home.jsx'
with open(filepath, 'r') as f:
    lines = f.readlines()

# 1. GSAP Replacement
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
                { opacity: 0, y: 50 },
                { opacity: 1, y: 0, duration: 1.2, ease: 'power4.out', stagger: 0.15, delay: 0.2 }
            );
            
            // Initial reveal for floating images
            gsap.fromTo(['.hero-float-1', '.hero-float-2', '.hero-float-3', '.hero-float-4'], 
                { opacity: 0, scale: 0.8, rotation: 0 },
                { opacity: 1, scale: 1, duration: 1.5, ease: 'expo.out', stagger: 0.15, delay: 0.4 }
            );

            // Continuous floating animation
            gsap.to('.hero-float-1', { y: '-30px', rotation: '-6deg', duration: 4, ease: 'sine.inOut', yoyo: true, repeat: -1, delay: 1 });
            gsap.to('.hero-float-2', { y: '20px', x: '10px', rotation: '4deg', duration: 5, ease: 'sine.inOut', yoyo: true, repeat: -1, delay: 1.5 });
            gsap.to('.hero-float-3', { y: '-25px', x: '-15px', duration: 3.5, ease: 'sine.inOut', yoyo: true, repeat: -1, delay: 1.2 });
            gsap.to('.hero-float-4', { y: '35px', rotation: '8deg', duration: 4.5, ease: 'sine.inOut', yoyo: true, repeat: -1, delay: 1.8 });
        }, heroRef);
        return () => ctx.revert();
    }, []);
"""

if gsap_start != -1 and gsap_end != -1:
    lines[gsap_start:gsap_end] = [gsap_effect]

# 2. Replace Hero Section
start_idx = -1
end_idx = -1
for i, line in enumerate(lines):
    if "{/* ══════════ HERO ══════════ */}" in line:
        start_idx = i
    if "{/* ══════════ FEATURES BAR ══════════ */}" in line:
        end_idx = i
        break

hero_jsx = """            {/* ══════════ HERO (Avant-Garde) ══════════ */}
            <section ref={heroRef} className="relative bg-[#faf7f1] overflow-hidden min-h-[90vh] md:min-h-[100vh] flex items-center justify-center pt-24 pb-16 lg:pt-0 lg:pb-0">
                
                {/* Decorative Background Elements */}
                <div className="absolute top-[-10%] left-[-10%] w-[40vw] h-[40vw] bg-[#e8e2d5]/40 rounded-full blur-3xl pointer-events-none" />
                <div className="absolute bottom-[-10%] right-[-10%] w-[30vw] h-[30vw] bg-[#e8e2d5]/60 rounded-full blur-3xl pointer-events-none" />

                {/* Free Floating Images */}
                <div className="absolute inset-0 w-full h-full pointer-events-none overflow-hidden">
                    {/* Image 1: Top Right - Pill Shape */}
                    <div className="hero-float-1 absolute top-[5%] md:top-[10%] right-[5%] md:right-[15%] w-[30vw] md:w-[20vw] max-w-[280px] h-[25vh] md:h-[40vh] max-h-[400px] rounded-full overflow-hidden shadow-2xl z-10 rotate-[-4deg]">
                        <img src={heroShowcaseItems[0]?.image} alt="Art 1" className="w-full h-full object-cover scale-110" />
                    </div>
                    
                    {/* Image 2: Bottom Left - Large Rectangle */}
                    <div className="hero-float-2 absolute bottom-[10%] md:bottom-[15%] left-[5%] md:left-[8%] w-[40vw] md:w-[25vw] max-w-[350px] h-[20vh] md:h-[35vh] max-h-[350px] rounded-[24px] md:rounded-[32px] overflow-hidden shadow-2xl z-20 rotate-[3deg]">
                        <img src={heroShowcaseItems[1]?.image} alt="Art 2" className="w-full h-full object-cover scale-110 grayscale-[30%]" />
                    </div>

                    {/* Image 3: Top Left - Small Circle */}
                    <div className="hero-float-3 absolute top-[15%] md:top-[20%] left-[10%] md:left-[20%] w-[25vw] md:w-[15vw] max-w-[180px] aspect-square rounded-full overflow-hidden shadow-lg z-0">
                        <img src={heroShowcaseItems[2]?.image || heroShowcaseItems[0]?.image} alt="Art 3" className="w-full h-full object-cover scale-110 sepia-[20%]" />
                    </div>

                    {/* Image 4: Bottom Right - Arch Shape */}
                    <div className="hero-float-4 absolute bottom-[5%] md:bottom-[15%] right-[10%] md:right-[25%] w-[35vw] md:w-[18vw] max-w-[240px] h-[25vh] md:h-[30vh] max-h-[300px] rounded-t-full rounded-b-xl overflow-hidden shadow-xl z-10 rotate-[6deg]">
                        <img src={heroShowcaseItems[3]?.image || heroShowcaseItems[1]?.image} alt="Art 4" className="w-full h-full object-cover scale-110" />
                    </div>
                </div>

                {/* Center Avant-Garde Text */}
                <div className="relative z-30 flex flex-col items-center text-center max-w-5xl px-6 pointer-events-auto mix-blend-difference">
                    <div className="hero-text-stagger mb-6 inline-block">
                        <div className="flex items-center gap-4 justify-center">
                            <span className="w-16 h-[1px] bg-[#faf7f1]"></span>
                            <span className="text-[10px] md:text-xs font-bold uppercase tracking-[0.3em] text-[#faf7f1]">The Art Collection</span>
                            <span className="w-16 h-[1px] bg-[#faf7f1]"></span>
                        </div>
                    </div>
                    
                    <h1 className="hero-text-stagger font-serif text-[4.5rem] md:text-[8rem] lg:text-[10rem] font-bold text-[#faf7f1] leading-[0.8] tracking-tighter mb-4 md:mb-8 uppercase">
                        Master<br />
                        <span className="italic font-light text-[#faf7f1]/90 lowercase tracking-normal pl-8 md:pl-32 block">pieces</span>
                    </h1>
                    
                    <p className="hero-text-stagger text-base md:text-xl text-[#faf7f1]/80 leading-relaxed max-w-xs md:max-w-lg mx-auto mb-8 md:mb-12 font-light">
                        Vượt ra khỏi mọi khuôn khổ. Nơi những kiệt tác nghệ thuật độc bản tìm thấy vị chủ nhân xứng tầm.
                    </p>
                    
                    <div className="hero-text-stagger">
                        <a href="#auction-floor" className="group relative inline-flex items-center justify-center w-28 h-28 md:w-40 md:h-40 rounded-full border border-[#faf7f1]/30 text-[#faf7f1] hover:bg-[#faf7f1] hover:text-[#1c1815] transition-all duration-500 overflow-hidden">
                            <span className="text-[10px] md:text-xs font-bold uppercase tracking-[0.2em] relative z-10 text-center leading-loose">
                                Khám phá<br/>Sàn đấu
                            </span>
                            <div className="absolute inset-0 bg-[#faf7f1] translate-y-full rounded-full group-hover:translate-y-0 transition-transform duration-500 ease-out z-0"></div>
                        </a>
                    </div>
                </div>
                
            </section>
"""

if start_idx != -1 and end_idx != -1:
    lines[start_idx:end_idx] = [hero_jsx]

with open(filepath, 'w') as f:
    f.writelines(lines)
