const API_BASE = '/api'

let unauthorizedHandler = null

export function setUnauthorizedHandler(handler) {
  unauthorizedHandler = handler
}

async function request(path, options = {}) {
  let response

  try {
    response = await fetch(`${API_BASE}${path}`, {
      headers: {
        'Content-Type': 'application/json',
        ...(options.headers ?? {}),
      },
      ...options,
    })
  } catch {
    throw new Error('Backend-ът не е достъпен. Стартирайте Spring Boot приложението на localhost:8080.')
  }

  if (response.status === 401) {
    if (unauthorizedHandler) {
      unauthorizedHandler()
    }
    const error = new Error('Сесията изтече. Влезте отново.')
    error.unauthorized = true
    throw error
  }

  if (!response.ok) {
    const error = await response.json().catch(() => null)
    const message =
      error?.errors?.join(', ') ??
      'Backend-ът не е достъпен. Стартирайте Spring Boot приложението на localhost:8080.'
    throw new Error(message)
  }

  if (response.status === 204) {
    return null
  }

  return response.json()
}

export const api = {
  session: () => request('/session'),
  login: async (username, password) => {
    let response

    try {
      response = await fetch(`${API_BASE}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({ username, password }),
      })
    } catch {
      throw new Error('Backend-ът не е достъпен. Стартирайте Spring Boot приложението на localhost:8080.')
    }

    if (response.status === 401) {
      throw new Error('Грешно потребителско име или парола.')
    }

    if (!response.ok) {
      throw new Error('Входът е неуспешен. Опитайте отново.')
    }

    return response.json()
  },
  logout: () => request('/logout', { method: 'POST' }),
  createUser: (payload) =>
    request('/users', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
  dashboard: () => request('/dashboard'),
  clients: () => request('/clients'),
  createIndividualClient: (payload) =>
    request('/clients/individual', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
  createLegalClient: (payload) =>
    request('/clients/legal', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
  accounts: () => request('/accounts'),
  createAccount: (payload) =>
    request('/accounts', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
  closeAccount: (id) =>
    request(`/accounts/${id}/close`, {
      method: 'PATCH',
    }),
  creditProducts: () => request('/credits/products'),
  credits: () => request('/credits'),
  createCredit: (payload) =>
    request('/credits', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
  installments: (creditId) => request(`/credits/${creditId}/installments`),
  payInstallment: (installmentId) =>
    request(`/credits/installments/${installmentId}/pay`, {
      method: 'PATCH',
    }),
}
