import { fileRepository } from '@/domains/file/file.repository'
import { NextRequest, NextResponse } from 'next/server'

export async function POST(request: NextRequest) {
  const formData = await request.formData()
  const file = formData.get('file')

  if (!(file instanceof File)) {
    return NextResponse.json({ error: '파일이 없습니다.' }, { status: 400 })
  }

  const { error, status, data } = await fileRepository.uploadFile(file)

  if (error) {
    return NextResponse.json({ error }, { status: status || 500 })
  }

  return NextResponse.json({ data }, { status: 200 })
}
