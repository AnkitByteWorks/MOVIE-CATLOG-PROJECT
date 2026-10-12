import { useAuthStore } from './store/authStore';
import type { AuthUser } from './types';

export const ACCESS_TOKEN_KEY = 'access_token';

export function getAccessToken(): string | null {
  const storeToken = useAuthStore.getState().accessToken;
  if (storeToken) return storeToken;
  if (typeof window !== 'undefined') {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }
  return null;
}

export function setAccessToken(token: string): void {
  const currentRefresh = useAuthStore.getState().refreshToken || '';
  useAuthStore.getState().setTokens(token.trim(), currentRefresh);
  if (typeof window !== 'undefined') {
    localStorage.setItem(ACCESS_TOKEN_KEY, token.trim());
    window.dispatchEvent(new Event('auth-changed'));
  }
}

export function removeAccessToken(): void {
  useAuthStore.getState().logout();
  if (typeof window !== 'undefined') {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    window.dispatchEvent(new Event('auth-changed'));
  }
}

export function isAuthenticated(): boolean {
  const token = getAccessToken();
  if (!token) return false;
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return false;
    const payload = JSON.parse(atob(parts[1]));
    if (payload.exp && payload.exp * 1000 < Date.now()) {
      return false;
    }
    return true;
  } catch {
    return false;
  }
}

export function getAuthUser(): AuthUser | null {
  const storeUser = useAuthStore.getState().user;
  if (storeUser) return storeUser;
  const token = getAccessToken();
  if (!token) return null;
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    const payload = JSON.parse(atob(parts[1]));
    return {
      id: payload.uid || payload.sub || '',
      username: payload.sub || '',
      email: payload.email || '',
      displayName: payload.displayName || payload.sub || null,
      role: (payload.role ? payload.role.replace('ROLE_', '') : 'USER') as 'USER' | 'ADMIN' | 'MODERATOR',
      enabled: true,
      emailVerified: false,
      createdAt: new Date().toISOString(),
      lastLoginAt: null,
    };
  } catch {
    return null;
  }
}

export { useAuthStore };
