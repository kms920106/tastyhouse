import pino from "pino";

/**
 * 서버 사이드 로거 (pino).
 *
 * - 개발 환경: pino-pretty 로 사람이 읽기 쉬운 단일 라인 컬러 출력.
 * - 운영 환경: stdout(JSON) 으로 출력해 로그 수집기와 연동한다.
 * - authorization/password/token/secret 필드는 자동으로 마스킹/제거된다.
 */

const isDev = process.env.NODE_ENV === "development";

const logger = pino(
  {
    level: process.env.LOG_LEVEL ?? (isDev ? "debug" : "info"),
    timestamp: pino.stdTimeFunctions.isoTime,
    redact: {
      paths: ["*.authorization", "*.password", "*.token", "*.secret"],
      remove: true,
    },
  },
  isDev
    ? pino.transport({
        target: "pino-pretty",
        options: {
          colorize: true,
          translateTime: "yyyy-mm-dd HH:MM:ss.l",
          ignore: "pid,hostname",
          messageFormat: "{msg}",
          singleLine: true,
        },
      })
    : pino.destination(1),
);

export default logger;
