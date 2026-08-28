// ============================================================
// src/store/useAuthStore.js
// Zustand store quản lý trạng thái xác thực toàn cục
// ============================================================
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

const useAuthStore = create(
  persist(
    (set) => ({
      token: localStorage.getItem('token') || null,
      user: null,

      setToken: (token) => {
        localStorage.setItem('token', token);
        set({ token });
      },

      setUser: (user) => set({ user }),

      logout: () => {
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        set({ token: null, user: null });
      },

      isAuthenticated: () => {
        const state = useAuthStore.getState();
        return !!state.token;
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({ token: state.token }),
    }
  )
);

export default useAuthStore;
