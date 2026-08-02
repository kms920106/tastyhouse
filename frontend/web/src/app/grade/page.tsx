import { getIsLoggedIn } from '@/lib/auth-config'
import GradePage from './_components/GradePage'

export default async function Page() {
  const isLoggedIn = await getIsLoggedIn()

  return <GradePage isLoggedIn={isLoggedIn} />
}
