'use client'

import AppTextarea from '@/components/ui/AppTextarea'
import { FaStar } from 'react-icons/fa'

/** 배달 평가 문구 — 컴포넌트에 인라인하지 않는다. */
export const DELIVERY_RATING_COPY = {
  TITLE: '배달은 어떠셨나요?',
  /** PDF 규격: 배달 평가는 고객 앱에 노출되지 않는다. */
  OWNER_ONLY_GUIDE: '배달 평가는 사장님에게만 보여요.',
  COMMENT_PLACEHOLDER: '배달에 대해 남기고 싶은 말이 있다면 적어주세요. (선택)',
}

export const DELIVERY_COMMENT_MAX_LENGTH = 500

const STARS = [1, 2, 3, 4, 5]

interface Props {
  rating: number
  comment: string
  onRatingChange: (rating: number) => void
  onCommentChange: (comment: string) => void
}

/**
 * 배달 평가 섹션. 호출부에서 `orderMethod === 'DELIVERY'`일 때만 렌더한다.
 *
 * 배달 경험은 메뉴가 아니라 매장 리뷰에 속하므로 매장 리뷰 폼의 일부로 제출된다.
 */
export default function DeliveryRatingSection({
  rating,
  comment,
  onRatingChange,
  onCommentChange,
}: Props) {
  return (
    <div className="flex flex-col gap-5 px-[15px] py-[30px]">
      <div className="flex flex-col items-center gap-2">
        <p className="text-base leading-[16px]">{DELIVERY_RATING_COPY.TITLE}</p>
        <p className="text-xs leading-[12px] text-[#666666]">
          {DELIVERY_RATING_COPY.OWNER_ONLY_GUIDE}
        </p>
      </div>
      <div className="flex justify-center gap-2">
        {STARS.map((star) => (
          <button
            key={star}
            type="button"
            aria-label={`${star}점`}
            onClick={() => onRatingChange(star === rating ? 0 : star)}
            className="cursor-pointer transition-transform hover:scale-110"
          >
            <FaStar size={40} className={star <= rating ? 'text-main' : 'text-line'} />
          </button>
        ))}
      </div>
      <div className="relative">
        <AppTextarea
          value={comment}
          onChange={(event) => onCommentChange(event.target.value)}
          maxLength={DELIVERY_COMMENT_MAX_LENGTH}
          rows={3}
          placeholder={DELIVERY_RATING_COPY.COMMENT_PLACEHOLDER}
        />
        <span className="absolute bottom-[15px] right-[15px] text-xs leading-[12px] text-[#cccccc]">
          {comment.length} / {DELIVERY_COMMENT_MAX_LENGTH}
        </span>
      </div>
    </div>
  )
}
