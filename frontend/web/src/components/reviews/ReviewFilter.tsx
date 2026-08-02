import CheckboxWithCount from '@/components/reviews/CheckboxWithCount'
import type { ReviewSortType } from '@/domains/review'
import { cn } from '@/lib/utils'
import Icon from '@/components/ui/Icon'

interface Props {
  count: number
  photoOnly: boolean
  onPhotoOnlyChange: (value: boolean) => void
  selectedRating: number | null
  onRatingChange: (rating: number | null) => void
  sortType: ReviewSortType
  onSortTypeChange: (sortType: ReviewSortType) => void
}

export default function ReviewFilter({
  count,
  photoOnly,
  onPhotoOnlyChange,
  selectedRating,
  onRatingChange,
  sortType,
  onSortTypeChange,
}: Props) {
  return (
    <div className="flex flex-col gap-[30px] pb-2.5 border-b border-line box-border">
      <div className="flex gap-2.5">
        <button
          onClick={() => onRatingChange(null)}
          className={cn(
            'px-[21px] py-[14px] bg-white text-sm leading-[14px] border-1 box-border rounded-[1px] cursor-pointer flex-shrink-0',
            selectedRating === null
              ? 'text-[#a11420] font-bold border-main'
              : 'text-[#aaaaaa] border-line',
          )}
        >
          전체
        </button>
        {[1, 2, 3, 4, 5].map((rating) => (
          <button
            key={rating}
            onClick={() => onRatingChange(rating)}
            className={cn(
              'px-[21px] py-[14px] bg-white text-sm leading-[14px] border-1 box-border rounded-[1px] cursor-pointer flex-shrink-0',
              selectedRating === rating
                ? 'text-[#a11420] font-bold border-main'
                : 'text-[#aaaaaa] border-line',
            )}
          >
            {rating}점
          </button>
        ))}
      </div>
      <div className="flex items-center justify-between">
        <CheckboxWithCount
          label="포토리뷰"
          count={count}
          checked={photoOnly}
          onChange={onPhotoOnlyChange}
        />
        <div className="flex items-center gap-1.5">
          <select
            name="review-sort"
            value={sortType}
            onChange={(e) => onSortTypeChange(e.target.value as ReviewSortType)}
            className="w-fit min-w-[30px] text-xs leading-[12px] text-right appearance-none cursor-pointer focus:outline-none"
          >
            <option value="recommended">추천순</option>
            <option value="latest">최신순</option>
            <option value="oldest">오래된순</option>
          </select>
          <Icon name="filter-nav" className="pointer-events-none" />
        </div>
      </div>
    </div>
  )
}
