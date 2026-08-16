import Icon from '@/components/ui/Icon'
import { cn } from '@/lib/utils'

interface Props {
  name: string
  checked: boolean
  onChange: (checked: boolean) => void
  disabled?: boolean
}

export default function FormCheckbox({ name, checked, onChange, disabled = false }: Props) {
  return (
    <label
      className={cn(
        'w-[20px] h-[20px] flex items-center justify-center shrink-0',
        disabled ? 'cursor-not-allowed' : 'cursor-pointer',
      )}
    >
      <input
        type="checkbox"
        name={name}
        checked={checked}
        onChange={(e) => onChange(e.target.checked)}
        disabled={disabled}
        className="sr-only"
      />
      <Icon name={checked ? 'check-on' : 'check-off'} />
    </label>
  )
}
