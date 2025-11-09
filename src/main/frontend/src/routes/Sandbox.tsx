import { Button } from '../components/ui/button'

export default function Sandbox() {
  return (
    <div className="space-y-4">
      <h2 className="text-xl font-semibold">Sandbox</h2>
      <div className="flex gap-2">
        <Button>Default</Button>
        <Button variant="secondary">Secondary</Button>
        <Button variant="outline">Outline</Button>
        <Button variant="ghost">Ghost</Button>
        <Button variant="link">Link</Button>
      </div>
      <p className="text-sm text-zinc-600">
        Tailwind styles and shadcn-style Button component should render above.
      </p>
    </div>
  )
}
