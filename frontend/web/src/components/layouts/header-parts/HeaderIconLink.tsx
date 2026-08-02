import Link, { LinkProps } from 'next/link'
import { HEADER_ICON_CLASS } from './constants'

interface Props extends LinkProps {
  children: React.ReactNode
  className?: string
}

export default function HeaderIconLink({ children, className, ...props }: Props) {
  return (
    <Link
      className={className ? `${HEADER_ICON_CLASS} ${className}` : HEADER_ICON_CLASS}
      {...props}
    >
      {children}
    </Link>
  )
}
