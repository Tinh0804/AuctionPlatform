import { useEffect, useRef, useState, useCallback } from 'react';

/**
 * Hook for scroll-triggered animations using Intersection Observer
 * @param {Object} options
 * @param {number} options.threshold - Visibility threshold (0-1)
 * @param {string} options.rootMargin - Root margin
 * @param {boolean} options.triggerOnce - Only trigger once
 * @returns {[ref, isVisible]}
 */
export function useScrollAnimation({ threshold = 0.1, rootMargin = '0px 0px -50px 0px', triggerOnce = true } = {}) {
    const ref = useRef(null);
    const [isVisible, setIsVisible] = useState(false);

    useEffect(() => {
        const element = ref.current;
        if (!element) return;

        const observer = new IntersectionObserver(
            ([entry]) => {
                if (entry.isIntersecting) {
                    setIsVisible(true);
                    if (triggerOnce) {
                        observer.unobserve(element);
                    }
                } else if (!triggerOnce) {
                    setIsVisible(false);
                }
            },
            { threshold, rootMargin }
        );

        observer.observe(element);
        return () => observer.disconnect();
    }, [threshold, rootMargin, triggerOnce]);

    return [ref, isVisible];
}

/**
 * Hook for staggered children animations
 * @param {number} itemCount - Number of items
 * @param {number} staggerDelay - Delay between each item in ms
 * @param {Object} options - IntersectionObserver options
 */
export function useStaggerAnimation(itemCount, staggerDelay = 100, options = {}) {
    const [containerRef, isVisible] = useScrollAnimation(options);
    const [visibleItems, setVisibleItems] = useState([]);

    useEffect(() => {
        if (isVisible) {
            const timers = [];
            for (let i = 0; i < itemCount; i++) {
                timers.push(
                    setTimeout(() => {
                        setVisibleItems(prev => [...prev, i]);
                    }, i * staggerDelay)
                );
            }
            return () => timers.forEach(clearTimeout);
        }
    }, [isVisible, itemCount, staggerDelay]);

    return [containerRef, visibleItems];
}

/**
 * Hook for parallax scrolling effect
 * @param {number} speed - Parallax speed (0.1 = subtle, 0.5 = dramatic)
 */
export function useParallax(speed = 0.3) {
    const ref = useRef(null);
    const [offset, setOffset] = useState(0);

    useEffect(() => {
        const handleScroll = () => {
            if (!ref.current) return;
            const rect = ref.current.getBoundingClientRect();
            const scrollProgress = (window.innerHeight - rect.top) / (window.innerHeight + rect.height);
            setOffset(scrollProgress * speed * 100);
        };

        window.addEventListener('scroll', handleScroll, { passive: true });
        handleScroll();
        return () => window.removeEventListener('scroll', handleScroll);
    }, [speed]);

    return [ref, offset];
}

/**
 * Hook for counting animation (number counter)
 * @param {number} end - Target number
 * @param {number} duration - Animation duration in ms
 * @param {boolean} start - Whether to start counting
 */
export function useCountUp(end, duration = 2000, start = false) {
    const [count, setCount] = useState(0);

    useEffect(() => {
        if (!start) return;
        
        let startTime;
        let animationFrame;

        const animate = (timestamp) => {
            if (!startTime) startTime = timestamp;
            const progress = Math.min((timestamp - startTime) / duration, 1);
            const eased = 1 - Math.pow(1 - progress, 3); // easeOutCubic
            setCount(Math.floor(eased * end));
            
            if (progress < 1) {
                animationFrame = requestAnimationFrame(animate);
            }
        };

        animationFrame = requestAnimationFrame(animate);
        return () => cancelAnimationFrame(animationFrame);
    }, [end, duration, start]);

    return count;
}

export default useScrollAnimation;
