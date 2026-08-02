import Avatar from '@/components/ui/Avatar'
import MemberGradeBadge from '@/components/members/MemberGradeBadge'
import MemberGradeIcon from '@/components/members/MemberGradeIcon'
import MemberGradeName from '@/components/members/MemberGradeName'
import MemberNickname from '@/components/members/MemberNickname'
import { MemberGradeCode } from '@/domains/member'
import Icon from '@/components/ui/Icon'
import { getRankPodiumIconName, type RankTopNo } from '@/components/ui/icon-helpers'

interface Props {
  rankNo: number | null
  profileImageUrl: string | null
  nickname: string
  grade: MemberGradeCode
  reviewCount: number
  isMe?: boolean
}

export default function RankMemberInfo({
  rankNo,
  profileImageUrl,
  nickname,
  grade,
  reviewCount,
  isMe = false,
}: Props) {
  return (
    <div className="flex justify-between items-center w-full">
      <div className="flex items-center gap-2.5">
        {/* 순위 표시: 1~3위는 아이콘, 4위 이상은 숫자 */}
        <div className="relative flex flex-col items-center justify-center flex-shrink-0 w-[22px] h-[30px]">
          {rankNo !== null && rankNo <= 3 ? (
            <Icon
              name={getRankPodiumIconName(rankNo as RankTopNo)}
              fill
              width={22}
              style={{ objectFit: 'contain' }}
            />
          ) : (
            <p className="text-xs">{rankNo ?? '-'}</p>
          )}
        </div>

        {/* 프로필 이미지 */}
        <Avatar src={profileImageUrl} alt={nickname} />

        {/* 닉네임 및 등급 정보 */}
        <div className="flex flex-col gap-2 min-w-0">
          <div className="flex items-center gap-1">
            {isMe && (
              <p className="w-fit px-[7.5px] py-[3px] bg-main text-[9px] leading-[9px] font-bold text-white rounded-lg">
                나
              </p>
            )}
            <MemberNickname size="md">{nickname}</MemberNickname>
          </div>
          <MemberGradeBadge
            gradeIcon={<MemberGradeIcon grade={grade} size={14} />}
            gradeName={<MemberGradeName grade={grade} size="xs" />}
          />
        </div>
      </div>
      <div className="text-xs leading-[12px] text-[#666666]">{reviewCount}개</div>
    </div>
  )
}
