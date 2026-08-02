'use client'

import MemberProfileCard from '@/components/members/MemberProfileCard'
import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { useMemberStats, useMemberProfile } from '@/domains/member/member.hook'

interface Props {
  memberId: number
}

export default function MemberDetailProfile({ memberId }: Props) {
  const {
    data: profileData,
    isLoading: isProfileLoading,
    isError: isProfileError,
  } = useMemberProfile(memberId)
  const {
    data: statsData,
    isLoading: isStatsLoading,
    isError: isStatsError,
  } = useMemberStats(memberId)

  if (isProfileError) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('프로필')} />
  }

  return (
    <MemberProfileCard
      memberId={memberId}
      nickname={profileData?.nickname}
      profileImageUrl={profileData?.profileImageUrl}
      memberGrade={profileData?.grade}
      statusMessage={profileData?.statusMessage}
      reviewCount={statsData?.reviewCount}
      followingCount={statsData?.followingCount}
      followerCount={statsData?.followerCount}
      isProfileLoading={isProfileLoading}
      isStatsLoading={isStatsLoading}
      isStatsError={isStatsError}
    />
  )
}
