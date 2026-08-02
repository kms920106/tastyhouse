'use client'

import Script from 'next/script'
import { useCallback, useState } from 'react'
import { env } from '@/lib/env'
import { escapeHtml } from '@/lib/sanitize'
import { MapLoadingIndicator } from './MapLoadingIndicator'

interface KakaoLatLng {
  getLat(): number
  getLng(): number
}

interface KakaoMarker {
  setMap(map: KakaoMap | null): void
}

interface KakaoCustomOverlay {
  setMap(map: KakaoMap | null): void
}

interface KakaoMap {
  setCenter(latLng: KakaoLatLng): void
  setLevel(level: number): void
}

interface KakaoMaps {
  LatLng: {
    new (lat: number, lng: number): KakaoLatLng
  }
  Map: {
    new (
      container: HTMLElement | null,
      options: {
        center: KakaoLatLng
        level: number
      },
    ): KakaoMap
  }
  Marker: {
    new (options: { position: KakaoLatLng; title?: string }): KakaoMarker
  }
  CustomOverlay: {
    new (options: { position: KakaoLatLng; content?: string }): KakaoCustomOverlay
  }
  load(callback: () => void): void
}

interface KakaoAPI {
  maps: KakaoMaps
}

// Window.kakao 전역 타입은 다른 카카오맵 컴포넌트에서 이미 선언하므로 재선언하지 않고,
// 이 컴포넌트가 사용하는 형태로 좁혀서 접근한다.
function getKakao(): KakaoAPI | undefined {
  return (window as unknown as { kakao?: KakaoAPI }).kakao
}

interface Props {
  name: string
  latitude: number
  longitude: number
}

export default function ShopMap({ name, latitude, longitude }: Props) {
  const [isMapReady, setIsMapReady] = useState(false)

  const loadKakaoMap = useCallback(() => {
    const kakao = getKakao()
    if (!kakao?.maps) {
      return
    }

    kakao.maps.load(() => {
      const mapContainer = document.getElementById('shop-map')
      if (!mapContainer) {
        return
      }

      const position = new kakao.maps.LatLng(latitude, longitude)

      const map = new kakao.maps.Map(mapContainer, {
        center: position,
        level: 3,
      })
      setIsMapReady(true)

      const marker = new kakao.maps.Marker({ position, title: name })
      marker.setMap(map)

      const content = `
        <div class="label" style="padding: 0 13px; background-color: white; border: 1px solid silver; border-radius: 10px">
          <span class="center" style="font-size: 13px!important; font-weight: bold">${escapeHtml(name)}</span>
        </div>
      `

      const customOverlay = new kakao.maps.CustomOverlay({
        position: new kakao.maps.LatLng(latitude - 0.00003, longitude),
        content,
      })
      customOverlay.setMap(map)
    })
  }, [name, latitude, longitude])

  return (
    <div className="flex flex-1 min-h-0">
      <div className="flex-1 relative">
        {!isMapReady && (
          <div className="absolute inset-0 bg-gray-100 z-10">
            <MapLoadingIndicator />
          </div>
        )}
        <Script
          strategy="afterInteractive"
          type="text/javascript"
          onReady={loadKakaoMap}
          src={`//dapi.kakao.com/v2/maps/sdk.js?appkey=${env.NEXT_PUBLIC_KAKAO_MAP_CLIENT}&autoload=false`}
        />
        <div id="shop-map" className="w-full h-full" />
      </div>
    </div>
  )
}
