/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        primary: {
          50:  '#FBEDF1',
          100: '#F8E9EE',
          200: '#F0C9D4',
          300: '#DF93A8',
          400: '#C65D7B',
          500: '#B42345',
          600: '#790D2B',
          700: '#5F0A22',
          800: '#4D071B',
          900: '#3A0514',
        },
        danger: {
          50:  '#FDF1F2',
          100: '#F9DEE1',
          300: '#E89AA3',
          400: '#D95F6C',
          500: '#C6283D',
          600: '#A71F31',
          700: '#7F1625',
        },
        warning: {
          50:  '#FFFBEB',
          100: '#FEF3C7',
          300: '#FCD34D',
          400: '#D9A21B',
          500: '#C78316',
          600: '#A66E12',
        },
        success: {
          50:  '#EFF8F2',
          100: '#DDF0E3',
          300: '#8FD0A3',
          400: '#3DA56A',
          500: '#16845B',
          600: '#116A49',
        },
        ink: {
          50:  '#F7F7FA',
          100: '#EDEDF1',
          200: '#E7E4E8',
          300: '#C9C6CE',
          500: '#686873',
          700: '#3A3A44',
          900: '#17171C',
        },
        dark: {
          800: '#1e293b',
          900: '#0f172a',
          950: '#020617',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      },
      animation: {
        'pulse-slow': 'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'fade-in': 'fadeIn 0.3s ease-in-out',
        'slide-up': 'slideUp 0.3s ease-out',
        'scale-in': 'scaleIn 0.22s cubic-bezier(0.16, 1, 0.3, 1)',
        'float': 'float 6s ease-in-out infinite',
        'blob': 'blob 22s ease-in-out infinite',
        'shine': 'shine 2.4s linear infinite',
        'ring-pulse': 'ringPulse 2.2s cubic-bezier(0.16, 1, 0.3, 1) infinite',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%': { transform: 'translateY(10px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        scaleIn: {
          '0%': { transform: 'scale(0.92) translateY(6px)', opacity: '0' },
          '100%': { transform: 'scale(1) translateY(0)', opacity: '1' },
        },
        float: {
          '0%, 100%': { transform: 'translateY(0)' },
          '50%': { transform: 'translateY(-6px)' },
        },
        blob: {
          '0%, 100%': { transform: 'translate(0, 0) scale(1)' },
          '33%': { transform: 'translate(40px, -30px) scale(1.08)' },
          '66%': { transform: 'translate(-30px, 25px) scale(0.95)' },
        },
        shine: {
          '0%': { backgroundPosition: '-200% 0' },
          '100%': { backgroundPosition: '200% 0' },
        },
        wiggle: {
          '0%, 100%': { transform: 'rotate(0deg)' },
          '20%': { transform: 'rotate(14deg)' },
          '40%': { transform: 'rotate(-10deg)' },
          '60%': { transform: 'rotate(6deg)' },
          '80%': { transform: 'rotate(-3deg)' },
        },
        ringPulse: {
          '0%': { transform: 'scale(1)', opacity: '0.55' },
          '100%': { transform: 'scale(1.6)', opacity: '0' },
        },
      },
    },
  },
  plugins: [],
}
