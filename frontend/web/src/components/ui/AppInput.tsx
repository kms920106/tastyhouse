import { cn } from '@/lib/utils'
import { Input } from './shadcn/input'

type Props = React.ComponentProps<'input'>

export default function AppInput({ className, ...props }: Props) {
  return (
    <Input
      className={cn(
        'w-full h-[50px] pl-[16px] pr-[40px] bg-white py-5 text-sm leading-[14px] border border-line box-border rounded-none shadow-none focus-visible:ring-0 focus-visible:border-[#666666] placeholder:text-[#aaaaaa] disabled:border-line disabled:text-[#aaaaaa] disabled:cursor-not-allowed disabled:opacity-100 disabled:bg-[#f9f9f9] read-only:bg-[#f8f8f8] read-only:text-[#aaaaaa] read-only:cursor-not-allowed read-only:focus-visible:border-line',
        className,
      )}
      {...props}
    />
  )
}
