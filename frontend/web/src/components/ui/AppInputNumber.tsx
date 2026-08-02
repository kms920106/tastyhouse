import { cn } from '@/lib/utils'
import AppInput from './AppInput'

type Props = Omit<
  React.ComponentProps<typeof AppInput>,
  'type' | 'onKeyDown' | 'onPaste'
>

export default function AppInputNumber(props: Props) {
  const handlePaste = (e: React.ClipboardEvent<HTMLInputElement>) => {
    const pastedText = e.clipboardData.getData('text')
    if (!/^\d+$/.test(pastedText)) {
      e.preventDefault()
    }
  }

  return (
    <AppInput
      type="number"
      inputMode="numeric"
      onPaste={handlePaste}
      {...props}
      className={cn(
        '[appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none',
        props.className,
      )}
    />
  )
}
