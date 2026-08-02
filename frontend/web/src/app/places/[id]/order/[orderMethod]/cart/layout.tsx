interface Props {
  children: React.ReactNode
}

export default function Layout({ children }: Props) {
  return <div className="min-h-screen flex flex-col bg-white">{children}</div>
}
