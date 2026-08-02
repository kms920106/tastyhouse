'use client'

import ShopOwnerMessageModal from '@/components/modals/ShopOwnerMessageModal'
import { FacilityDiv } from '@/components/shops/FacilityItem'
import { FacilitySelectorLayout } from '@/components/shops/FacilitySelectorLayout'
import ClampedText, { MoreButton } from '@/components/ui/ClampedText'
import { ShopAmenity, ShopBreakTime, ShopBusinessHour, ShopClosedDay } from '@/domains/shop'
import Link from 'next/link'
import { useState } from 'react'

type ShopInfoData = {
  phoneNumber: string | null
  ownerMessage: string | null
  ownerMessageCreatedAt: string | null
  businessHours: ShopBusinessHour[]
  breakTimes: ShopBreakTime[]
  closedDays: ShopClosedDay[]
  amenities: ShopAmenity[]
}

interface Props {
  shopInfo: ShopInfoData
}

export default function ShopDetailInfo({ shopInfo }: Props) {
  const {
    businessHours,
    breakTimes,
    closedDays,
    phoneNumber,
    amenities,
    ownerMessage,
    ownerMessageCreatedAt,
  } = shopInfo

  const [isShopOwnerMessageModalOpen, setIsShopOwnerMessageModalOpen] = useState(false)

  return (
    <>
      {ownerMessage && ownerMessageCreatedAt && (
        <>
          <div className="relative mt-[13px] px-[15px] py-[23px] pb-4 bg-[#f9f9f9] border border-[#cccccc] box-border rounded-[5px]">
            <div className="absolute -top-3 left-[10px] inline-block px-3.5 py-[6.5px] mb-3 bg-main text-xs leading-[12px] text-white rounded-full">
              사장님 한마디
            </div>
            <ClampedText
              text={ownerMessage}
              maxLines={1}
              className="text-xs bg-[#f9f9f9]"
              MoreButton={
                <MoreButton
                  onClick={() => setIsShopOwnerMessageModalOpen(true)}
                  className="!bg-[#f9f9f9] text-xs leading-[12px]"
                />
              }
            />
          </div>
          <ShopOwnerMessageModal
            open={isShopOwnerMessageModalOpen}
            onOpenChange={(open) => setIsShopOwnerMessageModalOpen(open)}
            message={ownerMessage}
            createdAt={ownerMessageCreatedAt}
          />
        </>
      )}
      <div className="pt-[30px] pb-5 space-y-[15px] border-b border-line box-border">
        {businessHours && businessHours.length > 0 && (
          <div className="flex justify-between">
            <h3 className="text-sm leading-[14px]">운영시간</h3>
            <div className="space-y-2">
              {businessHours.map((hour, index) => (
                <div key={index} className="flex gap-2">
                  <span className="text-sm leading-[14px]">{hour.dayTypeDescription}</span>
                  <span className="text-sm leading-[14px]">
                    {hour.openTime} - {hour.closeTime}
                  </span>
                </div>
              ))}
            </div>
          </div>
        )}
        {breakTimes && breakTimes.length > 0 && (
          <div className="flex justify-between">
            <h3 className="text-sm leading-[14px]">브레이크타임</h3>
            <div className="space-y-2">
              {breakTimes.map((breakTime, index) => (
                <div key={index} className="flex gap-2">
                  <span className="text-sm leading-[14px]">{breakTime.dayTypeDescription}</span>
                  <span className="text-sm leading-[14px]">
                    {breakTime.startTime} - {breakTime.endTime}
                  </span>
                </div>
              ))}
            </div>
          </div>
        )}
        {closedDays && closedDays.length > 0 && (
          <div className="flex justify-between">
            <h3 className="text-sm leading-[14px]">휴무일</h3>
            <div className="space-y-2">
              {closedDays.map((closedDay, index) => (
                <div key={index} className="flex gap-2">
                  <span className="text-sm leading-[14px]">{closedDay.description}</span>
                </div>
              ))}
            </div>
          </div>
        )}
        {phoneNumber && (
          <div className="flex justify-between">
            <h3 className="text-sm leading-[14px]">전화번호</h3>
            <Link href={`tel:${phoneNumber}`}>
              <p className="text-sm leading-[14px] underline">{phoneNumber}</p>
            </Link>
          </div>
        )}
      </div>
      {amenities && amenities.length > 0 && (
        <div className="pt-5">
          <h3 className="text-sm leading-[14px] mb-[15px]">편의시설</h3>
          <FacilitySelectorLayout>
            {amenities.map((amenity) => {
              return <FacilityDiv key={amenity.code} amenity={amenity} />
            })}
          </FacilitySelectorLayout>
        </div>
      )}
    </>
  )
}
