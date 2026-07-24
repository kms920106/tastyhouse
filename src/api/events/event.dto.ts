// 이벤트 상태: SCHEDULED(예정) / ACTIVE(진행중) / ENDED(종료)
export type EventStatus = "SCHEDULED" | "ACTIVE" | "ENDED";

// 이미지 파일 정보 (미등록 시 null). 등록/수정 요청에는 파일 id 만 전달한다.
export interface EventFileResponse {
  id: number;
  name: string;
  url: string;
}

// 이벤트 목록 조회
export interface EventListQueryRequest {
  name?: string;
  status?: EventStatus;
}

// 이벤트 목록 조회 (file 은 썸네일 기준)
export interface EventListItemResponse {
  id: number;
  name: string;
  status: EventStatus;
  file: EventFileResponse | null;
  startAt: string;
  endAt: string;
}

// 이벤트 등록
export interface EventCreateRequest {
  name: string;
  description?: string;
  subtitle?: string;
  thumbnailImageFileId?: number;
  bannerImageFileId?: number;
  contentHtml?: string;
  status: EventStatus;
  startAt: string;
  endAt: string;
}

// 이벤트 수정 (등록과 동일 필드/제약)
export type EventUpdateRequest = EventCreateRequest;

// 이벤트 상세 조회
export interface EventDetailResponse {
  id: number;
  name: string;
  description: string | null;
  subtitle: string | null;
  thumbnailFile: EventFileResponse | null;
  bannerFile: EventFileResponse | null;
  contentHtml: string | null;
  status: EventStatus;
  startAt: string;
  endAt: string;
  createdAt: string;
  updatedAt: string;
}

// 당첨자 발표 공지 등록/수정 (동일 필드)
export interface EventAnnouncementRequest {
  name: string;
  content: string;
  announcedAt: string;
}

// 당첨자 발표 공지 조회
export interface EventAnnouncementResponse {
  id: number;
  eventId: number;
  name: string;
  content: string;
  announcedAt: string;
}

// 당첨자 등록
export interface EventWinnerCreateRequest {
  rankNo: number;
  winnerName: string;
  phoneNumber: string;
  announcedAt: string;
}

// 당첨자 목록 조회 (순위 오름차순)
export interface EventWinnerResponse {
  id: number;
  eventId: number;
  rankNo: number;
  winnerName: string;
  phoneNumber: string;
  announcedAt: string;
}
