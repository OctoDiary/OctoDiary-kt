import * as React from "react";

// Configuration - can be moved to theme/config later
const UI_CONFIG = {
  breakpoints: {
    mobile: 768,
    tablet: 1024,
  },
} as const;

type BreakpointKey = keyof typeof UI_CONFIG.breakpoints;

// SSR-safe utility to check if we're on client
const isClient = typeof window !== 'undefined';

export function useIsMobile(breakpoint: BreakpointKey = 'mobile') {
  const [isMobile, setIsMobile] = React.useState<boolean>(false);

  // Memoize the breakpoint value to avoid unnecessary recalculations
  const breakpointValue = React.useMemo(
    () => UI_CONFIG.breakpoints[breakpoint],
    [breakpoint]
  );

  // Optimized check function using matchMedia only
  const checkMobile = React.useCallback(() => {
    if (!isClient) return false;
    return window.matchMedia(`(max-width: ${breakpointValue - 1}px)`).matches;
  }, [breakpointValue]);

  React.useEffect(() => {
    if (!isClient) return;

    // Initial check
    setIsMobile(checkMobile());

    // Set up efficient event listener
    const mql = window.matchMedia(`(max-width: ${breakpointValue - 1}px)`);
    const onChange = () => setIsMobile(mql.matches);
    
    mql.addEventListener("change", onChange);
    
    return () => mql.removeEventListener("change", onChange);
  }, [checkMobile, breakpointValue]);

  return isMobile;
}

// Additional hook for tablet detection if needed
export function useIsTablet() {
  return useIsMobile('tablet');
}
