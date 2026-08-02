// 회원 상태: ACTIVE(활성) / SUSPENDED(정지) / DELETED(탈퇴)
export type MemberStatus = "ACTIVE" | "SUSPENDED" | "DELETED";

// 회원 등급
export type MemberGrade = "NEWCOMER" | "ACTIVE" | "INSIDER" | "GOURMET" | "TEHA";

export type Gender = "MALE" | "FEMALE";

// 강제 탈퇴 사유 코드
export type WithdrawalReason =
  | "LOW_USAGE_FREQUENCY"
  | "INSUFFICIENT_CONTENT"
  | "SWITCH_TO_ANOTHER_SERVICE"
  | "PRIVACY_CONCERNS"
  | "OTHER";

// 회원 목록 조회
export interface MemberListQueryRequest {
  nickname?: string;
  username?: string;
  phone?: string;
  status?: MemberStatus;
  grade?: MemberGrade;
}

// 회원 목록 조회 (profileImageFilePath 는 원본 path, URL 아님)
export interface MemberListItemResponse {
  id: number;
  username: string;
  nickname: string;
  fullName: string;
  phoneNumber: string;
  gender: Gender;
  memberGrade: MemberGrade;
  memberStatus: MemberStatus;
  profileImageFilePath: string | null;
  createdAt: string;
}

// 회원 상세 조회 (profileImageUrl 은 접근 가능한 전체 URL)
export interface MemberDetailResponse {
  id: number;
  username: string;
  nickname: string;
  fullName: string;
  phoneNumber: string;
  gender: Gender;
  birthDate: number;
  memberGrade: MemberGrade;
  memberStatus: MemberStatus;
  statusMessage: string | null;
  profileImageUrl: string | null;
  pushNotificationEnabled: boolean;
  marketingInfoEnabled: boolean;
  eventInfoEnabled: boolean;
  createdAt: string;
}

// 회원 강제 탈퇴
export interface MemberWithdrawalRequest {
  reason: WithdrawalReason;
  reasonDetail?: string;
}
