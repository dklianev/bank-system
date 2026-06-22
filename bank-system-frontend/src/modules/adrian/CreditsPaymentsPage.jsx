import { useEffect, useMemo, useState } from 'react'
import { api } from '../../api'
import StatusMessage from '../../components/StatusMessage'
import { formatDate, formatMoney, statusClass, creditStatusLabel, installmentStatusLabel, clientTypeLabel, creditTypeLabel } from '../../utils'

const emptyCredit = {
  clientId: '',
  creditProductId: '',
  principalAmount: '',
  termMonths: '',
  startDate: '',
}

function todayInputValue() {
  const today = new Date()
  today.setMinutes(today.getMinutes() - today.getTimezoneOffset())
  return today.toISOString().slice(0, 10)
}

export default function CreditsPaymentsPage() {
  const [clients, setClients] = useState([])
  const [products, setProducts] = useState([])
  const [credits, setCredits] = useState([])
  const [installments, setInstallments] = useState([])
  const [selectedCreditId, setSelectedCreditId] = useState('')
  const [creditForm, setCreditForm] = useState(emptyCredit)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const minStartDate = useMemo(() => todayInputValue(), [])

  const selectedProduct = useMemo(
    () => products.find((product) => product.id === Number(creditForm.creditProductId)),
    [products, creditForm.creditProductId],
  )

  const selectedCredit = useMemo(
    () => credits.find((credit) => credit.id === Number(selectedCreditId)),
    [credits, selectedCreditId],
  )

  const loadData = async () => {
    const [clientsData, productsData, creditsData] = await Promise.all([
      api.clients(),
      api.creditProducts(),
      api.credits(),
    ])
    setClients(clientsData)
    setProducts(productsData)
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

  const submitCredit = async (event) => {
    event.preventDefault()
    setError('')
    setMessage('')

    try {
      const credit = await api.createCredit({
        ...creditForm,
        clientId: Number(creditForm.clientId),
        creditProductId: Number(creditForm.creditProductId),
        principalAmount: Number(creditForm.principalAmount),
        termMonths: Number(creditForm.termMonths),
        startDate: creditForm.startDate || null,
      })
      setCreditForm(emptyCredit)
      await loadData()
      setSelectedCreditId(String(credit.id))
      setMessage('Кредитът е отпуснат и погасителният план е генериран.')
    } catch (err) {
      setError(err.message)
    }
  }

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
          <h2>Кредити и плащания</h2>
          <p>Модул на Адриан: кредити, анюитетен план и плащания.</p>
        </div>
      </div>

      <StatusMessage message={message} />
      <StatusMessage message={error} type="error" />

      <div className="two-column">
        <div className="content-panel">
          <h3>Отпускане на кредит</h3>
          <form onSubmit={submitCredit}>
            <label className="form-label">Клиент</label>
            <select
              className="form-select mb-3"
              value={creditForm.clientId}
              onChange={(event) =>
                setCreditForm((current) => ({ ...current, clientId: event.target.value }))
              }
              required
            >
              <option value="">Изберете клиент</option>
              {clients.map((client) => (
                <option key={client.id} value={client.id}>
                  {client.displayName} ({clientTypeLabel(client.clientType)})
                </option>
              ))}
            </select>

            <label className="form-label">Вид кредит</label>
            <select
              className="form-select mb-3"
              value={creditForm.creditProductId}
              onChange={(event) =>
                setCreditForm((current) => ({ ...current, creditProductId: event.target.value }))
              }
              required
            >
              <option value="">Изберете вид</option>
              {products.map((product) => (
                <option key={product.id} value={product.id}>
                  {creditTypeLabel(product.code)}
                </option>
              ))}
            </select>

            {selectedProduct && (
              <div className="limits-box">
                Лихва: {selectedProduct.annualInterestRate}% | Макс. сума:{' '}
                {formatMoney(selectedProduct.maxAmount)} | Макс. срок:{' '}
                {selectedProduct.maxTermMonths} месеца
              </div>
            )}

            <label className="form-label">Отпусната сума</label>
            <input
              className="form-control mb-3"
              type="number"
              min="0.01"
              step="0.01"
              value={creditForm.principalAmount}
              onChange={(event) =>
                setCreditForm((current) => ({ ...current, principalAmount: event.target.value }))
              }
              required
            />

            <label className="form-label">Срок (месеци)</label>
            <input
              className="form-control mb-3"
              type="number"
              min="1"
              value={creditForm.termMonths}
              onChange={(event) =>
                setCreditForm((current) => ({ ...current, termMonths: event.target.value }))
              }
              required
            />

            <label className="form-label">Начална дата</label>
            <input
              className="form-control mb-3"
              type="date"
              min={minStartDate}
              value={creditForm.startDate}
              onChange={(event) =>
                setCreditForm((current) => ({ ...current, startDate: event.target.value }))
              }
            />

            <button className="btn btn-primary w-100" type="submit">
              Отпусни кредит
            </button>
          </form>
        </div>

        <div className="content-panel">
          <h3>Кредитни продукти</h3>
          <div className="table-responsive">
            <table className="table table-sm align-middle">
              <thead>
                <tr>
                  <th>Вид</th>
                  <th>Лихва</th>
                  <th>Макс. сума</th>
                  <th>Макс. срок</th>
                </tr>
              </thead>
              <tbody>
                {products.map((product) => (
                  <tr key={product.id}>
                    <td>{creditTypeLabel(product.code)}</td>
                    <td>{product.annualInterestRate}%</td>
                    <td>{formatMoney(product.maxAmount)}</td>
                    <td>{product.maxTermMonths}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div className="content-panel">
        <h3>Кредити</h3>
        <div className="table-responsive">
          <table className="table table-hover align-middle">
            <thead>
              <tr>
                <th>ID</th>
                <th>Клиент</th>
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
                  <td>{credit.clientDisplayName}</td>
                  <td>{creditTypeLabel(credit.creditProductCode)}</td>
                  <td>{formatMoney(credit.principalAmount)}</td>
                  <td>{credit.termMonths}</td>
                  <td>{formatMoney(credit.monthlyPayment)}</td>
                  <td>
                    <span className={`status-chip ${statusClass(credit.status)}`}>
                      {creditStatusLabel(credit.status)}
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
                  <td colSpan="8" className="empty-cell">
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
                ? `Кредит #${selectedCredit.id} на ${selectedCredit.clientDisplayName}`
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
              #{credit.id} - {credit.clientDisplayName} - {formatMoney(credit.principalAmount)}
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
                      {installmentStatusLabel(installment.status)}
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
