import { useCallback, useEffect, useRef, useState } from 'react'

interface UseIntersectionObserverOptions {
  threshold?: number
  rootMargin?: string
  enabled?: boolean
}

export function useIntersectionObserver<T extends HTMLElement = HTMLDivElement>({
  threshold = 0,
  rootMargin = '0px',
  enabled = true,
}: UseIntersectionObserverOptions = {}) {
  const targetRef = useRef<T>(null)
  const [isIntersecting, setIsIntersecting] = useState(false)

  const resetIntersecting = useCallback(() => {
    setIsIntersecting(false)
  }, [])

  useEffect(() => {
    const target = targetRef.current

    if (!enabled || !target) {
      setIsIntersecting(false)
      return
    }

    const observer = new IntersectionObserver(
      ([entry]) => {
        setIsIntersecting(entry.isIntersecting)
      },
      { threshold, rootMargin },
    )

    observer.observe(target)

    return () => {
      observer.disconnect()
    }
  }, [threshold, rootMargin, enabled])

  return { targetRef, isIntersecting, resetIntersecting }
}
