import Image from 'next/image'

interface Props {
  imageUrl: string
  priority?: boolean
}

export default function ReviewThumbnail({ imageUrl, priority }: Props) {
  return (
    <div className="relative aspect-square overflow-hidden bg-line">
      <Image
        src={imageUrl}
        alt="리뷰 이미지"
        fill
        sizes="33vw"
        className="object-cover"
        priority={priority}
      />
    </div>
  )
}
