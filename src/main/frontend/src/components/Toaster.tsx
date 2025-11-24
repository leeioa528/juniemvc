import * as Toast from '@radix-ui/react-toast'
import { PropsWithChildren, useState } from 'react'

export function ToasterProvider({ children }: PropsWithChildren) {
  const [open, setOpen] = useState(false)
  const [message, setMessage] = useState<string | null>(null)

  // Simple global toast API via window for demo purposes
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  ;(window as any).toast = (msg: string) => {
    setMessage(msg)
    setOpen(true)
  }

  return (
    <Toast.Provider>
      {children}
      <Toast.Root open={open} onOpenChange={setOpen} className="bg-black text-white px-3 py-2 rounded shadow-lg">
        <Toast.Title className="font-medium">Notification</Toast.Title>
        {message && <Toast.Description className="text-sm opacity-90">{message}</Toast.Description>}
      </Toast.Root>
      <Toast.Viewport className="fixed bottom-4 right-4 w-96 max-w-[100vw] outline-none" />
    </Toast.Provider>
  )
}
