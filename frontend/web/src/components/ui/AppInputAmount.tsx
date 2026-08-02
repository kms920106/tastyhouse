import AppInputNumber from './AppInputNumber'

type Props = Omit<React.ComponentProps<typeof AppInputNumber>, 'onChange'> & {
  onChange?: (e: React.ChangeEvent<HTMLInputElement>) => void
}

function stripLeadingZeros(value: string): string {
  if (value === '' || value === '0') return value
  return value.replace(/^0+/, '') || '0'
}

export default function AppInputAmount({ onChange, ...props }: Props) {
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const sanitized = stripLeadingZeros(e.target.value)
    if (sanitized !== e.target.value) {
      e.target.value = sanitized
    }
    onChange?.(e)
  }

  return <AppInputNumber onChange={handleChange} {...props} />
}
