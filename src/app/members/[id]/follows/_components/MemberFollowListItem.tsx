'use client'

import MemberProfileCell from '@/components/members/MemberProfileCell'
import AppConfirmDialog from '@/components/ui/AppConfirmDialog'
import AppOutlineButton from '@/components/ui/AppOutlineButton'
import FollowButton from '@/components/ui/FollowButton'
import { SocialMember } from '@/domains/member'
import { PAGE_PATHS } from '@/lib/paths'
import { useState } from 'react'
import { FiMoreVertical } from 'react-icons/fi'

interface Props {
  member: SocialMember
  isOwner: boolean
  onFollowToggle: (member: SocialMember) => void
  onRemoveFollower: (memberId: number) => void
}

export default function MemberFollowListItem({
  member,
  isOwner,
  onFollowToggle,
  onRemoveFollower,
}: Props) {
  const [removeConfirmOpen, setRemoveConfirmOpen] = useState(false)

  return (
    <div className="flex items-center justify-between">
      <MemberProfileCell
        nickname={member.nickname}
        memberGrade={member.grade}
        profileImageUrl={member.profileImageUrl}
        href={PAGE_PATHS.MEMBER_DETAIL(member.memberId)}
      />
      <div className="flex items-center gap-2">
        {isOwner ? (
          <>
            <AppOutlineButton
              onClick={() => setRemoveConfirmOpen(true)}
              className="h-[30px] px-7 py-2.5 text-xs leading-[12px] bg-line border border-[#cccccc] box-border rounded-[2.5px]"
            >
              삭제
            </AppOutlineButton>
            <AppConfirmDialog
              open={removeConfirmOpen}
              onOpenChange={setRemoveConfirmOpen}
              title="팔로워를 삭제하시겠습니까?"
              description={`${member.nickname}님을 팔로워 목록에서 삭제합니다.\n삭제 후에는 해당 팔로워 관계가 해제됩니다.`}
              confirmLabel="삭제"
              cancelLabel="취소"
              onConfirm={() => onRemoveFollower(member.memberId)}
            />
          </>
        ) : (
          <>
            <FollowButton following={member.following} onClick={() => onFollowToggle(member)} />
            <button className="w-8 h-8 flex items-center justify-center cursor-pointer">
              <FiMoreVertical size={22} color="#999999" />
            </button>
          </>
        )}
      </div>
    </div>
  )
}
