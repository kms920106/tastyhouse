'use client'

import AppButton from '@/components/ui/AppButton'
import AppInputText from '@/components/ui/AppInputText'
import AppPrimaryButton from '@/components/ui/AppPrimaryButton'
import { toast } from '@/components/ui/AppToaster'
import PostcodeModal from '@/components/ui/PostcodeModal'
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from '@/components/ui/shadcn/accordion'
import type { MemberDeliveryAddress } from '@/domains/member'
import { useCreateMyDeliveryAddress } from '@/domains/member/member.hook'
import { convertAddressToCoordinates } from '@/lib/kakaoLocal'
import { cn } from '@/lib/utils'
import { useState } from 'react'
import type { Address } from 'react-daum-postcode'

/** 좌표 변환 실패 시 안내 문구. 좌표 없는 주소는 배달팁이 계산되지 않으므로 저장하지 않는다. */
const COORDINATES_CONVERT_FAILED_MESSAGE =
  '주소의 위치를 확인할 수 없습니다. 주소를 다시 검색해 주세요.'

interface SelectedPostcode {
  roadAddress: string
  lotAddress: string
}

interface Props {
  deliveryAddresses: MemberDeliveryAddress[]
  isLoading: boolean
  /** 현재 선택된 배달 주소 id. 아직 고르지 않았으면 null */
  selectedDeliveryAddressId: number | null
  onDeliveryAddressSelect: (deliveryAddressId: number) => void
}

/**
 * 배달 주소 선택·등록 섹션.
 *
 * 배달 주문에서만 렌더합니다(호출하는 쪽에서 주문 방법을 판정). 새 주소는 우편번호 검색 →
 * 카카오 로컬 API 좌표 변환 → 상세주소 입력 순서로 등록하며, **좌표 변환에 실패하면 저장하지 않고
 * 재선택을 요청**합니다 — 좌표 없는 주소는 거리별 배달팁이 계산되지 않습니다.
 */
export default function DeliveryAddressSection({
  deliveryAddresses,
  isLoading,
  selectedDeliveryAddressId,
  onDeliveryAddressSelect,
}: Props) {
  const [isPostcodeOpen, setIsPostcodeOpen] = useState(false)
  const [selectedPostcode, setSelectedPostcode] = useState<SelectedPostcode | null>(null)
  const [detailAddress, setDetailAddress] = useState('')
  const [isConverting, setIsConverting] = useState(false)

  const { createDeliveryAddress, isCreating } = useCreateMyDeliveryAddress({
    onSuccess: (deliveryAddressId) => {
      onDeliveryAddressSelect(deliveryAddressId)
      setSelectedPostcode(null)
      setDetailAddress('')
      toast('배달 주소가 등록되었습니다.')
    },
    onError: (message) => {
      toast(message)
    },
  })

  const handlePostcodeComplete = (address: Address) => {
    setSelectedPostcode({
      roadAddress: address.roadAddress || address.address,
      lotAddress: address.jibunAddress,
    })
    setDetailAddress('')
  }

  const handleCreate = async () => {
    if (!selectedPostcode) {
      toast('주소를 검색해 주세요.')
      return
    }

    setIsConverting(true)
    const coordinates = await convertAddressToCoordinates(selectedPostcode.roadAddress)
    setIsConverting(false)

    // 좌표를 확보하지 못하면 저장을 시도하지 않고 재선택을 요청한다.
    if (!coordinates) {
      toast(COORDINATES_CONVERT_FAILED_MESSAGE)
      setSelectedPostcode(null)
      setDetailAddress('')
      return
    }

    createDeliveryAddress({
      roadAddress: selectedPostcode.roadAddress,
      lotAddress: selectedPostcode.lotAddress || undefined,
      detailAddress: detailAddress.trim() || undefined,
      latitude: coordinates.latitude,
      longitude: coordinates.longitude,
      isDefault: deliveryAddresses.length === 0,
    })
  }

  const isSubmitting = isConverting || isCreating

  return (
    <>
      {isPostcodeOpen && (
        <PostcodeModal
          onComplete={handlePostcodeComplete}
          onClose={() => setIsPostcodeOpen(false)}
        />
      )}
      <Accordion type="single" collapsible defaultValue="delivery-address">
        <AccordionItem value="delivery-address" className="border-b-0">
          <AccordionTrigger className="items-center px-[15px] py-5 hover:no-underline">
            <h2 className="text-base leading-[16px]">배달 주소</h2>
          </AccordionTrigger>
          <AccordionContent className="p-0">
            <div className="px-[15px] py-2.5 pb-5 flex flex-col gap-[15px]">
              {isLoading && (
                <p className="text-sm leading-[14px] text-[#666666]">주소를 불러오는 중입니다.</p>
              )}
              {!isLoading && deliveryAddresses.length === 0 && (
                <p className="text-sm leading-[14px] text-[#666666]">
                  등록된 배달 주소가 없습니다. 주소를 추가해 주세요.
                </p>
              )}
              {deliveryAddresses.length > 0 && (
                <ul className="flex flex-col gap-2.5">
                  {deliveryAddresses.map((address) => (
                    <li key={address.id}>
                      <button
                        type="button"
                        onClick={() => onDeliveryAddressSelect(address.id)}
                        className={cn(
                          'w-full px-[15px] py-3 text-left border border-line cursor-pointer',
                          selectedDeliveryAddressId === address.id && 'border-main',
                        )}
                      >
                        <div className="flex items-center gap-2">
                          {address.alias && (
                            <span className="text-xs leading-[12px] text-main">
                              {address.alias}
                            </span>
                          )}
                          {address.isDefault && (
                            <span className="text-xs leading-[12px] text-[#aaaaaa]">
                              기본 배송지
                            </span>
                          )}
                        </div>
                        <p className="mt-1.5 text-sm leading-relaxed">{address.roadAddress}</p>
                        {address.detailAddress && (
                          <p className="text-xs leading-[16px] text-[#666666]">
                            {address.detailAddress}
                          </p>
                        )}
                      </button>
                    </li>
                  ))}
                </ul>
              )}
              {selectedPostcode ? (
                <div className="flex flex-col gap-2.5">
                  <p className="text-sm leading-relaxed">{selectedPostcode.roadAddress}</p>
                  <AppInputText
                    value={detailAddress}
                    onChange={(e) => setDetailAddress(e.target.value)}
                    placeholder="상세주소를 입력해 주세요."
                  />
                  <div className="flex gap-2">
                    <AppButton
                      className="flex-1 h-[50px] text-sm leading-[14px]"
                      onClick={() => {
                        setSelectedPostcode(null)
                        setDetailAddress('')
                      }}
                      disabled={isSubmitting}
                    >
                      취소
                    </AppButton>
                    <AppPrimaryButton
                      className="flex-1 text-sm leading-[14px]"
                      onClick={handleCreate}
                      disabled={isSubmitting}
                    >
                      주소 저장
                    </AppPrimaryButton>
                  </div>
                </div>
              ) : (
                <AppButton
                  className="h-[50px] text-sm leading-[14px]"
                  onClick={() => setIsPostcodeOpen(true)}
                >
                  새 주소 추가
                </AppButton>
              )}
            </div>
          </AccordionContent>
        </AccordionItem>
      </Accordion>
    </>
  )
}
