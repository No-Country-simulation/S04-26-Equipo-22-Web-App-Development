import "./Auth.css";
import ChannelIcon from "../../components/ChannelIcon";

export default function AuthLayout({ children }) {
  return (
    <div className="auth-shell">
      <aside className="auth-shell__brand">
        <div className="auth-shell__grid" aria-hidden />
        <div className="auth-shell__glow auth-shell__glow--top" aria-hidden />
        <div className="auth-shell__glow auth-shell__glow--bottom" aria-hidden />

        <div className="auth-shell__top">
          <div className="auth-shell__logo">
            <span className="auth-shell__logo-dot" />
            TalentCircle
          </div>
          <span className="auth-shell__badge">Panel editorial</span>
        </div>

        <div className="auth-shell__pitch">
          <h2>
            Convierte la actividad de tu comunidad
            <span className="auth-shell__pitch-highlight"> en contenido listo para publicar.</span>
          </h2>
          <p>
            Cada viernes la IA analiza posts, preguntas y recursos.
            El lunes revisás borradores diferenciados para newsletter, LinkedIn y X.
          </p>
        </div>

        <div className="auth-shell__mockup" aria-hidden>
          <article className="mock-card mock-card--newsletter">
            <div className="mock-card__channel">
              <span className="mock-card__channel-icon mock-card__channel-icon--newsletter"><ChannelIcon channel="newsletter" size={14} /></span>
              Newsletter
            </div>
            <strong className="mock-card__title">Lo mejor de la comunidad</strong>
            <p className="mock-card__body">
              Esta semana la comunidad puso el foco en performance en React 19 y
              aplicaciones prácticas de IA al contenido editorial…
            </p>
          </article>

          <article className="mock-card mock-card--linkedin">
            <div className="mock-card__channel">
              <span className="mock-card__channel-icon mock-card__channel-icon--linkedin"><ChannelIcon channel="linkedin" size={14} /></span>
              LinkedIn
            </div>
            <strong className="mock-card__title">Resumen semanal</strong>
            <p className="mock-card__body">
              Esta semana en nuestra comunidad surgieron conversaciones muy potentes
              sobre arquitectura, performance e IA aplicada…
            </p>
          </article>

          <article className="mock-card mock-card--twitter">
            <div className="mock-card__channel">
              <span className="mock-card__channel-icon mock-card__channel-icon--twitter"><ChannelIcon channel="twitter" size={13} /></span>
              X
            </div>
            <p className="mock-card__body">
              🔥 Lo mejor de la semana en la comunidad: React 19, IA aplicada,
              testing en Spring Boot · #webdev
            </p>
          </article>
        </div>

        <div className="auth-shell__stats">
          <div>
            <strong>3</strong>
            <span>canales</span>
          </div>
          <div>
            <strong>IA</strong>
            <span>resúmenes</span>
          </div>
          <div>
            <strong>1 clic</strong>
            <span>aprobar</span>
          </div>
        </div>
      </aside>

      <main className="auth-shell__form-panel">
        <div className="auth-form">{children}</div>
      </main>
    </div>
  );
}
