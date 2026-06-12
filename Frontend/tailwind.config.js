/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // New modern palette
        primary: {
          DEFAULT: "#6366f1", // Indigo
          light: "#818cf8",
          dark: "#4f46e5",
          50: "#eef2ff",
          100: "#e0e7ff",
          200: "#c7d2fe",
          500: "#6366f1",
          600: "#4f46e5",
          700: "#4338ca",
          900: "#312e81",
        },
        accent: {
          DEFAULT: "#f59e0b", // Amber
          light: "#fbbf24",
          dark: "#d97706",
        },
        success: "#10b981",
        warning: "#f59e0b",
        danger: "#ef4444",
        info: "#3b82f6",
        
        // Surface colors
        surface: {
          DEFAULT: "#ffffff",
          50: "#f8fafc",
          100: "#f1f5f9",
          200: "#e2e8f0",
          300: "#cbd5e1",
        },
        dark: {
          DEFAULT: "#0f172a",
          50: "#1e293b",
          100: "#334155",
          200: "#475569",
          300: "#64748b",
        },
        
        // Legacy aliases for compatibility
        parchment: "#f8fafc",
        mahogany: {
          DEFAULT: "#6366f1",
          dark: "#4f46e5",
          light: "#818cf8"
        },
        gold: {
          DEFAULT: "#f59e0b",
          muted: "#d97706",
          light: "#fef3c7"
        },
        charcoal: "#1e293b",
        muted: "#64748b",
        borderline: "#e2e8f0",
        "surface-alt": "#f1f5f9",
      },
      fontFamily: {
        sans: ['"Inter"', 'system-ui', '-apple-system', 'sans-serif'],
        display: ['"Inter"', 'system-ui', 'sans-serif'],
        mono: ['"JetBrains Mono"', 'monospace'],
        // Legacy alias
        serif: ['"Inter"', 'system-ui', 'sans-serif'],
      },
      borderRadius: {
        '4xl': '2rem',
      },
      boxShadow: {
        'card': '0 1px 3px rgba(0,0,0,0.04), 0 1px 2px rgba(0,0,0,0.06)',
        'card-hover': '0 20px 40px rgba(0,0,0,0.08), 0 8px 16px rgba(0,0,0,0.06)',
        'modal': '0 25px 60px rgba(0,0,0,0.2)',
        'navbar': '0 1px 3px rgba(0,0,0,0.05)',
        'elevated': '0 20px 40px rgba(0,0,0,0.12)',
        'glow': '0 0 20px rgba(99,102,241,0.15)',
        'glow-accent': '0 0 20px rgba(245,158,11,0.2)',
        'inner-glow': 'inset 0 1px 0 0 rgba(255,255,255,0.05)',
      },
      backgroundImage: {
        'gradient-radial': 'radial-gradient(var(--tw-gradient-stops))',
        'hero-pattern': 'linear-gradient(135deg, #0f172a 0%, #1e1b4b 50%, #312e81 100%)',
        'glass': 'linear-gradient(135deg, rgba(255,255,255,0.1), rgba(255,255,255,0.05))',
        'shimmer-gradient': 'linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%)',
      },
      keyframes: {
        'fade-in': {
          '0%': { opacity: '0', transform: 'translateY(10px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' }
        },
        'fade-in-up': {
          '0%': { opacity: '0', transform: 'translateY(20px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' }
        },
        'slide-in-right': {
          '0%': { transform: 'translateX(100%)' },
          '100%': { transform: 'translateX(0)' }
        },
        'slide-out-right': {
          '0%': { transform: 'translateX(0)' },
          '100%': { transform: 'translateX(100%)' }
        },
        'slide-in-top': {
          '0%': { opacity: '0', transform: 'translateY(-10px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' }
        },
        'scale-in': {
          '0%': { opacity: '0', transform: 'scale(0.95)' },
          '100%': { opacity: '1', transform: 'scale(1)' }
        },
        'shimmer': {
          '0%': { backgroundPosition: '-200% 0' },
          '100%': { backgroundPosition: '200% 0' }
        },
        'pulse-glow': {
          '0%, 100%': { boxShadow: '0 0 0 0 rgba(99,102,241,0.4)' },
          '50%': { boxShadow: '0 0 0 10px rgba(99,102,241,0)' }
        },
        'bid-flash': {
          '0%': { backgroundColor: 'rgba(99,102,241,0.2)' },
          '100%': { backgroundColor: 'transparent' }
        },
        'count-up': {
          '0%': { opacity: '0', transform: 'translateY(10px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' }
        },
        'float': {
          '0%, 100%': { transform: 'translateY(0)' },
          '50%': { transform: 'translateY(-10px)' }
        },
        'gradient-shift': {
          '0%, 100%': { backgroundPosition: '0% 50%' },
          '50%': { backgroundPosition: '100% 50%' }
        },
        'reveal-up': {
          '0%': { opacity: '0', transform: 'translateY(60px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' }
        },
        'reveal-left': {
          '0%': { opacity: '0', transform: 'translateX(-60px)' },
          '100%': { opacity: '1', transform: 'translateX(0)' }
        },
        'reveal-right': {
          '0%': { opacity: '0', transform: 'translateX(60px)' },
          '100%': { opacity: '1', transform: 'translateX(0)' }
        },
        'reveal-scale': {
          '0%': { opacity: '0', transform: 'scale(0.8)' },
          '100%': { opacity: '1', transform: 'scale(1)' }
        },
        'rotate-in': {
          '0%': { opacity: '0', transform: 'rotate(-10deg) scale(0.9)' },
          '100%': { opacity: '1', transform: 'rotate(0) scale(1)' }
        },
        'blur-in': {
          '0%': { opacity: '0', filter: 'blur(10px)' },
          '100%': { opacity: '1', filter: 'blur(0)' }
        },
        'bounce-in': {
          '0%': { opacity: '0', transform: 'scale(0.3)' },
          '40%': { opacity: '1', transform: 'scale(1.05)' },
          '60%': { transform: 'scale(0.97)' },
          '80%': { transform: 'scale(1.02)' },
          '100%': { transform: 'scale(1)' }
        },
        'typewriter': {
          '0%': { width: '0' },
          '100%': { width: '100%' }
        },
        'glow-pulse': {
          '0%, 100%': { boxShadow: '0 0 5px rgba(99,102,241,0.2), 0 0 20px rgba(99,102,241,0.1)' },
          '50%': { boxShadow: '0 0 20px rgba(99,102,241,0.4), 0 0 60px rgba(99,102,241,0.2)' }
        },
        'float-rotate': {
          '0%, 100%': { transform: 'translateY(0) rotate(0deg)' },
          '25%': { transform: 'translateY(-8px) rotate(2deg)' },
          '75%': { transform: 'translateY(-3px) rotate(-1deg)' }
        },
        'shimmer-slide': {
          '0%': { transform: 'translateX(-100%)' },
          '100%': { transform: 'translateX(100%)' }
        },
        'antique-glow': {
          '0%, 100%': { boxShadow: '0 0 10px rgba(245,158,11,0.1), 0 0 30px rgba(245,158,11,0.05)' },
          '50%': { boxShadow: '0 0 20px rgba(245,158,11,0.3), 0 0 60px rgba(245,158,11,0.1)' }
        },
        'confetti-fall': {
          '0%': { transform: 'translateY(-100vh) rotate(0deg)', opacity: '1' },
          '100%': { transform: 'translateY(100vh) rotate(720deg)', opacity: '0' }
        },
        'ripple': {
          '0%': { transform: 'scale(0)', opacity: '1' },
          '100%': { transform: 'scale(4)', opacity: '0' }
        },
        'number-pop': {
          '0%, 100%': { transform: 'scale(1)' },
          '50%': { transform: 'scale(1.1)' }
        },
        'slide-up-fade': {
          '0%': { transform: 'translateY(30px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' }
        }
      },
      animation: {
        'fade-in': 'fade-in 0.4s ease-out',
        'fade-in-up': 'fade-in-up 0.5s ease-out',
        'slide-in-right': 'slide-in-right 0.3s ease-out',
        'slide-out-right': 'slide-out-right 0.3s ease-out',
        'slide-in-top': 'slide-in-top 0.3s ease-out',
        'scale-in': 'scale-in 0.3s ease-out',
        'shimmer': 'shimmer 1.5s infinite linear',
        'pulse-glow': 'pulse-glow 2s infinite',
        'bid-flash': 'bid-flash 0.6s ease-out',
        'count-up': 'count-up 0.3s ease-out',
        'float': 'float 3s ease-in-out infinite',
        'gradient-shift': 'gradient-shift 3s ease infinite',
        'reveal-up': 'reveal-up 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards',
        'reveal-left': 'reveal-left 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards',
        'reveal-right': 'reveal-right 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards',
        'reveal-scale': 'reveal-scale 0.7s cubic-bezier(0.16, 1, 0.3, 1) forwards',
        'rotate-in': 'rotate-in 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards',
        'blur-in': 'blur-in 0.6s ease-out forwards',
        'bounce-in': 'bounce-in 0.8s ease-out forwards',
        'glow-pulse': 'glow-pulse 3s ease-in-out infinite',
        'float-rotate': 'float-rotate 6s ease-in-out infinite',
        'shimmer-slide': 'shimmer-slide 2s ease-in-out infinite',
        'antique-glow': 'antique-glow 4s ease-in-out infinite',
        'confetti-fall': 'confetti-fall 3s ease-in-out forwards',
        'ripple': 'ripple 0.6s ease-out',
        'number-pop': 'number-pop 0.3s ease-out',
        'slide-up-fade': 'slide-up-fade 0.5s ease-out',
        // Legacy aliases
        'pulse-gold': 'pulse-glow 2s infinite',
      }
    },
  },
  plugins: [],
}
