import TimeAgo from '@/components/reviews/TimeAgo'

/** 사장님 답변 카드에 노출되는 고정 문구 — 컴포넌트에 인라인하지 않는다. */
export const REVIEW_OWNER_REPLY_COPY = {
  /**
   * 작성자 라벨. 가게명을 알 수 있는 화면(리뷰 상세)에서는 "○○ 사장님",
   * 가게명이 없는 목록 화면에서는 "사장님"으로 떨어진다.
   */
  AUTHOR_LABEL: (shopName?: string) => (shopName ? `${shopName} 사장님` : '사장님'),
}

interface Props {
  /** 가게명. 알 수 없는 화면에서는 생략하고 "사장님"만 표시한다. */
  shopName?: string
  content: string
  createdAt: string
}

/**
 * 리뷰에 달린 사장님 답변 카드.
 *
 * 답변이 없는 경우(`content === null`)에는 호출부에서 조건부로 렌더하지 않는다 —
 * 이 컴포넌트는 답변이 있다는 전제에서만 그려진다.
 *
 * 가시성 판정은 서버가 끝낸 상태로 내려온다. 클라이언트에서 추가로 거르지 않는다.
 */
export default function ReviewOwnerReply({ shopName, content, createdAt }: Props) {
  return (
    <div className="rounded-lg bg-[#f6f6f6] px-[15px] py-3.5">
      <div className="flex items-center justify-between gap-2">
        <p className="text-sm leading-[14px] font-bold text-foreground">
          {REVIEW_OWNER_REPLY_COPY.AUTHOR_LABEL(shopName)}
        </p>
        <TimeAgo date={createdAt} />
      </div>
      <p className="mt-2.5 text-sm leading-[20px] whitespace-pre-line text-foreground/80">
        {content}
      </p>
    </div>
  )
}
