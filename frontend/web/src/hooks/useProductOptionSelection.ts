'use client'

import type { ProductOptionGroup } from '@/domains/product'
import type { CartSelectedOption } from '@/lib/cart'
import { useCallback, useState } from 'react'

export function useProductOptionSelection(optionGroups: ProductOptionGroup[]) {
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

  return { options, handleRadioSelect, handleCheckboxToggle, getOptionsData }
}
