'use client'

import type { ProductOptionGroup } from '@/domains/product'
import type { CartSelectedOption } from '@/lib/cart'
import { useCallback, useMemo, useState } from 'react'

export function useProductOptionSelection(optionGroups: ProductOptionGroup[]) {
  // 일회용컵 보증금 옵션그룹은 추가금과 성격이 달라 화면에서 별도 섹션으로 분리해 보여준다.
  const normalOptionGroups = useMemo(
    () => optionGroups.filter((group) => group.groupType !== 'CUP_DEPOSIT'),
    [optionGroups],
  )
  const cupDepositOptionGroups = useMemo(
    () => optionGroups.filter((group) => group.groupType === 'CUP_DEPOSIT'),
    [optionGroups],
  )

  const [options, setOptions] = useState<Record<number, number | number[]>>(() => {
    const initial: Record<number, number | number[]> = {}
    optionGroups.forEach((group) => {
      if (group.multipleSelect) {
        initial[group.id] = []
      } else {
        const firstOption = group.options.find((opt) => !opt.soldOut)
        initial[group.id] = firstOption?.id ?? -1
      }
    })
    return initial
  })

  const handleRadioSelect = useCallback((groupId: number, optionId: number) => {
    setOptions((prev) => ({ ...prev, [groupId]: optionId }))
  }, [])

  const handleCheckboxToggle = useCallback(
    (groupId: number, optionId: number, maxSelect: number) => {
      setOptions((prev) => {
        const current = prev[groupId] as number[]
        if (current.includes(optionId)) {
          return { ...prev, [groupId]: current.filter((id) => id !== optionId) }
        }
        if (current.length >= maxSelect) return prev
        return { ...prev, [groupId]: [...current, optionId] }
      })
    },
    [],
  )

  const getOptionsData = useCallback((): CartSelectedOption[] => {
    const result: CartSelectedOption[] = []
    optionGroups.forEach((group) => {
      const selected = options[group.id]
      if (group.multipleSelect) {
        const selectedIds = selected as number[]
        selectedIds.forEach((optionId) => {
          result.push({ groupId: group.id, optionId })
        })
      } else {
        const optionId = selected as number
        if (optionId !== -1) result.push({ groupId: group.id, optionId })
      }
    })
    return result
  }, [optionGroups, options])

  return {
    options,
    handleRadioSelect,
    handleCheckboxToggle,
    getOptionsData,
    normalOptionGroups,
    cupDepositOptionGroups,
  }
}
