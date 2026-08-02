import HashTag from '@/components/ui/HashTag'

interface Props {
  tagNames: string[]
}

export default function ReviewTagList({ tagNames }: Props) {
  return (
    tagNames &&
    tagNames.length > 0 && (
      <div className="flex flex-wrap gap-[7px]">
        {tagNames.map((tag: string, index: number) => (
          <HashTag key={index} tag={tag} variant="secondary" />
        ))}
      </div>
    )
  )
}
