import MemberGradeBadge from '@/components/members/MemberGradeBadge'
import MemberGradeIcon from '@/components/members/MemberGradeIcon'
import MemberGradeName from '@/components/members/MemberGradeName'
import MemberNickname from '@/components/members/MemberNickname'
import Avatar from '@/components/ui/Avatar'
import { MemberGradeCode } from '@/domains/member'
import Link from 'next/link'

interface Props {
  nickname: string
  memberGrade: MemberGradeCode
  profileImageUrl: string | null
  href?: string
}

export default function MemberProfileCell({ nickname, memberGrade, profileImageUrl, href }: Props) {
  const avatar = <Avatar src={profileImageUrl} alt={nickname} />

  return (
    <div className="flex items-center gap-3">
      {href ? <Link href={href}>{avatar}</Link> : avatar}
      <div className="flex flex-col gap-[9px]">
        <MemberNickname size="md">{nickname}</MemberNickname>
        <MemberGradeBadge
          gradeIcon={<MemberGradeIcon grade={memberGrade} size={14} />}
          gradeName={<MemberGradeName grade={memberGrade} size="xs" />}
        />
      </div>
    </div>
  )
}
