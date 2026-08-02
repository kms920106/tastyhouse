import {
  Modal,
  ModalContentWrapper,
  ModalDescription,
  ModalFooter,
  ModalHeader,
  ModalTitle,
} from '@/components/ui/Modal'
import AppPrimaryButton from '../ui/AppPrimaryButton'

interface Props {
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function RankInfoModal({ open, onOpenChange }: Props) {
  return (
    <Modal open={open} onOpenChange={onOpenChange}>
      <ModalHeader>
        <ModalTitle className="pt-10 pb-2 text-base leading-[16px] text-center font-bold">
          랭킹 선정 기준 및 안내
        </ModalTitle>
      </ModalHeader>
      <ModalDescription className="sr-only">랭킹 선정 기준 및 안내</ModalDescription>
      <ModalContentWrapper className="px-5 py-[30px]">
        <h3 className="text-sm leading-[14px]">랭킹 선정 기준</h3>
        <ul className="py-5 space-y-1 list-none text-xs leading-relaxed border-b border-line">
          <li>1. 최근 6개월동안 테이스티하우스에 등록된 리뷰 합산</li>
          <li>
            2. 리뷰 작성 기준을 준수한 리뷰
            <span className="text-[#999999]">(*리뷰 작성시 포인트 적립 및 주의사항 참조)</span>
          </li>
          <li>3. 최근 6개월동안 테이스티하우스에 등록된 리뷰 합산</li>
        </ul>
        <p className="pt-5 text-xs leading-relaxed">
          랭킹은 1월1일, 7월 1일 두번 6개월을 기준으로 초기화되며, 랭킹에 보여지는 리뷰 갯수와
          등급만 초기화처리되며, 작성된 리뷰는 유지됩니다.
        </p>
      </ModalContentWrapper>
      <ModalFooter>
        <AppPrimaryButton onClick={() => onOpenChange(false)}>확인</AppPrimaryButton>
      </ModalFooter>
    </Modal>
  )
}
