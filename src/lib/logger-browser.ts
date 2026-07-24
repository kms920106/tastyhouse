import pino from "pino";

/**
 * 브라우저(클라이언트) 사이드 로거 (pino).
 *
 * - 개발 환경: 레벨/타임스탬프를 포함한 포맷으로 콘솔에 출력.
 * - error 이상 레벨은 `/api/logs` 로 전송해 서버 로그에 적재한다.
 */

const isDev = process.env.NODE_ENV !== "production";

const LEVEL_LABELS: Record<number, string> = {
  10: "TRACE",
  20: "DEBUG",
  30: "INFO",
  40: "WARN",
  50: "ERROR",
  60: "FATAL",
};

function formatLogTimestamp(time: number): string {
  const date = new Date(time);
  const pad = (n: number, width = 2) => String(n).padStart(width, "0");
  return (
    `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ` +
    `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}.${pad(date.getMilliseconds(), 3)}`
  );
}

const browserLogger = pino({
  level: isDev ? "debug" : "warn",
  browser: {
    asObject: true,
    write(logObject) {
      if (isDev) {
        const { level, time, msg } = logObject as {
          level: number;
          time: number;
          msg: string;
        };
        const label = LEVEL_LABELS[level] ?? "LOG";
        const timestamp = formatLogTimestamp(time);
        const formatted = `[${timestamp}] ${label}: ${msg}`;

        if (level >= 50) console.error(formatted);
        else if (level >= 40) console.warn(formatted);
        else console.log(formatted);
      }
    },
    transmit: {
      // error 이상만 서버로 전송
      level: "error",
      send(level, logEvent) {
        const body = JSON.stringify({
          level,
          ts: logEvent.ts,
          messages: logEvent.messages,
          bindings: logEvent.bindings,
          url: typeof window !== "undefined" ? window.location.href : undefined,
          userAgent: typeof navigator !== "undefined" ? navigator.userAgent : undefined,
        });

        // sendBeacon: 페이지 언로드 중에도 전송 보장, 비동기·비차단
        if (typeof navigator !== "undefined" && navigator.sendBeacon) {
          navigator.sendBeacon("/api/logs", body);
        } else {
          // sendBeacon 미지원 환경 폴백 (keepalive 로 언로드 시에도 전송 시도)
          fetch("/api/logs", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body,
            keepalive: true,
          }).catch(() => {
            // 로그 전송 실패는 사용자 경험에 영향 없도록 무시
          });
        }
      },
    },
  },
});

export default browserLogger;
