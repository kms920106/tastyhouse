import {
  Dialog,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogOverlay,
  DialogPortal,
  DialogTitle,
} from '@/components/ui/shadcn/dialog'
import { cn } from '@/lib/utils'
import * as DialogPrimitive from '@radix-ui/react-dialog'
import { DialogProps } from '@radix-ui/react-dialog'
import * as React from 'react'

interface ModalProps extends DialogProps {
  contentClassName?: string
}

export function Modal({ children, contentClassName, ...props }: ModalProps) {
  return (
    <Dialog {...props}>
      <DialogPortal>
        <DialogOverlay />
        <DialogPrimitive.Content
          onOpenAutoFocus={(e) => e.preventDefault()}
          onPointerDownOutside={(e) => e.preventDefault()}
          onEscapeKeyDown={(e) => e.preventDefault()}
          className={cn(
            'fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-[85%] bg-white z-50',
            contentClassName,
          )}
        >
          {children}
        </DialogPrimitive.Content>
      </DialogPortal>
    </Dialog>
  )
}

export function ModalHeader({ className, ...props }: React.HTMLAttributes<HTMLDivElement>) {
  return <DialogHeader className={className} {...props} />
}

export function ModalTitle({ className, ...props }: React.HTMLAttributes<HTMLDivElement>) {
  return <DialogTitle className={className} {...props} />
}

export function ModalDescription({
  className,
  ...props
}: React.HTMLAttributes<HTMLParagraphElement>) {
  return <DialogDescription className={className} {...props} />
}

export function ModalContentWrapper({ className, ...props }: React.HTMLAttributes<HTMLDivElement>) {
  return <div className={className} {...props} />
}

export function ModalFooter({ className, ...props }: React.HTMLAttributes<HTMLDivElement>) {
  return <DialogFooter className={className} {...props} />
}
