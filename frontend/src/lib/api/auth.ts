import axios, { InternalAxiosRequestConfig } from 'axios';
import { useAuthStore } from '../store/authStore';
import type {
  AuthResponse,
  AuthUser,
  ChangePasswordRequest,
  LoginRequest,
  RegisterRequest,
  UpdateProfileRequest,
} from '../types';

export const userApi = axios.create({
  baseURL: process.env.NEXT_PUBLIC_USER_API_URL || 'http://localhost:8081',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
});

// Request interceptor to attach access token
userApi.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = useAuthStore.getState().accessToken;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor to refresh on 401
let isRefreshing = false;
let refreshSubscribers: ((token: string) => void)[] = [];

function onTokenRefreshed(token: string) {
  refreshSubscribers.forEach((callback) => callback(token));
  refreshSubscribers = [];
}

userApi.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve) => {
          refreshSubscribers.push((token: string) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            resolve(userApi(originalRequest));
          });
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const currentRefreshToken = useAuthStore.getState().refreshToken;
        if (!currentRefreshToken) {
          throw new Error('No refresh token available');
        }

        const { data } = await axios.post<AuthResponse>(
          `${userApi.defaults.baseURL}/api/v1/auth/refresh`,
          { refreshToken: currentRefreshToken }
        );

        useAuthStore.getState().setTokens(data.accessToken, data.refreshToken);
        onTokenRefreshed(data.accessToken);

        originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;
        return userApi(originalRequest);
      } catch (refreshError) {
        refreshSubscribers = [];
        useAuthStore.getState().logout();
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

// Typed API methods
export const authApi = {
  async register(payload: RegisterRequest): Promise<AuthResponse> {
    const response = await userApi.post<AuthResponse>('/api/v1/auth/register', payload);
    return response.data;
  },

  async login(payload: LoginRequest): Promise<AuthResponse> {
    const response = await userApi.post<AuthResponse>('/api/v1/auth/login', payload);
    return response.data;
  },

  async refresh(refreshToken: string): Promise<AuthResponse> {
    const response = await userApi.post<AuthResponse>('/api/v1/auth/refresh', { refreshToken });
    return response.data;
  },

  async logout(refreshToken: string): Promise<{ message: string }> {
    const response = await userApi.post<{ message: string }>('/api/v1/auth/logout', { refreshToken });
    return response.data;
  },

  async getMe(): Promise<AuthUser> {
    const response = await userApi.get<AuthUser>('/api/v1/auth/me');
    return response.data;
  },

  async updateProfile(payload: UpdateProfileRequest): Promise<AuthUser> {
    const response = await userApi.patch<AuthUser>('/api/v1/users/me', payload);
    return response.data;
  },

  async changePassword(payload: ChangePasswordRequest): Promise<{ message: string }> {
    const response = await userApi.post<{ message: string }>('/api/v1/users/me/change-password', payload);
    return response.data;
  },
};
