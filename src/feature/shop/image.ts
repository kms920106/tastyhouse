/**
 * 스펙(docs/CEO-API-SHOP-SPEC-FOR-FRONTEND.md)은 이미지 파일의 fileId 만 내려주고
 * 바로 쓸 수 있는 표시용 URL 을 제공하지 않는다. 업로드 엔드포인트(/api/files/v1/upload)만
 * 확인된 상태라 아래 조회 경로는 잠정 규칙이다 — 실제 백엔드와 확인되면 이 함수만 고치면 된다.
 *
 * 경로가 틀릴 수 있으므로 이 URL 을 쓰는 <img> 는 반드시 onError 로 대체 UI 를 노출해야 한다.
 */
export function resolveFileUrl(fileId: number | null | undefined): string | null {
  if (!fileId) return null;
  const base = process.env.NEXT_PUBLIC_API_URL ?? "";
  return `${base}/api/files/v1/${fileId}`;
}
