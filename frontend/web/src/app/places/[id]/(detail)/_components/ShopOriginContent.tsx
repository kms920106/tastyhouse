'use client'

import { useShopOrigin } from '@/domains/shop/shop.hook'

interface Props {
  shopId: number
}

/**
 * 가게정보 탭의 원산지 영역.
 *
 * 농수산물 원산지 표시법상 필수 표시라 가게 단위로 한 번 작성된다(메뉴별이 아니다).
 * **미설정이거나 조회 실패면 아무것도 그리지 않는다** — 빈 제목만 남으면 표시 의무를 지킨 것처럼
 * 보인다. 원산지는 부가 영역이라 가게정보 탭 전체를 막을 이유가 없다.
 *
 * `ShopDetailTabs`가 `'use client'` 트리이므로 이 컴포넌트도 Fetcher 패턴(client + domain hook)으로
 * 작성한다 — Server Component(`server-only` repository 직접 호출)는 client 트리 안에서 성립하지 않는다.
 */
export default function ShopOriginContent({ shopId }: Props) {
  const { data: response, isLoading, error } = useShopOrigin(shopId)

  if (isLoading || error || !response?.data) {
    return null
  }

  const { sourceType, content, url } = response.data

  if (sourceType === 'FRANCHISE_URL') {
    if (!url) {
      return null
    }

    return (
      <div className="pt-[30px] pb-5 border-b border-line box-border">
        <h3 className="text-sm leading-[14px] mb-[15px]">원산지</h3>
        <a
          href={url}
          target="_blank"
          rel="noopener noreferrer"
          className="text-sm leading-[14px] text-main underline"
        >
          원산지 정보 보기
        </a>
      </div>
    )
  }

  if (!content) {
    return null
  }

  return (
    <div className="pt-[30px] pb-5 border-b border-line box-border">
      <h3 className="text-sm leading-[14px] mb-[15px]">원산지</h3>
      {/* 점주가 메뉴별로 줄을 나눠 쓰므로 줄바꿈이 사라지면 판독이 어렵다 */}
      <p className="text-sm leading-relaxed whitespace-pre-wrap text-[#666666]">{content}</p>
    </div>
  )
}
