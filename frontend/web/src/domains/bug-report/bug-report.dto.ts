export interface BugReportCreateRequest {
  device: string
  title: string
  content: string
  uploadedFileIds?: number[]
}

export interface BugReportResponse {
  id: number
  device: string
  title: string
  content: string
  imageUrls: string[]
  createdAt: string
}
