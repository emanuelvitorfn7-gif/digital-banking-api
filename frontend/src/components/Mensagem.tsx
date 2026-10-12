interface MensagemProps {
  tipo: 'erro' | 'sucesso'
  texto: string
}

export function Mensagem({ tipo, texto }: MensagemProps) {
  return (
    <p className={`mensagem mensagem--${tipo}`} role="status">
      {texto}
    </p>
  )
}
