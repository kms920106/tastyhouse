'use client'

import 'yet-another-react-lightbox/plugins/counter.css'
import 'yet-another-react-lightbox/styles.css'
import styles from '@/components/reviews/ReviewImageGallery.module.css'

import { useEffect } from 'react'
import { HiOutlineXMark } from 'react-icons/hi2'
import Lightbox, {
  ComponentProps,
  EVENT_ON_KEY_DOWN,
  VK_ESCAPE,
  createModule,
  useController,
} from 'yet-another-react-lightbox'
import Counter from 'yet-another-react-lightbox/plugins/counter'

interface Props {
  imageUrls: string[]
  isOpen: boolean
  currentIndex: number
  onClose: () => void
}

// ESC 키로 Lightbox가 닫히지 않도록 하는 커스텀 모듈
// Reference: https://github.com/igordanchenko/yet-another-react-lightbox/discussions/319
function DisableEscapeKey({ children }: ComponentProps) {
  const { subscribeSensors } = useController()

  useEffect(
    () =>
      subscribeSensors(EVENT_ON_KEY_DOWN, (event) => {
        if (event.key === VK_ESCAPE) {
          event.stopPropagation()
        }
      }),
    [subscribeSensors],
  )

  return <>{children}</>
}

const disableEscapeKeyPlugin = ({
  addModule,
}: {
  addModule: (module: ReturnType<typeof createModule>) => void
}) => {
  addModule(createModule('DisableEscapeKey', DisableEscapeKey))
}

export default function ImageLightbox({
  imageUrls,
  isOpen,
  currentIndex,
  onClose,
}: Props) {
  const slides = imageUrls.map((url, index) => ({ src: url, key: index }))

  return (
    <Lightbox
      className={styles.lightbox}
      open={isOpen}
      close={onClose}
      index={currentIndex}
      slides={slides}
      plugins={[Counter, disableEscapeKeyPlugin]}
      carousel={{ finite: imageUrls.length === 1 }}
      controller={{
        closeOnBackdropClick: false,
        closeOnPullDown: true,
      }}
      counter={{
        container: {
          style: {
            top: 'unset',
            bottom: '22%',
            left: '50%',
            right: 'auto',
            transform: 'translateX(-50%)',
            padding: '6.5px 15.5px',
            backgroundColor: '#000000',
            fontSize: '12px',
            lineHeight: '12px',
            letterSpacing: '-1px',
            color: '#ffffff',
            borderRadius: '12px',
            opacity: 0.5,
          },
        },
      }}
      toolbar={{
        buttons: [
          <button
            key="custom-close"
            type="button"
            aria-label="닫기"
            onClick={onClose}
            className={styles.closeButton}
          >
            <HiOutlineXMark size={24} color="white" />
          </button>,
        ],
      }}
      render={{
        buttonPrev: () => null,
        buttonNext: () => null,
      }}
      styles={{
        container: { backgroundColor: 'rgba(0, 0, 0, 1)' },
        slide: { padding: 0 },
      }}
    />
  )
}
