import { z } from "zod";

/**
 * 클라이언트 환경변수 검증 — 범위를 지도 키로만 좁힌다.
 *
 * `NEXT_PUBLIC_API_URL` 은 여기서 다루지 않는다. `api/shared/client.ts` 가 `?? ""` 폴백으로
 * 동작하는 계약을 이미 갖고 있어, 이곳으로 끌어오면 실패 지점만 늘어난다.
 *
 * **모듈 로드 시점에 throw 하지 않는다.** 지도 키가 없다고 대시보드 전체가 500 으로 떨어지면
 * 안 되기 때문이다. 대신 null 을 돌려주고, 호출부가 지도 대신 검색·트리 편집만 렌더한다.
 */

/** 카카오맵 JavaScript 키 — 빈 문자열이면 SDK 가 조용히 죽으므로 공백을 걸러낸다 */
const kakaoMapAppKeySchema = z.string().trim().min(1);

/**
 * 카카오맵 JavaScript 키를 돌려준다. 미설정이면 null.
 *
 * Next.js 는 `process.env.NEXT_PUBLIC_*` 를 빌드 시점에 문자열로 치환하므로,
 * 동적 접근(`process.env[key]`)이 아니라 반드시 이 형태로 직접 써야 한다.
 */
export function getKakaoMapAppKey(): string | null {
  const parsed = kakaoMapAppKeySchema.safeParse(process.env.NEXT_PUBLIC_KAKAO_MAP_CLIENT);
  return parsed.success ? parsed.data : null;
}
