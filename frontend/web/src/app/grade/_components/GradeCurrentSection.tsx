import FetchErrorState from '@/components/ui/FetchErrorState'
import MemberGradeBadge from '@/components/members/MemberGradeBadge'
import MemberGradeIcon from '@/components/members/MemberGradeIcon'
import MemberGradeName from '@/components/members/MemberGradeName'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import type { MemberGradeCode } from '@/domains/member'
import { getMemberGradeColor, getMemberGradeIcon } from '@/domains/member'
import { memberRepository } from '@/domains/member/member.repository'
import { memberService } from '@/domains/member/member.service'
import { cn } from '@/lib/utils'
import Icon from '@/components/ui/Icon'
import {
  getMemberGradeIconNameBySize,
  type MemberGradeIconCode,
} from '@/components/ui/icon-helpers'

export default async function GradeCurrentSection() {
  const [memberResult, myGradeResult] = await Promise.all([
    memberService.getMyProfile(),
    memberRepository.getMyGrade(),
  ])

  const member = memberResult.data

  if (memberResult.error || myGradeResult.error) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  const myGrade = myGradeResult.data

  return (
    <div className="px-[15px] pt-10 pb-5 text-center">
      <p className="text-base leading-[16px]">
        <span className="font-bold">{member?.nickname}</span> 님의 현재 등급은
      </p>
      {myGrade && (
        <div className="flex justify-center mt-[23px]">
          <Icon
            name={getMemberGradeIconNameBySize(
              getMemberGradeIcon(myGrade.currentGrade as MemberGradeCode) as MemberGradeIconCode,
              120,
            )}
            alt={myGrade.currentGradeDisplayName}
            width={73}
            height={75}
          />
        </div>
      )}
      {myGrade && (
        <p
          className={cn(
            'mt-5 text-[23px] leading-[23px] font-bold',
            getMemberGradeColor(myGrade.currentGrade as MemberGradeCode),
          )}
        >
          {myGrade.currentGradeDisplayName}
        </p>
      )}
      {myGrade && myGrade.nextGrade && (
        <div className="mt-[30px] py-5 bg-[#fcfcfc] border border-line box-border">
          <div className="flex items-center justify-center flex-wrap gap-x-0.5 text-sm leading-[14px]">
            리뷰 <span className="font-bold">{myGrade.reviewsNeededForNextGrade}</span> 개 추가 작성
            시
            <MemberGradeBadge
              gradeIcon={<MemberGradeIcon grade={myGrade.nextGrade} size={14} />}
              gradeName={<MemberGradeName grade={myGrade.nextGrade} size="sm" bold />}
            />
            달성
          </div>
          <p className="mt-2.5 text-xs leading-[12px] text-[#999999]">
            현재 작성 리뷰 수 <span className="font-bold">{myGrade.currentReviewCount} 개</span>
          </p>
        </div>
      )}
    </div>
  )
}
