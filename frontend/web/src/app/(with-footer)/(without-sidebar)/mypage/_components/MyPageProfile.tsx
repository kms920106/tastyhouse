'use client'

import MemberProfileImage from '@/components/members/MemberProfileImage'
import MemberProfileCard from '@/components/members/MemberProfileCard'
import AppPrimaryButton from '@/components/ui/AppPrimaryButton'
import FetchErrorState from '@/components/ui/FetchErrorState'
import Icon from '@/components/ui/Icon'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { useMyProfile, useMyStats } from '@/domains/member/member.hook'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'

interface Props {
  isLoggedIn: boolean
}

export default function MyPageProfile({ isLoggedIn }: Props) {
  const {
    memberProfile,
    isLoading: isProfileLoading,
    isError: isProfileError,
  } = useMyProfile({ enabled: isLoggedIn })
  const {
    data: statsData,
    isLoading: isStatsLoading,
    isError: isStatsError,
  } = useMyStats({ enabled: isLoggedIn })

  if (!isLoggedIn) {
    return (
      <div className="flex-1 flex flex-col items-center bg-white">
        <div className="-mt-[63px] relative z-10">
          <MemberProfileImage profileImageUrl={null} />
        </div>
        <AppPrimaryButton asChild className="w-auto px-10 mt-[21px]">
          <Link href={PAGE_PATHS.AUTH_LOGIN}>로그인하기</Link>
        </AppPrimaryButton>
      </div>
    )
  }

  if (isProfileError) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('프로필')} />
  }

  return (
    <MemberProfileCard
      memberId={memberProfile?.id ?? 0}
      nickname={memberProfile?.nickname}
      profileImageUrl={memberProfile?.profileImageUrl}
      memberGrade={memberProfile?.grade}
      statusMessage={memberProfile?.statusMessage}
      reviewCount={statsData?.reviewCount}
      followingCount={statsData?.followingCount}
      followerCount={statsData?.followerCount}
      isProfileLoading={isProfileLoading}
      isStatsLoading={isStatsLoading}
      isStatsError={isStatsError}
      editSlot={
        <Link href={PAGE_PATHS.ACCOUNT_PROFILE}>
          <Icon name="mypage/pen" />
        </Link>
      }
    />
  )
}
