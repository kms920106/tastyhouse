import FormCheckbox from '@/components/ui/FormCheckbox'

interface Props {
  ownerOnly: boolean
}

// 사장님만보기는 등록 시에만 정할 수 있고 이후 전환이 불가능하다(backend.md §3-2·frontend.md §1-4).
// 체크박스는 현재 상태만 disabled로 보여주고, 수정 요청 본문에는 ownerOnly 필드 자체를 넣지 않는다.
export default function ReviewEditOwnerOnlySection({ ownerOnly }: Props) {
  return (
    <div className="flex flex-col gap-2 px-[15px] py-5">
      <div className="flex items-center gap-2.5">
        <FormCheckbox name="ownerOnly" checked={ownerOnly} onChange={() => {}} disabled />
        <span className="text-sm leading-[14px] text-[#999999]">사장님만보기</span>
      </div>
      <p className="text-xs leading-relaxed text-[#666666]">등록 후에는 변경할 수 없습니다.</p>
    </div>
  )
}
