import { useEffect, useState } from 'react'
import { api } from '../api'
import { formatMoney } from '../utils'

export default function DashboardPage() {
  const [dashboard, setDashboard] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    api
      .dashboard()
      .then(setDashboard)
      .catch((err) => setError(err.message))
  }, [])

  return (
    <section>
      <div className="section-title">
        <div>
          <h2>Табло</h2>
          <p>Общ преглед на банковата система.</p>
        </div>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      <div className="stats-grid">
        <Stat label="Клиенти" value={dashboard?.clientsCount ?? 0} />
        <Stat label="Активни сметки" value={dashboard?.activeAccountsCount ?? 0} />
        <Stat label="Активни кредити" value={dashboard?.activeCreditsCount ?? 0} />
        <Stat label="Главница по кредити" value={formatMoney(dashboard?.totalCreditPrincipal)} />
      </div>

      <div className="content-panel">
        <h3>Демонстрационен сценарий</h3>
        <ol className="mb-0">
          <li>Добавяне на клиент: физическо или юридическо лице.</li>
          <li>Откриване на банкова сметка към избран клиент.</li>
          <li>Отпускане на кредит според лимитите на кредитния продукт.</li>
          <li>Генериране на анюитетен погасителен план.</li>
          <li>Маркиране на вноска като платена и проверка на статус.</li>
        </ol>
      </div>
    </section>
  )
}

function Stat({ label, value }) {
  return (
    <div className="stat-card">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  )
}
