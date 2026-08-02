'use client'

import Link from 'next/link'
import { useEffect, useRef, useState } from 'react'
import { MoreButton } from './MoreButton'
import TextContent from './TextContent'

const estimateIsClamped = (text: string | undefined, maxLines: number) => {
  if (!text) return false
  const charsPerLine = 20
  return text.length > charsPerLine * maxLines
}

interface Props {
  text: string
  maxLines?: number
  className?: string
  href?: string
  MoreButton?: React.ReactElement<{ onClick: () => void }>
}

export default function ClampedText({
  text,
  maxLines = 5,
  className = '',
  href,
  MoreButton: customMoreButton,
}: Props) {
  const textRef = useRef<HTMLParagraphElement>(null)
  const [isClamped, setIsClamped] = useState(() => estimateIsClamped(text, maxLines))
  const [isExpanded, setIsExpanded] = useState(false)

  useEffect(() => {
    const element = textRef.current
    if (!element) return

    const checkClamped = () => {
      setIsClamped(element.scrollHeight > element.clientHeight)
    }

    checkClamped()

    const observer = new ResizeObserver(checkClamped)
    observer.observe(element)

    return () => observer.disconnect()
  }, [text])

  const lineHeight = 23
  const maxHeight = lineHeight * maxLines

  const textContent = (
    <TextContent
      innerRef={textRef}
      text={text}
      className={className}
      style={!isExpanded ? { maxHeight: `${maxHeight}px`, overflow: 'hidden' } : undefined}
    />
  )

  const moreButtonElement = customMoreButton || <MoreButton onClick={() => setIsExpanded(true)} />

  return (
    <div className="relative">
      {href ? <Link href={href}>{textContent}</Link> : textContent}
      {isClamped && !isExpanded && moreButtonElement}
    </div>
  )
}

export { MoreButton } from './MoreButton'
