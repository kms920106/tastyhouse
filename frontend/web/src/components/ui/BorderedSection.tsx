import { ReactNode } from 'react'

interface Props {
  children: ReactNode
}

export default function BorderedSection({ children }: Props) {
  return (
    <section className="bg-white border-y border-line box-border">
      {children}
    </section>
  )
}
