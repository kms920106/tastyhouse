import { toast } from '@/components/ui/AppToaster'
import { uploadFileClient } from '@/lib/uploadFile'
import { useState, useTransition } from 'react'

interface UploadedFile {
  previewUrl: string
  fileId: number
}

interface UseFileUploadOptions {
  maxCount?: number
  allowedTypes?: string[]
  maxFileSize?: number
  errorMessages?: {
    maxCount?: string
    upload?: string
    fileType?: string
    fileSize?: string
  }
}

export default function useFileUpload({
  maxCount = Infinity,
  allowedTypes,
  maxFileSize,
  errorMessages = {},
}: UseFileUploadOptions = {}) {
  const [files, setFiles] = useState<File[]>([])
  const [uploadedFiles, setUploadedFiles] = useState<UploadedFile[]>([])
  const [isUploading, startUploading] = useTransition()

  const {
    maxCount: maxCountMessage = `사진은 최대 ${maxCount}장까지 업로드할 수 있습니다.`,
    upload: uploadErrorMessage = '사진 업로드 중 오류가 발생했습니다.',
    fileType: fileTypeErrorMessage = '허용되지 않는 파일 형식입니다.',
    fileSize: fileSizeErrorMessage = maxFileSize
      ? `파일 크기는 최대 ${maxFileSize / (1024 * 1024)}MB까지 가능합니다.`
      : '파일 크기가 너무 큽니다.',
  } = errorMessages

  const validateFile = (file: File): boolean => {
    if (allowedTypes && !allowedTypes.includes(file.type)) {
      toast(fileTypeErrorMessage)
      return false
    }
    if (maxFileSize && file.size > maxFileSize) {
      toast(fileSizeErrorMessage)
      return false
    }
    return true
  }

  const uploadNewFiles = (newFiles: File[]) => {
    startUploading(async () => {
      try {
        const results = await Promise.all(
          newFiles.map(async (file) => {
            const previewUrl = URL.createObjectURL(file)
            const response = await uploadFileClient(file)

            if (!response?.data) {
              throw new Error('파일 업로드에 실패했습니다.')
            }

            return { previewUrl, fileId: response.data }
          }),
        )

        setUploadedFiles((prev) => [...prev, ...results])
      } catch (error) {
        console.error('파일 업로드 실패:', error)
        toast(uploadErrorMessage)
        setFiles((prev) => prev.slice(0, prev.length - newFiles.length))
      }
    })
  }

  // PhotoUploader처럼 File[] 전체를 받는 방식 (삭제 포함)
  const handleFilesChange = (nextFiles: File[]) => {
    const isDeleted = nextFiles.length < files.length

    if (isDeleted) {
      const removedIndex = files.findIndex((f) => !nextFiles.includes(f))
      if (removedIndex !== -1) {
        setFiles((prev) => prev.filter((_, i) => i !== removedIndex))
        setUploadedFiles((prev) => prev.filter((_, i) => i !== removedIndex))
      }
      return
    }

    const newFiles = nextFiles.filter((f) => !files.includes(f))
    if (!newFiles.length) return

    if (files.length + newFiles.length > maxCount) {
      toast(maxCountMessage)
      return
    }

    const validFiles = newFiles.filter(validateFile)
    if (!validFiles.length) return

    setFiles([...files, ...validFiles])
    uploadNewFiles(validFiles)
  }

  // input[type=file] onChange 방식 (추가만)
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newFiles = Array.from(e.target.files || [])
    if (!newFiles.length) return

    const validFiles = newFiles.filter(validateFile)
    if (!validFiles.length) {
      e.target.value = ''
      return
    }

    if (uploadedFiles.length + validFiles.length > maxCount) {
      toast(maxCountMessage)
      e.target.value = ''
      return
    }

    e.target.value = ''

    setFiles((prev) => [...prev, ...validFiles])
    uploadNewFiles(validFiles)
  }

  const removeAt = (index: number) => {
    setFiles((prev) => prev.filter((_, i) => i !== index))
    setUploadedFiles((prev) => prev.filter((_, i) => i !== index))
  }

  const reset = () => {
    setFiles([])
    setUploadedFiles([])
  }

  return {
    files,
    uploadedFiles,
    isUploading,
    handleFilesChange,
    handleInputChange,
    removeAt,
    reset,
  }
}
