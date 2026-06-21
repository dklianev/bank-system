import { useEffect, useMemo, useState } from 'react'
import { api } from '../../api'
import StatusMessage from '../../components/StatusMessage'
import { formatDate, formatMoney, statusClass } from '../../utils'

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
      setMessage('Credit was granted and repayment plan was generated.')
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
      setMessage('Installment was marked as paid.')
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <section>
      <div className="section-title">
        <div>
          <h2>Credits and payments</h2>
          <p>Модул на Адриан: кредити, анюитетен план и плащания.</p>
        </div>
      </div>

      <StatusMessage message={message} />
      <StatusMessage message={error} type="error" />

      <div className="two-column">
        <div className="content-panel">
          <h3>Grant credit</h3>
          <form onSubmit={submitCredit}>
            <label className="form-label">Client</label>
            <select
              className="form-select mb-3"
              value={creditForm.clientId}
              onChange={(event) =>
                setCreditForm((current) => ({ ...current, clientId: event.target.value }))
              }
              required
            >
              <option value="">Choose client</option>
              {clients.map((client) => (
                <option key={client.id} value={client.id}>
                  {client.displayName} ({client.clientType})
                </option>
              ))}
            </select>

            <label className="form-label">Credit type</label>
            <select
              className="form-select mb-3"
              value={creditForm.creditProductId}
              onChange={(event) =>
                setCreditForm((current) => ({ ...current, creditProductId: event.target.value }))
              }
              required
            >
              <option value="">Choose type</option>
              {products.map((product) => (
                <option key={product.id} value={product.id}>
                  {product.name}
                </option>
              ))}
            </select>

            {selectedProduct && (
              <div className="limits-box">
                Rate: {selectedProduct.annualInterestRate}% | Max amount:{' '}
                {formatMoney(selectedProduct.maxAmount)} | Max term:{' '}
                {selectedProduct.maxTermMonths} months
              </div>
            )}

            <label className="form-label">Principal amount</label>
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

            <label className="form-label">Term in months</label>
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

            <label className="form-label">Start date</label>
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
              Grant credit
            </button>
          </form>
        </div>

        <div className="content-panel">
          <h3>Credit products</h3>
          <div className="table-responsive">
            <table className="table table-sm align-middle">
              <thead>
                <tr>
                  <th>Type</th>
                  <th>Rate</th>
                  <th>Max amount</th>
                  <th>Max term</th>
                </tr>
              </thead>
              <tbody>
                {products.map((product) => (
                  <tr key={product.id}>
                    <td>{product.name}</td>
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
        <h3>Credits</h3>
        <div className="table-responsive">
          <table className="table table-hover align-middle">
            <thead>
              <tr>
                <th>ID</th>
                <th>Client</th>
                <th>Type</th>
                <th>Amount</th>
                <th>Term</th>
                <th>Monthly payment</th>
                <th>Status</th>
                <th>Plan</th>
              </tr>
            </thead>
            <tbody>
              {credits.map((credit) => (
                <tr key={credit.id}>
                  <td>{credit.id}</td>
                  <td>{credit.clientDisplayName}</td>
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
                      Open
                    </button>
                  </td>
                </tr>
              ))}
              {credits.length === 0 && (
                <tr>
                  <td colSpan="8" className="empty-cell">
                    No credits yet.
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
            <h3>Repayment plan</h3>
            <p>
              {selectedCredit
                ? `Credit #${selectedCredit.id} for ${selectedCredit.clientDisplayName}`
                : 'Choose a credit to see the plan.'}
            </p>
          </div>
        </div>

        <select
          className="form-select mb-3"
          value={selectedCreditId}
          onChange={(event) => setSelectedCreditId(event.target.value)}
        >
          <option value="">Choose credit</option>
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
                <th>No.</th>
                <th>Due date</th>
                <th>Payment</th>
                <th>Principal</th>
                <th>Interest</th>
                <th>Remaining</th>
                <th>Status</th>
                <th>Action</th>
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
                      Pay
                    </button>
                  </td>
                </tr>
              ))}
              {installments.length === 0 && (
                <tr>
                  <td colSpan="8" className="empty-cell">
                    No repayment plan selected.
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
