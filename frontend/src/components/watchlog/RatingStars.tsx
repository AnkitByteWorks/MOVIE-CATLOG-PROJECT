"use client";

import * as React from "react";
import { Star } from "lucide-react";
import { cn } from "@/lib/utils";

interface RatingStarsProps {
  value?: number | null; // 1 to 10
  onChange?: (val: number | null) => void;
  readonly?: boolean;
  size?: "sm" | "md" | "lg";
  showValue?: boolean;
  className?: string;
}

export function RatingStars({
  value = null,
  onChange,
  readonly = false,
  size = "md",
  showValue = false,
  className,
}: RatingStarsProps) {
  const [hoverVal, setHoverVal] = React.useState<number | null>(null);

  const starSizes = {
    sm: "w-3.5 h-3.5",
    md: "w-5 h-5",
    lg: "w-6 h-6",
  };

  const textSizes = {
    sm: "text-xs",
    md: "text-sm",
    lg: "text-base",
  };

  const activeValue = hoverVal ?? value ?? 0;

  const handleMouseMove = (starIndex: number, e: React.MouseEvent<HTMLDivElement>) => {
    if (readonly || !onChange) return;
    const rect = e.currentTarget.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const isHalf = x < rect.width / 2;
    const computed = isHalf ? starIndex * 2 - 1 : starIndex * 2;
    setHoverVal(computed);
  };

  const handleClick = (starIndex: number, e: React.MouseEvent<HTMLDivElement>) => {
    if (readonly || !onChange) return;
    const rect = e.currentTarget.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const isHalf = x < rect.width / 2;
    const computed = isHalf ? starIndex * 2 - 1 : starIndex * 2;

    if (value === computed) {
      onChange(null); // allow deselecting
    } else {
      onChange(computed);
    }
  };

  return (
    <div
      className={cn("inline-flex items-center gap-1.5 select-none", className)}
      onMouseLeave={() => !readonly && setHoverVal(null)}
    >
      <div className="flex items-center gap-0.5">
        {[1, 2, 3, 4, 5].map((starIndex) => {
          const starThreshold = starIndex * 2;
          const halfThreshold = starIndex * 2 - 1;
          const isFull = activeValue >= starThreshold;
          const isHalf = !isFull && activeValue >= halfThreshold;

          return (
            <div
              key={starIndex}
              className={cn(
                "relative group transition-transform duration-100",
                !readonly && "cursor-pointer hover:scale-110 active:scale-95"
              )}
              onMouseMove={(e) => handleMouseMove(starIndex, e)}
              onClick={(e) => handleClick(starIndex, e)}
            >
              {/* Background empty star */}
              <Star
                className={cn(
                  starSizes[size],
                  "text-muted-foreground/30 fill-transparent transition-colors"
                )}
              />

              {/* Full star overlay */}
              {isFull && (
                <Star
                  className={cn(
                    starSizes[size],
                    "absolute inset-0 text-amber-400 fill-amber-400 transition-colors drop-shadow-[0_0_8px_rgba(251,191,36,0.3)]"
                  )}
                />
              )}

              {/* Half star overlay with clip-path */}
              {isHalf && (
                <div
                  className="absolute inset-0 overflow-hidden"
                  style={{ width: "50%" }}
                >
                  <Star
                    className={cn(
                      starSizes[size],
                      "text-amber-400 fill-amber-400 transition-colors drop-shadow-[0_0_8px_rgba(251,191,36,0.3)]"
                    )}
                  />
                </div>
              )}
            </div>
          );
        })}
      </div>

      {showValue && (
        <span
          className={cn(
            "font-mono font-semibold tracking-tight ml-1",
            textSizes[size],
            activeValue > 0 ? "text-amber-400" : "text-muted-foreground"
          )}
        >
          {activeValue > 0 ? `${activeValue}/10` : "Unrated"}
        </span>
      )}
    </div>
  );
}
