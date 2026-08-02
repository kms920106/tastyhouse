import { ButtonHTMLAttributes } from 'react'
import { HEADER_ICON_CLASS } from './constants'

type Props = ButtonHTMLAttributes<HTMLButtonElement>

export default function HeaderIconButton({ children, className, ...props }: Props) {
  return (
    <button
      className={className ? `${HEADER_ICON_CLASS} ${className}` : HEADER_ICON_CLASS}
      {...props}
    >
      {children}
    </button>
  )
}
