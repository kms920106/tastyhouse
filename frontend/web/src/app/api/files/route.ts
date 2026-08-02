import { readFile, stat } from 'fs/promises'
import { env } from '@/lib/env'
import { NextRequest, NextResponse } from 'next/server'
import path from 'path'

const MIME_TYPES: Record<string, string> = {
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.png': 'image/png',
  '.gif': 'image/gif',
  '.webp': 'image/webp',
  '.svg': 'image/svg+xml',
  '.avif': 'image/avif',
}

export async function GET(request: NextRequest) {
  const filePath = request.nextUrl.searchParams.get('path')

  if (!filePath) {
    return NextResponse.json({ error: 'path is required' }, { status: 400 })
  }

  try {
    // 업로드 디렉토리의 절대 경로 구성
    const uploadDir = env.UPLOAD_DIR ?? path.join(process.cwd(), '..', 'upload')
    const absolutePath = path.join(uploadDir, filePath)

    // 경로 탐색 공격 방지
    if (!absolutePath.startsWith(uploadDir)) {
      return NextResponse.json({ error: 'Invalid path' }, { status: 400 })
    }

    const fileStat = await stat(absolutePath)
    if (!fileStat.isFile()) {
      return NextResponse.json({ error: 'Not a file' }, { status: 400 })
    }

    const ext = path.extname(absolutePath).toLowerCase()
    const contentType = MIME_TYPES[ext]
    if (!contentType) {
      return NextResponse.json({ error: 'Unsupported file type' }, { status: 400 })
    }

    const fileBuffer = await readFile(absolutePath)

    return new NextResponse(new Uint8Array(fileBuffer), {
      headers: {
        'Content-Type': contentType,
        'Cache-Control': 'public, max-age=31536000, immutable',
      },
    })
  } catch {
    return NextResponse.json({ error: 'File not found' }, { status: 404 })
  }
}
