import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'
import { ReactNode } from 'react'

interface Props {
  memberId: number | string
  reviewCount: number | undefined
  followingCount: number | undefined
  followerCount: number | undefined
}

function StatLabel({ children }: { children: ReactNode }) {
  return <span className="text-xs leading-[12px]">{children}</span>
}

function StatValue({ children }: { children: ReactNode }) {
  return <span className="text-xs leading-[12px] font-bold">{children}</span>
}

export default function MemberProfileStats({ memberId, reviewCount, followingCount, followerCount }: Props) {
  return (
    <div className="flex items-center justify-center gap-10 mt-[53px] mb-[30px]">
      <div className="flex items-center gap-1">
        <StatLabel>리뷰</StatLabel>
        <StatValue>{reviewCount ?? '-'}</StatValue>
      </div>
      <Link
        href={PAGE_PATHS.MEMBER_FOLLOWS(memberId, 'following')}
        className="flex items-center gap-1"
      >
        <StatLabel>팔로잉</StatLabel>
        <StatValue>{followingCount ?? '-'}</StatValue>
      </Link>
      <Link
        href={PAGE_PATHS.MEMBER_FOLLOWS(memberId, 'follower')}
        className="flex items-center gap-1"
      >
        <StatLabel>팔로워</StatLabel>
        <StatValue>{followerCount ?? '-'}</StatValue>
      </Link>
    </div>
  )
}
