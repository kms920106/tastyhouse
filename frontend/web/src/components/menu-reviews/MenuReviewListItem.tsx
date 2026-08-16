import TimeAgo from '@/components/reviews/TimeAgo'
import Avatar from '@/components/ui/Avatar'
import Rating from '@/components/ui/Rating'
import type { MenuReviewListItem as MenuReviewListItemData } from '@/domains/menu-review'

interface Props {
  menuReview: MenuReviewListItemData
}

/**
 * 상품 상세의 메뉴 평가 카드.
 *
 * 매장 리뷰 카드(`ReviewListItem`)를 재사용하지 않는다 — 그 컴포넌트는 댓글·좋아요 같은
 * 소셜 기능을 전제하는데, 메뉴 평가에는 그런 기능이 없다(설계 원칙 2).
 */
export default function MenuReviewListItem({ menuReview }: Props) {
  return (
    <div className="py-5">
      <div className="flex justify-between">
        <div className="flex items-center gap-2.5">
          <Avatar src={menuReview.memberProfileImageUrl} alt={menuReview.memberNickname} />
          <div className="flex flex-col gap-2">
            <p className="text-sm leading-[14px]">{menuReview.memberNickname}</p>
            <TimeAgo date={menuReview.createdAt} />
          </div>
        </div>
        <Rating as="p" value={menuReview.rating} />
      </div>
      {menuReview.comment && (
        <p className="mt-[15px] text-sm leading-[20px] whitespace-pre-line">{menuReview.comment}</p>
      )}
    </div>
  )
}
