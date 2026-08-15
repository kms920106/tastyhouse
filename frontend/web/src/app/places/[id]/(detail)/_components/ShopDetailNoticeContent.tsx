'use client'

import ShopNoticeModal from '@/components/modals/ShopNoticeModal'
import ReviewImageGallery from '@/components/reviews/ReviewImageGallery'
import BorderedSection from '@/components/ui/BorderedSection'
import ClampedText, { MoreButton } from '@/components/ui/ClampedText'
import { useShopNotice } from '@/domains/shop/shop.hook'
import { useState } from 'react'
import { ShopDetailNoticeSkeleton } from './ShopDetailNoticeSkeleton'

interface Props {
  shopId: number
  /**
   * 바깥을 `BorderedSection` 으로 감쌀지 여부.
   *
   * 공지 미등록이 대다수 가게의 정상 상태라, 부모가 미리 `BorderedSection` 을 두면 내용 없는
   * 흰 띠가 남는다. 그래서 섹션 경계를 이 컴포넌트가 직접 소유해 렌더 여부와 함께 결정한다.
   * 이미 `BorderedSection` 안에 놓이는 정보 탭(②)에서는 중첩을 피하려고 false 로 쓴다
   * (`src/app/CLAUDE.md` 4.10).
   */
  bordered?: boolean
}

/**
 * 사장님 공지 영역.
 *
 * 가게 상세 최상단 · 정보 탭 · 리뷰 탭 세 곳에서 렌더되며, `useShopNotice` 의 queryKey 가
 * `shopId` 하나이므로 요청은 한 번만 나간다.
 *
 * 에러·미등록을 `FetchErrorState` 가 아니라 `null` 로 처리하는 이유: 공지는 가게 정보의 필수
 * 요소가 아니라 부가 정보다. 공지가 없는 대다수 가게에서 에러 박스가 보이면 안 된다.
 */
export default function ShopDetailNoticeContent({ shopId, bordered = true }: Props) {
  const { data, isLoading, error } = useShopNotice(shopId)
  const [isModalOpen, setIsModalOpen] = useState(false)

  if (isLoading) return <Wrapper bordered={bordered}>{<ShopDetailNoticeSkeleton />}</Wrapper>
  if (error) return null
  if (!data?.data) return null

  const notice = data.data

  return (
    <Wrapper bordered={bordered}>
      <div className="relative mt-[13px] px-[15px] py-[23px] pb-4 bg-[#f9f9f9] border border-[#cccccc] box-border rounded-[5px]">
        <div className="absolute -top-3 left-[10px] inline-block px-3.5 py-[6.5px] mb-3 bg-main text-xs leading-[12px] text-white rounded-full">
          사장님 공지
        </div>
        <ClampedText
          text={notice.content}
          maxLines={2}
          className="text-xs bg-[#f9f9f9]"
          MoreButton={
            <MoreButton
              onClick={() => setIsModalOpen(true)}
              className="!bg-[#f9f9f9] text-xs leading-[12px]"
            />
          }
        />
        {notice.imageUrls.length > 0 && (
          <div className="mt-3">
            <ReviewImageGallery imageUrls={notice.imageUrls} />
          </div>
        )}
      </div>
      <ShopNoticeModal open={isModalOpen} onOpenChange={setIsModalOpen} notice={notice} />
    </Wrapper>
  )
}

function Wrapper({ bordered, children }: { bordered: boolean; children: React.ReactNode }) {
  if (!bordered) return <>{children}</>
  // 좌우 여백은 다른 BorderedSection 내용물과 같은 규칙으로 준다.
  return (
    <BorderedSection>
      <div className="px-[15px] pb-[15px]">{children}</div>
    </BorderedSection>
  )
}
