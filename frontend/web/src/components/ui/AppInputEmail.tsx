import AppInput from './AppInput'

type Props = Omit<React.ComponentProps<typeof AppInput>, 'type'>

export default function AppInputEmail(props: Props) {
  return <AppInput type="email" autoComplete="email" inputMode="email" {...props} />
}
