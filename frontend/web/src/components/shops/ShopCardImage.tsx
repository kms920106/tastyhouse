import { cn } from '@/lib/utils'
import Image from 'next/image'

interface Props {
  src: string
  alt: string
  className?: string
}

export function ShopCardImage({ src, alt, className }: Props) {
  return (
    <div className={cn('relative mb-[15px] aspect-square overflow-hidden', className)}>
      <Image
        src={src}
        alt={alt}
        fill
        sizes="(max-width: 768px) 50vw, 33vw"
        className="object-cover transition-transform duration-300"
      />
    </div>
  )
}
