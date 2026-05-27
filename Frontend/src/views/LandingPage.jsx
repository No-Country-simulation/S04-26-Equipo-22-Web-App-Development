import { Link } from "react-router-dom";
import { Sparkles, FileText, CheckCircle2, Download, ArrowRight, Users, BarChart3 } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import "./LandingPage.css";

const STEPS = [
  {
    icon: BarChart3,
    title: "Recolecta actividad",
    desc: "El sistema monitorea las comunidades y detecta los posts, preguntas y recursos más relevantes de la semana.",
  },
  {
    icon: Sparkles,
    title: "La IA genera borradores",
    desc: "Un LLM analiza las contribuciones más valiosas y genera contenido diferenciado para Newsletter, LinkedIn y X.",
  },
  {
    icon: FileText,
    title: "El editor revisa y edita",
    desc: "Abrí el panel de revisión, editá cada borrador por canal, y aprobá cuando esté listo.",
  },
  {
    icon: Download,
    title: "Publicá o exportá",
    desc: "Exportá en TXT, Markdown o JSON, o copiá al portapapeles para publicar en cada plataforma.",
  },
];

const FEATURES = [
  { icon: Sparkles, label: "IA con Gemini", desc: "Genera resúmenes y borradores adaptados a cada canal." },
  { icon: Users, label: "Multi-comunidad", desc: "Monitorea múltiples comunidades en Discord, Slack, Telegram." },
  { icon: CheckCircle2, label: "Flujo editorial", desc: "Revisión, edición y aprobación antes de publicar." },
];

export default function LandingPage() {
  const { isAuthenticated } = useAuth();

  return (
    <div className="landing">
      <nav className="landing__nav">
        <span className="landing__logo">TalentCircle</span>
        <div className="landing__nav-links">
          {isAuthenticated ? (
            <Link to="/dashboard" className="landing__nav-cta">Ir al panel →</Link>
          ) : (
            <>
              <Link to="/login" className="landing__nav-link">Iniciar sesión</Link>
              <Link to="/register" className="landing__nav-cta">Registrarse</Link>
            </>
          )}
        </div>
      </nav>

      <header className="landing__hero">
        <span className="landing__badge">
          <Sparkles size={14} /> Pipeline de contenido con IA
        </span>
        <h1>Transforma la actividad de tu comunidad en contenido listo para publicar</h1>
        <p>
          TalentCircle monitorea tus comunidades técnicas, identifica las contribuciones más
          relevantes con IA, y genera borradores diferenciados para Newsletter, LinkedIn y X
          — listos para tu revisión.
        </p>
        <div className="landing__hero-actions">
          {isAuthenticated ? (
            <Link to="/dashboard" className="landing__btn-primary">Ir al panel</Link>
          ) : (
            <>
              <Link to="/register" className="landing__btn-primary">Empezar gratis</Link>
              <Link to="/login" className="landing__btn-secondary">Ya tengo cuenta</Link>
            </>
          )}
        </div>
      </header>

      <section className="landing__how">
        <h2>Cómo funciona</h2>
        <div className="landing__steps">
          {STEPS.map((step, i) => (
            <div className="landing__step" key={i}>
              <div className="landing__step-number">{i + 1}</div>
              <div className="landing__step-icon"><step.icon size={22} /></div>
              <h3>{step.title}</h3>
              <p>{step.desc}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="landing__features">
        {FEATURES.map((f, i) => (
          <div className="landing__feature" key={i}>
            <span className="landing__feature-icon"><f.icon size={20} /></span>
            <h3>{f.label}</h3>
            <p>{f.desc}</p>
          </div>
        ))}
      </section>

      <section className="landing__cta-section">
        <h2>Dejá de compilar contenido a mano</h2>
        <p>Automatizá el proceso y publicá contenido de calidad cada semana.</p>
        {isAuthenticated ? (
          <Link to="/dashboard" className="landing__btn-primary">Ir al panel <ArrowRight size={16} /></Link>
        ) : (
          <Link to="/register" className="landing__btn-primary">Crear cuenta <ArrowRight size={16} /></Link>
        )}
      </section>

      <footer className="landing__footer">
        <span>TalentCircle © {new Date().getFullYear()}</span>
      </footer>
    </div>
  );
}
