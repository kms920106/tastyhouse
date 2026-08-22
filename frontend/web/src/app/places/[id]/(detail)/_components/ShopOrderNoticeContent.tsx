'use client'

import ClampedText from '@/components/ui/ClampedText'
import { useShopOrderNotice } from '@/domains/shop/shop.hook'

interface Props {
  shopId: number
}

/**
 * 메뉴판 최상단 주문안내 영역.
 *
 * 미설정·관리자 게시중단은 서버가 응답 `data` 를 null 로 내려주므로 화면은 상태를 분기하지 않고
 * 표시 여부만 결정한다(`frontend.md` C-2).
 *
 * 로딩·에러를 스켈레톤이나 `FetchErrorState` 가 아니라 `null` 로 처리하는 이유는 사장님 공지
 * (`ShopDetailNoticeContent`)와 같다 — 주문안내는 메뉴판의 필수 요소가 아닌 부가 정보이므로,
 * 미설정이 정상인 대다수 가게에서 빈 띠나 에러 박스가 메뉴 목록 위에 얹혀서는 안 된다.
 */
export default function ShopOrderNoticeContent({ shopId }: Props) {
  const { data, isLoading, error } = useShopOrderNotice(shopId)

  if (isLoading || error) return null
  if (!data?.data) return null

  const { content } = data.data

  return (
    <div className="pt-[15px]">
      <div className="relative px-[15px] py-[23px] pb-4 bg-white border border-[#cccccc] box-border rounded-[5px]">
        <div className="absolute -top-3 left-[10px] inline-block px-3.5 py-[6.5px] bg-main text-xs leading-[12px] text-white rounded-full">
          주문안내
        </div>
        {/*
          PDF 요구사항: 1줄을 넘으면 접힌 상태로 노출하고 더보기로 펼친다.
          maxLines={1} 이면 ClampedText 가 실제 scrollHeight 로 넘침을 판정하므로,
          1줄에 들어가는 짧은 문구에는 더보기가 아예 나타나지 않는다.

          MoreButton 을 넘기지 않고 기본값을 쓴다 — ClampedText 는 커스텀 버튼을 그대로 렌더할 뿐
          내부 펼침 핸들러를 주입하지 않으므로, 커스텀 버튼을 넘기면 펼치기가 동작하지 않는다.
          기본 MoreButton 의 배경이 흰색이라 박스 배경도 흰색으로 맞춘다.
        */}
        <ClampedText text={content} maxLines={1} className="text-xs" />
      </div>
    </div>
  )
}
