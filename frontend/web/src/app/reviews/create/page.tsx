'use client'

import PhotoUploader from '@/components/ui/PhotoUploader'
import ReviewTextarea from '@/components/reviews/ReviewTextarea'
import ReviewCreateSubmitButton from '@/app/reviews/create/_components/ReviewCreateSubmitButton'
import TagInput from '@/components/reviews/TagInput'
import { useCallback, useState } from 'react'
import { MdOutlineArrowBackIos } from 'react-icons/md'

export default function Page() {
  const [form, setForm] = useState({
    shopName: '',
    menuName: '',
    content: '',
    tags: [] as string[],
  })
  const [isUploading, setIsUploading] = useState(false)
  const [uploadedFileIds, setUploadedFileIds] = useState<number[]>([])
  console.log(uploadedFileIds)

  type FormType = typeof form
  const handleChange = <K extends keyof FormType>(key: K, value: FormType[K]) => {
    setForm((prev) => ({ ...prev, [key]: value }))
  }

  const handleUploadedFileIdsChange = useCallback((fileIds: number[]) => {
    setUploadedFileIds(fileIds)
  }, [])

  const handleUploadingChange = useCallback((uploading: boolean) => {
    setIsUploading(uploading)
  }, [])

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="relative flex items-center h-14 bg-white border-b border-line box-border">
        <button className="absolute top-1/2 -translate-y-1/2 w-14 h-14 flex items-center justify-center cursor-pointer">
          <MdOutlineArrowBackIos size={20} />
        </button>
        <p className="absolute left-1/2 -translate-x-1/2 text-[17px]">리뷰작성</p>
      </div>
      <div className="flex flex-col gap-2.5">
        <div className="flex flex-col gap-2.5 px-4 py-6 bg-white">
          <label className="text-xs text-gray-700" htmlFor="shopName">
            상호명 <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            className="w-full h-[50px] px-4 text-sm border border-border-input box-border focus:border-input-focus focus:ring-1 focus:ring-border-input-focus outline-none"
            id="shopName"
            onChange={(e) => handleChange('shopName', e.target.value)}
            autoComplete="off"
          ></input>
        </div>
        <div className="flex flex-col gap-2.5 px-4 py-6 bg-white">
          <label className="text-xs text-gray-700" htmlFor="menuName">
            메뉴명
          </label>
          <input
            type="text"
            className="w-full h-[50px] px-4 text-sm border border-border-input box-border focus:border-input-focus focus:ring-1 focus:ring-border-input-focus outline-none"
            id="menuName"
            onChange={(e) => handleChange('menuName', e.target.value)}
            autoComplete="off"
          ></input>
        </div>
        <ReviewTextarea value={form.content} onChange={(val) => handleChange('content', val)} />
        <PhotoUploader
          onUploadedFileIdsChange={handleUploadedFileIdsChange}
          onUploadingChange={handleUploadingChange}
        />
        <TagInput value={form.tags} onChange={(val) => handleChange('tags', val)} />
        <div className="px-4 py-6 bg-white">
          <p className="text-gray-900 mb-2">리뷰 작성 시 주의 사항</p>
          <ul className="list-disc list-inside text-gray-700 space-y-1">
            <li className="text-xs text-[#666666]">
              해당 메뉴와 무관한 사진을 첨부한 리뷰는 통보 없이 삭제 및 적립 혜택이 취소될 수
              있습니다.
            </li>
          </ul>
        </div>
        <ReviewCreateSubmitButton form={form} disabled={isUploading} />
      </div>
    </div>
  )
}
