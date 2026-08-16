'use client'

import MenuReviewItem from '@/components/menu-reviews/MenuReviewItem'
import { MENU_REVIEW_COPY } from '@/domains/menu-review'
import type { MenuReviewWritableItem } from '@/domains/menu-review'

interface Props {
  /** 이 주문의 ID. 저장 후 캐시 무효화에 쓰이도록 항목에 내려준다. */
  orderId: number
  items: MenuReviewWritableItem[]
}

/**
 * 메뉴 평가 섹션.
 *
 * 매장 리뷰 폼과 **같은 폼 안에 두지 않는다** — 제출이 하나로 묶이면
 * "매장 리뷰 없이 메뉴 평가만 남긴다"는 요구가 깨진다. 항목마다 자기 저장 버튼을 갖는다.
 *
 * 평가 제외 대상(주류·사이드)은 서버가 이미 걸러 내려주므로 프론트가 다시 거르지 않는다.
 */
export default function MenuReviewSection({ orderId, items }: Props) {
  // 평가 가능한 메뉴가 0개면 섹션 자체를 렌더하지 않는다.
  if (items.length === 0) return null

  return (
    <section className="px-[15px] py-5">
      <h2 className="text-base leading-[16px]">{MENU_REVIEW_COPY.SECTION_TITLE}</h2>
      <p className="mt-2 text-xs leading-relaxed text-[#666666]">
        {MENU_REVIEW_COPY.SECTION_DESCRIPTION}
      </p>
      <div className="mt-2.5 flex flex-col divide-y divide-line">
        {items.map((item) => (
          // 평가 상태(menuReviewId)를 키에 포함해, 재조회로 값이 바뀌면 항목이 새 초기값으로 다시 마운트되게 한다.
          // 키가 orderProductId뿐이면 React가 인스턴스를 재사용해 갱신된 서버 값이 무시된다.
          <MenuReviewItem
            key={`${item.orderProductId}-${item.menuReviewId ?? 'new'}`}
            orderId={orderId}
            item={item}
          />
        ))}
      </div>
    </section>
  )
}
