import Icon from '@/components/ui/Icon'

interface Props {
  checked: boolean
  onChange: (value: boolean) => void
}

export default function CircleCheckbox({ checked, onChange }: Props) {
  return (
    <button
      type="button"
      onClick={() => onChange(!checked)}
      className="flex items-center justify-center shrink-0 w-[25px] h-[25px] cursor-pointer"
    >
      <Icon name={checked ? 'allcheck-on' : 'allcheck-off'} />
    </button>
  )
}
