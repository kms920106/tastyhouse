import { PAGE_PATHS } from '@/lib/paths'
import Icon from '@/components/ui/Icon'
import Link from 'next/link'

export default function QuickReviewLink() {
  return (
    <Link
      href={PAGE_PATHS.REVIEW_CREATE}
      className="absolute bottom-5 right-5 flex items-center justify-center w-[50px] h-[50px] bg-white rounded-full shadow-md hover:shadow-lg transition-all duration-200 active:scale-95 pointer-events-auto"
      aria-label="빠른 리뷰 작성하기"
    >
      <div className="relative w-[26px] h-[26px]">
        <Icon
          name="home/fast-review"
          alt=""
          fill
          width={26}
          style={{ objectFit: 'contain' }}
        />
      </div>
    </Link>
  )
}
