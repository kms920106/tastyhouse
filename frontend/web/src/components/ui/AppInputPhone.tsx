import { useRef } from 'react'
import { cn } from '@/lib/utils'
import AppInput from './AppInput'

type Props = Omit<
  React.ComponentProps<typeof AppInput>,
  'type' | 'inputMode' | 'onInput' | 'onPaste'
>

export default function AppInputPhone({ onChange, ...props }: Props) {
  const ref = useRef<HTMLInputElement>(null)

  const handleInput = (e: React.FormEvent<HTMLInputElement>) => {
    const input = e.currentTarget
    const digitsOnly = input.value.replace(/\D/g, '')
    if (input.value !== digitsOnly) {
      input.value = digitsOnly
    }
    onChange?.({ ...e, target: input, currentTarget: input } as React.ChangeEvent<HTMLInputElement>)
  }

  const handlePaste = (e: React.ClipboardEvent<HTMLInputElement>) => {
    const pastedText = e.clipboardData.getData('text')
    if (!/^\d+$/.test(pastedText)) {
      e.preventDefault()
    }
  }

  return (
    <AppInput
      ref={ref}
      type="tel"
      inputMode="numeric"
      onInput={handleInput}
      onPaste={handlePaste}
      {...props}
      onChange={undefined}
      className={cn(props.className)}
    />
  )
}
