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
