'use client'

import MemberNickname from '@/components/members/MemberNickname'
import TimeAgo from '@/components/reviews/TimeAgo'
import Avatar from '@/components/ui/Avatar'
import {
  Drawer,
  DrawerClose,
  DrawerContent,
  DrawerDescription,
  DrawerTitle,
  DrawerTrigger,
} from '@/components/ui/shadcn/drawer'
import type { ReviewComment } from '@/domains/review'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'
import { FiMoreVertical } from 'react-icons/fi'
import { useReply } from './ReviewReplyProvider'

interface Props {
  comment: ReviewComment
  currentMemberId: number | null
}

export default function ReviewCommentItem({ comment, currentMemberId }: Props) {
  const { triggerReply } = useReply()
  const isMyComment = currentMemberId !== null && comment.memberId === currentMemberId

  return (
    <div className="flex gap-2.5">
      <Link href={PAGE_PATHS.MEMBER_DETAIL(comment.memberId)}>
        <Avatar src={comment.memberProfileImageUrl} alt={comment.memberNickname} />
      </Link>
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-[15px] mb-2.5">
          <MemberNickname size="sm">{comment.memberNickname}</MemberNickname>
          <TimeAgo date={comment.createdAt} />
        </div>
        <p className="text-xs leading-relaxed whitespace-pre-wrap break-words">{comment.content}</p>
        <button
          className="mt-[15px] text-xs leading-[12px] text-[#999999] cursor-pointer"
          onClick={() => triggerReply(comment.id, comment.memberNickname, comment.memberId)}
        >
          답글달기
        </button>
      </div>
      {!isMyComment && (
        <Drawer>
          <DrawerTrigger asChild>
            <button className="h-[18px] cursor-pointer flex-shrink-0">
              <FiMoreVertical size={18} color="#999999" />
            </button>
          </DrawerTrigger>
          <DrawerContent className="bg-transparent p-[15px] border-none">
            <DrawerTitle className="sr-only">댓글 옵션</DrawerTitle>
            <DrawerDescription className="sr-only">신고, 차단</DrawerDescription>
            <div className="text-center bg-white rounded-[14px]">
              <DrawerClose asChild>
                <button className="w-full py-[20.5px] text-sm leading-[14px]">신고</button>
              </DrawerClose>
              <div className="h-px bg-[#f6f6f6]" />
              <DrawerClose asChild>
                <button className="w-full py-[20.5px] text-sm leading-[14px]">차단</button>
              </DrawerClose>
            </div>
          </DrawerContent>
        </Drawer>
      )}
    </div>
  )
}
