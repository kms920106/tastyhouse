'use client'

import { getMapMarkers } from '@/actions/shop'
import type { ShopMapMarker } from '@/domains/shop'
import Script from 'next/script'
import { useCallback, useRef, useState } from 'react'
import { env } from '@/lib/env'
import { escapeHtml } from '@/lib/sanitize'

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
  getCenter(): KakaoLatLng
  getLevel(): number
}

type KakaoMarkerImage = object

type KakaoSize = object

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
    new (options: { position: KakaoLatLng; title?: string; image?: KakaoMarkerImage }): KakaoMarker
  }
  CustomOverlay: {
    new (options: { position: KakaoLatLng; content?: string }): KakaoCustomOverlay
  }
  MarkerImage: {
    new (src: string, size: KakaoSize): KakaoMarkerImage
  }
  Size: {
    new (width: number, height: number): KakaoSize
  }
  load(callback: () => void): void
  event: {
    addListener(target: KakaoMap, type: string, handler: () => void): void
  }
}

interface KakaoAPI {
  maps: KakaoMaps
}

declare global {
  interface Window {
    kakao: KakaoAPI
  }
}

export default function KakaoMap() {
  const [latitude, setLatitude] = useState(37.5666103)
  const [longitude, setLongitude] = useState(126.9783882)
  const [isMapReady, setIsMapReady] = useState(false)
  const [fetchError, setFetchError] = useState(false)

  // useRef로 마커들을 관리하여 최신 상태를 항상 참조
  const markersRef = useRef<KakaoMarker[]>([])
  // 커스텀 오버레이들을 관리하기 위한 ref
  const overlaysRef = useRef<KakaoCustomOverlay[]>([])

  // 기존 마커들을 제거하는 함수
  const clearMarkers = useCallback(() => {
    markersRef.current.forEach((marker) => {
      marker.setMap(null)
    })
    markersRef.current = []
  }, [])

  // 기존 커스텀 오버레이들을 제거하는 함수
  const clearOverlay = useCallback(() => {
    overlaysRef.current.forEach((overlay) => {
      overlay.setMap(null)
    })
    overlaysRef.current = []
  }, [])

  // 새로운 마커들을 생성하는 함수
  const createMarkers = useCallback((markers: ShopMapMarker[], mapInstance: KakaoMap) => {
    const newMarkers: KakaoMarker[] = []

    markers.forEach((place) => {
      const marker = new window.kakao.maps.Marker({
        position: new window.kakao.maps.LatLng(place.latitude, place.longitude),
      })
      marker.setMap(mapInstance)
      newMarkers.push(marker)

      const content = `
        <div class="label" style="padding: 0 13px; background-color: white; border: 1px solid silver; border-radius: 10px">
          <span class="center" style="font-size: 13px!important; font-weight: bold">${escapeHtml(place.name)}</span>
        </div>
      `

      // 커스텀 오버레이를 생성합니다
      const customOverlay = new window.kakao.maps.CustomOverlay({
        position: new window.kakao.maps.LatLng(place.latitude - 0.00003, place.longitude),
        content,
      })

      customOverlay.setMap(mapInstance)
      overlaysRef.current.push(customOverlay)
    })

    markersRef.current = newMarkers
  }, [])

  // 장소 데이터를 가져오는 함수
  const fetchAndUpdatePlaces = useCallback(
    async (lat: number, lng: number, mapInstance: KakaoMap) => {
      try {
        setFetchError(false)
        const markers = await getMapMarkers({ latitude: lat, longitude: lng })
        clearMarkers()
        clearOverlay()
        createMarkers(markers, mapInstance)
      } catch (error) {
        console.error('장소 데이터를 가져오는 중 오류 발생:', error)
        setFetchError(true)
        clearMarkers()
        clearOverlay()
      }
    },
    [clearMarkers, clearOverlay, createMarkers],
  )

  const loadKakaoMap = useCallback(() => {
    if (!window.kakao?.maps) {
      return
    }

    window.kakao.maps.load(async () => {
      const mapContainer = document.getElementById('map')
      if (!mapContainer) {
        return
      }

      const mapOption = {
        center: new window.kakao.maps.LatLng(latitude, longitude),
        level: 3,
      }

      // 지도 생성
      const newMap = new window.kakao.maps.Map(mapContainer, mapOption)
      setIsMapReady(true)

      // 초기 장소 데이터 로드
      await fetchAndUpdatePlaces(latitude, longitude, newMap)

      window.kakao.maps.event.addListener(newMap, 'dragend', async () => {
        const latlng = newMap.getCenter()
        const newLatitude = latlng.getLat()
        const newLongitude = latlng.getLng()

        // 상태 업데이트
        setLatitude(newLatitude)
        setLongitude(newLongitude)

        // 새로운 좌표로 장소 데이터 업데이트
        await fetchAndUpdatePlaces(newLatitude, newLongitude, newMap)
      })
    })
  }, [latitude, longitude, fetchAndUpdatePlaces])

  return (
    <div className="flex h-screen">
      <div className="flex-1 relative">
        {!isMapReady && (
          <div className="absolute inset-0 flex items-center justify-center bg-gray-100 z-10">
            <span className="text-gray-500">지도를 불러오는 중...</span>
          </div>
        )}
        {fetchError && (
          <div className="absolute top-4 left-1/2 -translate-x-1/2 bg-red-50 text-red-600 px-4 py-2 rounded-lg shadow z-10">
            장소 정보를 불러오지 못했습니다.
          </div>
        )}
        <Script
          strategy="afterInteractive"
          type="text/javascript"
          onReady={loadKakaoMap}
          src={`//dapi.kakao.com/v2/maps/sdk.js?appkey=${env.NEXT_PUBLIC_KAKAO_MAP_CLIENT}&autoload=false`}
        />
        <div id="map" className="w-full h-full" />
      </div>
    </div>
  )
}
