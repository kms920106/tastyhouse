interface Props {
  onClick?: () => void
  disabled?: boolean
  children: React.ReactNode
}

export default function MenuGridButton({ onClick, disabled, children }: Props) {
  return (
    <button
      onClick={onClick}
      disabled={disabled}
      className="flex flex-col items-center justify-center gap-2 h-24 bg-[#fdfdfd] border border-line"
    >
      {children}
    </button>
  )
}
