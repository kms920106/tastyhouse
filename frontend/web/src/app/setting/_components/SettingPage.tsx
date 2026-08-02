import SettingHeader from './SettingHeader'
import SettingMenuList from './SettingMenuList'

interface Props {
  isLoggedIn: boolean
}

export default function SettingPage({ isLoggedIn }: Props) {
  return (
    <>
      <SettingHeader />
      <SettingMenuList isLoggedIn={isLoggedIn} />
    </>
  )
}
