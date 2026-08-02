import PaymentFailContent from './_components/PaymentFailContent'

interface Props {
  searchParams: Promise<{
    code?: string
    message?: string
  }>
}

export default async function Page({ searchParams }: Props) {
  const { code, message } = await searchParams

  return <PaymentFailContent errorCode={code} errorMessage={message} />
}
