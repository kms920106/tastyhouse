import { cn } from '@/lib/utils'
import { Skeleton } from '@/components/ui/shadcn/skeleton'

interface Props {
  className?: string
}

export default function HashTagSkeleton({ className }: Props) {
  return <Skeleton className={cn('h-7 w-20 rounded-[15px]', className)} />
}
