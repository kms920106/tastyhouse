import { env } from '@/lib/env'

const KAKAO_LOCAL_ADDRESS_SEARCH_URL = 'https://dapi.kakao.com/v2/local/search/address.json'

export interface Coordinates {
  latitude: number
  longitude: number
}

interface KakaoAddressSearchResponse {
  documents: { x: string; y: string }[]
}

/**
 * 주소 문자열을 카카오 로컬 API로 좌표(위·경도)로 변환합니다.
 *
 * `react-daum-postcode`는 주소만 주고 좌표를 주지 않으므로, 배달 주소를 저장하기 전에 이 함수로
 * 좌표를 확보해야 합니다. **변환에 실패하면 주소를 저장하지 말고 재선택을 요청해야 합니다** —
 * 좌표 없는 주소는 거리별 배달팁이 계산되지 않아 무료 배달이 되는 취약점이 됩니다.
 *
 * @param address - 도로명 또는 지번 주소
 * @returns 변환된 좌표. 변환할 수 없으면 null
 */
export async function convertAddressToCoordinates(address: string): Promise<Coordinates | null> {
  const trimmedAddress = address.trim()
  if (!trimmedAddress) {
    return null
  }

  try {
    const url = `${KAKAO_LOCAL_ADDRESS_SEARCH_URL}?query=${encodeURIComponent(trimmedAddress)}`
    const response = await fetch(url, {
      headers: { Authorization: `KakaoAK ${env.NEXT_PUBLIC_KAKAO_REST_API_KEY}` },
    })

    if (!response.ok) {
      return null
    }

    const { documents }: KakaoAddressSearchResponse = await response.json()
    const [first] = documents ?? []
    if (!first) {
      return null
    }

    const latitude = Number(first.y)
    const longitude = Number(first.x)
    if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) {
      return null
    }

    return { latitude, longitude }
  } catch {
    return null
  }
}
