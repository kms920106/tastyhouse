import { cn } from '@/lib/utils'
import { CSSProperties, RefObject } from 'react'

interface Props {
  text: string
  className?: string
  style?: CSSProperties
  innerRef?: RefObject<HTMLParagraphElement | null>
}

export default function TextContent({ text, className = '', style, innerRef }: Props) {
  return (
    <p
      ref={innerRef}
      className={cn('text-sm leading-[23px] whitespace-pre-wrap break-words', className)}
      style={style}
    >
      {text}
    </p>
  )
}
