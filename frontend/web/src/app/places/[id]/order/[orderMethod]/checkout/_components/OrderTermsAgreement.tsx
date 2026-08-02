import CircleCheckbox from '@/components/ui/CircleCheckbox'

interface Props {
  agreed: boolean
  onAgreementChange: (agreed: boolean) => void
}

export default function OrderTermsAgreement({
  agreed,
  onAgreementChange,
}: Props) {
  return (
    <div className="px-[15px] py-5">
      <label className="flex items-center gap-2.5 cursor-pointer">
        <CircleCheckbox checked={agreed} onChange={() => onAgreementChange(!agreed)} />
        <span className="text-sm leading-[21px]">
          위 주문의 상품 및 결제, 주문 정보 등을 확인하였으며, 이에 동의합니다. (필수)
        </span>
      </label>
    </div>
  )
}
