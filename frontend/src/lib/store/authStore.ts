import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import type { AuthResponse, AuthUser } from '../types';

interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  user: AuthUser | null;
  isHydrated: boolean;
  setAuth: (response: AuthResponse) => void;
  setUser: (user: AuthUser) => void;
  setTokens: (accessToken: string, refreshToken: string) => void;
  logout: () => void;
  setHydrated: (val: boolean) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      accessToken: null,
      refreshToken: null,
      user: null,
      isHydrated: false,

      setAuth: (response: AuthResponse) => {
        if (typeof window !== 'undefined') {
          localStorage.setItem('access_token', response.accessToken);
        }
        set({
          accessToken: response.accessToken,
          refreshToken: response.refreshToken,
          user: response.user,
        });
      },

      setUser: (user: AuthUser) => {
        set({ user });
      },

      setTokens: (accessToken: string, refreshToken: string) => {
        if (typeof window !== 'undefined') {
          localStorage.setItem('access_token', accessToken);
        }
        set({ accessToken, refreshToken });
      },

      logout: () => {
        if (typeof window !== 'undefined') {
          localStorage.removeItem('access_token');
        }
        set({
          accessToken: null,
          refreshToken: null,
          user: null,
        });
      },

      setHydrated: (val: boolean) => {
        set({ isHydrated: val });
      },
    }),
    {
      name: 'cinepulse-auth',
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        user: state.user,
      }),
      onRehydrateStorage: () => (state) => {
        if (state) {
          state.setHydrated(true);
          // Sync access_token for legacy readers if exists
          if (state.accessToken && typeof window !== 'undefined') {
            localStorage.setItem('access_token', state.accessToken);
          }
        }
      },
    }
  )
);
