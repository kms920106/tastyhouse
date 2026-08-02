import { cn } from '@/lib/utils'
import Image from 'next/image'

interface Props {
  src: string | null | undefined
  alt: string
  size: 50 | 60 | 65 | 75
  rounded?: 'none' | '1px' | '2.5px'
  className?: string
}

export default function ImageContainer({
  src,
  alt,
  size = 75,
  rounded = 'none',
  className,
}: Props) {
  const sizeClass =
    size === 50
      ? 'w-[50px] h-[50px]'
      : size === 60
        ? 'w-[60px] h-[60px]'
        : size === 65
          ? 'w-[65px] h-[65px]'
          : 'w-[75px] h-[75px]'
  const sizeValue = `${size}px`

  return (
    <div
      className={cn(
        'relative flex-shrink-0 overflow-hidden',
        sizeClass,
        className,
        rounded === '2.5px' && 'rounded-[2.5px]',
      )}
    >
      {src && <Image src={src} alt={alt} fill className="object-cover" sizes={sizeValue} />}
    </div>
  )
}
