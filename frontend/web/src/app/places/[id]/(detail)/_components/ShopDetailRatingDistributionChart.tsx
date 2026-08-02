'use client'

import { cn } from '@/lib/utils'
import * as ProgressPrimitive from '@radix-ui/react-progress'
import * as React from 'react'

// 평점 분포 바 차트 컴포넌트
export default function ShopDetailRatingDistributionChart({
  ratingCounts,
}: {
  ratingCounts: Record<string, number>
}) {
  const maxCount = Math.max(...[5, 4, 3, 2, 1].map((rating) => ratingCounts[String(rating)] || 0))
  const [progress, setProgress] = React.useState(0)

  React.useEffect(() => {
    const timer = setTimeout(() => setProgress(100), 500)
    return () => clearTimeout(timer)
  }, [])

  return (
    <div className="flex items-end justify-center gap-[13px]">
      {[5, 4, 3, 2, 1].map((rating) => {
        const count = ratingCounts[String(rating)] || 0
        const targetPercentage = maxCount > 0 ? (count / maxCount) * 100 : 0
        const currentPercentage = (targetPercentage * progress) / 100
        const hasData = count > 0

        return (
          <div key={rating} className="flex flex-col items-center gap-[13px]">
            <div className="w-[5px] h-[50px] rounded-full bg-line">
              <ProgressPrimitive.Root
                value={currentPercentage}
                className={cn('relative h-full w-full')}
              >
                <ProgressPrimitive.Indicator
                  className={cn(
                    'absolute bottom-0 left-0 right-0 transition-all rounded-[2px]',
                    hasData ? 'bg-main' : 'bg-line',
                  )}
                  style={{ height: `${currentPercentage}%` }}
                />
              </ProgressPrimitive.Root>
            </div>
            <span
              className={cn('text-xs leading-[12px]', hasData ? 'text-main' : 'text-[#aaaaaa]')}
            >
              {rating}점
            </span>
          </div>
        )
      })}
    </div>
  )
}
