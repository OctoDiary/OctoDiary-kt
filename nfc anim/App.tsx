import { NFCPaymentAnimation } from "./components/NFCPaymentAnimation";
import React, { createContext, useContext, useState, useCallback, useMemo, memo } from "react";
import { Moon, Sun } from "lucide-react";

// Theme Context Interface
interface ThemeContextType {
  isDark: boolean;
  toggleTheme: () => void;
}

// Theme Context
const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

// Theme Provider
export function ThemeProvider({ children }: { children: React.ReactNode }) {
  const [isDark, setIsDark] = useState(false);
  
  const toggleTheme = useCallback(() => {
    setIsDark(prev => !prev);
  }, []);

  const value = useMemo(() => ({
    isDark,
    toggleTheme
  }), [isDark, toggleTheme]);

  return (
    <ThemeContext.Provider value={value}>
      <div className={isDark ? "dark" : ""}>
        {children}
      </div>
    </ThemeContext.Provider>
  );
}

// Theme Hook
export function useTheme() {
  const context = useContext(ThemeContext);
  if (!context) {
    throw new Error('useTheme must be used within ThemeProvider');
  }
  return context;
}

// Theme Error Boundary
class ThemeErrorBoundary extends React.Component<
  { children: React.ReactNode },
  { hasError: boolean }
> {
  constructor(props: { children: React.ReactNode }) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error: Error) {
    console.error('Theme system error:', error);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="flex items-center justify-center min-h-screen">
          <div className="text-center p-4">
            <h2 className="text-lg font-semibold mb-2">Theme System Error</h2>
            <p className="text-muted-foreground">Please refresh the page.</p>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

// Memoized Theme Toggle Component
const ThemeToggle = memo(function ThemeToggle() {
  const { isDark, toggleTheme } = useTheme();
  
  return (
    <button
      onClick={toggleTheme}
      className="fixed top-4 right-4 z-50 p-3 rounded-full bg-card border border-border shadow-lg hover:bg-accent transition-colors"
      aria-label={`Switch to ${isDark ? 'light' : 'dark'} theme`}
    >
      {isDark ? (
        <Sun className="w-5 h-5 text-foreground" />
      ) : (
        <Moon className="w-5 h-5 text-foreground" />
      )}
    </button>
  );
});

// App Layout Component
function AppLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="size-full min-h-screen bg-background">
      {children}
    </div>
  );
}

// Main App Component
export default function App() {
  return (
    <ThemeErrorBoundary>
      <ThemeProvider>
        <AppLayout>
          <ThemeToggle />
          <NFCPaymentAnimation />
        </AppLayout>
      </ThemeProvider>
    </ThemeErrorBoundary>
  );
}
