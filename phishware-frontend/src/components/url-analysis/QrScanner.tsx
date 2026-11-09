import { useCallback, useEffect, useRef, useState } from 'react'
import jsQR from 'jsqr'
import { Camera, CameraOff, ImageUp, Loader2 } from 'lucide-react'
import { useLang } from '@/i18n/LanguageContext'
import type { Strings } from '@/i18n/translations'

interface Props {
  onDecoded: (text: string) => void
  disabled?: boolean
}

type Mode = 'idle' | 'camera' | 'error'

const SCAN_INTERVAL_MS = 200
const MAX_IMAGE_SIDE = 1600

/** Busca un QR en los píxeles de un canvas. */
function decodeCanvas(canvas: HTMLCanvasElement): string | null {
  const ctx = canvas.getContext('2d', { willReadFrequently: true })
  if (!ctx || canvas.width === 0) return null
  const { data, width, height } = ctx.getImageData(0, 0, canvas.width, canvas.height)
  const code = jsQR(data, width, height, { inversionAttempts: 'attemptBoth' })
  return code?.data?.trim() ? code.data : null
}

function cameraErrorMessage(t: Strings['qr'], err: unknown): string {
  const name = err instanceof DOMException ? err.name : ''
  if (name === 'NotAllowedError') return t.cameraDenied
  if (name === 'NotFoundError' || name === 'OverconstrainedError') return t.cameraMissing
  if (name === 'NotReadableError') return t.cameraBusy
  if (!window.isSecureContext) return t.cameraInsecure
  return t.cameraGeneric
}

export default function QrScanner({ onDecoded, disabled }: Props) {
  const { t } = useLang()
  const videoRef = useRef<HTMLVideoElement>(null)
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const streamRef = useRef<MediaStream | null>(null)
  const timerRef = useRef<number | null>(null)
  const fileRef = useRef<HTMLInputElement>(null)

  const [mode, setMode] = useState<Mode>('idle')
  const [message, setMessage] = useState<string | null>(null)
  const [starting, setStarting] = useState(false)
  const [readingImage, setReadingImage] = useState(false)

  const stopCamera = useCallback(() => {
    if (timerRef.current) window.clearInterval(timerRef.current)
    timerRef.current = null
    streamRef.current?.getTracks().forEach(t => t.stop())
    streamRef.current = null
    if (videoRef.current) videoRef.current.srcObject = null
  }, [])

  // Apagar la cámara al salir de la pantalla
  useEffect(() => stopCamera, [stopCamera])

  async function startCamera() {
    setMessage(null)
    if (!navigator.mediaDevices?.getUserMedia) {
      setMode('error')
      setMessage(cameraErrorMessage(t.qr, null))
      return
    }
    setStarting(true)
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: { ideal: 'environment' }, width: { ideal: 1280 }, height: { ideal: 720 } },
        audio: false,
      })
      streamRef.current = stream
      const video = videoRef.current!
      video.srcObject = stream
      await video.play()
      setMode('camera')

      timerRef.current = window.setInterval(() => {
        const canvas = canvasRef.current
        if (!canvas || video.readyState < video.HAVE_ENOUGH_DATA) return
        canvas.width = video.videoWidth
        canvas.height = video.videoHeight
        canvas.getContext('2d', { willReadFrequently: true })?.drawImage(video, 0, 0)
        const text = decodeCanvas(canvas)
        if (text) {
          stopCamera()
          setMode('idle')
          navigator.vibrate?.(80)
          onDecoded(text)
        }
      }, SCAN_INTERVAL_MS)
    } catch (err) {
      stopCamera()
      setMode('error')
      setMessage(cameraErrorMessage(t.qr, err))
    } finally {
      setStarting(false)
    }
  }

  function closeCamera() {
    stopCamera()
    setMode('idle')
  }

  async function decodeFile(file: File) {
    if (!file.type.startsWith('image/')) {
      setMessage(t.qr.badFileType)
      return
    }
    setReadingImage(true)
    setMessage(null)
    const url = URL.createObjectURL(file)
    try {
      const img = new Image()
      img.src = url
      await img.decode()
      const scale = Math.min(1, MAX_IMAGE_SIDE / Math.max(img.width, img.height))
      const canvas = canvasRef.current!
      canvas.width = Math.round(img.width * scale)
      canvas.height = Math.round(img.height * scale)
      canvas.getContext('2d', { willReadFrequently: true })?.drawImage(img, 0, 0, canvas.width, canvas.height)
      const text = decodeCanvas(canvas)
      if (text) {
        onDecoded(text)
      } else {
        setMessage(t.qr.noQrInImage)
      }
    } catch {
      setMessage(t.qr.unreadableImage)
    } finally {
      URL.revokeObjectURL(url)
      setReadingImage(false)
      if (fileRef.current) fileRef.current.value = ''
    }
  }

  // Permite pegar una captura de pantalla con Ctrl/Cmd+V
  function handlePaste(e: React.ClipboardEvent) {
    const file = Array.from(e.clipboardData.files).find(f => f.type.startsWith('image/'))
    if (file) {
      e.preventDefault()
      decodeFile(file)
    }
  }

  return (
    <div onPaste={handlePaste} className="space-y-4">
      <div className={`relative overflow-hidden rounded-xl border divider bg-ink-900 ${mode === 'camera' ? 'aspect-video' : 'hidden'}`}>
        <video ref={videoRef} className="w-full h-full object-cover" muted playsInline aria-label="Vista de la cámara" />
        {/* Marco guía */}
        <div className="pointer-events-none absolute inset-0 flex items-center justify-center">
          <div className="w-1/2 max-w-64 aspect-square rounded-2xl border-2 border-white/80 shadow-[0_0_0_9999px_rgba(23,23,28,0.45)]" />
        </div>
        <p className="absolute bottom-3 inset-x-0 text-center text-xs text-white/90">Apunta la cámara al código QR</p>
      </div>
      <canvas ref={canvasRef} className="hidden" />

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        {mode === 'camera' ? (
          <button type="button" onClick={closeCamera} className="btn-secondary flex items-center justify-center gap-2">
            <CameraOff className="w-4 h-4" /> Detener cámara
          </button>
        ) : (
          <button
            type="button"
            onClick={startCamera}
            disabled={disabled || starting}
            className="btn-primary flex items-center justify-center gap-2"
          >
            {starting ? <Loader2 className="w-4 h-4 animate-spin" /> : <Camera className="w-4 h-4" />}
            Escanear con la cámara
          </button>
        )}
        <button
          type="button"
          onClick={() => fileRef.current?.click()}
          disabled={disabled || readingImage}
          className="btn-secondary flex items-center justify-center gap-2"
        >
          {readingImage ? <Loader2 className="w-4 h-4 animate-spin" /> : <ImageUp className="w-4 h-4" />}
          Subir imagen del QR
        </button>
        <input
          ref={fileRef}
          type="file"
          accept="image/*"
          className="hidden"
          onChange={(e) => { const f = e.target.files?.[0]; if (f) decodeFile(f) }}
        />
      </div>

      {message && (
        <p role="alert" className={`text-sm font-medium ${mode === 'error' ? 'text-danger-600' : 'text-warning-600'}`}>{message}</p>
      )}
      <p className="text-xs text-ink-500 dark:text-ink-300">
        La imagen se procesa en tu navegador y no se sube a ningún servidor. Solo se analiza el enlace que contenga el QR.
        También puedes pegar una captura con Ctrl/⌘+V.
      </p>
    </div>
  )
}
