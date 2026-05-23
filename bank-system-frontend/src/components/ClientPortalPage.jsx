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
      setMessage('Installment was marked as paid.')
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <section>
      <div className="section-title">
        <div>
          <h2>My banking</h2>
          <p>Преглед на собствените сметки, кредити и погасителен план.</p>
        </div>
      </div>

      <StatusMessage message={message} />
      <StatusMessage message={error} type="error" />

      <div className="content-panel">
        <h3>My accounts</h3>
        <div className="table-responsive">
          <table className="table table-sm align-middle">
            <thead>
              <tr>
                <th>IBAN</th>
                <th>Balance</th>
                <th>Status</th>
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
                    No accounts yet.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      <div className="content-panel">
        <h3>My credits</h3>
        <div className="table-responsive">
          <table className="table table-hover align-middle">
            <thead>
              <tr>
                <th>ID</th>
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
                  <td colSpan="7" className="empty-cell">
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
                ? `Credit #${selectedCredit.id} — ${selectedCredit.creditProductName}`
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
              #{credit.id} - {credit.creditProductName} - {formatMoney(credit.principalAmount)}
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
