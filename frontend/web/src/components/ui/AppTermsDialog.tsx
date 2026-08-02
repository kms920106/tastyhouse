import AppPrimaryButton from '@/components/ui/AppPrimaryButton'
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/shadcn/dialog'

interface Props {
  open?: boolean
  onOpenChange?: (open: boolean) => void
  title: string
  children: React.ReactNode
  confirmLabel?: string
  onConfirm?: () => void
}

export default function AppTermsDialog({
  open,
  onOpenChange,
  title,
  children,
  confirmLabel = '확인',
  onConfirm,
}: Props) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent
        showCloseButton={false}
        className="sm:max-w-sm gap-0 p-0 overflow-hidden rounded-[2.5px] border-none"
        onInteractOutside={(e) => e.preventDefault()}
        onEscapeKeyDown={(e) => e.preventDefault()}
      >
        <DialogHeader className="gap-[17px] py-10 text-center">
          <DialogTitle className="text-[17px] leading-[17px] font-normal">{title}</DialogTitle>
        </DialogHeader>
        <div className="max-h-[50vh] overflow-y-auto px-[15px] pb-6 text-xs leading-relaxed text-left whitespace-pre-line">
          {children}
        </div>
        <DialogFooter className="flex-row gap-0 sm:flex-row">
          <DialogClose asChild>
            <AppPrimaryButton onClick={onConfirm}>{confirmLabel}</AppPrimaryButton>
          </DialogClose>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
