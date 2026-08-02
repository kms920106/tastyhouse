interface ShareData {
  title?: string
  text?: string
  url: string
}

/**
 * 네이티브 공유 시트를 열어 콘텐츠를 공유합니다.
 * 모바일(iOS/Android)에서 네이티브 공유 UI가 표시됩니다.
 * Web Share API를 지원하지 않는 환경에서는 false를 반환합니다.
 */
export async function share(data: ShareData): Promise<boolean> {
  if (!navigator.share) {
    return false
  }

  try {
    await navigator.share(data)
    return true
  } catch (error) {
    // 사용자가 공유를 취소한 경우
    if ((error as Error).name === 'AbortError') {
      return true
    }
    console.error('공유 실패:', error)
    return false
  }
}

/**
 * 텍스트를 클립보드에 복사합니다.
 * Clipboard API를 지원하지 않는 환경에서는 false를 반환합니다.
 */
export async function copyToClipboard(text: string): Promise<boolean> {
  if (!navigator.clipboard) {
    return false
  }

  try {
    await navigator.clipboard.writeText(text)
    return true
  } catch {
    return false
  }
}

/**
 * Web Share API 지원 여부를 확인합니다.
 */
export function canShare(): boolean {
  return typeof navigator !== 'undefined' && !!navigator.share
}
