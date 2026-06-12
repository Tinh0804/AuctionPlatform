import { createContext, useContext, useCallback, useEffect, useState } from 'react';
import { CheckCircle, XCircle, AlertTriangle, Info, X } from 'lucide-react';

const toastConfig = {
    success: {
        icon: <CheckCircle className="w-5 h-5 shrink-0" />,
        bgColor: 'bg-gradient-to-r from-emerald-50 to-emerald-100/50',
        borderColor: 'border-emerald-400/40',
        iconColor: 'text-emerald-600',
        textColor: 'text-emerald-900',
        shadowColor: 'shadow-[0_8px_32px_rgba(16,185,129,0.25)]'
    },
    error: {
        icon: <XCircle className="w-5 h-5 shrink-0" />,
        bgColor: 'bg-gradient-to-r from-red-50 to-red-100/50',
        borderColor: 'border-red-400/40',
        iconColor: 'text-red-600',
        textColor: 'text-red-900',
        shadowColor: 'shadow-[0_8px_32px_rgba(239,68,68,0.25)]'
    },
    warning: {
        icon: <AlertTriangle className="w-5 h-5 shrink-0" />,
        bgColor: 'bg-gradient-to-r from-amber-200 to-yellow-100',
        borderColor: 'border-amber-600/70',
        iconColor: 'text-amber-800',
        textColor: 'text-amber-950',
        shadowColor: 'shadow-[0_8px_28px_rgba(217,119,6,0.32)]'
    },
    info: {
        icon: <Info className="w-5 h-5 shrink-0" />,
        bgColor: 'bg-gradient-to-r from-[#FFF8ED] to-[#F8F1E6]',
        borderColor: 'border-[#9A6A2F]/40',
        iconColor: 'text-[#9A6A2F]',
        textColor: 'text-[#2F2418]',
        shadowColor: 'shadow-[0_8px_32px_rgba(154,106,47,0.25)]'
    },
};

const Toast = ({ message, type = 'info', onClose, duration = 4000 }) => {
    const [visible, setVisible] = useState(false);
    const [exiting, setExiting] = useState(false);
    const config = toastConfig[type] || toastConfig.info;

    useEffect(() => {
        requestAnimationFrame(() => setVisible(true));

        const timer = setTimeout(() => {
            setExiting(true);
            setTimeout(onClose, 300);
        }, duration);

        return () => clearTimeout(timer);
    }, [duration, onClose]);

    return (
        <div className={`fixed top-24 right-4 z-[9999] w-[min(22rem,calc(100vw-2rem))] transition-all duration-500 ease-out ${
            visible && !exiting 
                ? 'translate-x-0 opacity-100 scale-100' 
                : 'translate-x-12 opacity-0 scale-95'
        }`}>
            <div className={`flex items-start gap-3 px-4 py-3 rounded-2xl border-2 backdrop-blur-sm ${config.bgColor} ${config.borderColor} ${config.shadowColor}`}>
                <div className={`${config.iconColor} mt-0.5`}>
                    {config.icon}
                </div>
                <p className={`text-xs font-semibold ${config.textColor} flex-grow leading-relaxed`}>
                    {message}
                </p>
                <button 
                    onClick={() => { setExiting(true); setTimeout(onClose, 300); }} 
                    className={`p-1 ${config.textColor} opacity-50 hover:opacity-100 hover:bg-black/5 transition-all shrink-0`}
                    aria-label="Close"
                >
                    <X className="w-4 h-4" />
                </button>
            </div>
        </div>
    );
};

// Toast Context
const ToastContext = createContext(null);

export const useToast = () => {
    const context = useContext(ToastContext);
    if (!context) {
        throw new Error('useToast must be used within a ToastProvider');
    }
    return context;
};

export const ToastProvider = ({ children }) => {
    const [toasts, setToasts] = useState([]);

    const addToast = useCallback((message, type = 'info', duration = 4000) => {
        const id = Date.now() + Math.random();
        setToasts(prev => [...prev, { id, message, type, duration }]);
    }, []);

    const removeToast = useCallback((id) => {
        setToasts(prev => prev.filter(t => t.id !== id));
    }, []);

    const toast = {
        success: (msg, dur) => addToast(msg, 'success', dur),
        error: (msg, dur) => addToast(msg, 'error', dur),
        warning: (msg, dur) => addToast(msg, 'warning', dur),
        info: (msg, dur) => addToast(msg, 'info', dur),
    };

    return (
        <ToastContext.Provider value={{ addToast, toast }}>
            {children}
            {toasts.map(t => (
                <Toast key={t.id} message={t.message} type={t.type} duration={t.duration} onClose={() => removeToast(t.id)} />
            ))}
        </ToastContext.Provider>
    );
};

export default Toast;
