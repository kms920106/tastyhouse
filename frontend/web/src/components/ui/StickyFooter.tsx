import { ReactNode } from 'react'

interface Props {
  children: ReactNode
}

export default function StickyFooter({ children }: Props) {
  return (
    <>
      <div className="h-[70px]" aria-hidden="true" />
      <div className="fixed bottom-0 left-0 right-0 bg-white border-t border-line box-border z-1">
        {children}
      </div>
    </>
  )
}
