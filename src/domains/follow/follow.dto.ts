import { MemberGradeCode } from '../member'

export interface MemberSocialProfileListItemResponse {
  memberId: number
  nickname: string
  grade: MemberGradeCode
  profileImageUrl: string | null
  following: boolean
}

export interface IsFollowingResponse {
  memberId: number
  following: boolean
}
