import Icon from '@/components/ui/Icon'

interface Props {
  name: string
  checked: boolean
  onChange: (checked: boolean) => void
}

export default function FormCheckbox({ name, checked, onChange }: Props) {
  return (
    <label className="w-[20px] h-[20px] flex items-center justify-center cursor-pointer shrink-0">
      <input
        type="checkbox"
        name={name}
        checked={checked}
        onChange={(e) => onChange(e.target.checked)}
        className="sr-only"
      />
      <Icon name={checked ? 'check-on' : 'check-off'} />
    </label>
  )
}
