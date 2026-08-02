import {
  Modal,
  ModalContentWrapper,
  ModalDescription,
  ModalFooter,
  ModalHeader,
  ModalTitle,
} from '@/components/ui/Modal'
import { formatDate } from '@/lib/date'
import AppPrimaryButton from '../ui/AppPrimaryButton'

interface Props {
  open: boolean
  onOpenChange: (open: boolean) => void
  message: string
  createdAt: string
}

export default function ShopOwnerMessageModal({
  open,
  onOpenChange,
  message,
  createdAt,
}: Props) {
  const createdAtFormatted = formatDate(createdAt, 'YYYY년 M월 D일')

  return (
    <Modal open={open} onOpenChange={onOpenChange}>
      <ModalHeader>
        <ModalTitle className="pt-10 pb-[30px] text-base leading-[16px] text-center font-bold">
          사장님 한마디
        </ModalTitle>
      </ModalHeader>
      <ModalDescription className="sr-only">사장님 한마디</ModalDescription>
      <ModalContentWrapper className="px-5 pb-[30px]">
        <p className="mb-10 text-[14px] leading-relaxed whitespace-pre-wrap">{message}</p>
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
