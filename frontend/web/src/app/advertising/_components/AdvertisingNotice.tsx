export default function AdvertisingNotice() {
  return (
    <div className="px-[15px] py-[30px] flex flex-col gap-5">
      <p className="text-sm leading-[14px]">광고 및 제휴 상담신청 유의사항</p>
      <div className="flex flex-col gap-1">
        <p className="text-sm leading-relaxed text-[#666666]">
          • 먼저 신청하신 고객님의 상담이 지연될 경우 희망시간대보다 늦게 전화를 드릴 수 있습니다.
        </p>
        <p className="text-sm leading-relaxed text-[#666666]">
          • 등록하신 전화번호는 상담 관련 이외에는 사용되지 않으며, 기존에 등록하신 번호는 변경되지
          않습니다.
        </p>
      </div>
    </div>
  )
}
