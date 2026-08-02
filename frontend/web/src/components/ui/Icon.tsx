import NextImage, { type ImageProps } from 'next/image'
import { cn } from '@/lib/utils'
import { type IconName, ICON_REGISTRY } from './icon-registry'

type PassthroughImageProps = Omit<ImageProps, 'src' | 'alt' | 'width' | 'height' | 'fill'>

interface IconProps extends PassthroughImageProps {
  name: IconName
  width?: number
  height?: number
  fill?: boolean
  alt?: string
  className?: string
}

export default function Icon({
  name,
  width,
  height,
  fill = false,
  alt,
  className,
  ...rest
}: IconProps) {
  const meta = ICON_REGISTRY[name]
  const metaAlt = 'alt' in meta ? (meta.alt as string | undefined) : undefined
  const resolvedAlt = alt ?? metaAlt ?? ''
  const ariaHidden = resolvedAlt === '' ? true : undefined

  if (fill) {
    return (
      <NextImage
        src={meta.src}
        alt={resolvedAlt}
        aria-hidden={ariaHidden}
        fill
        sizes={`${width ?? meta.width}px`}
        className={cn(className)}
        {...rest}
      />
    )
  }

  return (
    <NextImage
      src={meta.src}
      alt={resolvedAlt}
      aria-hidden={ariaHidden}
      width={width ?? meta.width}
      height={height ?? meta.height}
      className={cn(className)}
      {...rest}
    />
  )
}
