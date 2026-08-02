import MemberNickname from '@/components/members/MemberNickname'
import TimeAgo from '@/components/reviews/TimeAgo'
import Avatar from '@/components/ui/Avatar'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'

interface Props {
  memberId: number
  profileImageUrl: string | null
  nickname: string
  createdAt: string
}

export default function ReviewAuthorInfo({
  memberId,
  profileImageUrl,
  nickname,
  createdAt,
}: Props) {
  return (
    <div className="flex items-center gap-2.5">
      <Link href={PAGE_PATHS.MEMBER_DETAIL(memberId)}>
        <Avatar src={profileImageUrl} alt={nickname} priority />
      </Link>
      <div className="flex flex-col gap-2">
        <MemberNickname size="md">{nickname}</MemberNickname>
        <TimeAgo date={createdAt} />
      </div>
    </div>
  )
}
