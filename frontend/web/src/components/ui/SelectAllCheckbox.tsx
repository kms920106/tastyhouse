import CircleCheckbox from './CircleCheckbox'

interface Props {
  label: string
  selectedCount: number
  totalCount: number
  checked: boolean
  onChange: (value: boolean) => void
}

export default function SelectAllCheckbox({
  label,
  selectedCount,
  totalCount,
  checked,
  onChange,
}: Props) {
  return (
    <div className="flex items-center gap-2">
      <CircleCheckbox checked={checked} onChange={onChange} />
      <button onClick={() => onChange(!checked)} className="text-sm leading-[14px]">
        {label} ({selectedCount}/{totalCount})
      </button>
    </div>
  )
}
