import { cn } from '@/lib/utils'
import { Textarea } from './shadcn/textarea'

type Props = React.ComponentProps<'textarea'> & {
  error?: boolean
}

export default function AppTextarea({ className, error, ...props }: Props) {
  return (
    <Textarea
      className={cn(
        'field-sizing-fixed w-full px-[16px] py-[19px] text-sm leading-[14px] border border-line box-border bg-white shadow-none rounded-none resize-none focus-visible:ring-0 focus-visible:border-[#666666] placeholder:text-[#aaaaaa]',
        error && 'border-[#bc4040] focus-visible:border-[#bc4040]',
        className,
      )}
      {...props}
    />
  )
}
