import { Banner } from '@/domains/banner/banner.model'
import Image from 'next/image'
import Link from 'next/link'

interface Props {
  banner: Banner
  priority?: boolean
}

export default function HomeBannerListItem({ banner, priority = false }: Props) {
  if (banner.linkUrl) {
    return (
      <Link
        href={banner.linkUrl}
        rel="noopener noreferrer"
        className="relative block w-full h-full"
      >
        <Image
          src={banner.imageUrl}
          alt={banner.title}
          fill
          sizes="(max-width: 500px) 100vw, 500px"
          className="object-cover"
          priority={priority}
        />
      </Link>
    )
  }

  return (
    <div className="relative w-full h-full">
      <Image
        src={banner.imageUrl}
        alt={banner.title}
        fill
        sizes="(max-width: 500px) 100vw, 500px"
        className="object-cover"
        priority={priority}
      />
    </div>
  )
}
