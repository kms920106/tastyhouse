import MenuReviewListItem from '@/components/menu-reviews/MenuReviewListItem'
import { MENU_REVIEW_COPY, MENU_REVIEW_PAGE_SIZE } from '@/domains/menu-review'
import { menuReviewRepository } from '@/domains/menu-review/menu-review.repository'
import { productRepository } from '@/domains/product/product.repository'

interface Props {
  productId: number
}

/**
 * 상품 상세의 메뉴 평가 목록.
 *
 * 상품 평점(`rating`)은 기존 필드를 그대로 쓴다 — 값의 근거만 서버에서 MENU_REVIEW로 바뀌었고
 * API 계약은 동일하다.
 */
export default async function MenuReviewListSection({ productId }: Props) {
  // 총 개수는 상품 상세의 `menuReviewCount`가 권위 있는 값이다 — 목록 응답에 페이징 메타가 없으면
  // 한 페이지 크기(5)까지밖에 세지 못한다.
  const [{ data, pagination }, { data: product }] = await Promise.all([
    menuReviewRepository.getProductMenuReviews(productId, {
      page: 0,
      size: MENU_REVIEW_PAGE_SIZE,
    }),
    productRepository.getProductById(productId),
  ])

  const menuReviews = data ?? []
  const totalCount = product?.menuReviewCount ?? pagination?.totalElements ?? menuReviews.length

  return (
    <div className="px-[15px] py-5">
      <div className="flex items-baseline gap-2">
        <h2 className="text-base leading-[16px]">{MENU_REVIEW_COPY.LIST_TITLE}</h2>
        <span className="text-xs leading-[12px] text-[#999999]">
          {MENU_REVIEW_COPY.LIST_COUNT(totalCount)}
        </span>
      </div>
      {menuReviews.length === 0 ? (
        <p className="w-full py-4 text-sm leading-relaxed text-[#999999] text-center">
          {MENU_REVIEW_COPY.LIST_EMPTY}
        </p>
      ) : (
        <div className="flex flex-col divide-y divide-line">
          {menuReviews.map((menuReview) => (
            <MenuReviewListItem key={menuReview.id} menuReview={menuReview} />
          ))}
        </div>
      )}
    </div>
  )
}
