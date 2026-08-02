'use client'

import AppInputText from '@/components/ui/AppInputText'
import Icon from '@/components/ui/Icon'

interface Props {
  value: string
  onChange: (value: string) => void
}

export default function SearchInput({ value, onChange }: Props) {
  return (
    <div className="relative flex items-center shrink-0">
      <AppInputText
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className="!bg-[#fafafa] rounded-[2.5px] border-line pr-[40px]"
      />
      <button className="absolute right-[17px] top-1/2 -translate-y-1/2">
        <Icon name="search" />
      </button>
    </div>
  )
}
