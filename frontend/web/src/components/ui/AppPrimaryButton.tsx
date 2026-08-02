import AppButton, { type Props } from '@/components/ui/AppButton'
import { cn } from '@/lib/utils'

export default function AppPrimaryButton({ className, ...props }: Props) {
  return (
    <AppButton
      className={cn(
        'w-full h-[50px] text-base leading-[16px] border-0',
        'bg-main text-white hover:bg-main/90 hover:text-white',
        'disabled:bg-main/50 disabled:cursor-not-allowed',
        className,
      )}
      {...props}
    />
  )
}
