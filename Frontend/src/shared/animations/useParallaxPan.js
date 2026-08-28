import { useEffect, useRef } from 'react';
import gsap from 'gsap';

export function useParallaxPan({
  strength = 30,
  lerp = 0.1,
} = {}) {
  const containerRef = useRef(null);
  const targetRef = useRef(null);

  useEffect(() => {
    const container = containerRef.current;
    const target = targetRef.current;
    
    if (!container || !target) return;

    let targetX = 0;
    let targetY = 0;
    let currentX = 0;
    let currentY = 0;
    let animationFrameId;

    const handleMouseMove = (e) => {
      const { left, top, width, height } = container.getBoundingClientRect();
      const x = (e.clientX - left) / width - 0.5;
      const y = (e.clientY - top) / height - 0.5;
      
      targetX = x * strength;
      targetY = y * strength;
    };

    const animate = () => {
      currentX += (targetX - currentX) * lerp;
      currentY += (targetY - currentY) * lerp;

      gsap.set(target, {
        x: currentX,
        y: currentY,
      });

      animationFrameId = requestAnimationFrame(animate);
    };

    container.addEventListener('mousemove', handleMouseMove);
    container.addEventListener('mouseleave', () => {
      targetX = 0;
      targetY = 0;
    });
    
    animate();

    return () => {
      container.removeEventListener('mousemove', handleMouseMove);
      cancelAnimationFrame(animationFrameId);
    };
  }, [strength, lerp]);

  return { containerRef, targetRef };
}
