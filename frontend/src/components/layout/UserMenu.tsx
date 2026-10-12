'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import {
  BarChart3,
  Bookmark,
  Calendar,
  LogIn,
  LogOut,
  User,
  UserPlus,
} from 'lucide-react';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { useAuth, useLogout } from '@/lib/hooks/useAuth';

export function UserMenu() {
  const router = useRouter();
  const { isAuthenticated, user, isHydrated } = useAuth();
  const logoutMutation = useLogout();

  // Until hydration finishes, render a subtle skeleton placeholder
  if (!isHydrated) {
    return <div className="w-8 h-8 rounded-full bg-white/5 animate-pulse" />;
  }

  // Guest State
  if (!isAuthenticated || !user) {
    return (
      <div className="flex items-center gap-2">
        <Link href="/login">
          <Button
            variant="ghost"
            size="sm"
            className="text-xs text-zinc-300 hover:text-white hover:bg-white/5"
          >
            <LogIn className="w-3.5 h-3.5 mr-1.5 text-zinc-400" />
            Sign In
          </Button>
        </Link>
        <Link href="/register">
          <Button
            size="sm"
            className="text-xs bg-rose-600 hover:bg-rose-500 text-white font-medium shadow-sm shadow-rose-950"
          >
            <UserPlus className="w-3.5 h-3.5 mr-1.5" />
            Sign Up
          </Button>
        </Link>
      </div>
    );
  }

  // Authenticated State
  const initials = (user.displayName || user.username || 'U').slice(0, 2).toUpperCase();

  return (
    <DropdownMenu>
      <DropdownMenuTrigger
        render={
          <button
            aria-label="User navigation menu"
            className="rounded-full focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-rose-500 transition-transform active:scale-95"
          />
        }
      >
        <Avatar className="w-8 h-8 border border-white/20 hover:border-rose-500 transition-colors cursor-pointer shadow-md">
          {user.avatarUrl && <AvatarImage src={user.avatarUrl} alt={user.username} />}
          <AvatarFallback className="bg-gradient-to-tr from-rose-600 to-amber-600 text-white text-xs font-bold">
            {initials}
          </AvatarFallback>
        </Avatar>
      </DropdownMenuTrigger>

      <DropdownMenuContent
        align="end"
        className="w-56 bg-[#131318]/95 border-white/10 text-white backdrop-blur-xl shadow-2xl p-1.5"
      >
        <DropdownMenuLabel className="font-normal px-2.5 py-2">
          <div className="flex flex-col space-y-1">
            <div className="flex items-center justify-between">
              <p className="text-sm font-semibold truncate text-white">
                {user.displayName || user.username}
              </p>
              <Badge
                variant="outline"
                className="text-[10px] px-1.5 py-0 border-rose-500/40 text-rose-300 font-mono"
              >
                {user.role}
              </Badge>
            </div>
            <p className="text-xs text-zinc-400 truncate font-mono">@{user.username}</p>
          </div>
        </DropdownMenuLabel>

        <DropdownMenuSeparator className="bg-white/10 my-1" />

        <DropdownMenuGroup>
          <DropdownMenuItem
            onClick={() => router.push('/profile')}
            className="text-xs text-zinc-200 hover:text-white focus:bg-white/10 cursor-pointer rounded-md px-2.5 py-2"
          >
            <User className="w-4 h-4 mr-2 text-rose-400" />
            My Profile & Settings
          </DropdownMenuItem>
          <DropdownMenuItem
            onClick={() => router.push('/watchlog')}
            className="text-xs text-zinc-200 hover:text-white focus:bg-white/10 cursor-pointer rounded-md px-2.5 py-2"
          >
            <Calendar className="w-4 h-4 mr-2 text-amber-400" />
            Watch Diary
          </DropdownMenuItem>
          <DropdownMenuItem
            onClick={() => router.push('/stats')}
            className="text-xs text-zinc-200 hover:text-white focus:bg-white/10 cursor-pointer rounded-md px-2.5 py-2"
          >
            <BarChart3 className="w-4 h-4 mr-2 text-emerald-400" />
            My Watch Stats
          </DropdownMenuItem>
          <DropdownMenuItem
            onClick={() => router.push('/watchlist')}
            className="text-xs text-zinc-200 hover:text-white focus:bg-white/10 cursor-pointer rounded-md px-2.5 py-2"
          >
            <Bookmark className="w-4 h-4 mr-2 text-sky-400" />
            My Watchlist
          </DropdownMenuItem>
        </DropdownMenuGroup>

        <DropdownMenuSeparator className="bg-white/10 my-1" />

        <DropdownMenuItem
          onClick={() => logoutMutation.mutate()}
          disabled={logoutMutation.isPending}
          className="text-xs text-rose-400 hover:text-rose-300 focus:bg-rose-950/40 cursor-pointer rounded-md px-2.5 py-2"
        >
          <LogOut className="w-4 h-4 mr-2" />
          Sign Out
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
