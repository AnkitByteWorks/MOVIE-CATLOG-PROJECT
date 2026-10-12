'use client';

import { Suspense, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { CheckCircle, ChevronDown, ChevronUp, LogOut, Sparkles } from 'lucide-react';
import { toast } from 'sonner';
import { LoginForm } from '@/components/auth/LoginForm';
import { Button } from '@/components/ui/button';
import { useAuth } from '@/lib/hooks/useAuth';
import { useAuthStore } from '@/lib/store/authStore';

const DEV_TEST_TOKEN =
  'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJtb3ZpZWJ1ZmYiLCJ1aWQiOiIwMDAwMDAwMC0wMDAwLTAwMDAtMDAwMC0wMDAwMDAwMDAwMDEiLCJlbWFpbCI6Im1vdmllYnVmZkBleGFtcGxlLmNvbSIsInJvbGUiOiJST0xFX1VTRVIiLCJpYXQiOjE3OTE0NTk5OTUsImV4cCI6MTc5MjA2NDc5NX0.c1S3_N7-NB8BNCtLKFRb7KLj4CBeAVkwk_KIYkRjjGNInjqhQP6-j8BeZs-qaSgBw7_Tjk4_tqZgiDDId9oPYA';

function LoginContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const from = searchParams.get('from') || '/';

  const { isAuthenticated, user, logout } = useAuth();
  const setTokens = useAuthStore((s) => s.setTokens);
  const setUser = useAuthStore((s) => s.setUser);

  const [showDevPanel, setShowDevPanel] = useState(false);

  const handleQuickDevLogin = () => {
    setTokens(DEV_TEST_TOKEN, 'dev-refresh-token');
    setUser({
      id: '00000000-0000-0000-0000-000000000001',
      username: 'moviebuff',
      email: 'moviebuff@example.com',
      displayName: 'Movie Buff',
      role: 'USER',
      enabled: true,
      emailVerified: true,
      createdAt: new Date().toISOString(),
      lastLoginAt: new Date().toISOString(),
    });
    toast.success("Logged in as 'moviebuff' (Dev Test User)");
    router.push(from);
  };

  return (
    <div className="min-h-screen pt-24 pb-16 px-4 flex flex-col justify-center items-center relative overflow-hidden">
      {/* Background radial glow */}
      <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-rose-600/10 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute bottom-1/4 left-1/2 -translate-x-1/2 translate-y-1/2 w-96 h-96 bg-amber-500/5 rounded-full blur-3xl pointer-events-none" />

      {/* Already logged in alert */}
      {isAuthenticated && user && (
        <div className="w-full max-w-md mb-6 p-4 rounded-xl bg-emerald-950/30 border border-emerald-500/30 flex items-center justify-between text-white backdrop-blur-md">
          <div className="flex items-center gap-3">
            <CheckCircle className="w-5 h-5 text-emerald-400 shrink-0" />
            <div>
              <p className="text-xs font-semibold text-emerald-300">Signed In</p>
              <p className="text-xs text-zinc-300">
                Logged in as <span className="font-semibold text-white">{user.displayName || user.username}</span>
              </p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <Button
              size="sm"
              variant="outline"
              onClick={() => router.push(from)}
              className="text-xs border-emerald-500/40 text-emerald-300 hover:bg-emerald-950/40"
            >
              Continue
            </Button>
            <Button
              size="sm"
              variant="ghost"
              onClick={() => logout()}
              className="text-xs text-rose-400 hover:text-rose-300 hover:bg-rose-950/20"
            >
              <LogOut className="w-3.5 h-3.5 mr-1" />
              Sign Out
            </Button>
          </div>
        </div>
      )}

      {/* Primary Login Form */}
      <LoginForm />

      {/* Optional Developer Panel */}
      <div className="w-full max-w-md mt-6">
        <button
          type="button"
          onClick={() => setShowDevPanel(!showDevPanel)}
          className="flex items-center justify-center gap-1.5 text-xs text-zinc-500 hover:text-zinc-400 mx-auto transition-colors"
        >
          <span>Developer Tools</span>
          {showDevPanel ? <ChevronUp className="w-3.5 h-3.5" /> : <ChevronDown className="w-3.5 h-3.5" />}
        </button>

        {showDevPanel && (
          <div className="mt-3 p-4 rounded-xl bg-black/40 border border-white/5 space-y-2 backdrop-blur-md">
            <div className="flex items-center justify-between text-xs">
              <span className="font-medium text-zinc-300 flex items-center gap-1.5">
                <Sparkles className="w-3.5 h-3.5 text-amber-400" />
                One-Click Test Account
              </span>
              <span className="font-mono text-zinc-500 text-[10px]">@moviebuff</span>
            </div>
            <p className="text-[11px] text-zinc-400">
              Instantly bypass login using the local mock JWT token for testing.
            </p>
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={handleQuickDevLogin}
              className="w-full text-xs border-white/10 text-zinc-300 hover:bg-white/5"
            >
              Sign In as Test User
            </Button>
          </div>
        )}
      </div>
    </div>
  );
}

export default function LoginPage() {
  return (
    <Suspense fallback={<div className="min-h-screen pt-28 text-center text-zinc-400">Loading...</div>}>
      <LoginContent />
    </Suspense>
  );
}
