import { useState } from 'react'
import type { FormEvent } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { listDocuments, queryKnowledge, uploadDocument } from '../api/client'

export function KnowledgePage() {
  const queryClient = useQueryClient()
  const docs = useQuery({ queryKey: ['docs'], queryFn: listDocuments })
  const [question, setQuestion] = useState('What is the procedure for damaged goods?')
  const [answer, setAnswer] = useState<string | null>(null)
  const [file, setFile] = useState<File | null>(null)

  async function onUpload(e: FormEvent) {
    e.preventDefault()
    if (!file) return
    await uploadDocument(file)
    setFile(null)
    await queryClient.invalidateQueries({ queryKey: ['docs'] })
  }

  async function onAsk(e: FormEvent) {
    e.preventDefault()
    const res = await queryKnowledge(question)
    setAnswer(res.answer)
  }

  return (
    <div className="mx-auto max-w-4xl px-6 py-10">
      <h1 className="mb-6 text-3xl font-semibold">Knowledge Base</h1>
      <form onSubmit={onUpload} className="mb-8 flex flex-wrap items-end gap-3 rounded-2xl border border-[var(--border)] bg-[var(--panel)] p-4">
        <input type="file" accept=".txt,.md,.pdf,.docx" onChange={(e) => setFile(e.target.files?.[0] ?? null)} />
        <button className="rounded-lg bg-[var(--accent)] px-4 py-2 text-white">Upload</button>
      </form>
      <ul className="mb-8 space-y-2 text-sm">
        {docs.data?.map((d) => (
          <li key={d.id} className="rounded-lg border border-[var(--border)] px-3 py-2">{d.title} — {d.status}</li>
        ))}
      </ul>
      <form onSubmit={onAsk} className="mb-4 flex gap-3">
        <input className="flex-1 rounded-lg border border-[var(--border)] bg-[#0d1524] px-3 py-2" value={question} onChange={(e) => setQuestion(e.target.value)} />
        <button className="rounded-lg bg-[var(--accent)] px-4 py-2 text-white">Query</button>
      </form>
      {answer && <pre className="whitespace-pre-wrap rounded-2xl border border-[var(--border)] bg-[var(--panel)] p-4 text-sm">{answer}</pre>}
    </div>
  )
}
