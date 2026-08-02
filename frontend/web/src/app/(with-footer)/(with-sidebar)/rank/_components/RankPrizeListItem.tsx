import Image from 'next/image'
import Icon from '@/components/ui/Icon'
import { getRankPodiumIconName, type RankTopNo } from '@/components/ui/icon-helpers'

interface Prize {
  id: number
  prizeRank: number
  name: string
  brand: string
  imageUrl: string
}

interface Props {
  prize: Prize
}

export default function RankPrizeListItem({ prize }: Props) {
  const { prizeRank, name, brand, imageUrl } = prize

  return (
    <div className="flex flex-col flex-1 items-center min-w-0">
      <div className="relative w-full max-w-[144px] mb-[15px] aspect-square">
        <div className="absolute top-0 left-0 z-10 w-[25%]">
          <Icon
            name={getRankPodiumIconName(prizeRank as RankTopNo)}
            className="w-full h-auto"
          />
        </div>
        <div className="flex items-center justify-center w-full h-full bg-white border border-line rounded-full">
          <Image src={imageUrl} alt={name} width={80} height={80} className="w-[55%] h-auto" />
        </div>
      </div>
      <div className="flex flex-col gap-1 w-full text-center">
        <p className="text-[11px] leading-[11px] truncate">{brand}</p>
        <p className="text-[11px] leading-[11px] truncate">{name}</p>
      </div>
    </div>
  )
}
