import logger from '@/lib/logger'
import { NextRequest, NextResponse } from 'next/server'

interface ClientLogPayload {
  level: string
  ts: number
  messages: unknown[]
  bindings: unknown[]
  url?: string
  userAgent?: string
}

// pino transmit의 messages 배열에서 로그 메시지를 조합합니다.
// messages: [mergeObject, formatString, ...args] 형태로 전달됩니다.
function buildMessage(messages: unknown[]): string {
  const textParts: string[] = []

  for (const item of messages) {
    if (typeof item === 'string') {
      textParts.push(item)
    }
  }

  // '%s' 포맷 문자열 치환: 첫 번째 문자열이 포맷, 나머지가 인자
  if (textParts.length >= 2 && textParts[0].includes('%s')) {
    let result = textParts[0]
    for (let i = 1; i < textParts.length; i++) {
      result = result.replace('%s', textParts[i])
    }
    return result
  }

  return textParts.join(' ')
}

// 브라우저에서 전송된 클라이언트 오류 로그를 수신하여 서버 pino로 기록합니다.
export async function POST(request: NextRequest) {
  try {
    const body: ClientLogPayload = await request.json()
    const message = buildMessage(body.messages)

    const logContext = {
      url: body.url,
      userAgent: body.userAgent,
    }

    if (body.level === 'error' || body.level === 'fatal') {
      logger.error(logContext, message)
    } else if (body.level === 'warn') {
      logger.warn(logContext, message)
    } else {
      logger.info(logContext, message)
    }

    return NextResponse.json({ ok: true })
  } catch (e) {
    logger.error({ err: e }, '[CLIENT_LOG PARSE ERROR]')
    return NextResponse.json({ ok: false }, { status: 400 })
  }
}
