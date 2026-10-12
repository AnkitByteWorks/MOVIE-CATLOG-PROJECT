"use client";

import * as React from "react";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
import {
  Check,
  Eye,
  RotateCcw,
  Pencil,
  Trash2,
  ChevronDown,
  Loader2,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { LogWatchDialog } from "./LogWatchDialog";
import {
  useMovieWatchState,
  useRewatch,
  useDeleteWatchLog,
} from "@/lib/hooks/useWatchlog";
import { isAuthenticated } from "@/lib/auth";
import { cn } from "@/lib/utils";

interface LogWatchButtonProps {
  tmdbId: number;
  mediaType?: "MOVIE" | "TV";
  title: string;
  posterUrl?: string | null;
  backdropUrl?: string | null;
  size?: "sm" | "default" | "lg";
  className?: string;
  variant?: "default" | "secondary" | "outline";
}

export function LogWatchButton({
  tmdbId,
  mediaType = "MOVIE",
  title,
  posterUrl,
  backdropUrl,
  size = "default",
  className,
}: LogWatchButtonProps) {
  const router = useRouter();
  const [dialogOpen, setDialogOpen] = React.useState(false);
  const [isEditing, setIsEditing] = React.useState(false);

  const { isWatched, watchLog, rewatchCount, isLoading } = useMovieWatchState(
    tmdbId,
    mediaType
  );
  const rewatchMutation = useRewatch();
  const deleteMutation = useDeleteWatchLog();

  const handleInitialClick = () => {
    if (!isAuthenticated()) {
      toast.error("Please log in first to record your watch.");
      const currentPath = typeof window !== "undefined" ? window.location.pathname + window.location.search : "/";
      router.push(`/login?from=${encodeURIComponent(currentPath)}`);
      return;
    }

    setIsEditing(false);
    setDialogOpen(true);
  };

  const handleEditClick = () => {
    setIsEditing(true);
    setDialogOpen(true);
  };

  const handleRewatchClick = () => {
    if (!watchLog?.id) return;
    rewatchMutation.mutate(watchLog.id);
  };

  const handleDeleteClick = () => {
    if (!watchLog?.id) return;
    if (confirm(`Remove "${title}" from your watch history?`)) {
      deleteMutation.mutate(watchLog.id);
    }
  };

  if (isLoading) {
    return (
      <Button
        size={size}
        variant="outline"
        disabled
        className={cn("gap-2 border-border/40 bg-background/30", className)}
      >
        <Loader2 className="w-4 h-4 animate-spin text-muted-foreground" />
        <span>Checking...</span>
      </Button>
    );
  }

  return (
    <>
      <div className={cn("inline-flex items-center", className)}>
        {!isWatched ? (
          <Button
            size={size}
            onClick={handleInitialClick}
            className="gap-2 bg-gradient-to-r from-rose-600 to-rose-700 hover:from-rose-500 hover:to-rose-600 text-white font-medium shadow-md shadow-rose-950/40 transition-all hover:scale-[1.02] active:scale-[0.98]"
          >
            <Eye className="w-4 h-4 text-white" />
            <span>Log Watch</span>
          </Button>
        ) : (
          <div className="inline-flex rounded-lg shadow-md border border-emerald-500/30 bg-emerald-950/20 divide-x divide-emerald-500/30 overflow-hidden">
            <button
              type="button"
              onClick={handleRewatchClick}
              disabled={rewatchMutation.isPending}
              className="flex items-center gap-1.5 px-3 py-2 text-xs md:text-sm font-semibold text-emerald-300 hover:bg-emerald-900/30 hover:text-emerald-200 transition-colors"
              title="Click to quickly log a rewatch"
            >
              {rewatchMutation.isPending ? (
                <Loader2 className="w-4 h-4 animate-spin" />
              ) : (
                <Check className="w-4 h-4 text-emerald-400 shrink-0" />
              )}
              <span>Watched</span>
              {rewatchCount > 0 && (
                <span className="ml-1 px-1.5 py-0.2 rounded-full text-[10px] font-mono bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                  x{rewatchCount + 1}
                </span>
              )}
            </button>

            <DropdownMenu>
              <DropdownMenuTrigger
                className="px-2 py-2 text-emerald-300 hover:bg-emerald-900/30 hover:text-emerald-200 transition-colors"
                aria-label="More watch options"
              >
                <ChevronDown className="w-4 h-4" />
              </DropdownMenuTrigger>
              <DropdownMenuContent
                align="end"
                className="w-48 bg-[#18181f] border-border/50 text-foreground z-50"
              >
                <DropdownMenuItem
                  onClick={handleRewatchClick}
                  disabled={rewatchMutation.isPending}
                  className="gap-2 cursor-pointer hover:bg-white/5 focus:bg-white/5"
                >
                  <RotateCcw className="w-4 h-4 text-amber-400" />
                  <span>Rewatch (+1)</span>
                </DropdownMenuItem>

                <DropdownMenuItem
                  onClick={handleEditClick}
                  className="gap-2 cursor-pointer hover:bg-white/5 focus:bg-white/5"
                >
                  <Pencil className="w-4 h-4 text-rose-400" />
                  <span>Edit Review & Rating</span>
                </DropdownMenuItem>

                <DropdownMenuSeparator className="bg-border/30" />

                <DropdownMenuItem
                  onClick={handleDeleteClick}
                  disabled={deleteMutation.isPending}
                  className="gap-2 cursor-pointer text-red-400 hover:text-red-300 hover:bg-red-950/30 focus:bg-red-950/30 focus:text-red-300"
                >
                  <Trash2 className="w-4 h-4" />
                  <span>Remove from Watchlog</span>
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        )}
      </div>

      <LogWatchDialog
        open={dialogOpen}
        onOpenChange={setDialogOpen}
        mediaInfo={{
          tmdbId,
          mediaType,
          title,
          posterUrl,
          backdropUrl,
        }}
        initialData={isEditing ? watchLog : null}
      />
    </>
  );
}
