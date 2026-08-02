import { getMemberGradeIcon, getMemberGradeName } from '@/domains/member'
import { MemberGradeCode } from '@/domains/member'
import Icon from '@/components/ui/Icon'
import { getMemberGradeIconName, type MemberGradeIconCode } from '@/components/ui/icon-helpers'

interface Props {
  grade: MemberGradeCode
  size?: number
}

export default function MemberGradeIcon({ grade, size = 14 }: Props) {
  const gradeIcon = getMemberGradeIcon(grade) as MemberGradeIconCode
  const gradeName = getMemberGradeName(grade)

  return (
    <div className="relative" style={{ width: size, height: size }}>
      <Icon
        name={getMemberGradeIconName(gradeIcon)}
        alt={gradeName}
        fill
        width={size}
        style={{ objectFit: 'contain' }}
      />
    </div>
  )
}
