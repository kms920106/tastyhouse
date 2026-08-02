'use client'

import { createComment, createReply } from '@/actions/review'
import { toast } from '@/components/ui/AppToaster'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { useState } from 'react'
import ReviewCommentSubmitButton from './ReviewCommentSubmitButton'
import { useReply } from './ReviewReplyProvider'

interface Props {
  reviewId: number
}

export default function ReviewCommentSubmitButtonClient({ reviewId }: Props) {
  const [isSubmitting, setIsSubmitting] = useState(false)
  const { replyTarget, clearReply, commentText, setCommentText, setIsFocused, textareaRef } =
    useReply()

  const handleSubmit = async () => {
    let content = commentText.trim()

    // 답글 모드일 때 @nickname 제거
    if (replyTarget) {
      const mentionPrefix = `@${replyTarget.nickname} `
      if (content.startsWith(mentionPrefix)) {
        content = content.slice(mentionPrefix.length).trim()
      }
    }

    if (!content) {
      toast(replyTarget ? '답글을 입력해 주세요.' : '댓글을 입력해 주세요.')
      return
    }

    if (isSubmitting) return

    setIsSubmitting(true)

    if (!replyTarget) {
      // 댓글 등록
      const request = { content }
      const { error, data } = await createComment(reviewId, request)

      if (error || !data) {
        toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
        setIsSubmitting(false)
        return
      }
    } else {
      // 답글 등록
      const { error, data } = await createReply(reviewId, replyTarget.commentId, {
        content,
        replyToMemberId: replyTarget.memberId,
      })

      if (error || !data) {
        toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
        setIsSubmitting(false)
        return
      }
    }

    setCommentText('')
    setIsFocused(false)
    clearReply()
    textareaRef.current?.blur()
    setIsSubmitting(false)
  }

  return <ReviewCommentSubmitButton onClick={handleSubmit} isSubmitting={isSubmitting} />
}
