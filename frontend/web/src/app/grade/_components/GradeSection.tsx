import Header, { HeaderCenter, HeaderLeft, HeaderTitle } from '@/components/layouts/Header'
import { BackButton } from '@/components/layouts/header-parts'
import BorderedSection from '@/components/ui/BorderedSection'
import MemberGradeBadge from '@/components/members/MemberGradeBadge'
import MemberGradeIcon from '@/components/members/MemberGradeIcon'
import MemberGradeName from '@/components/members/MemberGradeName'
import SectionStack from '@/components/ui/SectionStack'
import { gradeRepository } from '@/domains/grade/grade.repository'
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
import GradeInfoItem from './GradeInfoItem'

export default async function GradeSection() {
  const [memberResult, myGradeResult, gradeInfoListResult] = await Promise.all([
    memberService.getMyProfile(),
    memberRepository.getMyGrade(),
    gradeRepository.getGradeInfoList(),
  ])

  const member = memberResult.data
  const myGrade = myGradeResult.data
  const gradeInfoList = gradeInfoListResult.data ?? []

  return (
    <section className="min-h-screen">
      <Header variant="white" height={55} showBorder={false}>
        <HeaderLeft>
          <BackButton />
        </HeaderLeft>
        <HeaderCenter>
          <HeaderTitle>등급</HeaderTitle>
        </HeaderCenter>
      </Header>
      <SectionStack>
        <BorderedSection>
          <div className="px-[15px] pt-10 pb-5 text-center">
            <p className="text-base leading-[16px]">
              <span className="font-bold">{member?.nickname}</span> 님의 현재 등급은
            </p>
            {myGrade && (
              <div className="flex justify-center mt-[23px]">
                <Icon
                  name={getMemberGradeIconNameBySize(
                    getMemberGradeIcon(
                      myGrade.currentGrade as MemberGradeCode,
                    ) as MemberGradeIconCode,
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
                  리뷰 <span className="font-bold">{myGrade.reviewsNeededForNextGrade}</span> 개
                  추가 작성 시
                  <MemberGradeBadge
                    gradeIcon={<MemberGradeIcon grade={myGrade.nextGrade} size={14} />}
                    gradeName={<MemberGradeName grade={myGrade.nextGrade} size="sm" bold />}
                  />
                  달성
                </div>
                <p className="mt-2.5 text-xs leading-[12px] text-[#999999]">
                  현재 작성 리뷰 수{' '}
                  <span className="font-bold">{myGrade.currentReviewCount} 개</span>
                </p>
              </div>
            )}
          </div>
        </BorderedSection>
        <BorderedSection>
          <div className="px-[15px] py-[25px]">
            <h2 className="text-[17px] leading-[17px] font-bold text-gray-900">
              등급 세부 조건 안내
            </h2>
            <p className="mt-2.5 text-xs leading-[12px] text-[#999999]">
              리뷰 작성 수에 따라 등급이 변경됩니다.
            </p>

            <div className="flex flex-col gap-[13px] mt-[25px]">
              {gradeInfoList.map((gradeInfo) => (
                <GradeInfoItem key={gradeInfo.grade} gradeInfo={gradeInfo} />
              ))}
            </div>
          </div>
        </BorderedSection>
      </SectionStack>
    </section>
  )
}
