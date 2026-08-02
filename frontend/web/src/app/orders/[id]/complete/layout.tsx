interface Props {
  children: React.ReactNode
}

export default function Layout({ children }: Props) {
  return <div className="flex flex-col h-dvh overflow-hidden">{children}</div>
}
