import BorderedSection from '@/components/ui/BorderedSection'
import type { ProductOptionGroup } from '@/domains/product'
import CupDepositPolicyLink from './CupDepositPolicyLink'
import ShopOrderMenuDetailOptionItem from './ShopOrderMenuDetailOptionItem'

interface Props {
  normalOptionGroups: ProductOptionGroup[]
  cupDepositOptionGroups: ProductOptionGroup[]
  options: Record<number, number | number[]>
  onRadioSelect: (groupId: number, optionId: number) => void
  onCheckboxToggle: (groupId: number, optionId: number, maxSelect: number) => void
}

export default function ShopOrderMenuDetailOptionList({
  normalOptionGroups,
  cupDepositOptionGroups,
  options,
  onRadioSelect,
  onCheckboxToggle,
}: Props) {
  const renderOptionGroup = (group: ProductOptionGroup) => (
    <BorderedSection key={group.id}>
      <div className="px-4 py-5">
        <div className="flex items-center justify-between">
          <h3 className="text-base leading-[16px] font-bold">
            {group.name}
            {group.required && <span className="text-main ml-1">*</span>}
          </h3>
          {group.groupType === 'CUP_DEPOSIT' && <CupDepositPolicyLink />}
        </div>
        <div className="flex flex-col gap-[15px] mt-5">
          {group.options.map((option) => (
            <ShopOrderMenuDetailOptionItem
              key={option.id}
              option={option}
              isMultiple={group.multipleSelect}
              isSelected={
                group.multipleSelect
                  ? (options[group.id] as number[]).includes(option.id)
                  : options[group.id] === option.id
              }
              onSelect={() =>
                group.multipleSelect
                  ? onCheckboxToggle(group.id, option.id, group.maxSelect)
                  : onRadioSelect(group.id, option.id)
              }
            />
          ))}
        </div>
      </div>
    </BorderedSection>
  )

  return (
    <>
      {normalOptionGroups.map(renderOptionGroup)}
      {/* 일회용컵 보증금 옵션그룹은 추가금과 성격이 달라 일반 옵션그룹과 별도 섹션으로 분리한다 */}
      {cupDepositOptionGroups.map(renderOptionGroup)}
    </>
  )
}
