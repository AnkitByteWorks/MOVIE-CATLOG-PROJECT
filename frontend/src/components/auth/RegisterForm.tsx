'use client';

import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { useRouter, useSearchParams } from 'next/navigation';
import { Check, Eye, EyeOff, Film, Loader2, UserPlus, X } from 'lucide-react';
import { motion } from 'framer-motion';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { useRegister } from '@/lib/hooks/useAuth';

const registerSchema = z
  .object({
    username: z
      .string()
      .min(3, 'Username must be at least 3 characters')
      .max(30, 'Username cannot exceed 30 characters')
      .regex(/^[a-zA-Z0-9_]+$/, 'Username can only contain letters, numbers, and underscores'),
    displayName: z.string().max(50, 'Display name cannot exceed 50 characters').optional(),
    email: z.string().email('Please enter a valid email address'),
    password: z
      .string()
      .min(8, 'Password must be at least 8 characters')
      .max(72, 'Password cannot exceed 72 characters')
      .regex(/[A-Z]/, 'Password must contain at least one uppercase letter')
      .regex(/[a-z]/, 'Password must contain at least one lowercase letter')
      .regex(/[0-9]/, 'Password must contain at least one digit')
      .regex(/[^a-zA-Z0-9]/, 'Password must contain at least one special character'),
    confirmPassword: z.string().min(1, 'Please confirm your password'),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: 'Passwords do not match',
    path: ['confirmPassword'],
  });

type RegisterFormData = z.infer<typeof registerSchema>;

