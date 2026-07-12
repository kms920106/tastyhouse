'use client'

import MemberProfileCell from '@/components/members/MemberProfileCell'
import FollowButton from '@/components/ui/FollowButton'
import { SocialMember } from '@/domains/member'
import { PAGE_PATHS } from '@/lib/paths'

interface Props {
  member: SocialMember
  onFollowToggle: (member: SocialMember) => void
}

export default function MemberSearchResultItem({ member, onFollowToggle }: Props) {
  return (
    <div className="flex items-center justify-between">
      <MemberProfileCell
        nickname={member.nickname}
        memberGrade={member.grade}
        profileImageUrl={member.profileImageUrl}
        href={PAGE_PATHS.MEMBER_DETAIL(member.memberId)}
      />
      <FollowButton following={member.following} onClick={() => onFollowToggle(member)} />
    </div>
  )
}
