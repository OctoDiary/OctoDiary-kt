import { motion } from "motion/react";
import { X, Check, Wifi } from "lucide-react";
import { useState, useEffect } from "react";

// Иконка двух карт для режима ожидания
function CardsIcon({ className }: { className?: string }) {
  return (
    <svg
      className={className}
      viewBox="0 0 24 24"
      fill="currentColor"
      xmlns="http://www.w3.org/2000/svg"
    >
      <rect x="2" y="6" width="14" height="9" rx="2" />
      <rect x="8" y="9" width="14" height="9" rx="2" />
    </svg>
  );
}

export function NFCPaymentAnimation() {
  const [stage, setStage] = useState<
    | "waiting"
    | "approaching"
    | "success"
    | "error"
    | "returning"
  >("waiting");
  const [result, setResult] = useState<"success" | "error">(
    "success",
  );

  useEffect(() => {
    // Автоматическая анимация
    const sequence = async () => {
      await new Promise((resolve) => setTimeout(resolve, 1000));
      setStage("approaching");

      await new Promise((resolve) => setTimeout(resolve, 800));
      // Случайный результат
      const isSuccess = Math.random() > 0.3;
      setResult(isSuccess ? "success" : "error");
      setStage(isSuccess ? "success" : "error");

      await new Promise((resolve) => setTimeout(resolve, 1000));
      setStage("returning");

      await new Promise((resolve) => setTimeout(resolve, 800));
      setStage("waiting");
    };

    if (stage === "waiting") {
      const timer = setTimeout(sequence, 500);
      return () => clearTimeout(timer);
    }
  }, [stage]);

  // Адаптивные позиции карты для разных ориентаций
  const cardPosition = {
    waiting: {
      portrait: { x: -150, y: -350 },
      landscape: { x: -150, y: -300 },
    },
    approaching: { x: 0, y: 0 },
    success: { x: 0, y: 0 },
    error: { x: 0, y: 0 },
    returning: {
      portrait: { x: -150, y: -350 },
      landscape: { x: -150, y: -300 },
    },
  };

  const getCardPosition = (currentStage: typeof stage) => {
    const pos = cardPosition[currentStage];
    if (typeof pos === "object" && "portrait" in pos) {
      // Проверяем ориентацию
      const isLandscape =
        typeof window !== "undefined" &&
        window.innerWidth > window.innerHeight;
      return isLandscape ? pos.landscape : pos.portrait;
    }
    return pos;
  };

  return (
    <div className="relative w-full h-screen flex items-center justify-center p-4">
      {/* Турникет */}
      <div className="relative">
        <motion.div
          className="w-64 portrait:h-80 landscape:h-[32rem] bg-muted rounded-2xl shadow-xl flex flex-col items-center justify-center gap-4 portrait:gap-6 p-6 portrait:p-8"
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.5 }}
        >
          {/* Панель ожидания */}
          <motion.div
            className="w-24 h-24 rounded-xl flex items-center justify-center bg-card dark:bg-background border-4 transition-all shadow-md"
            animate={{
              borderColor:
                stage === "waiting" ||
                stage === "approaching" ||
                stage === "returning"
                  ? "#fb923c"
                  : "rgb(209, 213, 219)",
              scale:
                stage === "waiting" ||
                stage === "approaching" ||
                stage === "returning"
                  ? 1.1
                  : 1,
            }}
            transition={{ duration: 0.3 }}
          >
            <CardsIcon className="w-10 h-10 text-orange-500 rotate-90" />
          </motion.div>

          {/* Панель отмены */}
          <motion.div
            className="w-24 h-24 rounded-xl flex items-center justify-center bg-card dark:bg-background border-4 transition-all shadow-md"
            animate={{
              borderColor:
                stage === "error"
                  ? "#ef4444"
                  : "rgb(209, 213, 219)",
              scale: stage === "error" ? 1.1 : 1,
            }}
            transition={{ duration: 0.3 }}
          >
            <X className="w-10 h-10 text-red-500" />
          </motion.div>

          {/* Панель готово */}
          <motion.div
            className="w-24 h-24 rounded-xl flex items-center justify-center bg-card dark:bg-background border-4 transition-all shadow-md"
            animate={{
              borderColor:
                stage === "success"
                  ? "#22c55e"
                  : "rgb(209, 213, 219)",
              scale: stage === "success" ? 1.1 : 1,
            }}
            transition={{ duration: 0.3 }}
          >
            <Check className="w-10 h-10 text-green-500" />
          </motion.div>
        </motion.div>

        {/* Карта "Москвёнок" */}
        <motion.div
          className="absolute w-56 h-36 bg-slate-700 dark:bg-slate-800 rounded-2xl shadow-2xl"
          animate={getCardPosition(stage)}
          transition={{
            duration: 0.6,
            ease: [0.4, 0, 0.2, 1],
          }}
          style={{
            top: "50%",
            left: "50%",
            marginTop: "-72px",
            marginLeft: "-112px",
          }}
        >
          {/* Название "Москвёнок" в углу */}
          <p className="absolute top-6 left-6 text-white text-xl tracking-wide">
            Москвёнок
          </p>

          {/* Чип */}
          <div className="absolute top-16 left-6 w-10 h-8 bg-yellow-400 rounded" />

          {/* NFC иконка */}
          <div className="absolute top-6 right-6">
            <Wifi className="w-6 h-6 text-white rotate-90" />
          </div>
        </motion.div>
      </div>
    </div>
  );
}