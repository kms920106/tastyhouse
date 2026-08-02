interface Props {
  label: string
  required?: boolean
  error?: string
  children: (props: { className?: string }) => React.ReactNode
}

export default function AppFormField({ label, required, error, children }: Props) {
  const inputClassName = error ? 'border-[#bc4040] focus-visible:border-[#bc4040]' : undefined

  return (
    <div className="flex flex-col gap-2.5">
      <h3 className="flex items-center text-xs h-3 overflow-hidden">
        {label}
        {required && (
          <span className="text-[17px] leading-3 text-[#bc4040] ml-[5px]">
            *
          </span>
        )}
      </h3>
      {children({ className: inputClassName })}
      {error && <p className="text-xs leading-[12px] text-[#bc4040]">{error}</p>}
    </div>
  )
}
