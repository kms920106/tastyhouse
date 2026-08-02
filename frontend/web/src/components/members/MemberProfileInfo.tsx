import MemberProfileImage from '@/components/members/MemberProfileImage'
import MemberGradeInfo from '@/components/members/MemberGradeInfo'
import type { MemberGradeCode } from '@/domains/member'
import { ReactNode } from 'react'
import MemberNickname from '@/components/members/MemberNickname'

interface Props {
  nickname: string
  profileImageUrl: string | null
  memberGrade: MemberGradeCode | undefined
  statusMessage: string | null | undefined
  editSlot?: ReactNode
}

export default function MemberProfileInfo({
  nickname,
  profileImageUrl,
  memberGrade,
  statusMessage,
  editSlot,
}: Props) {
  return (
    <>
      <div className="-mt-[63px] relative z-10">
        <MemberProfileImage profileImageUrl={profileImageUrl} />
      </div>
      <div className="flex items-center gap-[1.5px] mt-[21px]">
        <MemberNickname size="lg">{nickname}</MemberNickname>
        {editSlot}
      </div>
      <MemberGradeInfo memberGrade={memberGrade} />
      {statusMessage && (
        <p className="text-sm leading-[14px] text-center mt-[15px] px-8">{statusMessage}</p>
      )}
    </>
  )
}
