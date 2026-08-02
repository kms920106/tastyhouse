import { getMemberGradeColor, getMemberGradeName } from '@/domains/member'
import { MemberGradeCode } from '@/domains/member'
import { cn } from '@/lib/utils'

export type MemberGradeNameSize = 'xs' | 'sm' | 'md' | 'lg'

const sizeClass: Record<MemberGradeNameSize, string> = {
  xs: 'text-xs leading-[12px]',
  sm: 'text-sm leading-[14px]',
  md: 'text-base leading-[16px]',
  lg: 'text-lg leading-[18px]',
}

interface Props {
  grade: MemberGradeCode
  size: MemberGradeNameSize
  bold?: boolean
}

export default function MemberGradeName({
  grade,
  size = 'xs',
  bold = false,
}: Props) {
  const gradeName = getMemberGradeName(grade)
  const gradeColor = getMemberGradeColor(grade)

  return <span className={cn(sizeClass[size], gradeColor, bold && 'font-bold')}>{gradeName}</span>
}
