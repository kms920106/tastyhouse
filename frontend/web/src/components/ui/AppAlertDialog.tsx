import AppPrimaryButton from './AppPrimaryButton'
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from './shadcn/dialog'

interface Props {
  open?: boolean
  onOpenChange?: (open: boolean) => void
  trigger?: React.ReactNode
  title: string
  description: string
  descriptionComponent?: React.ReactNode
  confirmLabel?: string
  onConfirm?: () => void
}

export default function AppAlertDialog({
  open,
  onOpenChange,
  trigger,
  title,
  description,
  descriptionComponent,
  confirmLabel = '확인',
  onConfirm,
}: Props) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      {trigger && <DialogTrigger asChild>{trigger}</DialogTrigger>}
      <DialogContent
        showCloseButton={false}
        className="sm:max-w-sm gap-0 p-0 overflow-hidden rounded-[2.5px] border-none"
        onInteractOutside={(e) => e.preventDefault()}
        onEscapeKeyDown={(e) => e.preventDefault()}
      >
        <DialogHeader className="gap-[17px] px-7 pt-[30px] pb-6 text-center">
          <DialogTitle className="text-[17px] leading-[17px] font-normal">{title}</DialogTitle>
          <DialogDescription className="text-sm leading-relaxed text-[#999999] whitespace-pre-line">
            {description}
          </DialogDescription>
          {descriptionComponent && descriptionComponent}
        </DialogHeader>
        <DialogFooter className="flex-row gap-0 sm:flex-row">
          <DialogClose asChild>
            <AppPrimaryButton onClick={onConfirm}>{confirmLabel}</AppPrimaryButton>
          </DialogClose>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
