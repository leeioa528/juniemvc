import { useEffect, useState } from 'react'
import { listBeers, type BeerDTO, type ListBeersParams } from '../services/beers.service'
import { Link } from 'react-router-dom'

export default function BeersList() {
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(10)
  const [data, setData] = useState<{ content: BeerDTO[]; totalPages: number }>({ content: [], totalPages: 0 })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const params: ListBeersParams = { page, size }
    setLoading(true)
    listBeers(params)
      .then((res) => {
        setData({ content: res.content, totalPages: res.totalPages })
        setError(null)
      })
      .catch((e) => setError(e.message ?? 'Failed to load beers'))
      .finally(() => setLoading(false))
  }, [page, size])

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">Beers</h1>
      {error && <div className="text-red-600">{error}</div>}
      {loading ? (
        <div>Loading…</div>
      ) : (
        <table className="w-full text-sm">
          <thead>
            <tr className="text-left border-b">
              <th className="py-2">Name</th>
              <th className="py-2">Style</th>
              <th className="py-2">Price</th>
              <th className="py-2">UPC</th>
              <th className="py-2">Actions</th>
            </tr>
          </thead>
          <tbody>
            {data.content.map((b) => (
              <tr key={b.id} className="border-b">
                <td className="py-2">{b.beerName}</td>
                <td className="py-2">{b.beerStyle}</td>
                <td className="py-2">{b.price}</td>
                <td className="py-2">{b.upc}</td>
                <td className="py-2">
                  <Link className="underline" to={`/beers/${b.id}`}>View</Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      <div className="flex items-center gap-2">
        <button disabled={page===0} onClick={() => setPage((p)=>Math.max(0,p-1))} className="px-2 py-1 border rounded">Prev</button>
        <span>Page {page+1} of {Math.max(1, data.totalPages)}</span>
        <button disabled={page+1>=data.totalPages} onClick={() => setPage((p)=>p+1)} className="px-2 py-1 border rounded">Next</button>
        <label className="ml-4 text-sm">Page size
          <select value={size} onChange={(e)=> setSize(Number(e.target.value))} className="ml-2 border rounded px-1 py-1">
            {[5,10,20,50].map(s => <option key={s} value={s}>{s}</option>)}
          </select>
        </label>
      </div>
    </div>
  )
}
