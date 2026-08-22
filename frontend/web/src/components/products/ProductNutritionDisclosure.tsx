'use client'

import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from '@/components/ui/shadcn/accordion'
import { useProductNutrition } from '@/domains/product/product.hook'
import type { ProductNutrition } from '@/domains/product/product.model'
import { useState } from 'react'

interface Props {
  productId: number
}

/** 세트 메뉴일 때 수치보다 먼저 보여주는 안내문구 */
const SET_MENU_NOTICE =
  '메뉴구성에 따라 영양성분이 다르므로, 각각의 메뉴에 대한 영양성분을 확인해주시기 바랍니다.'

const ITEM_VALUE = 'product-nutrition'

/** 표시 순서와 단위. 필수 5종이 먼저 오고 선택 항목이 뒤따른다 */
const NUMERIC_ROWS: { key: keyof ProductNutrition; label: string; unit: string }[] = [
  { key: 'calorie', label: '열량', unit: 'kcal' },
  { key: 'sugars', label: '당류', unit: 'g' },
  { key: 'protein', label: '단백질', unit: 'g' },
  { key: 'saturatedFat', label: '포화지방', unit: 'g' },
  { key: 'natrium', label: '나트륨', unit: 'mg' },
  { key: 'carbohydrate', label: '탄수화물', unit: 'g' },
  { key: 'cholesterol', label: '콜레스테롤', unit: 'mg' },
  { key: 'fat', label: '지방', unit: 'g' },
  { key: 'transFat', label: '트랜스지방', unit: 'g' },
  { key: 'caffeine', label: '카페인', unit: 'mg' },
]

const TEXT_ROWS: { key: keyof ProductNutrition; label: string }[] = [
  { key: 'flavor', label: '맛' },
  { key: 'size', label: '사이즈' },
  { key: 'totalAmount', label: '총 제공량' },
  { key: 'servingSize', label: '1회 제공량' },
]

/**
 * 접이식 영양성분·알레르기 표시.
 *
 * **기본은 접힌 상태이고, 펼칠 때 조회한다** — 대부분의 메뉴가 미입력이라 목록 진입마다
 * 부르면 낭비다. 노출 여부 판정은 부모(`ProductNutritionSection`)가 서버에서 끝낸 상태다.
 */
export default function ProductNutritionDisclosure({ productId }: Props) {
  const [opened, setOpened] = useState(false)
  const { data, isLoading } = useProductNutrition(productId, opened)

  const nutrition = data?.data ?? null

  return (
    <Accordion
      type="single"
      collapsible
      onValueChange={(value) => {
        // 한 번 열면 계속 활성화해 둔다 — 접었다 펼 때마다 다시 부르지 않는다.
        if (value === ITEM_VALUE) setOpened(true)
      }}
    >
      <AccordionItem value={ITEM_VALUE} className="border-b-0">
        <AccordionTrigger className="items-center px-[15px] py-5 hover:no-underline">
          <h2 className="text-base leading-[16px]">영양성분 및 알레르기성분 표시 보기</h2>
        </AccordionTrigger>
        <AccordionContent className="p-0">
          <div className="px-[15px] py-2.5 pb-5">
            {isLoading || !nutrition ? (
              <p className="text-sm leading-[14px] text-[#666666]">불러오는 중입니다.</p>
            ) : (
              <div className="space-y-[15px]">
                {nutrition.setMenu && (
                  <p className="text-sm leading-relaxed text-[#666666]">{SET_MENU_NOTICE}</p>
                )}

                {TEXT_ROWS.map(({ key, label }) => {
                  const value = nutrition[key]
                  if (typeof value !== 'string' || value === '') return null
                  return (
                    <div key={key} className="flex">
                      <span className="w-30 text-sm leading-[14px] text-[#666666]">{label}</span>
                      <span className="text-sm leading-[14px]">{value}</span>
                    </div>
                  )
                })}

                {NUMERIC_ROWS.map(({ key, label, unit }) => {
                  const value = nutrition[key]
                  if (typeof value !== 'number') return null
                  return (
                    <div key={key} className="flex">
                      <span className="w-30 text-sm leading-[14px] text-[#666666]">{label}</span>
                      <span className="text-sm leading-[14px]">
                        {value}
                        {unit}
                      </span>
                    </div>
                  )
                })}

                {nutrition.allergens.length > 0 && (
                  <div className="flex">
                    <span className="w-30 text-sm leading-[14px] text-[#666666]">
                      알레르기 유발성분
                    </span>
                    {/* 서버가 한글 라벨로 주므로 화면이 코드→라벨 매핑을 갖지 않는다 */}
                    <span className="flex-1 text-sm leading-relaxed">
                      {nutrition.allergens.join(', ')}
                    </span>
                  </div>
                )}
              </div>
            )}
          </div>
        </AccordionContent>
      </AccordionItem>
    </Accordion>
  )
}
