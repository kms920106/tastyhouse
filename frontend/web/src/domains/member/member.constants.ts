import type { MemberGradeCode, WithdrawReason } from './member.types'

const MEMBER_GRADE_NAMES: Record<MemberGradeCode, string> = {
  NEWCOMER: '신입멤버',
  ACTIVE: '열심멤버',
  INSIDER: '인싸멤버',
  GOURMET: '미식멤버',
  TEHA: '테하멤버',
}

const MEMBER_GRADE_ICONS: Record<MemberGradeCode, string> = {
  NEWCOMER: '01',
  ACTIVE: '02',
  INSIDER: '03',
  GOURMET: '04',
  TEHA: '05',
}

const MEMBER_GRADE_COLORS: Record<MemberGradeCode, string> = {
  NEWCOMER: 'text-[#4a6db3]',
  ACTIVE: 'text-[#ed771f]',
  INSIDER: 'text-[#a5a5a5]',
  GOURMET: 'text-[#f4aa14]',
  TEHA: 'text-main',
}

export const getMemberGradeName = (grade: MemberGradeCode): string => {
  return MEMBER_GRADE_NAMES[grade]
}

export const getMemberGradeIcon = (grade: MemberGradeCode): string => {
  return MEMBER_GRADE_ICONS[grade]
}

export const getMemberGradeColor = (grade: MemberGradeCode): string => {
  return MEMBER_GRADE_COLORS[grade]
}

const WITHDRAW_REASON_NAMES: Record<WithdrawReason, string> = {
  LOW_USAGE_FREQUENCY: '서비스 이용 빈도가 낮아서',
  INSUFFICIENT_CONTENT: '콘텐츠가 부족해서',
  SWITCH_TO_ANOTHER_SERVICE: '다른 서비스로 이동',
  PRIVACY_CONCERNS: '개인정보 보호 우려',
  OTHER: '기타',
}

export const getWithdrawReasonName = (reason: WithdrawReason): string => {
  return WITHDRAW_REASON_NAMES[reason] || reason
}

export const WITHDRAW_REASON_OPTIONS: ReadonlyArray<{ value: WithdrawReason; label: string }> = (
  Object.entries(WITHDRAW_REASON_NAMES) as [WithdrawReason, string][]
).map(([value, label]) => ({ value, label }))
