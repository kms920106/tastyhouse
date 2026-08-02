import CircleCheckbox from '../ui/CircleCheckbox'

const formatCount = (count: number): string => {
  return count >= 99 ? '99+' : String(count)
}

interface Props {
  label: string
  count: number
  checked: boolean
  onChange: (value: boolean) => void
}

export default function CheckboxWithCount({ label, count, checked, onChange }: Props) {
  return (
    <label className="flex items-center gap-2 cursor-pointer">
      <CircleCheckbox checked={checked} onChange={onChange} />
      <span className="text-sm leading-[14px]">
        {label} ({formatCount(count)})
      </span>
    </label>
  )
}
