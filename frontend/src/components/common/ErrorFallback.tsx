import React from "react";
import { AlertTriangle, RefreshCw } from "lucide-react";
import { Button } from "@/components/ui/button";

interface ErrorFallbackProps {
  title?: string;
  message?: string;
  onRetry?: () => void;
  className?: string;
}

export function ErrorFallback({
  title = "Something went wrong",
  message = "Failed to load content from the catalog. Please try again.",
  onRetry,
  className = "",
}: ErrorFallbackProps) {
  return (
    <div
      className={`flex flex-col items-center justify-center p-6 sm:p-8 rounded-xl bg-[#131318] border border-rose-500/20 text-center max-w-md mx-auto my-4 shadow-lg ${className}`}
    >
      <div className="w-12 h-12 rounded-full bg-rose-500/10 border border-rose-500/30 flex items-center justify-center text-rose-500 mb-3">
        <AlertTriangle className="w-6 h-6" />
      </div>
      <h3 className="text-base font-semibold text-white mb-1">{title}</h3>
      <p className="text-xs text-zinc-400 mb-4 max-w-xs">{message}</p>
      {onRetry && (
        <Button
          onClick={onRetry}
          variant="outline"
          size="sm"
          className="border-rose-500/30 text-rose-300 hover:bg-rose-500/10 gap-2 rounded-lg"
        >
          <RefreshCw className="w-3.5 h-3.5" />
          <span>Try Again</span>
        </Button>
      )}
    </div>
  );
}
