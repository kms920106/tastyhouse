import SelectAllCheckbox from '@/components/ui/SelectAllCheckbox'

interface Props {
  selectedCount: number
  totalCount: number
  allSelected: boolean
  onToggleSelectAll: () => void
  onDeleteSelected: () => void
}

export default function CartSelectionControl({
  selectedCount,
  totalCount,
  allSelected,
  onToggleSelectAll,
  onDeleteSelected,
}: Props) {
  return (
    <div className="flex items-center p-[15px]">
      <SelectAllCheckbox
        label="전체선택"
        selectedCount={selectedCount}
        totalCount={totalCount}
        checked={allSelected}
        onChange={onToggleSelectAll}
      />
      <button onClick={onDeleteSelected} className="ml-auto text-xs leading-[12px] text-[#999999]">
        삭제
      </button>
    </div>
  )
}
