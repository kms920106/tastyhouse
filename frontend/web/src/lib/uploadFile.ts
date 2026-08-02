import { ApiResponse } from './api'

export async function uploadFileClient(file: File): Promise<ApiResponse<number>> {
  const formData = new FormData()
  formData.append('file', file)

  const response = await fetch('/api/files/upload', {
    method: 'POST',
    body: formData,
  })

  const json = await response.json().catch(() => null)

  if (!response.ok) {
    return { error: json?.error || '파일 업로드에 실패했습니다.', status: response.status }
  }

  return { data: json?.data as number, status: response.status }
}
