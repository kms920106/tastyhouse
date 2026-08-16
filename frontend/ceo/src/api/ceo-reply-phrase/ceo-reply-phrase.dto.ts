/**
 * 자주 쓰는 문구 DTO — `docs/tasks/backend.md` 3-4 응답과 1:1 대응.
 *
 * 이 레이어를 벗어나지 않는다(`src/api/AGENTS.md`). UI/feature 는
 * `@/feature/ceo-reply-phrase/domain` 의 도메인 타입만 import 한다.
 */

export interface CeoReplyPhraseResponse {
  id: number;
  /** 점주가 입력한 이름. 미입력이면 null */
  name: string | null;
  /** 화면 표시명. `name` 이 있으면 그 값, 없으면 `content` 앞부분 + 말줄임 (서버가 파생) */
  displayName: string;
  content: string;
  sort: number;
  createdAt: string;
}

/** 등록·수정 공용 본문. 서버 제약은 `name` `@Size(max = 50)`, `content` `@NotBlank @Size(max = 1000)` */
export interface CeoReplyPhraseUpsertRequest {
  name?: string;
  content: string;
}
