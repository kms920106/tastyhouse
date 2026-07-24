import { NextResponse } from "next/server";

import logger from "@/lib/logger";

/**
 * 클라이언트(브라우저) 로거가 error 이상 레벨에서 전송하는 로그 수집 엔드포인트.
 *
 * `logger-browser.ts` 의 transmit.send 가 sendBeacon/fetch 로 호출한다.
 * 수신한 페이로드를 서버 로거로 다시 기록해 중앙 로그에 적재한다.
 */

interface ClientLogPayload {
  level?: number | string;
  ts?: number;
  messages?: unknown[];
  bindings?: unknown[];
  url?: string;
  userAgent?: string;
}

export async function POST(request: Request): Promise<NextResponse> {
  let payload: ClientLogPayload;
  try {
    payload = (await request.json()) as ClientLogPayload;
  } catch {
    return NextResponse.json({ success: false }, { status: 400 });
  }

  logger.error(
    {
      source: "browser",
      clientTs: payload.ts,
      url: payload.url,
      userAgent: payload.userAgent,
      messages: payload.messages,
      bindings: payload.bindings,
    },
    "[CLIENT LOG]",
  );

  return NextResponse.json({ success: true });
}
