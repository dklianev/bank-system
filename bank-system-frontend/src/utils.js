export function formatMoney(value) {
  if (value === null || value === undefined || value === '') {
    return '0.00 EUR'
  }

  return `${Number(value).toFixed(2)} EUR`
}

export function formatDate(value) {
  if (!value) {
    return '-'
  }

  return new Intl.DateTimeFormat('bg-BG').format(new Date(value))
}

export function statusClass(status) {
  if (status === 'ACTIVE' || status === 'PAID') {
    return 'status-positive'
  }

  if (status === 'PAID_OFF') {
    return 'status-neutral'
  }

  return 'status-muted'
}

export function accountStatusLabel(status) {
  if (status === 'ACTIVE') return 'Активна'
  if (status === 'CLOSED') return 'Закрита'
  return status
}

export function creditStatusLabel(status) {
  if (status === 'ACTIVE') return 'Активен'
  if (status === 'PAID_OFF') return 'Изплатен'
  return status
}

export function installmentStatusLabel(status) {
  if (status === 'PENDING') return 'Предстояща'
  if (status === 'PAID') return 'Платена'
  return status
}

export function clientTypeLabel(type) {
  if (type === 'INDIVIDUAL') return 'Физическо лице'
  if (type === 'LEGAL') return 'Юридическо лице'
  return type
}

export function creditTypeLabel(code) {
  if (code === 'CONSUMER') return 'Потребителски'
  if (code === 'MORTGAGE') return 'Ипотечен'
  return code
}
