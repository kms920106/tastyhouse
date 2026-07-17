export type MemberStatus = "ACTIVE" | "SUSPENDED" | "DELETED";

export type MemberGrade = "NEWCOMER" | "ACTIVE" | "INSIDER" | "GOURMET" | "TEHA";

export type Gender = "MALE" | "FEMALE";

export type WithdrawalReason =
  | "LOW_USAGE_FREQUENCY"
  | "INSUFFICIENT_CONTENT"
  | "SWITCH_TO_ANOTHER_SERVICE"
  | "PRIVACY_CONCERNS"
  | "OTHER";

export interface MemberListItem {
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

export interface MemberDetail {
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
