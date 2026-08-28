import { useCallback } from 'react';

/**
 * Hook for ripple effect on click
 * Usage: const ripple = useRipple(); then onClick={ripple}
 */
export function useRipple(color = 'rgba(255, 255, 255, 0.6)') {
    return useCallback((event) => {
        const button = event.currentTarget;
        const ripple = document.createElement('span');
        const rect = button.getBoundingClientRect();
        const size = Math.max(rect.width, rect.height);
        const x = event.clientX - rect.left - size / 2;
        const y = event.clientY - rect.top - size / 2;

        ripple.style.width = ripple.style.height = `${size}px`;
        ripple.style.left = `${x}px`;
        ripple.style.top = `${y}px`;
        ripple.style.position = 'absolute';
        ripple.style.borderRadius = '50%';
        ripple.style.backgroundColor = color;
        ripple.style.pointerEvents = 'none';
        ripple.style.animation = 'ripple-animation 600ms ease-out';
        ripple.classList.add('ripple-effect');

        // Ensure button has position relative
        if (getComputedStyle(button).position === 'static') {
            button.style.position = 'relative';
        }
        button.style.overflow = 'hidden';

        button.appendChild(ripple);

        setTimeout(() => {
            ripple.remove();
        }, 600);
    }, [color]);
}

/**
 * Hook for magnetic button effect
 * Returns ref to attach to button
 */
export function useMagnetic(strength = 0.3) {
    const handleMouseMove = useCallback((e) => {
        const button = e.currentTarget;
        const rect = button.getBoundingClientRect();
        const x = e.clientX - rect.left - rect.width / 2;
        const y = e.clientY - rect.top - rect.height / 2;
        
        button.style.transform = `translate(${x * strength}px, ${y * strength}px)`;
    }, [strength]);

    const handleMouseLeave = useCallback((e) => {
        const button = e.currentTarget;
        button.style.transform = 'translate(0, 0)';
    }, []);

    return { onMouseMove: handleMouseMove, onMouseLeave: handleMouseLeave };
}

export default useRipple;
