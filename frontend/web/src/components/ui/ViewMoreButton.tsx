import Link from 'next/link'

interface Props {
  href: string
  label?: string
}

export default function ViewMoreButton({ href, label = '더 보러가기' }: Props) {
  return (
    <Link
      href={href}
      className="inline-block w-1/2 py-[11.5px] text-sm text-center bg-white border border-line"
    >
      {label}
    </Link>
  )
}
