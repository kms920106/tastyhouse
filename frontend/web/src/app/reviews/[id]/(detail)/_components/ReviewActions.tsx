'use client'

import { ReactNode } from 'react'
import { IoChatboxOutline } from 'react-icons/io5'
import { useReply } from './ReviewReplyProvider'

interface Props {
  reviewId: number
  reviewLike: ReactNode
}

export default function ReviewActions({ reviewLike }: Props) {
  const { textareaRef } = useReply()

  return (
    <div className="flex items-center gap-5 mt-[15px] pt-[17px] border-t border-line box-border">
      {reviewLike}
      <button
        onClick={() => textareaRef.current?.focus()}
        className="flex items-center gap-1.5 cursor-pointer"
      >
        <IoChatboxOutline size={17} />
        <span className="text-xs leading-[12px]">댓글</span>
      </button>
    </div>
  )
}
