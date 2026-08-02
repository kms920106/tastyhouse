'use client'

import { toast } from '@/components/ui/AppToaster'
import { formatDecimal } from '@/lib/number'
import { PAGE_PATHS } from '@/lib/paths'
import { copyToClipboard } from '@/lib/share'
import Link from 'next/link'
import { ReactNode } from 'react'
import { GrCopy } from 'react-icons/gr'
import { TfiLocationPin } from 'react-icons/tfi'

interface Props {
  id: number
  name: string
  roadAddress: string
  lotAddress: string
  rating: number
  bookmarkButton: ReactNode
}

export default function ShopDetailSummaryInfo({
  id,
  name,
  roadAddress,
  lotAddress,
  rating,
  bookmarkButton,
}: Props) {
  const handleCopyAddress = async () => {
    const success = await copyToClipboard(roadAddress)
    if (success) {
      toast('주소가 복사되었습니다.')
    }
  }

  return (
    <>
      <div className="flex items-start justify-between mb-5">
        <h2 className="text-lg leading-[18px]">{name}</h2>
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
    </>
  )
}
