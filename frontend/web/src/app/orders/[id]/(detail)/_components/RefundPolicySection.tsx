export default function RefundPolicySection() {
  return (
    <div className="px-[15px] py-5 bg-white">
      <div className="space-y-5">
        <h3 className="text-base leading-[16px]">결제 취소 시 환불 규정 안내</h3>
        <p className="text-[13px] leading-[13px] text-[#666666]">
          결제 취소 시점은 예약 날짜를 기준으로 합니다.
        </p>
        <div className="space-y-2.5">
          <p className="text-[13px] leading-[13px] text-[#666666]">• 3일 전 취소: 전액 환불</p>
          <p className="text-[13px] leading-[13px] text-[#666666]">
            • 2일 전 취소: 결제 금액의 80% 환불
          </p>
          <p className="text-[13px] leading-[13px] text-[#666666]">
            • 1일 전 취소: 결제 금액의 50% 환불
          </p>
          <p className="text-[13px] leading-[13px] text-[#666666]">• 당일 취소: 환불 불가</p>
        </div>
      </div>
    </div>
  )
}
