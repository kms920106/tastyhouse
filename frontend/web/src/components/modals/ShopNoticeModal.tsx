'use client'

import ReviewImageGallery from '@/components/reviews/ReviewImageGallery'
import {
  Modal,
  ModalContentWrapper,
  ModalDescription,
  ModalFooter,
  ModalHeader,
  ModalTitle,
} from '@/components/ui/Modal'
import type { ShopNotice } from '@/domains/shop'
import { formatDate } from '@/lib/date'
import AppPrimaryButton from '../ui/AppPrimaryButton'

interface Props {
  open: boolean
  onOpenChange: (open: boolean) => void
  notice: ShopNotice
}

/**
 * 사장님 공지 전문 모달.
 *
 * `ShopOwnerMessageModal`(사장님 한마디)과 같은 형태이되, 이미지 목록을 함께 보여준다.
 * 이미지 확대는 `ReviewImageGallery` 가 스와이퍼와 라이트박스를 함께 들고 있어 그대로 재사용한다.
 */
export default function ShopNoticeModal({ open, onOpenChange, notice }: Props) {
  const createdAtFormatted = formatDate(notice.createdAt, 'YYYY년 M월 D일')

  return (
    <Modal open={open} onOpenChange={onOpenChange}>
      <ModalHeader>
        <ModalTitle className="pt-10 pb-[30px] text-base leading-[16px] text-center font-bold">
          사장님 공지
        </ModalTitle>
      </ModalHeader>
      <ModalDescription className="sr-only">사장님 공지</ModalDescription>
      <ModalContentWrapper className="px-5 pb-[30px]">
        <p className="mb-5 text-[14px] leading-relaxed whitespace-pre-wrap">{notice.content}</p>
        {notice.imageUrls.length > 0 && (
          <div className="mb-10">
            <ReviewImageGallery imageUrls={notice.imageUrls} />
          </div>
        )}
        <p className="text-xs leading-[12px] text-[#cccccc] text-right">
          {createdAtFormatted} 작성됨
        </p>
      </ModalContentWrapper>
      <ModalFooter>
        <AppPrimaryButton onClick={() => onOpenChange(false)}>확인</AppPrimaryButton>
      </ModalFooter>
    </Modal>
  )
}
