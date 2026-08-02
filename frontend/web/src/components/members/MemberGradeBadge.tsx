import { ReactNode } from 'react'

interface Props {
  gradeIcon: ReactNode
  gradeName: ReactNode
}

export default function MemberGradeBadge({ gradeIcon, gradeName }: Props) {
  return (
    <div className="flex items-center gap-[5px]">
      {gradeIcon}
      {gradeName}
    </div>
  )
}
