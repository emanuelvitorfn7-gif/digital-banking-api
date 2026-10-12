import { useId, type ReactNode } from 'react'
import { maskCurrencyInput } from '../../lib/masks'

interface CurrencyInputProps {
  label: string
  value: string
  onChange: (masked: string) => void
  error?: string | null
  hint?: ReactNode
  disabled?: boolean
}

export function CurrencyInput({
  label,
  value,
  onChange,
  error,
  hint,
  disabled = false,
}: CurrencyInputProps) {
  const inputId = useId()

  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={inputId} className="text-sm font-medium text-slate-700">
        {label}
      </label>
      <div
        className={`flex items-center rounded-lg border bg-white px-3 shadow-sm transition-colors ${
          error ? 'border-red-400 focus-within:border-red-500' : 'border-slate-300 focus-within:border-brand-500'
        }`}
      >
        <span className="text-sm text-slate-400">R$</span>
        <input
          id={inputId}
          inputMode="numeric"
          autoComplete="off"
          disabled={disabled}
          aria-invalid={error ? true : undefined}
          aria-describedby={error ? `${inputId}-error` : undefined}
          value={value}
          onChange={(event) => onChange(maskCurrencyInput(event.target.value))}
          className="w-full bg-transparent py-2 pl-2 text-right text-sm text-slate-900 outline-none placeholder:text-slate-400"
          placeholder="0,00"
        />
      </div>
      {hint && !error ? <p className="text-xs text-slate-500">{hint}</p> : null}
      {error ? (
        <p id={`${inputId}-error`} className="text-xs font-medium text-red-600">
          {error}
        </p>
      ) : null}
    </div>
  )
}
