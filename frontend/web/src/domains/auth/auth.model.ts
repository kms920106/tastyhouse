import type { MemberGender } from '@/domains/member'

export interface SocialProfile {
  providerId: string
  email: string | null
  nickname: string | null
  profileImageUrl: string | null
  name: string | null
  phoneNumber: string | null
  gender: MemberGender | null
  birthYear: string | null
  birthMonth: string | null
  birthDay: string | null
}
