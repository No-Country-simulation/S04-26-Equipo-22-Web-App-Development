import { Link } from "react-router-dom";
import { FileText, CircleCheck, Eye, Users, ArrowRight } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import "./HomePage.css";

const SECTIONS = [
  {
    to: "/drafts",
    title: "Borradores",
    description: "Revisa los borradores semanales generados por IA.",
    icon: FileText,
    accent: "blue",
  },
  {
    to: "/approval",
    title: "Aprobación",
    description: "Aprueba o rechaza el contenido antes de su publicación.",
    icon: CircleCheck,
    accent: "emerald",
  },
  {
    to: "/preview",
    title: "Preview por canal",
    description: "Visualiza el contenido como se verá en cada plataforma.",
    icon: Eye,
    accent: "violet",
  },
  {
    to: "/communities",
    title: "Comunidades",
    description: "Administra las comunidades monitoreadas.",
    icon: Users,
    accent: "amber",
  },
];

export default function HomePage() {
  const { user } = useAuth();
  const firstName = user?.email ? user.email.split("@")[0] : "";

  return (
    <div className="home-page">
      <header className="home-page__hero">
        <span className="home-page__eyebrow">
          <span className="home-page__eyebrow-dot" />
          Panel semanal
        </span>
        <h1>
          Bienvenido{firstName ? ", " : ""}
          {firstName && <span className="home-page__hero-accent">{firstName}</span>}
        </h1>
        <p>
          Tu centro de revisión de contenido en TalentCircle. Edita, aprueba y
          publica lo que la IA preparó esta semana para tus comunidades.
        </p>
      </header>

      <div className="home-page__grid">
        {SECTIONS.map((s, i) => {
          const Icon = s.icon;
          return (
            <Link
              key={s.to}
              to={s.to}
              className={`home-page__card home-page__card--${s.accent}`}
              style={{ animationDelay: `${i * 60}ms` }}
            >
              <span className="home-page__card-icon" aria-hidden="true">
                <Icon />
              </span>
              <h2>{s.title}</h2>
              <p>{s.description}</p>
              <span className="home-page__card-arrow" aria-hidden="true">
                <ArrowRight />
              </span>
            </Link>
          );
        })}
      </div>
    </div>
  );
}
