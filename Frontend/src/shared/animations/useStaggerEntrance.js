import { useEffect, useRef } from 'react';
import gsap from 'gsap';

export function useStaggerEntrance({
  selector = '.stagger-item',
  y = 40,
  stagger = 0.1,
  duration = 0.8,
  ease = 'power3.out',
  delay = 0,
} = {}) {
  const containerRef = useRef(null);

  useEffect(() => {
    const ctx = gsap.context(() => {
      gsap.from(selector, {
        y,
        opacity: 0,
        stagger,
        duration,
        ease,
        delay,
        clearProps: 'all'
      });
    }, containerRef);

    return () => ctx.revert();
  }, [selector, y, stagger, duration, ease, delay]);

  return containerRef;
}
