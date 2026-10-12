'use client';

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';
import { useSyncExternalStore } from 'react';
import { toast } from 'sonner';
import { authApi } from '../api/auth';
import { useAuthStore } from '../store/authStore';
import type {
  ChangePasswordRequest,
  LoginRequest,
  RegisterRequest,
  UpdateProfileRequest,
} from '../types';

export function useAuth() {
  const accessToken = useAuthStore((s) => s.accessToken);
  const user = useAuthStore((s) => s.user);
  const isHydrated = useAuthStore((s) => s.isHydrated);
  const logoutAction = useAuthStore((s) => s.logout);

  return {
    token: accessToken,
    accessToken,
    isAuthenticated: !!accessToken,
    user,
    isHydrated,
    logout: logoutAction,
  };
}

export function useIsMounted(): boolean {
  return useSyncExternalStore(
    () => () => {},
    () => true,
    () => false
  );
}

export function useMe() {
  const accessToken = useAuthStore((s) => s.accessToken);
  const setUser = useAuthStore((s) => s.setUser);

  return useQuery({
    queryKey: ['auth', 'me'],
    queryFn: async () => {
      const data = await authApi.getMe();
      setUser(data);
      return data;
    },
    enabled: !!accessToken,
    staleTime: 5 * 60 * 1000,
  });
}

export function useLogin() {
  const queryClient = useQueryClient();
  const setAuth = useAuthStore((s) => s.setAuth);

  return useMutation({
    mutationFn: (data: LoginRequest) => authApi.login(data),
    onSuccess: (response) => {
      setAuth(response);
      queryClient.invalidateQueries({ queryKey: ['auth'] });
      queryClient.invalidateQueries({ queryKey: ['watchlog'] });
      queryClient.invalidateQueries({ queryKey: ['watchlist'] });
      const displayName = response.user.displayName || response.user.username;
      toast.success(`Welcome back, ${displayName}!`);
    },
    onError: (error: unknown) => {
      const err = error as { response?: { data?: { message?: string; error?: string } }; message?: string };
      const message =
        err.response?.data?.message ||
        err.response?.data?.error ||
        err.message ||
        'Invalid username/email or password';
      toast.error(message);
    },
  });
}

export function useRegister() {
  const queryClient = useQueryClient();
  const setAuth = useAuthStore((s) => s.setAuth);

  return useMutation({
    mutationFn: (data: RegisterRequest) => authApi.register(data),
    onSuccess: (response) => {
      setAuth(response);
      queryClient.invalidateQueries({ queryKey: ['auth'] });
      toast.success('Account created successfully! Welcome to CinePulse.');
    },
    onError: (error: unknown) => {
      const err = error as { response?: { data?: { message?: string; error?: string } }; message?: string };
      const message =
        err.response?.data?.message ||
        err.response?.data?.error ||
        err.message ||
        'Failed to register account';
      toast.error(message);
    },
  });
}

export function useLogout() {
  const queryClient = useQueryClient();
  const router = useRouter();
  const refreshToken = useAuthStore((s) => s.refreshToken);
  const logoutStore = useAuthStore((s) => s.logout);

  return useMutation({
    mutationFn: async () => {
      if (refreshToken) {
        try {
          await authApi.logout(refreshToken);
        } catch {
          // Ignore server errors on logout (e.g. token already expired)
        }
      }
    },
    onSettled: () => {
      logoutStore();
      queryClient.clear();
      toast.success('Logged out successfully');
      router.push('/login');
    },
  });
}

export function useUpdateProfile() {
  const queryClient = useQueryClient();
  const setUser = useAuthStore((s) => s.setUser);

  return useMutation({
    mutationFn: (data: UpdateProfileRequest) => authApi.updateProfile(data),
    onSuccess: (updatedUser) => {
      setUser(updatedUser);
      queryClient.invalidateQueries({ queryKey: ['auth', 'me'] });
      toast.success('Profile updated successfully');
    },
    onError: (error: unknown) => {
      const err = error as { response?: { data?: { message?: string; error?: string } }; message?: string };
      const message =
        err.response?.data?.message ||
        err.response?.data?.error ||
        err.message ||
        'Failed to update profile';
      toast.error(message);
    },
  });
}

export function useChangePassword() {
  return useMutation({
    mutationFn: (data: ChangePasswordRequest) => authApi.changePassword(data),
    onSuccess: () => {
      toast.success('Password changed successfully');
    },
    onError: (error: unknown) => {
      const err = error as { response?: { data?: { message?: string; error?: string } }; message?: string };
      const message =
        err.response?.data?.message ||
        err.response?.data?.error ||
        err.message ||
        'Failed to change password. Please check your current password.';
      toast.error(message);
    },
  });
}
