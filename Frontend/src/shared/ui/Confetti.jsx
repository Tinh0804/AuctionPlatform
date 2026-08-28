import { useEffect, useState } from 'react';

/**
 * Confetti component for celebration effects
 * Usage: <Confetti active={true} />
 */
export default function Confetti({ active = false, duration = 3000 }) {
    const [particles, setParticles] = useState([]);

    useEffect(() => {
        if (!active) return;

        const colors = ['#f59e0b', '#10b981', '#3b82f6', '#ef4444', '#8b5cf6', '#ec4899'];
        const particleCount = 50;
        const newParticles = [];

        for (let i = 0; i < particleCount; i++) {
            newParticles.push({
                id: i,
                color: colors[Math.floor(Math.random() * colors.length)],
                left: Math.random() * 100,
                animationDelay: Math.random() * 0.5,
                size: Math.random() * 8 + 4,
            });
        }

        setParticles(newParticles);

        const timer = setTimeout(() => {
            setParticles([]);
        }, duration);

        return () => clearTimeout(timer);
    }, [active, duration]);

    if (particles.length === 0) return null;

    return (
        <div className="fixed inset-0 pointer-events-none z-[9999] overflow-hidden">
            {particles.map((particle) => (
                <div
                    key={particle.id}
                    className="absolute top-0 animate-confetti-fall"
                    style={{
                        left: `${particle.left}%`,
                        width: `${particle.size}px`,
                        height: `${particle.size}px`,
                        backgroundColor: particle.color,
                        animationDelay: `${particle.animationDelay}s`,
                        animationDuration: `${2 + Math.random()}s`,
                    }}
                />
            ))}
        </div>
    );
}
