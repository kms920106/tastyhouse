'use client'

import { toast } from '@/components/ui/AppToaster'
import ImageContainer from '@/components/ui/ImageContainer'
import { copyToClipboard } from '@/lib/share'
import { GrCopy } from 'react-icons/gr'
import { TfiLocationPin } from 'react-icons/tfi'

interface Props {
  shopName: string
  shopImageUrl: string | null
  roadAddress: string | null
  lotAddress: string | null
}

export default function ReservationShopSection({
  shopName,
  shopImageUrl,
  roadAddress,
  lotAddress,
}: Props) {
  const handleCopyAddress = async () => {
    if (!roadAddress) return

    const success = await copyToClipboard(roadAddress)
    if (success) {
      toast('주소가 복사되었습니다.')
    }
  }

  return (
    <div className="px-[15px] pt-5 pb-[15px]">
      <h2 className="text-base leading-[16px] mb-4">{shopName}</h2>
      <div className="flex items-center justify-between">
        <div className="flex-1 flex items-center gap-[15px]">
          <ImageContainer src={shopImageUrl ?? ''} alt={shopName} size={60} />
          <div className="flex flex-col gap-[7px] min-w-0">
            {roadAddress && (
              <div className="text-sm leading-relaxed line-clamp-2">{roadAddress}</div>
            )}
            {lotAddress && (
              <span className="text-xs leading-[12px] text-[#aaaaaa]">[지번] {lotAddress}</span>
            )}
            <div className="mt-1">
              <div className="flex gap-[11px]">
                <button className="flex items-center gap-[3px] cursor-pointer">
                  <TfiLocationPin size={12} className="text-main" />
                  <span className="text-xs leading-[12px] text-main">지도</span>
                </button>
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
        </div>
        <div className="flex items-start justify-between gap-3">
          {/* TODO: 추후 리뷰 작성 기능 연동 시 Link로 교체 */}
          <button className="flex items-center justify-center shrink-0 px-[11px] py-2.5 bg-main text-xs leading-[12px] text-white cursor-pointer">
            리뷰작성
          </button>
        </div>
      </div>
    </div>
  )
}
