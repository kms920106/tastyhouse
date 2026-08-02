import AppInput from './AppInput'

type Props = Omit<React.ComponentProps<typeof AppInput>, 'type'>

export default function AppInputPassword(props: Props) {
  return <AppInput type="password" {...props} />
}
