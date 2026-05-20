export default function StatusMessage({ message, type = 'info' }) {
  if (!message) {
    return null
  }

  return (
    <div className={`alert alert-${type === 'error' ? 'danger' : 'success'} py-2`}>
      {message}
    </div>
  )
}
