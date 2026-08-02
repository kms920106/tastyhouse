'use client'

interface Props {
  isOpen: boolean
  closePanel: () => void
}

export default function SideBar({ isOpen, closePanel }: Props) {
  return (
    <aside
      className={`absolute top-0 left-0 z-40 size-70 h-full bg-white shadow-lg transition-transform duration-300
        ${isOpen ? 'translate-x-0' : '-translate-x-full'}`}
    >
      <div className="p-4 flex justify-between items-center border-b">
        <h2 className="text-lg font-bold">사이드바</h2>
        <button onClick={closePanel} className="p-1 rounded">
          ✕
        </button>
      </div>
      <div className="p-4">여기에 컨텐츠</div>
    </aside>
  )
}
