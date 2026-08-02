import AppButton, { type Props } from '@/components/ui/AppButton'
import { cn } from '@/lib/utils'

export default function AppOutlineButton({ className, ...props }: Props) {
  return (
    <AppButton
      className={cn(
        'w-[100px] h-[50px] text-sm leading-[14px] border-[#666666] disabled:border-line disabled:text-[#aaaaaa] disabled:cursor-not-allowed',
        className,
      )}
      {...props}
    />
  )
}
