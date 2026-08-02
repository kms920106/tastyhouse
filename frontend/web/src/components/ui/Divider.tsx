import { cn } from '@/lib/utils'

interface Props {
  className?: string
}

/** 리스트 아이템 사이·섹션을 구분하는 장식용 수평 구분선 */
export default function Divider({ className }: Props) {
  return <div aria-hidden="true" className={cn('border-t border-line my-[15px]', className)} />
}
