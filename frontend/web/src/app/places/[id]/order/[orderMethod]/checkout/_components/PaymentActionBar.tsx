import AppPrimaryButton from '@/components/ui/AppPrimaryButton'

interface Props {
  onPaymentClick: () => void
}

export default function PaymentActionBar({ onPaymentClick }: Props) {
  return (
    <div className="px-[15px] py-5">
      <AppPrimaryButton onClick={onPaymentClick}>
        결제하기
      </AppPrimaryButton>
    </div>
  )
}
