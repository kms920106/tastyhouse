'use client'

import MemberGradeBadge from '@/components/members/MemberGradeBadge'
import MemberGradeIcon from '@/components/members/MemberGradeIcon'
import MemberGradeName from '@/components/members/MemberGradeName'
import MemberNickname from '@/components/members/MemberNickname'
import Avatar from '@/components/ui/Avatar'
import GuestLoginBanner from '@/components/ui/GuestLoginBanner'
import Icon from '@/components/ui/Icon'
import { useMyProfile, useMyReviewCount } from '@/domains/member/member.hook'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'

interface Props {
  isLoggedIn: boolean
}

export default function MenuSidebarProfile({ isLoggedIn }: Props) {
  const { memberProfile } = useMyProfile({ enabled: isLoggedIn })
  const { reviewCount } = useMyReviewCount({ enabled: isLoggedIn })

  if (!memberProfile) {
    return (
      <div className="px-[15px] mt-10">
        <GuestLoginBanner
          title="내 프로필을 확인하려면?"
          description="로그인 후 프로필 확인해 보세요"
        />
      </div>
    )
  }

  const { nickname, grade, profileImageUrl } = memberProfile

  return (
    <div className="flex items-start gap-3 px-[15px] mt-10">
      <Avatar src={profileImageUrl} alt={nickname} />
      <div className="flex flex-col gap-2 min-w-0">
        <div className="flex items-center gap-[2.5px]">
          <MemberNickname size="md">{nickname}</MemberNickname>
          <Link href={PAGE_PATHS.ACCOUNT_PROFILE}>
            <Icon name="mypage/pen" />
          </Link>
        </div>
        <div className="flex items-end gap-[1px]">
          <MemberGradeBadge
            gradeIcon={<MemberGradeIcon grade={grade} size={14} />}
            gradeName={<MemberGradeName grade={grade} size="sm" bold />}
          />
          <p className="text-xs leading-[12px]">
            (리뷰 <span className="font-bold">{reviewCount}</span>개)
          </p>
        </div>
      </div>
    </div>
  )
}
