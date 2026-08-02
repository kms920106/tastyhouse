import { cn } from '@/lib/utils'
import Image from 'next/image'

const DEFAULT_PROFILE_IMAGE_SM = '/images/account/profile/profile-random-sm.png'
const DEFAULT_PROFILE_IMAGE_BG = '/images/account/profile/profile-random-bg.png'

interface Props {
  src?: string | null
  alt: string
  size?: 'sm' | 'md' | 'bg'
  className?: string
  priority?: boolean
}

const sizeMap = {
  sm: { dimension: 30, className: 'size-[30px]', defaultImage: DEFAULT_PROFILE_IMAGE_SM },
  md: { dimension: 40, className: 'size-10', defaultImage: DEFAULT_PROFILE_IMAGE_SM },
  bg: { dimension: 125, className: 'size-[125px]', defaultImage: DEFAULT_PROFILE_IMAGE_BG },
} as const

export default function Avatar({
  src,
  alt,
  size = 'md',
  className = '',
  priority = false,
}: Props) {
  const { dimension, className: sizeClassName, defaultImage } = sizeMap[size]
  return (
    <Image
      src={src ? src : defaultImage}
      alt={alt}
      width={dimension}
      height={dimension}
      className={cn('flex-shrink-0 rounded-full', sizeClassName, className)}
      priority={priority}
    />
  )
}
