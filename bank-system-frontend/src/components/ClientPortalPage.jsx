import { useEffect, useMemo, useState } from 'react'
import { api } from '../api'
import StatusMessage from './StatusMessage'
import { formatDate, formatMoney, statusClass } from '../utils'

export default function ClientPortalPage() {
  const [accounts, setAccounts] = useState([])
  const [credits, setCredits] = useState([])
  const [installments, setInstallments] = useState([])
  const [selectedCreditId, setSelectedCreditId] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const selectedCredit = useMemo(
    () => credits.find((credit) => credit.id === Number(selectedCreditId)),
    [credits, selectedCreditId],
  )

  const loadData = async () => {
    const [accountsData, creditsData] = await Promise.all([api.accounts(), api.credits()])
    setAccounts(accountsData)
    setCredits(creditsData)
  }

  const loadInstallments = async (creditId) => {
    if (!creditId) {
      setInstallments([])
      return
    }

    setInstallments(await api.installments(creditId))
  }

  useEffect(() => {
    loadData().catch((err) => setError(err.message))
  }, [])

  useEffect(() => {
    loadInstallments(selectedCreditId).catch((err) => setError(err.message))
  }, [selectedCreditId])

  const payInstallment = async (installmentId) => {
    setError('')
    setMessage('')

    try {
      await api.payInstallment(installmentId)
      await loadData()
      await loadInstallments(selectedCreditId)
      setMessage('Вноската е отбелязана като платена.')
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <section>
      <div className="section-title">
        <div>
          <h2>Клиентски портал</h2>
          <p>Преглед на собствените сметки, кредити и погасителен план.</p>
        </div>
      </div>

      <StatusMessage message={message} />
      <StatusMessage message={error} type="error" />

      <div className="content-panel">
        <h3>Моите сметки</h3>
        <div className="table-responsive">
          <table className="table table-sm align-middle">
            <thead>
              <tr>
                <th>IBAN</th>
                <th>Наличност</th>
                <th>Статус</th>
              </tr>
            </thead>
            <tbody>
              {accounts.map((account) => (
                <tr key={account.id}>
                  <td>{account.iban}</td>
                  <td>{formatMoney(account.balance)}</td>
                  <td>
                    <span className={`status-chip ${statusClass(account.status)}`}>
                      {account.status}
                    </span>
                  </td>
                </tr>
              ))}
              {accounts.length === 0 && (
                <tr>
                  <td colSpan="3" className="empty-cell">
                    Все още няма сметки.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      <div className="content-panel">
        <h3>Моите кредити</h3>
        <div className="table-responsive">
          <table className="table table-hover align-middle">
            <thead>
              <tr>
                <th>ID</th>
                <th>Вид</th>
                <th>Сума</th>
                <th>Срок</th>
                <th>Месечна вноска</th>
                <th>Статус</th>
                <th>План</th>
              </tr>
            </thead>
            <tbody>
              {credits.map((credit) => (
                <tr key={credit.id}>
                  <td>{credit.id}</td>
                  <td>{credit.creditProductName}</td>
                  <td>{formatMoney(credit.principalAmount)}</td>
                  <td>{credit.termMonths}</td>
                  <td>{formatMoney(credit.monthlyPayment)}</td>
                  <td>
                    <span className={`status-chip ${statusClass(credit.status)}`}>
                      {credit.status}
                    </span>
                  </td>
                  <td>
                    <button
                      className="btn btn-sm btn-outline-primary"
                      type="button"
                      onClick={() => setSelectedCreditId(String(credit.id))}
                    >
                      Преглед
                    </button>
                  </td>
                </tr>
              ))}
              {credits.length === 0 && (
                <tr>
                  <td colSpan="7" className="empty-cell">
                    Все още няма кредити.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      <div className="content-panel">
        <div className="section-title compact">
          <div>
            <h3>Погасителен план</h3>
            <p>
              {selectedCredit
                ? `Кредит #${selectedCredit.id} — ${selectedCredit.creditProductName}`
                : 'Изберете кредит, за да видите плана.'}
            </p>
          </div>
        </div>

        <select
          className="form-select mb-3"
          value={selectedCreditId}
          onChange={(event) => setSelectedCreditId(event.target.value)}
        >
          <option value="">Изберете кредит</option>
          {credits.map((credit) => (
            <option key={credit.id} value={credit.id}>
              #{credit.id} - {credit.creditProductName} - {formatMoney(credit.principalAmount)}
            </option>
          ))}
        </select>

        <div className="table-responsive installments-table">
          <table className="table table-sm table-hover align-middle">
            <thead>
              <tr>
                <th>№</th>
                <th>Падеж</th>
                <th>Вноска</th>
                <th>Главница</th>
                <th>Лихва</th>
                <th>Остатък</th>
                <th>Статус</th>
                <th>Действие</th>
              </tr>
            </thead>
            <tbody>
              {installments.map((installment) => (
                <tr key={installment.id}>
                  <td>{installment.installmentNumber}</td>
                  <td>{formatDate(installment.dueDate)}</td>
                  <td>{formatMoney(installment.paymentAmount)}</td>
                  <td>{formatMoney(installment.principalPart)}</td>
                  <td>{formatMoney(installment.interestPart)}</td>
                  <td>{formatMoney(installment.remainingPrincipal)}</td>
                  <td>
                    <span className={`status-chip ${statusClass(installment.status)}`}>
                      {installment.status}
                    </span>
                  </td>
                  <td>
                    <button
                      className="btn btn-sm btn-outline-success"
                      type="button"
                      disabled={installment.status === 'PAID'}
                      onClick={() => payInstallment(installment.id)}
                    >
                      Плати
                    </button>
                  </td>
                </tr>
              ))}
              {installments.length === 0 && (
                <tr>
                  <td colSpan="8" className="empty-cell">
                    Не е избран погасителен план.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </section>
  )
}
