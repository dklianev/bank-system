import { useEffect, useState } from 'react'
import { api, setUnauthorizedHandler } from './api'
import DashboardPage from './components/DashboardPage'
import ContributionPage from './components/ContributionPage'
import LoginPage from './components/LoginPage'
import ClientPortalPage from './components/ClientPortalPage'
import ClientsAccountsPage from './modules/dimitar/ClientsAccountsPage'
import CreditsPaymentsPage from './modules/adrian/CreditsPaymentsPage'
import './styles.css'

const adminPages = [
  { id: 'dashboard', label: 'Табло' },
  { id: 'clients', label: 'Клиенти и сметки' },
  { id: 'credits', label: 'Кредити и плащания' },
  { id: 'contribution', label: 'Екип' },
]

const clientPages = [{ id: 'portal', label: 'Моят профил' }]

const roleLabel = (role) => ({
  ADMIN: 'Администратор',
  CLIENT: 'Клиент',
}[role] ?? role)

export default function App() {
  const [session, setSession] = useState(null)
  const [loading, setLoading] = useState(true)
  const [activePage, setActivePage] = useState('dashboard')

  useEffect(() => {
    setUnauthorizedHandler(() => setSession(null))

    api
      .session()
      .then((data) => {
        setSession(data)
        setActivePage(data.role === 'ADMIN' ? 'dashboard' : 'portal')
      })
      .catch(() => setSession(null))
      .finally(() => setLoading(false))
  }, [])

  const onLoggedIn = (data) => {
    setSession(data)
    setActivePage(data.role === 'ADMIN' ? 'dashboard' : 'portal')
  }

  const logout = async () => {
    try {
      await api.logout()
    } catch {
      // Ignore network errors on logout; the session is cleared locally regardless.
    }
    setSession(null)
  }

  if (loading) {
    return <div className="login-screen"><div className="login-card content-panel">Зареждане…</div></div>
  }

  if (!session) {
    return <LoginPage onLoggedIn={onLoggedIn} />
  }

  const isAdmin = session.role === 'ADMIN'
  const pages = isAdmin ? adminPages : clientPages

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand-block">
          <div className="brand-mark">EUR</div>
          <div>
            <h1>Банкова система</h1>
            <p>CSCB634 · Java/MySQL</p>
          </div>
        </div>

        <nav className="nav-list" aria-label="Main navigation">
          {pages.map((page) => (
            <button
              key={page.id}
              className={activePage === page.id ? 'active' : ''}
              type="button"
              onClick={() => setActivePage(page.id)}
            >
              {page.label}
            </button>
          ))}
        </nav>

        <div className="sidebar-footer">
          <span>Димитър F112194</span>
          <span>Адриан F112519</span>
        </div>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <div>
            <strong>Банкови операции</strong>
            <span>Клиенти, сметки и погасителни планове</span>
          </div>
          <div className="topbar-session">
            <span className="session-user">
              {session.displayName} · {roleLabel(session.role)}
            </span>
            <button className="btn btn-sm btn-outline-secondary" type="button" onClick={logout}>
              Изход
            </button>
          </div>
        </header>

        {isAdmin && activePage === 'dashboard' && <DashboardPage />}
        {isAdmin && activePage === 'clients' && <ClientsAccountsPage />}
        {isAdmin && activePage === 'credits' && <CreditsPaymentsPage />}
        {isAdmin && activePage === 'contribution' && <ContributionPage />}
        {!isAdmin && activePage === 'portal' && <ClientPortalPage />}
      </main>
    </div>
  )
}
