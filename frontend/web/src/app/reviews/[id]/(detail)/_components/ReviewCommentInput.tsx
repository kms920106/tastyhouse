'use client'

import Avatar from '@/components/ui/Avatar'
import { useMyProfile } from '@/domains/member/member.hook'
import { PAGE_PATHS } from '@/lib/paths'
import { useRouter } from 'next/navigation'
import React, { useCallback, useEffect, useRef } from 'react'
import ReviewCommentSubmitButtonClient from './ReviewCommentSubmitButtonClient'
import { useReply } from './ReviewReplyProvider'

interface Props {
  isLoggedIn: boolean
  reviewId: number
}

export default function ReviewCommentInput({ isLoggedIn, reviewId }: Props) {
  const router = useRouter()

  const { memberProfile } = useMyProfile({ enabled: isLoggedIn })

  const containerRef = useRef<HTMLDivElement>(null)

  const {
    replyTarget,
    textareaRef,
    clearReply,
    commentText,
    setCommentText,
    isFocused,
    setIsFocused,
  } = useReply()

  // 답글 모드가 활성화되면 @nickname 입력
  useEffect(() => {
    if (replyTarget) {
      setCommentText(`@${replyTarget.nickname} `)
      setIsFocused(true)
    }
  }, [replyTarget, setCommentText, setIsFocused])

  const handleFocus = useCallback(() => {
    if (!isLoggedIn) {
      router.push(PAGE_PATHS.AUTH_LOGIN)
      return
    }
    setIsFocused(true)
  }, [isLoggedIn, router, setIsFocused])

  const handleBlur = useCallback(
    (e: React.FocusEvent<HTMLTextAreaElement>) => {
      // relatedTarget: blur 시 다음으로 focus되는 요소
      // 버튼 클릭 시 blur가 먼저 발생하므로, 컨테이너 내부 요소로 이동하는 경우 focus 유지
      if (containerRef.current?.contains(e.relatedTarget as Node)) {
        return
      }
      // 텍스트가 있으면 focus 상태 유지 (사용자가 입력 중일 수 있음)
      if (commentText.trim()) {
        return
      }
      setIsFocused(false)
      clearReply()
    },
    [commentText, clearReply, setIsFocused],
  )

  // 버튼 표시 조건: focus 상태이거나 텍스트가 있을 때
  const showButton = isFocused || commentText.trim()

  return (
    <div ref={containerRef} className="flex items-center gap-[7px] flex-1">
      <Avatar src={memberProfile?.profileImageUrl ?? null} alt="내 프로필" />
      <div className="flex-1 px-4 py-2.5 border border-line box-border rounded-[20px] grid">
        <textarea
          ref={textareaRef}
          rows={1}
          value={commentText}
          onChange={(e) => setCommentText(e.target.value)}
          onFocus={handleFocus}
          onBlur={handleBlur}
          placeholder={replyTarget ? '답글을 입력하세요.' : '댓글을 입력하세요.'}
          className="min-w-0 text-sm leading-normal bg-transparent outline-none resize-none overflow-y-hidden placeholder:text-[#aaaaaa] [field-sizing:content]"
        />
      </div>
      {showButton && isLoggedIn && <ReviewCommentSubmitButtonClient reviewId={reviewId} />}
    </div>
  )
}
