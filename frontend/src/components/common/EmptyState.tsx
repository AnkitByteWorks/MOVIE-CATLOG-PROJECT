import React from "react";
import Link from "next/link";
import { Film, SearchX, LucideIcon } from "lucide-react";
import { Button, buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";

interface EmptyStateProps {
  title?: string;
  description?: string;
  icon?: "film" | "search" | LucideIcon;
  actionLabel?: string;
  actionHref?: string;
  onAction?: () => void;
  className?: string;
}

export function EmptyState({
  title = "No results found",
  description = "Try adjusting your search terms or filters to find what you're looking for.",
  icon = "search",
  actionLabel,
  actionHref,
  onAction,
  className = "",
}: EmptyStateProps) {
  const IconComponent =
    typeof icon === "function"
      ? icon
      : icon === "film"
      ? Film
      : SearchX;

  return (
    <div
      className={`flex flex-col items-center justify-center text-center p-8 sm:p-12 rounded-2xl bg-[#131318]/50 border border-white/5 max-w-lg mx-auto ${className}`}
    >
      <div className="w-16 h-16 rounded-2xl bg-rose-500/10 border border-rose-500/20 flex items-center justify-center mb-4 text-rose-500 shadow-lg shadow-rose-950/20">
        <IconComponent className="w-8 h-8" />
      </div>

      <h3 className="text-lg font-semibold text-white mb-2">{title}</h3>
      <p className="text-sm text-zinc-400 mb-6 max-w-sm">{description}</p>

      {actionLabel && actionHref && (
        <Link
          href={actionHref}
          className={cn(
            buttonVariants({ variant: "default" }),
            "bg-rose-600 hover:bg-rose-700 text-white"
          )}
        >
          {actionLabel}
        </Link>
      )}

      {actionLabel && !actionHref && onAction && (
        <Button
          onClick={onAction}
          className="bg-rose-600 hover:bg-rose-700 text-white"
        >
          {actionLabel}
        </Button>
      )}
    </div>
  );
}
