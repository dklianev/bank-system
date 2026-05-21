import { useState } from 'react'
import { api } from '../api'
import StatusMessage from './StatusMessage'

export default function LoginPage({ onLoggedIn }) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const submit = async (event) => {
    event.preventDefault()
    setError('')
    setBusy(true)

    try {
      const session = await api.login(username, password)
      onLoggedIn(session)
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="login-screen">
      <div className="login-card content-panel">
        <div className="brand-block">
          <div className="brand-mark">EUR</div>
          <div>
            <h1>Bank Registry</h1>
            <p>CSCB634 · Java/MySQL</p>
          </div>
        </div>

        <h3>Sign in</h3>
        <p className="login-hint">
          Служители влизат с административен профил. Клиентите използват профил, създаден от банката.
        </p>

        <StatusMessage message={error} type="error" />

        <form onSubmit={submit}>
          <label className="form-label">Username</label>
          <input
            className="form-control mb-3"
            type="text"
            value={username}
            onChange={(event) => setUsername(event.target.value)}
            autoComplete="username"
            required
          />

          <label className="form-label">Password</label>
          <input
            className="form-control mb-3"
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            autoComplete="current-password"
            required
          />

          <button className="btn btn-primary w-100" type="submit" disabled={busy}>
            {busy ? 'Signing in…' : 'Sign in'}
          </button>
        </form>
      </div>
    </div>
  )
}
