'use client'

import ShopDeliveryTipDialog from '@/components/shops/ShopDeliveryTipDialog'
import ShopPriceBadges from '@/components/shops/ShopPriceBadges'
import { toast } from '@/components/ui/AppToaster'
import {
  getShopOperatingStatusName,
  type ShopOperatingStatus,
  type ShopPriceBadges as ShopPriceBadgesData,
} from '@/domains/shop'
import { formatDecimal, formatNumber } from '@/lib/number'
import { PAGE_PATHS } from '@/lib/paths'
import { copyToClipboard } from '@/lib/share'
import { cn } from '@/lib/utils'
import Link from 'next/link'
import { ReactNode, useState } from 'react'
import { GrCopy } from 'react-icons/gr'
import { TfiLocationPin } from 'react-icons/tfi'

interface Props {
  id: number
  name: string
  roadAddress: string
  lotAddress: string
  rating: number
  /** 가게 최소주문금액. 0이면 미설정이라 노출하지 않는다 */
  minOrderAmount: number
  /** 배달팁 하한. 상한과 함께 0이면 노출하지 않는다 */
  minDeliveryTip: number
  /** 배달팁 상한 (고객 주소 확정 전) */
  maxDeliveryTip: number
  /** 가게 영업 상태. PREPARING이면 지금 주문을 받지 않는다 */
  operatingStatus: ShopOperatingStatus
  /** 서버가 완성해 내려주는 한글 사유 문구. 영업중이면 null */
  unavailableReasonName: string | null
  /** 가격 뱃지. 조회 실패 시 null 이고 이때는 노출하지 않는다 */
  priceBadges: ShopPriceBadgesData | null
  bookmarkButton: ReactNode
}

export default function ShopDetailSummaryInfo({
  id,
  name,
  roadAddress,
  lotAddress,
  rating,
  minOrderAmount,
  minDeliveryTip,
  maxDeliveryTip,
  operatingStatus,
  unavailableReasonName,
  priceBadges,
  bookmarkButton,
}: Props) {
  const [isDeliveryTipDialogOpen, setIsDeliveryTipDialogOpen] = useState(false)

  const deliveryTipLabel =
    minDeliveryTip === maxDeliveryTip
      ? `${formatNumber(minDeliveryTip)}원`
      : `${formatNumber(minDeliveryTip)}~${formatNumber(maxDeliveryTip)}원`

  const handleCopyAddress = async () => {
    const success = await copyToClipboard(roadAddress)
    if (success) {
      toast('주소가 복사되었습니다.')
    }
  }

  return (
    <>
      <div className="flex items-start justify-between mb-5">
        <div className="flex flex-col gap-[7px] min-w-0">
          <h2 className="text-lg leading-[18px]">{name}</h2>
          <div className="flex flex-wrap items-center gap-[5px]">
            <span
              className={cn(
                'text-xs leading-[12px]',
                operatingStatus === 'OPEN' ? 'text-main' : 'text-[#999999]',
              )}
            >
              {getShopOperatingStatusName(operatingStatus)}
            </span>
            {/* 사유 문구는 서버가 완성해 내려주므로 그대로 표시한다 */}
            {operatingStatus !== 'OPEN' && unavailableReasonName && (
              <span className="text-xs leading-[12px] text-[#aaaaaa]">{unavailableReasonName}</span>
            )}
            {/* 조건 판정은 서버가 한 값이다 — 둘 다 false 면 컴포넌트가 null 을 돌려 자리를 비운다 */}
            {priceBadges && <ShopPriceBadges badges={priceBadges} />}
          </div>
        </div>
        <span className="text-[19px] leading-[18px] text-main">{formatDecimal(rating, 1)}</span>
      </div>
      <div className="flex justify-between gap-3">
        <div className="flex-1 flex flex-col gap-[7px] min-w-0">
          <div className="text-sm leading-relaxed line-clamp-2">{roadAddress}</div>
          <div className="relative text-xs leading-[12px] text-[#aaaaaa]">
            <span>[지번] {lotAddress}</span>
            <div className="absolute top-0 right-0 flex gap-[11px]">
              <Link href={PAGE_PATHS.PLACE_MAP(id)} className="flex items-center gap-[3px]">
                <TfiLocationPin size={12} className="text-main" />
                <span className="text-xs leading-[12px] text-main">지도</span>
              </Link>
              <button
                className="flex items-center gap-[3px] cursor-pointer"
                onClick={handleCopyAddress}
              >
                <GrCopy size={12} className="text-main" />
                <span className="text-xs leading-[12px] text-main">복사</span>
              </button>
            </div>
          </div>
        </div>
        {bookmarkButton}
      </div>
      {minOrderAmount > 0 && (
        <div className="mt-[7px] text-xs leading-[12px] text-[#aaaaaa]">
          최소주문금액 {formatNumber(minOrderAmount)}원
        </div>
      )}
      {maxDeliveryTip > 0 && (
        <div className="mt-[7px] text-xs leading-[12px] text-[#aaaaaa]">
          <button
            type="button"
            className="cursor-pointer underline"
            onClick={() => setIsDeliveryTipDialogOpen(true)}
          >
            배달팁 {deliveryTipLabel}
          </button>
        </div>
      )}
      <ShopDeliveryTipDialog
        open={isDeliveryTipDialogOpen}
        onOpenChange={setIsDeliveryTipDialogOpen}
        shopId={id}
      />
    </>
  )
}
