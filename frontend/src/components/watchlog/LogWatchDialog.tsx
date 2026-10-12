"use client";

import * as React from "react";
import Image from "next/image";
import { format } from "date-fns";
import {
  Calendar as CalendarIcon,
  Film,
  Tv,
  MapPin,
  Clock,
  Loader2,
  CheckCircle2,
} from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/dialog";
import { Button, buttonVariants } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Calendar } from "@/components/ui/calendar";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { RatingStars } from "./RatingStars";
import { useCreateWatchLog, useUpdateWatchLog } from "@/lib/hooks/useWatchlog";
import { WatchLog, WatchLocation } from "@/lib/types";
import { cn } from "@/lib/utils";

interface MediaInfo {
  tmdbId: number;
  mediaType: "MOVIE" | "TV";
  title: string;
  posterUrl?: string | null;
  backdropUrl?: string | null;
}

interface LogWatchDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  mediaInfo: MediaInfo;
  initialData?: WatchLog | null;
  onSuccess?: () => void;
}

const LOCATION_OPTIONS: { value: WatchLocation; label: string; icon: string }[] = [
  { value: "HOME", label: "Home", icon: "🛋️" },
  { value: "THEATER", label: "Theater", icon: "🎬" },
  { value: "STREAMING", label: "Streaming", icon: "📺" },
  { value: "PLANE", label: "Plane", icon: "✈️" },
  { value: "OTHER", label: "Other", icon: "🍿" },
];

