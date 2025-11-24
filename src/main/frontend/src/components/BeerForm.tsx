import { useEffect, useState } from 'react'
import type { BeerDTO } from '../services/beers.service'

export type BeerFormProps = {
  initial?: Partial<BeerDTO>
  onSubmit: (values: BeerDTO) => Promise<void> | void
  submitting?: boolean
}

const defaults: BeerDTO = {
  beerName: '',
  beerStyle: '',
  upc: '',
  quantityOnHand: 0,
  price: 0,
}

export default function BeerForm({ initial, onSubmit, submitting }: BeerFormProps) {
  const [values, setValues] = useState<BeerDTO>({ ...defaults, ...(initial ?? {}) })
  const [errors, setErrors] = useState<Record<string, string>>({})

  useEffect(() => {
    setValues({ ...defaults, ...(initial ?? {}) })
  }, [initial])

  function validate(v: BeerDTO): Record<string, string> {
    const e: Record<string, string> = {}
    if (!v.beerName?.trim()) e.beerName = 'Name is required'
    if (!v.beerStyle?.trim()) e.beerStyle = 'Style is required'
    if (!v.upc?.trim()) e.upc = 'UPC is required'
    if (String(v.upc).length < 6) e.upc = 'UPC looks too short'
    if (v.quantityOnHand == null || v.quantityOnHand < 0) e.quantityOnHand = 'Quantity must be >= 0'
    if (v.price == null || v.price <= 0) e.price = 'Price must be > 0'
    return e
  }

  async function handleSubmit(ev: React.FormEvent) {
    ev.preventDefault()
    const e = validate(values)
    setErrors(e)
    if (Object.keys(e).length > 0) return
    await onSubmit(values)
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4 max-w-xl">
      <div>
        <label className="block text-sm font-medium">Name</label>
        <input
          className="mt-1 w-full border rounded px-2 py-1"
          value={values.beerName}
          onChange={(e) => setValues((v) => ({ ...v, beerName: e.target.value }))}
        />
        {errors.beerName && <div className="text-red-600 text-sm">{errors.beerName}</div>}
      </div>
      <div>
        <label className="block text-sm font-medium">Style</label>
        <input
          className="mt-1 w-full border rounded px-2 py-1"
          value={values.beerStyle}
          onChange={(e) => setValues((v) => ({ ...v, beerStyle: e.target.value }))}
        />
        {errors.beerStyle && <div className="text-red-600 text-sm">{errors.beerStyle}</div>}
      </div>
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium">UPC</label>
          <input
            className="mt-1 w-full border rounded px-2 py-1"
            value={values.upc}
            onChange={(e) => setValues((v) => ({ ...v, upc: e.target.value }))}
          />
          {errors.upc && <div className="text-red-600 text-sm">{errors.upc}</div>}
        </div>
        <div>
          <label className="block text-sm font-medium">Quantity</label>
          <input
            type="number"
            className="mt-1 w-full border rounded px-2 py-1"
            value={values.quantityOnHand}
            onChange={(e) => setValues((v) => ({ ...v, quantityOnHand: Number(e.target.value) }))}
          />
          {errors.quantityOnHand && <div className="text-red-600 text-sm">{errors.quantityOnHand}</div>}
        </div>
      </div>
      <div>
        <label className="block text-sm font-medium">Price</label>
        <input
          type="number"
          step="0.01"
          className="mt-1 w-full border rounded px-2 py-1"
          value={values.price}
          onChange={(e) => setValues((v) => ({ ...v, price: Number(e.target.value) }))}
        />
        {errors.price && <div className="text-red-600 text-sm">{errors.price}</div>}
      </div>
      <div>
        <label className="block text-sm font-medium">Description</label>
        <textarea
          className="mt-1 w-full border rounded px-2 py-1"
          value={values.description ?? ''}
          onChange={(e) => setValues((v) => ({ ...v, description: e.target.value }))}
          rows={3}
        />
      </div>
      <div className="flex gap-2">
        <button disabled={submitting} type="submit" className="px-3 py-1 rounded border">
          {submitting ? 'Saving…' : 'Save'}
        </button>
      </div>
    </form>
  )
}
