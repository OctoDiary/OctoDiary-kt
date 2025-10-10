import { NFCPaymentAnimation } from "./components/NFCPaymentAnimation";
import { useState } from "react";
import { Moon, Sun } from "lucide-react";

export default function App() {
  const [isDark, setIsDark] = useState(false);

  return (
    <div className={isDark ? "dark" : ""}>
      <div className="size-full min-h-screen bg-background">
        {/* Переключатель темы */}
        <button
          onClick={() => setIsDark(!isDark)}
          className="fixed top-4 right-4 z-50 p-3 rounded-full bg-card border border-border shadow-lg hover:bg-accent transition-colors"
          aria-label="Переключить тему"
        >
          {isDark ? (
            <Sun className="w-5 h-5 text-foreground" />
          ) : (
            <Moon className="w-5 h-5 text-foreground" />
          )}
        </button>
        
        <NFCPaymentAnimation />
      </div>
    </div>
  );
}
