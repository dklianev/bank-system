import { useEffect, useState } from 'react'
import { api } from '../../api'
import StatusMessage from '../../components/StatusMessage'
import { formatMoney, statusClass } from '../../utils'

const emptyIndividual = {
  firstName: '',
  lastName: '',
  egn: '',
}

const emptyLegal = {
  companyName: '',
  eik: '',
  representativeFirstName: '',
  representativeLastName: '',
}

const emptyAccount = {
  clientId: '',
  iban: '',
  initialBalance: '0.00',
}

const emptyUser = {
  clientId: '',
  username: '',
  password: '',
}

export default function ClientsAccountsPage() {
  const [clients, setClients] = useState([])
  const [accounts, setAccounts] = useState([])
  const [clientType, setClientType] = useState('INDIVIDUAL')
  const [individualForm, setIndividualForm] = useState(emptyIndividual)
  const [legalForm, setLegalForm] = useState(emptyLegal)
  const [accountForm, setAccountForm] = useState(emptyAccount)
  const [userForm, setUserForm] = useState(emptyUser)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const loadData = async () => {
    const [clientsData, accountsData] = await Promise.all([api.clients(), api.accounts()])
    setClients(clientsData)
    setAccounts(accountsData)
  }

  useEffect(() => {
    loadData().catch((err) => setError(err.message))
  }, [])

  const submitClient = async (event) => {
    event.preventDefault()
    setError('')
    setMessage('')

    try {
      if (clientType === 'INDIVIDUAL') {
        await api.createIndividualClient(individualForm)
        setIndividualForm(emptyIndividual)
      } else {
        await api.createLegalClient(legalForm)
        setLegalForm(emptyLegal)
      }

      await loadData()
      setMessage('Клиентът е записан успешно.')
    } catch (err) {
      setError(err.message)
    }
  }

  const submitAccount = async (event) => {
    event.preventDefault()
    setError('')
    setMessage('')

    try {
      await api.createAccount({
        ...accountForm,
        clientId: Number(accountForm.clientId),
        initialBalance: Number(accountForm.initialBalance),
      })
      setAccountForm(emptyAccount)
      await loadData()
      setMessage('Сметката е открита успешно.')
    } catch (err) {
      setError(err.message)
    }
  }

  const closeAccount = async (id) => {
    setError('')
    setMessage('')

    try {
      await api.closeAccount(id)
      await loadData()
      setMessage('Сметката е закрита.')
    } catch (err) {
      setError(err.message)
    }
  }

  const submitUser = async (event) => {
    event.preventDefault()
    setError('')
    setMessage('')

    try {
      const created = await api.createUser({
        ...userForm,
        clientId: Number(userForm.clientId),
      })
      setUserForm(emptyUser)
      setMessage(`Достъпът „${created.username}" е създаден за ${created.clientDisplayName}.`)
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <section>
      <div className="section-title">
        <div>
          <h2>Клиенти и сметки</h2>
          <p>Модул на Димитър: клиенти, банкови сметки и основа на базата.</p>
        </div>
      </div>

      <StatusMessage message={message} />
      <StatusMessage message={error} type="error" />

      <div className="two-column">
        <div className="content-panel">
          <h3>Добавяне на клиент</h3>
          <form onSubmit={submitClient}>
            <label className="form-label">Вид клиент</label>
            <select
              className="form-select mb-3"
              value={clientType}
              onChange={(event) => setClientType(event.target.value)}
            >
              <option value="INDIVIDUAL">Физическо лице</option>
              <option value="LEGAL">Юридическо лице</option>
            </select>

            {clientType === 'INDIVIDUAL' ? (
              <IndividualClientForm form={individualForm} setForm={setIndividualForm} />
            ) : (
              <LegalClientForm form={legalForm} setForm={setLegalForm} />
            )}

            <button className="btn btn-primary w-100" type="submit">
              Запази клиент
            </button>
          </form>
        </div>

        <div className="content-panel">
          <h3>Откриване на сметка</h3>
          <form onSubmit={submitAccount}>
            <label className="form-label">Клиент</label>
            <select
              className="form-select mb-3"
              value={accountForm.clientId}
              onChange={(event) =>
                setAccountForm((current) => ({ ...current, clientId: event.target.value }))
              }
              required
            >
              <option value="">Изберете клиент</option>
              {clients.map((client) => (
                <option key={client.id} value={client.id}>
                  {client.displayName} ({client.clientType})
                </option>
              ))}
            </select>

            <label className="form-label">IBAN</label>
            <input
              className="form-control mb-3"
              placeholder="BG12BANK123456ABCDEFGH"
              maxLength="22"
              value={accountForm.iban}
              onChange={(event) =>
                setAccountForm((current) => ({
                  ...current,
                  iban: event.target.value.toUpperCase(),
                }))
              }
              required
            />

            <label className="form-label">Начална наличност</label>
            <input
              className="form-control mb-3"
              type="number"
              min="0"
              step="0.01"
              value={accountForm.initialBalance}
              onChange={(event) =>
                setAccountForm((current) => ({ ...current, initialBalance: event.target.value }))
              }
              required
            />

            <button className="btn btn-success w-100" type="submit">
              Открий сметка
            </button>
          </form>
        </div>
      </div>

      <div className="content-panel">
        <h3>Клиенти</h3>
        <div className="table-responsive">
          <table className="table table-hover align-middle">
            <thead>
              <tr>
                <th>ID</th>
                <th>Вид</th>
                <th>Име</th>
                <th>ЕГН / ЕИК</th>
                <th>Представител</th>
                <th>Сметки</th>
                <th>Кредити</th>
              </tr>
            </thead>
            <tbody>
              {clients.map((client) => (
                <tr key={client.id}>
                  <td>{client.id}</td>
                  <td>{client.clientType}</td>
                  <td>{client.displayName}</td>
                  <td>{client.identifier}</td>
                  <td>{client.representativeName ?? '-'}</td>
                  <td>{client.accountsCount}</td>
                  <td>{client.creditsCount}</td>
                </tr>
              ))}
              {clients.length === 0 && (
                <tr>
                  <td colSpan="7" className="empty-cell">
                    Все още няма клиенти.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      <div className="content-panel">
        <h3>Банкови сметки</h3>
        <div className="table-responsive">
          <table className="table table-hover align-middle">
            <thead>
              <tr>
                <th>ID</th>
                <th>IBAN</th>
                <th>Клиент</th>
                <th>Наличност</th>
                <th>Статус</th>
                <th>Действие</th>
              </tr>
            </thead>
            <tbody>
              {accounts.map((account) => (
                <tr key={account.id}>
                  <td>{account.id}</td>
                  <td>{account.iban}</td>
                  <td>{account.clientDisplayName}</td>
                  <td>{formatMoney(account.balance)}</td>
                  <td>
                    <span className={`status-chip ${statusClass(account.status)}`}>
                      {account.status}
                    </span>
                  </td>
                  <td>
                    <button
                      className="btn btn-sm btn-outline-secondary"
                      type="button"
                      disabled={account.status === 'CLOSED'}
                      onClick={() => closeAccount(account.id)}
                    >
                      Закрий
                    </button>
                  </td>
                </tr>
              ))}
              {accounts.length === 0 && (
                <tr>
                  <td colSpan="6" className="empty-cell">
                    Все още няма сметки.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      <div className="content-panel">
        <h3>Създаване на достъп за клиент</h3>
        <p className="login-hint">
          Дава на клиента достъп до собствените му сметки и кредити през клиентския портал.
        </p>
        <form onSubmit={submitUser} className="two-column">
          <div>
            <label className="form-label">Клиент</label>
            <select
              className="form-select mb-3"
              value={userForm.clientId}
              onChange={(event) =>
                setUserForm((current) => ({ ...current, clientId: event.target.value }))
              }
              required
            >
              <option value="">Изберете клиент</option>
              {clients.map((client) => (
                <option key={client.id} value={client.id}>
                  {client.displayName} ({client.clientType})
                </option>
              ))}
            </select>

            <label className="form-label">Потребителско име</label>
            <input
              className="form-control mb-3"
              minLength="3"
              maxLength="40"
              value={userForm.username}
              onChange={(event) =>
                setUserForm((current) => ({ ...current, username: event.target.value }))
              }
              required
            />
          </div>

          <div>
            <label className="form-label">Парола</label>
            <input
              className="form-control mb-3"
              type="password"
              minLength="6"
              maxLength="72"
              value={userForm.password}
              onChange={(event) =>
                setUserForm((current) => ({ ...current, password: event.target.value }))
              }
              required
            />

            <button className="btn btn-primary w-100 mt-1" type="submit">
              Създай достъп
            </button>
          </div>
        </form>
      </div>
    </section>
  )
}

function IndividualClientForm({ form, setForm }) {
  return (
    <>
      <label className="form-label">Име</label>
      <input
        className="form-control mb-3"
        value={form.firstName}
        onChange={(event) => setForm((current) => ({ ...current, firstName: event.target.value }))}
        required
      />
      <label className="form-label">Фамилия</label>
      <input
        className="form-control mb-3"
        value={form.lastName}
        onChange={(event) => setForm((current) => ({ ...current, lastName: event.target.value }))}
        required
      />
      <label className="form-label">ЕГН</label>
      <input
        className="form-control mb-3"
        value={form.egn}
        onChange={(event) => setForm((current) => ({ ...current, egn: event.target.value }))}
        required
      />
    </>
  )
}

function LegalClientForm({ form, setForm }) {
  return (
    <>
      <label className="form-label">Име на фирмата</label>
      <input
        className="form-control mb-3"
        value={form.companyName}
        onChange={(event) =>
          setForm((current) => ({ ...current, companyName: event.target.value }))
        }
        required
      />
      <label className="form-label">ЕИК</label>
      <input
        className="form-control mb-3"
        value={form.eik}
        onChange={(event) => setForm((current) => ({ ...current, eik: event.target.value }))}
        required
      />
      <label className="form-label">Име на представител</label>
      <input
        className="form-control mb-3"
        value={form.representativeFirstName}
        onChange={(event) =>
          setForm((current) => ({ ...current, representativeFirstName: event.target.value }))
        }
        required
      />
      <label className="form-label">Фамилия на представител</label>
      <input
        className="form-control mb-3"
        value={form.representativeLastName}
        onChange={(event) =>
          setForm((current) => ({ ...current, representativeLastName: event.target.value }))
        }
        required
      />
    </>
  )
}
