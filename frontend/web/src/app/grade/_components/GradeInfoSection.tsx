import FetchErrorState from '@/components/ui/FetchErrorState'
import { gradeRepository } from '@/domains/grade/grade.repository'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import GradeInfoItem from './GradeInfoItem'

export default async function GradeInfoSection() {
  const { error, status, data } = await gradeRepository.getGradeInfoList()

  if ((error && status === 404) || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('등급 세부 조건')} />
  }

  if (error) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  return (
    <div className="px-[15px] py-[25px]">
      <h2 className="text-[17px] leading-[17px] font-bold text-gray-900">등급 세부 조건 안내</h2>
      <p className="mt-2.5 text-xs leading-[12px] text-[#999999]">
        리뷰 작성 수에 따라 등급이 변경됩니다.
      </p>
      <div className="flex flex-col gap-[13px] mt-[25px]">
        {data.map((gradeInfo) => (
          <GradeInfoItem key={gradeInfo.grade} gradeInfo={gradeInfo} />
        ))}
      </div>
    </div>
  )
}
