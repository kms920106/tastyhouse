import ReviewTextarea from '@/components/reviews/ReviewTextarea'
import TagInput from '@/components/reviews/TagInput'
import AppFormField from '@/components/ui/AppFormField'

interface Props {
  content: string
  contentError?: string
  tags: string[]
  onContentChange: (value: string) => void
  onTagsChange: (tags: string[]) => void
}

export default function ReviewEditContentSection({
  content,
  contentError,
  tags,
  onContentChange,
  onTagsChange,
}: Props) {
  return (
    <div className="flex flex-col gap-5 px-[15px] py-[30px]">
      <AppFormField label="내용" required error={contentError}>
        {({ className }) => (
          <ReviewTextarea
            value={content}
            onChange={onContentChange}
            error={!!contentError}
            className={className}
          />
        )}
      </AppFormField>
      <AppFormField label="태그">
        {() => <TagInput value={tags} onChange={onTagsChange} />}
      </AppFormField>
    </div>
  )
}
