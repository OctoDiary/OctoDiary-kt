"use client";

import * as React from "react";
import * as TooltipPrimitive from "@radix-ui/react-tooltip";

import { cn } from "./utils";

// Extracted styles for better performance and maintainability
const tooltipContentBaseClasses = "bg-primary text-primary-foreground animate-in fade-in-0 zoom-in-95 data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=closed]:zoom-out-95 data-[side=bottom]:slide-in-from-top-2 data-[side=left]:slide-in-from-right-2 data-[side=right]:slide-in-from-left-2 data-[side=top]:slide-in-from-bottom-2 z-50 w-fit origin-(--radix-tooltip-content-transform-origin) rounded-md px-3 py-1.5 text-xs text-balance";

const tooltipArrowClasses = "bg-primary fill-primary z-50 size-2.5 translate-y-[calc(-50%_-_2px)] rotate-45 rounded-[2px]";

// Memoized styles for performance optimization
const tooltipStyles = React.memo(() => ({
  content: tooltipContentBaseClasses,
  arrow: tooltipArrowClasses
}));

tooltipStyles.displayName = "TooltipStyles";

/**
 * Tooltip Provider component with sensible defaults
 * Should be placed at the root of your application for optimal performance
 */
function TooltipProvider({
  delayDuration = 500, // Better default for UX
  skipDelayDuration = 300,
  ...props
}: React.ComponentProps<typeof TooltipPrimitive.Provider>) {
  return (
    <TooltipPrimitive.Provider
      data-slot="tooltip-provider"
      delayDuration={delayDuration}
      skipDelayDuration={skipDelayDuration}
      {...props}
    />
  );
}

/**
 * Tooltip Root component - does NOT include its own Provider
 * Use this when you have a Provider at the app root level
 */
function TooltipRoot({
  ...props
}: React.ComponentProps<typeof TooltipPrimitive.Root>) {
  return <TooltipPrimitive.Root data-slot="tooltip" {...props} />;
}

/**
 * Tooltip component that includes its own Provider for convenience
 * For app-wide tooltips, use TooltipRoot with a single TooltipProvider at the root
 */
function Tooltip({
  ...props
}: React.ComponentProps<typeof TooltipPrimitive.Root>) {
  return (
    <TooltipRoot data-slot="tooltip" {...props} />
  );
}

function TooltipTrigger({
  ...props
}: React.ComponentProps<typeof TooltipPrimitive.Trigger>) {
  return <TooltipPrimitive.Trigger data-slot="tooltip-trigger" {...props} />;
}

function TooltipContent({
  className,
  sideOffset = 4, // Better default for visual spacing
  children,
  ...props
}: React.ComponentProps<typeof TooltipPrimitive.Content>) {
  const styles = React.useMemo(() => tooltipStyles(), []);
  
  return (
    <TooltipPrimitive.Portal>
      <TooltipPrimitive.Content
        data-slot="tooltip-content"
        sideOffset={sideOffset}
        className={cn(styles.content, className)}
        {...props}
      >
        {children}
        <TooltipPrimitive.Arrow 
          className={styles.arrow}
          width={10}
          height={5}
        />
      </TooltipPrimitive.Content>
    </TooltipPrimitive.Portal>
  );
}

export { 
  Tooltip, 
  TooltipRoot, // For advanced usage with external Provider
  TooltipTrigger, 
  TooltipContent, 
  TooltipProvider 
};
