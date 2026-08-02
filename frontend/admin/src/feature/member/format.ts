import type { Gender, MemberGrade, MemberStatus } from "./domain";

// 회원 상태 한글 라벨
export function memberStatusLabel(status: MemberStatus): string {
  switch (status) {
    case "ACTIVE":
      return "활성";
    case "SUSPENDED":
      return "정지";
    case "DELETED":
      return "탈퇴";
    default:
      return status;
  }
}

// 회원 상태 Badge variant
export function memberStatusBadgeVariant(status: MemberStatus): "default" | "secondary" | "outline" {
  switch (status) {
    case "ACTIVE":
      return "default";
    case "SUSPENDED":
      return "outline";
    default:
      return "secondary";
  }
}

// 회원 등급 한글 라벨
export function memberGradeLabel(grade: MemberGrade): string {
  switch (grade) {
    case "NEWCOMER":
      return "뉴커머";
    case "ACTIVE":
      return "액티브";
    case "INSIDER":
      return "인사이더";
    case "GOURMET":
      return "구르메";
    case "TEHA":
      return "테하";
    default:
      return grade;
  }
}

// 성별 한글 라벨
export function genderLabel(gender: Gender): string {
  return gender === "MALE" ? "남성" : "여성";
}

/** 생년월일(YYYYMMDD 정수) -> "YYYY-MM-DD" */
export function formatBirthDate(birthDate: number | null | undefined): string {
  if (birthDate == null) return "-";
  const raw = String(birthDate);
  if (raw.length !== 8) return raw;
  return `${raw.slice(0, 4)}-${raw.slice(4, 6)}-${raw.slice(6, 8)}`;
}
