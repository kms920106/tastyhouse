import Link, { LinkProps } from 'next/link'

interface Props extends LinkProps {
  children: React.ReactNode
  className?: string
}

export default function IconLink({ children, className, ...props }: Props) {
  return (
    <Link className={`flex items-center justify-center ${className ?? ''}`} {...props}>
      {children}
    </Link>
  )
}
