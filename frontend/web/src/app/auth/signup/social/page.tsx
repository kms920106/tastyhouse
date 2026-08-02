import AuthSignupSocialPage from './_components/AuthSignupSocialPage'

interface Props {
  searchParams: Promise<{
    provider?: string
    tempToken?: string
  }>
}

export default async function Page({ searchParams }: Props) {
  const { provider, tempToken } = await searchParams

  return <AuthSignupSocialPage providerParam={provider} tempTokenParam={tempToken} />
}
