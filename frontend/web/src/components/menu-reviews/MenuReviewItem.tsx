'use client'

import { createMenuReview, updateMenuReview } from '@/actions/menu-review'
import MenuReviewStarInput from '@/components/menu-reviews/MenuReviewStarInput'
import AppTextarea from '@/components/ui/AppTextarea'
import { toast } from '@/components/ui/AppToaster'
import ImageContainer from '@/components/ui/ImageContainer'
import {
  MENU_REVIEW_COMMENT_MAX_LENGTH,
  MENU_REVIEW_COPY,
  getMenuReviewErrorMessage,
} from '@/domains/menu-review'
import type { MenuReviewWritableItem } from '@/domains/menu-review'
import { useRouter } from 'next/navigation'
import { useState, useTransition } from 'react'

/** 이미 평가한 메뉴에 다시 등록을 시도했을 때 서버가 주는 코드 */
const MENU_REVIEW_ALREADY_EXISTS_ERROR_CODE = 'MENU_REVIEW_ALREADY_EXISTS'

interface Props {
  /** 이 주문 항목이 속한 주문 ID. 저장 후 캐시 무효화 대상 경로를 만드는 데 쓴다. */
  orderId: number
  item: MenuReviewWritableItem
}

/**
 * 메뉴 평가 항목 한 건.
 *
 * 항목마다 자기 저장 버튼을 갖는다 — 한 항목이 실패해도 다른 항목의 저장 결과를 되돌리지 않는다.
 * `menuReviewId`가 있으면 등록(POST)이 아니라 수정(PUT)으로 나간다.
 */
export default function MenuReviewItem({ orderId, item }: Props) {
  const router = useRouter()
  const [menuReviewId, setMenuReviewId] = useState<number | null>(item.menuReviewId)
  const [rating, setRating] = useState(item.rating ?? 0)
  const [comment, setComment] = useState(item.comment ?? '')
  const [isCommentOpen, setIsCommentOpen] = useState(!!item.comment)
  const [isSaving, startSaving] = useTransition()

  const isEditing = menuReviewId !== null

  const handleSave = () => {
    if (rating < 1) {
      toast(MENU_REVIEW_COPY.RATING_REQUIRED)
      return
    }

    startSaving(async () => {
      const trimmedComment = comment.trim()
      const body = { rating, comment: trimmedComment || undefined }

      const result = isEditing
        ? await updateMenuReview(menuReviewId, { orderId, ...body })
        : await createMenuReview({ orderId, orderProductId: item.orderProductId, ...body })

      if (result.error) {
        toast(getMenuReviewErrorMessage(result.errorCode))

        // 이미 평가한 항목이면 서버가 가진 menuReviewId를 모르는 상태다.
        // 재조회해 수정 모드로 전환하지 않으면 재시도해도 같은 오류만 반복된다.
        if (result.errorCode === MENU_REVIEW_ALREADY_EXISTS_ERROR_CODE) {
          router.refresh()
        }
        return
      }

      // 등록 응답은 생성된 menuReviewId다. 저장해 두면 다음 저장부터 수정으로 나간다.
      if (!isEditing && typeof result.data === 'number') {
        setMenuReviewId(result.data)
      }

      toast(MENU_REVIEW_COPY.SAVED)
    })
  }

  return (
    <div className="flex flex-col gap-3 py-4">
      <div className="flex items-center gap-2.5">
        <ImageContainer src={item.productImageUrl} alt={item.productName} size={50} rounded="2.5px" />
        <div className="flex flex-col gap-2">
          <p className="text-sm leading-[14px]">{item.productName}</p>
          <MenuReviewStarInput value={rating} onChange={setRating} disabled={isSaving} />
        </div>
      </div>

      {isCommentOpen ? (
        <div className="relative">
          <AppTextarea
            value={comment}
            onChange={(event) => setComment(event.target.value)}
            maxLength={MENU_REVIEW_COMMENT_MAX_LENGTH}
            rows={3}
            placeholder={MENU_REVIEW_COPY.COMMENT_PLACEHOLDER}
          />
          <span className="absolute bottom-[15px] right-[15px] text-xs leading-[12px] text-[#cccccc]">
            {comment.length} / {MENU_REVIEW_COMMENT_MAX_LENGTH}
          </span>
        </div>
      ) : (
        <button
          type="button"
          onClick={() => setIsCommentOpen(true)}
          className="self-start text-xs leading-[12px] text-[#666666] underline cursor-pointer"
        >
          {MENU_REVIEW_COPY.COMMENT_TOGGLE}
        </button>
      )}

      <button
        type="button"
        onClick={handleSave}
        disabled={isSaving}
        className="self-end px-4 py-2 bg-main text-xs leading-[12px] text-white cursor-pointer disabled:opacity-50"
      >
        {isEditing ? MENU_REVIEW_COPY.UPDATE : MENU_REVIEW_COPY.SAVE}
      </button>
    </div>
  )
}
