'use client';

import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { useRouter, useSearchParams } from 'next/navigation';
import { Eye, EyeOff, Film, Loader2, LogIn } from 'lucide-react';
import { motion } from 'framer-motion';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { useLogin } from '@/lib/hooks/useAuth';

const loginSchema = z.object({
  usernameOrEmail: z.string().min(1, 'Username or email is required'),
  password: z.string().min(1, 'Password is required'),
});

type LoginFormData = z.infer<typeof loginSchema>;

export function LoginForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const from = searchParams.get('from') || '/';

  const [showPassword, setShowPassword] = useState(false);
  const loginMutation = useLogin();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      usernameOrEmail: '',
      password: '',
    },
  });

  const onSubmit = (data: LoginFormData) => {
    loginMutation.mutate(data, {
      onSuccess: () => {
        router.push(from);
      },
    });
  };

  const isLoading = isSubmitting || loginMutation.isPending;

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
          <CardTitle className="text-2xl font-bold font-serif tracking-tight">Welcome Back</CardTitle>
          <CardDescription className="text-zinc-400 text-sm">
            Sign in to your CinePulse account to sync your watchlist, logs, and stats
          </CardDescription>
        </CardHeader>

        <form onSubmit={handleSubmit(onSubmit)}>
          <CardContent className="space-y-4">
            <div className="space-y-1.5">
              <label className="text-xs font-semibold uppercase tracking-wider text-zinc-300" htmlFor="usernameOrEmail">
                Username or Email
              </label>
              <Input
                id="usernameOrEmail"
                type="text"
                placeholder="moviefan or you@example.com"
                disabled={isLoading}
                {...register('usernameOrEmail')}
                className="bg-black/40 border-white/10 text-white placeholder:text-zinc-500 focus-visible:ring-rose-500"
              />
              {errors.usernameOrEmail && (
                <p className="text-xs text-rose-400 mt-1">{errors.usernameOrEmail.message}</p>
              )}
            </div>

            <div className="space-y-1.5">
              <div className="flex items-center justify-between">
                <label className="text-xs font-semibold uppercase tracking-wider text-zinc-300" htmlFor="password">
                  Password
                </label>
              </div>
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
              {errors.password && <p className="text-xs text-rose-400 mt-1">{errors.password.message}</p>}
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
                  Signing In...
                </>
              ) : (
                <>
                  <LogIn className="mr-2 h-4 w-4" />
                  Sign In
                </>
              )}
            </Button>

            <div className="text-center text-xs text-zinc-400">
              Don&apos;t have an account?{' '}
              <a
                href={`/register${from !== '/' ? `?from=${encodeURIComponent(from)}` : ''}`}
                className="text-rose-400 hover:text-rose-300 font-medium underline underline-offset-4"
              >
                Create one now
              </a>
            </div>
          </CardFooter>
        </form>
      </Card>
    </motion.div>
  );
}