export function RegisterForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const from = searchParams.get('from') || '/';

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const registerMutation = useRegister();

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      username: '',
      displayName: '',
      email: '',
      password: '',
      confirmPassword: '',
    },
    mode: 'onChange',
  });

  const passwordValue = watch('password') || '';

  // Password requirement checks
  const hasLength = passwordValue.length >= 8 && passwordValue.length <= 72;
  const hasUpper = /[A-Z]/.test(passwordValue);
  const hasLower = /[a-z]/.test(passwordValue);
  const hasNumber = /[0-9]/.test(passwordValue);
  const hasSpecial = /[^a-zA-Z0-9]/.test(passwordValue);

  const passedCount = [hasLength, hasUpper, hasLower, hasNumber, hasSpecial].filter(Boolean).length;

  let strengthLabel = 'Very Weak';
  let strengthColor = 'bg-rose-500';
  let strengthTextColor = 'text-rose-400';
  const strengthPercent = (passedCount / 5) * 100;

  if (passedCount >= 5) {
    strengthLabel = 'Strong';
    strengthColor = 'bg-emerald-500';
    strengthTextColor = 'text-emerald-400';
  } else if (passedCount >= 3) {
    strengthLabel = 'Moderate';
    strengthColor = 'bg-amber-500';
    strengthTextColor = 'text-amber-400';
  }

  const onSubmit = (data: RegisterFormData) => {
    registerMutation.mutate(
      {
        username: data.username,
        displayName: data.displayName || undefined,
        email: data.email,
        password: data.password,
      },
      {
        onSuccess: () => {
          router.push(from);
        },
      }
    );
  };

  const isLoading = isSubmitting || registerMutation.isPending;

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3 }}
      className="w-full max-w-md mx-auto"
    >
      <Card className="bg-[#131318]/90 border-white/10 backdrop-blur-xl shadow-2xl shadow-rose-950/20 text-white">
        <CardHeader className="space-y-2 text-center pb-6">
          <div className="flex justify-center mb-1">
            <div className="h-12 w-12 rounded-2xl bg-gradient-to-tr from-rose-600 to-amber-500 flex items-center justify-center shadow-lg shadow-rose-600/30">
              <Film className="h-6 w-6 text-white" />
            </div>
          </div>
          <CardTitle className="text-2xl font-bold font-serif tracking-tight">Create Account</CardTitle>
          <CardDescription className="text-zinc-400 text-sm">
            Join CinePulse to log your movies, track watch streaks, and build your cinematic library
          </CardDescription>
        </CardHeader>

        <form onSubmit={handleSubmit(onSubmit)}>
          <CardContent className="space-y-4">
            <div className="space-y-1.5">
              <label className="text-xs font-semibold uppercase tracking-wider text-zinc-300" htmlFor="username">
                Username *
              </label>
              <Input
                id="username"
                type="text"
                placeholder="cinéphile99"
                disabled={isLoading}
                {...register('username')}
                className="bg-black/40 border-white/10 text-white placeholder:text-zinc-500 focus-visible:ring-rose-500"
              />
              {errors.username && <p className="text-xs text-rose-400 mt-1">{errors.username.message}</p>}
            </div>

            <div className="space-y-1.5">
              <label className="text-xs font-semibold uppercase tracking-wider text-zinc-300" htmlFor="displayName">
                Display Name (Optional)
              </label>
              <Input
                id="displayName"
                type="text"
                placeholder="Alex Morgan"
                disabled={isLoading}
                {...register('displayName')}
                className="bg-black/40 border-white/10 text-white placeholder:text-zinc-500 focus-visible:ring-rose-500"
              />
              {errors.displayName && <p className="text-xs text-rose-400 mt-1">{errors.displayName.message}</p>}
            </div>

            <div className="space-y-1.5">
              <label className="text-xs font-semibold uppercase tracking-wider text-zinc-300" htmlFor="email">
                Email Address *
              </label>
              <Input
                id="email"
                type="email"
                placeholder="alex@example.com"
                disabled={isLoading}
                {...register('email')}
                className="bg-black/40 border-white/10 text-white placeholder:text-zinc-500 focus-visible:ring-rose-500"
              />
              {errors.email && <p className="text-xs text-rose-400 mt-1">{errors.email.message}</p>}
            </div>

            <div className="space-y-1.5">
              <label className="text-xs font-semibold uppercase tracking-wider text-zinc-300" htmlFor="password">
                Password *
              </label>
              <div className="relative">
                <Input
                  id="password"
                  type={showPassword ? 'text' : 'password'}
                  placeholder="••••••••"
                  disabled={isLoading}
                  {...register('password')}
                  className="bg-black/40 border-white/10 text-white placeholder:text-zinc-500 pr-10 focus-visible:ring-rose-500"
                />
                <button
                  type="button"
                  tabIndex={-1}
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-zinc-400 hover:text-white transition-colors"
                >
                  {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>

              {/* Password strength meter */}
              {passwordValue && (
                <div className="mt-2 space-y-2 p-3 bg-black/50 rounded-lg border border-white/5">
                  <div className="flex justify-between items-center text-xs">
                    <span className="text-zinc-400">Strength:</span>
                    <span className={`font-semibold ${strengthTextColor}`}>{strengthLabel}</span>
                  </div>
                  <div className="h-1.5 w-full bg-zinc-800 rounded-full overflow-hidden">
                    <div
                      className={`h-full ${strengthColor} transition-all duration-300`}
                      style={{ width: `${strengthPercent}%` }}
                    />
                  </div>
                  <div className="grid grid-cols-2 gap-1 pt-1 text-[11px]">
                    <div className={`flex items-center gap-1 ${hasLength ? 'text-emerald-400' : 'text-zinc-500'}`}>
                      {hasLength ? <Check className="h-3 w-3" /> : <X className="h-3 w-3" />}
                      <span>8-72 characters</span>
                    </div>
                    <div className={`flex items-center gap-1 ${hasUpper ? 'text-emerald-400' : 'text-zinc-500'}`}>
                      {hasUpper ? <Check className="h-3 w-3" /> : <X className="h-3 w-3" />}
                      <span>Uppercase letter</span>
                    </div>
                    <div className={`flex items-center gap-1 ${hasLower ? 'text-emerald-400' : 'text-zinc-500'}`}>
                      {hasLower ? <Check className="h-3 w-3" /> : <X className="h-3 w-3" />}
                      <span>Lowercase letter</span>
                    </div>
                    <div className={`flex items-center gap-1 ${hasNumber ? 'text-emerald-400' : 'text-zinc-500'}`}>
                      {hasNumber ? <Check className="h-3 w-3" /> : <X className="h-3 w-3" />}
                      <span>Number</span>
                    </div>
                    <div className={`flex items-center gap-1 col-span-2 ${hasSpecial ? 'text-emerald-400' : 'text-zinc-500'}`}>
                      {hasSpecial ? <Check className="h-3 w-3" /> : <X className="h-3 w-3" />}
                      <span>Special character</span>
                    </div>
                  </div>
                </div>
              )}
              {errors.password && <p className="text-xs text-rose-400 mt-1">{errors.password.message}</p>}
            </div>

            <div className="space-y-1.5">
              <label className="text-xs font-semibold uppercase tracking-wider text-zinc-300" htmlFor="confirmPassword">
                Confirm Password *
              </label>
              <div className="relative">
                <Input
                  id="confirmPassword"
                  type={showConfirmPassword ? 'text' : 'password'}
                  placeholder="••••••••"
                  disabled={isLoading}
                  {...register('confirmPassword')}
                  className="bg-black/40 border-white/10 text-white placeholder:text-zinc-500 pr-10 focus-visible:ring-rose-500"
                />
                <button
                  type="button"
                  tabIndex={-1}
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-zinc-400 hover:text-white transition-colors"
                >
                  {showConfirmPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
              {errors.confirmPassword && (
                <p className="text-xs text-rose-400 mt-1">{errors.confirmPassword.message}</p>
              )}
            </div>
          </CardContent>

          <CardFooter className="flex flex-col space-y-4 pt-2">
            <Button
              type="submit"
              disabled={isLoading}
              className="w-full bg-gradient-to-r from-rose-600 to-rose-700 hover:from-rose-500 hover:to-rose-600 text-white font-medium py-2.5 rounded-lg shadow-lg shadow-rose-600/25 transition-all"
            >
              {isLoading ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Creating Account...
                </>
              ) : (
                <>
                  <UserPlus className="mr-2 h-4 w-4" />
                  Create Account
                </>
              )}
            </Button>

            <div className="text-center text-xs text-zinc-400">
              Already have an account?{' '}
              <a
                href={`/login${from !== '/' ? `?from=${encodeURIComponent(from)}` : ''}`}
                className="text-rose-400 hover:text-rose-300 font-medium underline underline-offset-4"
              >
                Sign in
              </a>
            </div>
          </CardFooter>
        </form>
      </Card>
    </motion.div>
  );
}
