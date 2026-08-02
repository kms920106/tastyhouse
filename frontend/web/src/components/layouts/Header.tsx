'use client'

import { cn } from '@/lib/utils'
import { createContext } from 'react'

interface SlotProps {
  children: React.ReactNode
}

export function HeaderLeft({ children }: SlotProps) {
  return (
    <div className="absolute left-0 top-1/2 -translate-y-1/2 flex items-center justify-center">
      {children}
    </div>
  )
}

export function HeaderCenter({ children }: SlotProps) {
  return <div className="absolute left-1/2 -translate-x-1/2">{children}</div>
}

export function HeaderRight({ children }: SlotProps) {
  return (
    <div className="absolute right-0 top-1/2 -translate-y-1/2 flex items-center justify-center">
      {children}
    </div>
  )
}

interface TitleProps {
  children: React.ReactNode
  className?: string
}

export function HeaderTitle({ children, className }: TitleProps) {
  return <h1 className={cn('text-[17px] leading-[17px]', className)}>{children}</h1>
}

interface HeaderContextValue {
  variant: 'primary' | 'white'
}

const HeaderContext = createContext<HeaderContextValue | null>(null)

interface Props {
  children: React.ReactNode
  variant?: 'primary' | 'white'
  height: number
  showBorder?: boolean
}

export default function Header({
  children,
  variant = 'primary',
  height,
  showBorder = true,
}: Props) {
  const isPrimary = variant === 'primary'

  return (
    <HeaderContext.Provider value={{ variant }}>
      <header
        className={cn(
          'relative flex items-center',
          showBorder && 'border-b border-line box-border',
          isPrimary ? 'bg-main text-white' : 'bg-white text-black',
        )}
        style={{ height: `${height}px` }}
      >
        {children}
      </header>
    </HeaderContext.Provider>
  )
}
