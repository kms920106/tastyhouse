import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'
import RankMemberInfo from './RankMemberInfo'
import { MemberGradeCode } from '@/domains/member'

interface RankMember {
  memberId: number
  nickname: string
  profileImageUrl: string | null
  reviewCount: number
  rankNo: number | null
  grade: MemberGradeCode
}

interface Props {
  rankMember: RankMember
}

export default function RankMemberListItem({ rankMember }: Props) {
  const { memberId, nickname, profileImageUrl, reviewCount, rankNo, grade } = rankMember

  return (
    <Link href={PAGE_PATHS.MEMBER_DETAIL(memberId)}>
      <div className="flex justify-between items-center py-[15px] pl-4 pr-5 bg-[#fcfcfc] border border-line rounded-[2.5px]">
        <RankMemberInfo
          rankNo={rankNo}
          profileImageUrl={profileImageUrl}
          nickname={nickname}
          grade={grade}
          reviewCount={reviewCount}
        />
      </div>
    </Link>
  )
}
