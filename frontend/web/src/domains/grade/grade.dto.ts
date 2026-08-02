import type { MemberGradeCode } from '@/domains/member'

export interface GradeInfoItemResponse {
  grade: MemberGradeCode
  displayName: string
  minReviewCount: number
  maxReviewCount: number | null
}
