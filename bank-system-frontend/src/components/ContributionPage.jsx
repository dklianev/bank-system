export default function ContributionPage() {
  return (
    <section>
      <div className="section-title">
        <div>
          <h2>Team contribution</h2>
          <p>Разпределение на работата за документацията и защитата.</p>
        </div>
      </div>

      <div className="two-column">
        <div className="content-panel contribution-panel">
          <h3>Димитър Николаев Клянев - F112194</h3>
          <ul>
            <li>Моделиране на клиентите: физически и юридически лица.</li>
            <li>Банкови сметки, IBAN, баланс и статус.</li>
            <li>Основна структура на базата и връзки client-account.</li>
            <li>Backend и frontend за клиенти и сметки.</li>
          </ul>
        </div>

        <div className="content-panel contribution-panel">
          <h3>Адриан Роберт Витиг - F112519</h3>
          <ul>
            <li>Кредитни продукти: потребителски и ипотечен кредит.</li>
            <li>Отпускане на кредит с лимити за сума и срок.</li>
            <li>Анюитетен погасителен план и плащания.</li>
            <li>Backend и frontend за кредити и вноски.</li>
          </ul>
        </div>
      </div>

      <div className="content-panel">
        <h3>Shared work</h3>
        <p className="mb-0">
          И двамата участват в интеграцията, тестовете, документацията и демо сценария.
          Разделението е по банкови модули, за да има ясен индивидуален принос.
        </p>
      </div>
    </section>
  )
}
