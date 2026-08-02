import AppButton, { type Props } from '@/components/ui/AppButton'
import { cn } from '@/lib/utils'

export default function AppFullButton({ className, ...props }: Props) {
  return (
    <AppButton
      className={cn(
        'w-full h-[50px] text-base leading-[16px] hover:text-white border-0',
        className,
      )}
      {...props}
    />
  )
}
