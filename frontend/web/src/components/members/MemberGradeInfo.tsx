import { getMemberGradeColor, getMemberGradeIcon, getMemberGradeName } from '@/domains/member'
import { MemberGradeCode } from '@/domains/member'
import { cn } from '@/lib/utils'
import Icon from '@/components/ui/Icon'
import { getMemberGradeIconNameBySize, type MemberGradeIconCode } from '@/components/ui/icon-helpers'

interface Props {
  memberGrade: MemberGradeCode | null | undefined
}

export default function MemberGradeInfo({ memberGrade }: Props) {
  const grade = memberGrade ?? 'NEWCOMER'
  const gradeName = getMemberGradeName(grade)
  const gradeIcon = getMemberGradeIcon(grade)
  const gradeColor = getMemberGradeColor(grade)

  return (
    <div className="flex items-center gap-1.5 mt-2">
      <div className="relative w-[14px] h-[14px]">
        <Icon
          name={getMemberGradeIconNameBySize(gradeIcon as MemberGradeIconCode, 40)}
          alt={gradeName}
          fill
          width={14}
          style={{ objectFit: 'contain' }}
        />
      </div>
      <span className={cn('text-sm leading-[14px] font-bold', gradeColor)}>{gradeName}</span>
    </div>
  )
}
