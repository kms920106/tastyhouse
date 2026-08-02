import Icon from '@/components/ui/Icon'

interface Props {
  title: string
}

export default function SearchResultAllSectionHeader({ title }: Props) {
  return (
    <div className="flex items-center gap-[9px] mb-5">
      <h2 className="text-base leading-[16px] font-bold">{title}</h2>
      <Icon name="nav-right" width={8} height={14} />
    </div>
  )
}
