import { cn } from '@/lib/utils'

interface Props {
  rank: number
  keyword: string
  isNew: boolean
}

export default function SearchPopularKeywordListItem({ rank, keyword, isNew }: Props) {
  return (
    <div className="flex items-center gap-5">
      <span className={cn('w-5 text-sm font-normal text-right shrink-0', rank <= 3 && 'text-main')}>
        {rank}
      </span>
      <span className="flex-1 text-sm leading-[16px] font-regular">{keyword}</span>
      {isNew && <span className="text-sm leading-[16px] text-main">NEW</span>}
    </div>
  )
}
