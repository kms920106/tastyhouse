'use client'

import Icon from '@/components/ui/Icon'

interface Props {
  checked: boolean
  onChange: (checked: boolean) => void
}

export default function ToggleSwitch({ checked, onChange }: Props) {
  return (
    <button type="button" onClick={() => onChange(!checked)} className="shrink-0">
      <Icon name={checked ? 'toggle-on' : 'toggle-off'} />
    </button>
  )
}
