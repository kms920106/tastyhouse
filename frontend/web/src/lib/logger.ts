import pino from 'pino'

const isDev = process.env.NODE_ENV === 'development'

const logger = pino(
  {
    level: process.env.LOG_LEVEL ?? (isDev ? 'debug' : 'info'),
    timestamp: pino.stdTimeFunctions.isoTime,
    redact: {
      paths: ['*.authorization', '*.password', '*.token', '*.secret'],
      remove: true,
    },
  },
  isDev
    ? pino.transport({
        target: 'pino-pretty',
        options: {
          colorize: true,
          translateTime: 'yyyy-mm-dd HH:MM:ss.l',
          ignore: 'pid,hostname',
          messageFormat: '{msg}',
          singleLine: true,
        },
      })
    : pino.destination(1),
)

export default logger
