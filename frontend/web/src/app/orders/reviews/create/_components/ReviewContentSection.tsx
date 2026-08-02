import PhotoUploader from '@/components/ui/PhotoUploader'
import ReviewTextarea from '@/components/reviews/ReviewTextarea'
import TagInput from '@/components/reviews/TagInput'
import AppFormField from '@/components/ui/AppFormField'

interface Props {
  content: string
  contentError?: string
  tags: string[]
  onContentChange: (value: string) => void
  onTagsChange: (tags: string[]) => void
  onUploadedFileIdsChange: (fileIds: number[]) => void
  onUploadingChange: (uploading: boolean) => void
}

export default function ReviewContentSection({
  content,
  contentError,
  tags,
  onContentChange,
  onTagsChange,
  onUploadedFileIdsChange,
  onUploadingChange,
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
      <div className="flex flex-col gap-5">
        <AppFormField label="사진">
          {() => (
            <PhotoUploader
              onUploadedFileIdsChange={onUploadedFileIdsChange}
              onUploadingChange={onUploadingChange}
            />
          )}
        </AppFormField>
        <div>
          <p className="text-sm leading-relaxed text-[#999999]">
            해당 음식과 무관한 사진을 첨부한 리뷰는 통보없이 삭제 및 적립 혜택이 취소될 수 있습니다.
          </p>
        </div>
      </div>
      <AppFormField label="태그">
        {() => <TagInput value={tags} onChange={onTagsChange} />}
      </AppFormField>
    </div>
  )
}
