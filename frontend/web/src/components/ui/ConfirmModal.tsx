import { Modal, ModalDescription, ModalFooter, ModalHeader, ModalTitle } from './Modal'

interface Props {
  open: boolean
  title?: string
  description: string
  onConfirm: () => void
  onCancel: () => void
  confirmLabel?: string
  cancelLabel?: string
}

export default function ConfirmModal({
  open,
  title,
  description,
  onConfirm,
  onCancel,
  confirmLabel = '확인',
  cancelLabel = '취소',
}: Props) {
  return (
    <Modal open={open} onOpenChange={(open) => !open && onCancel()}>
      <ModalHeader className="px-5 pt-7 pb-0">
        {title && <ModalTitle className="text-base leading-[16px] font-bold">{title}</ModalTitle>}
        <ModalDescription className="mt-2.5 text-sm leading-relaxed text-[#666666]">
          {description}
        </ModalDescription>
      </ModalHeader>
      <ModalFooter className="flex p-0 mt-5 border-t border-line">
        <button
          onClick={onCancel}
          className="flex-1 py-4 text-sm leading-[14px] text-[#666666] border-r border-line"
        >
          {cancelLabel}
        </button>
        <button onClick={onConfirm} className="flex-1 py-4 text-sm leading-[14px] text-main">
          {confirmLabel}
        </button>
      </ModalFooter>
    </Modal>
  )
}
