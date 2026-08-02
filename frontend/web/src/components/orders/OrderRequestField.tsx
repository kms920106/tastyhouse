'use client'

import AppFormField from '@/components/ui/AppFormField'
import AppTextarea from '@/components/ui/AppTextarea'
import { cn } from '@/lib/utils'

interface Props {
  value: string
  onChange: (value: string) => void
  maxLength?: number
}

export default function OrderRequestField({ value, onChange, maxLength }: Props) {
  return (
    <div className="px-[15px] py-6">
      <AppFormField label="요청사항">
        {({ className }) => (
          <AppTextarea
            value={value}
            onChange={(e) => onChange(e.target.value)}
            placeholder="요청사항을 입력해 주세요."
            maxLength={maxLength}
            className={cn('h-32', className)}
          />
        )}
      </AppFormField>
    </div>
  )
}