function LogWatchForm({
  mediaInfo,
  initialData,
  onClose,
  onSuccess,
}: {
  mediaInfo: MediaInfo;
  initialData?: WatchLog | null;
  onClose: () => void;
  onSuccess?: () => void;
}) {
  const isEditing = !!initialData?.id;

  const [watchedDate, setWatchedDate] = React.useState<Date>(() =>
    initialData?.watchedAt ? new Date(initialData.watchedAt) : new Date()
  );
  const [rating, setRating] = React.useState<number | null>(() =>
    initialData ? initialData.rating : null
  );
  const [review, setReview] = React.useState<string>(() =>
    initialData?.review ? initialData.review : ""
  );
  const [location, setLocation] = React.useState<WatchLocation | null>(() =>
    initialData ? initialData.watchLocation : "HOME"
  );
  const [isCalendarOpen, setIsCalendarOpen] = React.useState(false);

  const createMutation = useCreateWatchLog();
  const updateMutation = useUpdateWatchLog();
  const isSubmitting = createMutation.isPending || updateMutation.isPending;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      if (isEditing && initialData?.id) {
        await updateMutation.mutateAsync({
          id: initialData.id,
          data: {
            rating,
            review: review.trim() || null,
            watchLocation: location,
          },
        });
      } else {
        await createMutation.mutateAsync({
          tmdbId: mediaInfo.tmdbId,
          mediaType: mediaInfo.mediaType,
          watchedAt: watchedDate.toISOString(),
          rating,
          review: review.trim() || null,
          watchLocation: location,
        });
      }

      onClose();
      onSuccess?.();
    } catch {
      // Error handled by mutation toast
    }
  };

  const posterSrc =
    mediaInfo.posterUrl ||
    (initialData?.posterUrl ? initialData.posterUrl : null);

  return (
    <>
      {/* Header with Title and Media Preview */}
      <div className="relative p-6 pb-4 border-b border-border/20 bg-gradient-to-b from-rose-950/20 to-transparent">
        <div className="flex gap-4 items-start">
          {posterSrc ? (
            <div className="relative w-14 h-20 rounded-md overflow-hidden shrink-0 border border-border/30 bg-black/40 shadow-md">
              <Image
                src={posterSrc}
                alt={mediaInfo.title}
                fill
                className="object-cover"
                sizes="56px"
              />
            </div>
          ) : (
            <div className="w-14 h-20 rounded-md shrink-0 border border-border/30 bg-muted/20 flex items-center justify-center text-muted-foreground">
              {mediaInfo.mediaType === "MOVIE" ? (
                <Film className="w-6 h-6" />
              ) : (
                <Tv className="w-6 h-6" />
              )}
            </div>
          )}

          <div className="flex-1 min-w-0">
            <span className="inline-flex items-center gap-1.5 px-2 py-0.5 rounded text-[10px] font-semibold uppercase tracking-wider bg-rose-500/10 text-rose-400 border border-rose-500/20 mb-1.5">
              {mediaInfo.mediaType === "MOVIE" ? "Movie" : "TV Show"}
            </span>
            <DialogTitle className="text-lg font-serif font-bold text-white leading-tight line-clamp-1">
              {isEditing ? "Edit Watch Log" : "Log Watch"}
            </DialogTitle>
            <DialogDescription className="text-xs text-muted-foreground line-clamp-1 mt-0.5">
              {mediaInfo.title}
            </DialogDescription>
          </div>
        </div>
      </div>

      {/* Form Body */}
      <form onSubmit={handleSubmit} className="p-6 space-y-5">
        {/* Watched Date */}
        {!isEditing && (
          <div className="space-y-1.5">
            <label className="text-xs font-medium text-muted-foreground flex items-center gap-1.5">
              <Clock className="w-3.5 h-3.5 text-rose-400" />
              Date Watched
            </label>
            <Popover open={isCalendarOpen} onOpenChange={setIsCalendarOpen}>
              <PopoverTrigger
                className={cn(
                  buttonVariants({ variant: "outline" }),
                  "w-full justify-start text-left font-normal bg-background/50 border-border/40 hover:bg-background/80 hover:text-white cursor-pointer"
                )}
              >
                <CalendarIcon className="mr-2 h-4 w-4 text-rose-400" />
                {watchedDate ? format(watchedDate, "PPP") : "Select date"}
              </PopoverTrigger>
              <PopoverContent className="w-auto p-0 bg-[#18181f] border-border/50 text-white z-50">
                <Calendar
                  mode="single"
                  selected={watchedDate}
                  onSelect={(date) => {
                    if (date) {
                      setWatchedDate(date);
                      setIsCalendarOpen(false);
                    }
                  }}
                  disabled={(date) => date > new Date()}
                  autoFocus
                />
              </PopoverContent>
            </Popover>
          </div>
        )}

        {/* Rating */}
        <div className="space-y-1.5">
          <div className="flex items-center justify-between">
            <label className="text-xs font-medium text-muted-foreground">
              Your Rating
            </label>
            <span className="text-xs font-mono font-medium text-amber-400">
              {rating ? `${rating} / 10` : "Not rated"}
            </span>
          </div>
          <div className="p-3 rounded-lg bg-background/40 border border-border/30 flex items-center justify-center">
            <RatingStars
              value={rating}
              onChange={setRating}
              size="lg"
              className="gap-2"
            />
          </div>
        </div>

        {/* Location */}
        <div className="space-y-1.5">
          <label className="text-xs font-medium text-muted-foreground flex items-center gap-1.5">
            <MapPin className="w-3.5 h-3.5 text-rose-400" />
            Watch Location
          </label>
          <div className="grid grid-cols-5 gap-1.5">
            {LOCATION_OPTIONS.map((opt) => {
              const isSelected = location === opt.value;
              return (
                <button
                  key={opt.value}
                  type="button"
                  onClick={() => setLocation(isSelected ? null : opt.value)}
                  className={cn(
                    "flex flex-col items-center justify-center gap-1 py-2 px-1 rounded-md border text-xs transition-all",
                    isSelected
                      ? "bg-rose-500/20 border-rose-500 text-rose-200 font-medium shadow-sm"
                      : "bg-background/40 border-border/30 text-muted-foreground hover:bg-background/80 hover:text-white"
                  )}
                >
                  <span className="text-sm">{opt.icon}</span>
                  <span className="text-[10px] tracking-tight">{opt.label}</span>
                </button>
              );
            })}
          </div>
        </div>

        {/* Review / Notes */}
        <div className="space-y-1.5">
          <div className="flex items-center justify-between">
            <label className="text-xs font-medium text-muted-foreground">
              Review & Thoughts (optional)
            </label>
            <span
              className={cn(
                "text-[10px] font-mono",
                review.length > 4500
                  ? "text-rose-400"
                  : "text-muted-foreground"
              )}
            >
              {review.length} / 5000
            </span>
          </div>
          <Textarea
            value={review}
            onChange={(e) => setReview(e.target.value.slice(0, 5000))}
            placeholder="What did you think of the cinematography, story, performances...?"
            className="min-h-24 max-h-40 bg-background/50 border-border/40 resize-none text-xs focus-visible:ring-rose-500/50"
          />
        </div>

        {/* Footer Actions */}
        <DialogFooter className="pt-2 gap-2 sm:gap-0">
          <Button
            type="button"
            variant="ghost"
            onClick={onClose}
            disabled={isSubmitting}
            className="text-muted-foreground hover:text-white hover:bg-white/5"
          >
            Cancel
          </Button>
          <Button
            type="submit"
            disabled={isSubmitting}
            className="bg-rose-600 hover:bg-rose-500 text-white font-medium shadow-lg shadow-rose-900/30"
          >
            {isSubmitting ? (
                <>
                  <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                  Saving...
                </>
              ) : isEditing ? (
                "Update Log"
              ) : (
                <>
                  <CheckCircle2 className="w-4 h-4 mr-1.5" />
                  Log Watch
                </>
              )}
          </Button>
        </DialogFooter>
      </form>
    </>
  );
}

export function LogWatchDialog({
  open,
  onOpenChange,
  mediaInfo,
  initialData,
  onSuccess,
}: LogWatchDialogProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md bg-[#131318] border-border/40 text-foreground p-0 overflow-hidden shadow-2xl">
        {open && (
          <LogWatchForm
            key={initialData?.id || "new-log"}
            mediaInfo={mediaInfo}
            initialData={initialData}
            onClose={() => onOpenChange(false)}
            onSuccess={onSuccess}
          />
        )}
      </DialogContent>
    </Dialog>
  );
}
