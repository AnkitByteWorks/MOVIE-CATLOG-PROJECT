'use client';

import { Suspense } from 'react';
import { RegisterForm } from '@/components/auth/RegisterForm';

function RegisterContent() {
  return (
    <div className="min-h-screen pt-24 pb-16 px-4 flex flex-col justify-center items-center relative overflow-hidden">
      {/* Background radial glow */}
      <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-rose-600/10 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute bottom-1/4 left-1/2 -translate-x-1/2 translate-y-1/2 w-96 h-96 bg-amber-500/5 rounded-full blur-3xl pointer-events-none" />

      <RegisterForm />
    </div>
  );
}

export default function RegisterPage() {
  return (
    <Suspense fallback={<div className="min-h-screen pt-28 text-center text-zinc-400">Loading...</div>}>
      <RegisterContent />
    </Suspense>
  );
}
