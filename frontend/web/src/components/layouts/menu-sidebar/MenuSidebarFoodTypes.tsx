'use client'

import { useSidebar } from '@/components/ui/shadcn/sidebar'
import { useShopFoodTypes } from '@/domains/shop/shop.hook'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'
import { useState } from 'react'
import { TfiArrowCircleLeft, TfiArrowCircleRight } from 'react-icons/tfi'
import MenuGridButton from './MenuGridButton'
import MenuGridItem from './MenuGridItem'

const PAGE_SIZE = 6

export default function MenuSidebarFoodTypes() {
  const { setOpenMobile } = useSidebar()
  const { foodTypes } = useShopFoodTypes()
  const [categoryPage, setCategoryPage] = useState(0)

  const categoryTotalPages = Math.ceil(foodTypes.length / PAGE_SIZE)
  const visibleFoodTypes = foodTypes.slice(categoryPage * PAGE_SIZE, (categoryPage + 1) * PAGE_SIZE)

  const isPrevDisabled = categoryPage === 0
  const isNextDisabled = categoryPage >= categoryTotalPages - 1

  const handlePrev = () => {
    if (categoryPage > 0) setCategoryPage((p) => p - 1)
  }

  const handleNext = () => {
    if (categoryPage < categoryTotalPages - 1) setCategoryPage((p) => p + 1)
  }

  return (
    <div className="flex flex-col flex-1">
      <div className="grid grid-cols-2 gap-2.5 px-[15px] py-10">
        {/* 음식 버튼 */}
        {visibleFoodTypes.map((foodType) => (
          <Link
            key={foodType.code}
            href={`${PAGE_PATHS.PLACES}?foodTypes=${foodType.code}`}
            onClick={() => setOpenMobile(false)}
            className="flex flex-col items-center justify-center gap-[15px] h-24 bg-[#fdfdfd] border border-line box-border"
          >
            <MenuGridItem foodType={foodType} />
          </Link>
        ))}

        {/* 이전 버튼 */}
        <MenuGridButton onClick={handlePrev} disabled={isPrevDisabled}>
          <TfiArrowCircleLeft size={25} color="#333333" />
          <span className="text-[13px]">이전</span>
        </MenuGridButton>

        {/* 다음 버튼 */}
        <MenuGridButton onClick={handleNext} disabled={isNextDisabled}>
          <TfiArrowCircleRight size={25} color="#333333" />
          <span className="text-[13px]">다음</span>
        </MenuGridButton>
      </div>
    </div>
  )
}
