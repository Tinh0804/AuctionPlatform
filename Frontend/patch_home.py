import os

filepath = 'src/pages/public/Home.jsx'
with open(filepath, 'r') as f:
    lines = f.readlines()

# 1. Update imports
for i, line in enumerate(lines):
    if line.startswith("import { useEffect, useState }"):
        lines[i] = "import { useEffect, useState, useRef } from 'react';\nimport gsap from 'gsap';\n"
        break

# 2. Add heroRef
for i, line in enumerate(lines):
    if "const [viewMode, setViewMode] = useState('grid');" in line:
        lines.insert(i + 1, "    const heroRef = useRef(null);\n")
        break

# 3. Add GSAP effect
gsap_effect = """
    useEffect(() => {
        const ctx = gsap.context(() => {
            gsap.fromTo('.hero-text-stagger', 
                { opacity: 0, y: 30 },
                { opacity: 1, y: 0, duration: 1, ease: 'power3.out', stagger: 0.1, delay: 0.1 }
            );
            gsap.fromTo('.hero-image-stagger', 
                { opacity: 0, scale: 0.95, y: 30 },
                { opacity: 1, scale: 1, y: 0, duration: 1.2, ease: 'power4.out', stagger: 0.15, delay: 0.2 }
            );
            gsap.to('.hero-image-pan', {
                y: '-5%',
                duration: 20,
                ease: 'sine.inOut',
                yoyo: true,
                repeat: -1
            });
        }, heroRef);
        return () => ctx.revert();
    }, []);
"""
for i, line in enumerate(lines):
    if "useEffect(() => {" in line: # Insert before the first useEffect
        lines.insert(i, gsap_effect)
        break

# 4. Replace Hero Section
start_idx = -1
end_idx = -1
for i, line in enumerate(lines):
    if "{/* ══════════ HERO ══════════ */}" in line:
        start_idx = i
    if "{/* ══════════ FEATURES BAR ══════════ */}" in line:
        end_idx = i
        break

hero_jsx = """            {/* ══════════ HERO ══════════ */}
            <section ref={heroRef} className="relative bg-[#faf7f1] overflow-hidden min-h-[90vh] flex items-center pt-24 pb-16 lg:pt-0 lg:pb-0">
                <div className="max-w-[1400px] w-full mx-auto px-6 md:px-10 flex flex-col lg:flex-row items-center justify-between gap-12 lg:gap-8">
                    
                    {/* Left: Minimalist Text */}
                    <div className="w-full lg:w-[45%] z-10 flex flex-col justify-center">
                        <div className="hero-text-stagger flex items-center gap-3 mb-6">
                            <span className="w-10 h-[1px] bg-[#1c1815]"></span>
                            <span className="text-[10px] font-bold uppercase tracking-[0.2em] text-[#1c1815]">Đấu giá nghệ thuật</span>
                        </div>
                        
                        <h1 className="hero-text-stagger font-serif text-[4rem] md:text-[5.5rem] lg:text-[6.5rem] font-bold text-[#1c1815] leading-[0.9] tracking-tighter mb-8">
                            Nghệ thuật<br />
                            <span className="italic font-light text-[#746b62]">& Di sản</span>
                        </h1>
                        
                        <p className="hero-text-stagger text-[17px] text-[#746b62] leading-relaxed max-w-md mb-12">
                            Tuyển tập những kiệt tác độc bản dành cho giới mộ điệu. Khám phá các phiên đấu giá kín với trải nghiệm thanh lịch và riêng tư nhất.
                        </p>
                        
                        <div className="hero-text-stagger">
                            <a href="#auction-floor" className="group inline-flex items-center gap-4 bg-[#1c1815] text-[#faf7f1] px-8 py-5 text-sm font-bold uppercase tracking-[0.2em] transition-all hover:bg-[#2a241f] hover:shadow-[0_20px_40px_rgba(28,24,21,0.2)] rounded-full">
                                Khám phá ngay
                                <ArrowRight className="w-4 h-4 transition-transform group-hover:translate-x-1" />
                            </a>
                        </div>
                    </div>

                    {/* Right: Creative Bento Gallery */}
                    <div className="w-full lg:w-[50%] h-[600px] lg:h-[750px] relative mt-10 lg:mt-0">
                        <div className="grid grid-cols-12 grid-rows-6 gap-4 h-full w-full">
                            
                            {/* Main Large Image */}
                            <div className="hero-image-stagger col-span-8 row-span-5 rounded-[24px] overflow-hidden relative shadow-xl border border-[#1c1815]/5">
                                <img
                                    src={heroShowcaseItems[0].image}
                                    alt="Featured Art"
                                    className="hero-image-pan w-full h-[120%] object-cover contrast-[1.05]"
                                />
                                <div className="absolute inset-0 shadow-[inset_0_0_60px_rgba(0,0,0,0.1)] pointer-events-none" />
                                <div className="absolute bottom-6 left-6 bg-[#faf7f1]/90 backdrop-blur-md px-5 py-3 rounded-2xl">
                                    <span className="block text-[9px] uppercase tracking-widest font-bold text-[#746b62] mb-1">Tác phẩm nổi bật</span>
                                    <span className="font-serif text-lg text-[#1c1815] leading-none">{heroShowcaseItems[0].name}</span>
                                </div>
                            </div>

                            {/* Top Right Small */}
                            <div className="hero-image-stagger col-span-4 row-span-2 rounded-[24px] overflow-hidden relative shadow-md border border-[#1c1815]/5">
                                <img
                                    src={heroShowcaseItems[1].image}
                                    alt="Art Detail"
                                    className="hero-image-pan w-full h-[120%] object-cover grayscale-[20%]"
                                />
                            </div>

                            {/* Mid Right Dark Box */}
                            <div className="hero-image-stagger col-span-4 row-span-2 rounded-[24px] bg-[#1c1815] text-[#faf7f1] flex flex-col items-center justify-center p-4 relative overflow-hidden shadow-xl">
                                <div className="absolute inset-0 bg-[radial-gradient(circle_at_center,white_0%,transparent_100%)] opacity-10" />
                                <span className="font-serif text-4xl mb-1">42</span>
                                <span className="text-[9px] uppercase tracking-[0.2em] text-[#faf7f1]/50 text-center">Phiên đấu giá<br/>đang mở</span>
                            </div>

                            {/* Bottom Right Wide */}
                            <div className="hero-image-stagger col-span-12 lg:col-span-11 row-span-1 lg:row-start-6 lg:col-start-2 rounded-[24px] bg-[#e8e2d5] p-6 flex items-center justify-between border border-[#1c1815]/5 shadow-inner">
                                <div>
                                    <h4 className="font-serif text-xl text-[#1c1815] mb-1">Sưu tầm chuyên nghiệp</h4>
                                    <p className="text-xs text-[#746b62]">Dịch vụ giám định và bảo chứng độc quyền.</p>
                                </div>
                                <div className="w-10 h-10 rounded-full border border-[#1c1815]/20 flex items-center justify-center shrink-0">
                                    <ShieldCheck className="w-4 h-4 text-[#1c1815]" />
                                </div>
                            </div>
                            
                        </div>
                    </div>
                    
                </div>
            </section>
"""

if start_idx != -1 and end_idx != -1:
    lines[start_idx:end_idx] = [hero_jsx]

with open(filepath, 'w') as f:
    f.writelines(lines)
