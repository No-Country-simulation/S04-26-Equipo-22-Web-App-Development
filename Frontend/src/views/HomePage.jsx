import { useMemo, useRef } from "react";
import { Link } from "react-router-dom";
import {
  FileText,
  CircleCheck,
  Eye,
  Users,
  ArrowRight,
  Calendar,
  Clock,
  Activity,
  Sparkles,
  AlertCircle,
} from "lucide-react";
import gsap from "gsap";
import { useGSAP } from "@gsap/react";
import { useAuth } from "../context/AuthContext";
import { useFetch } from "../hooks/useFetch";
import * as draftsApi from "../api/drafts";
import * as communitiesApi from "../api/communities";
import GenerateWithAI from "../components/GenerateWithAI";
import "./HomePage.css";

const PENDING_STATUSES = new Set(["GENERATED", "IN_REVIEW"]);
const DATE_FORMATTER = new Intl.DateTimeFormat("es-AR", {
  weekday: "long",
  day: "numeric",
  month: "long",
});

function formatFridayAt18(date) {
  if (!date) return "—";
  const formatted = DATE_FORMATTER.format(date);
  // Intl with es-AR yields strings like "viernes, 8 de mayo" — normalize and append hour
  return `${formatted}, 18:00`;
}

function getNextFridayAt18(now = new Date()) {
  const result = new Date(now);
  const day = result.getDay(); // 0=Sun ... 5=Fri
  if (day === 5) {
    // If it's Friday and we haven't hit 18:00 yet, today counts.
    const todayAt18 = new Date(result);
    todayAt18.setHours(18, 0, 0, 0);
    if (now <= todayAt18) {
      return todayAt18;
    }
  }
  const daysUntilFriday = (5 - day + 7) % 7 || 7;
  result.setDate(result.getDate() + daysUntilFriday);
  result.setHours(18, 0, 0, 0);
  return result;
}

function getLastFridayFromWeekOf(weekOf) {
  if (!weekOf) return null;
  // weekOf is a Monday (e.g. "2026-05-08") in this product context — but the brief says
  // the last Friday of generation, so we treat the date as the Friday EOD when drafts were generated.
  // The mock uses Friday-ish dates ("2026-05-08" is a Friday in 2026), so parse directly.
  const [y, m, d] = weekOf.split("-").map(Number);
  if (!y || !m || !d) return null;
  const dt = new Date(y, m - 1, d);
  dt.setHours(18, 0, 0, 0);
  return dt;
}

const SECTIONS = [
  {
    to: "/drafts",
    title: "Borradores",
    description: "Revisa los borradores semanales generados por IA.",
    icon: FileText,
    accent: "blue",
    badgeKey: "totalDrafts",
  },
  {
    to: "/approval",
    title: "Aprobación",
    description: "Aprueba o rechaza el contenido antes de su publicación.",
    icon: CircleCheck,
    accent: "emerald",
    badgeKey: "pendingDrafts",
  },
  {
    to: "/preview",
    title: "Preview por canal",
    description: "Visualiza el contenido como se verá en cada plataforma.",
    icon: Eye,
    accent: "violet",
    badgeKey: "channels",
  },
  {
    to: "/communities",
    title: "Comunidades",
    description: "Administra las comunidades monitoreadas.",
    icon: Users,
    accent: "amber",
    badgeKey: "activeCommunities",
  },
];

export default function HomePage() {
  const { user } = useAuth();
  const firstName = user?.email ? user.email.split("@")[0] : "";
  const containerRef = useRef(null);

  const { data, loading, error } = useFetch(async () => {
    const [draftsRes, communitiesRes] = await Promise.allSettled([
      draftsApi.listDrafts(),
      communitiesApi.listCommunities({ onlyActive: true }),
    ]);

    const drafts = draftsRes.status === "fulfilled" ? (Array.isArray(draftsRes.value) ? draftsRes.value : []) : [];
    const communities = communitiesRes.status === "fulfilled" ? (Array.isArray(communitiesRes.value) ? communitiesRes.value : []) : [];

    if (draftsRes.status === "rejected" && communitiesRes.status === "rejected") {
      throw new Error("No pudimos cargar la información del pipeline.");
    }

    return { drafts, communities, partialError: draftsRes.status === "rejected" || communitiesRes.status === "rejected" };
  });

  const drafts = data?.drafts ?? [];
  const communities = data?.communities ?? [];

  const metrics = useMemo(() => {
    const sortedByWeek = [...drafts].sort((a, b) =>
      (b.weekOf || "").localeCompare(a.weekOf || "")
    );
    const lastWeekOf = sortedByWeek[0]?.weekOf || null;
    const lastWeekDrafts = lastWeekOf
      ? drafts.filter((d) => d.weekOf === lastWeekOf)
      : [];

    const postsAnalyzed = lastWeekDrafts.reduce(
      (acc, d) => acc + (Array.isArray(d.sourceContributions) ? d.sourceContributions.length : 0),
      0
    );

    const pendingDrafts = drafts.filter((d) => PENDING_STATUSES.has(d.status)).length;

    return {
      lastFriday: getLastFridayFromWeekOf(lastWeekOf),
      nextFriday: getNextFridayAt18(),
      totalDrafts: drafts.length,
      pendingDrafts,
      activeCommunities: communities.length,
      postsAnalyzed,
      topicsGenerated: lastWeekDrafts.length,
      channels: 3,
      lastWeekOf,
    };
  }, [drafts, communities]);

  const badgeMap = {
    totalDrafts: metrics.totalDrafts,
    pendingDrafts: metrics.pendingDrafts,
    channels: metrics.channels,
    activeCommunities: metrics.activeCommunities,
  };

  const showEmptyHint = !loading && drafts.length === 0;

  useGSAP(() => {
    const container = containerRef.current;
    if (!container) return;

    const tl = gsap.timeline({
      defaults: { ease: "power2.out", clearProps: "all" },
    });

    tl.fromTo(".home-page__eyebrow",
      { opacity: 0, y: 8 },
      { opacity: 1, y: 0, duration: 0.4 }
    );
    tl.fromTo(".home-page__hero h1",
      { opacity: 0, y: 12 },
      { opacity: 1, y: 0, duration: 0.5 },
      0.05
    );
    tl.fromTo(".home-page__hero p",
      { opacity: 0 },
      { opacity: 1, duration: 0.4 },
      0.15
    );
    tl.fromTo(".home-page__pipeline",
      { opacity: 0, y: 10 },
      { opacity: 1, y: 0, duration: 0.5 },
      0.2
    );
    tl.fromTo(".home-page__stat",
      { opacity: 0, y: 14 },
      { opacity: 1, y: 0, duration: 0.4, stagger: 0.06 },
      0.3
    );
    tl.fromTo(".generate-ai",
      { opacity: 0, y: 10 },
      { opacity: 1, y: 0, duration: 0.4 },
      0.45
    );
    tl.fromTo(".home-page__card",
      { opacity: 0, y: 16 },
      { opacity: 1, y: 0, duration: 0.4, stagger: 0.06 },
      0.5
    );

    if (!loading) {
      container.querySelectorAll(".home-page__stat-value").forEach((el) => {
        const target = parseInt(el.textContent, 10);
        if (isNaN(target) || target === 0) return;
        const proxy = { val: 0 };
        gsap.to(proxy, {
          val: target,
          duration: 0.8,
          delay: 0.4,
          ease: "power2.out",
          snap: { val: 1 },
          onUpdate: () => { el.textContent = Math.round(proxy.val); },
        });
      });
    }

    gsap.to(".home-page__hero-accent", {
      backgroundPosition: "200% center",
      duration: 4,
      repeat: -1,
      ease: "none",
    });

  }, { scope: containerRef, dependencies: [loading] });

  return (
    <div className="home-page" ref={containerRef}>
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

      <section
        className="home-page__pipeline"
        aria-label="Estado del pipeline semanal"
      >
        <div className="home-page__pipeline-info">
          <div className="home-page__pipeline-cell">
            <span className="home-page__pipeline-icon home-page__pipeline-icon--blue">
              <Calendar />
            </span>
            <div>
              <span className="home-page__pipeline-label">Última generación</span>
              <strong className="home-page__pipeline-value">
                {loading
                  ? "Cargando..."
                  : metrics.lastFriday
                  ? formatFridayAt18(metrics.lastFriday)
                  : "—"}
              </strong>
            </div>
          </div>

          <div className="home-page__pipeline-divider" aria-hidden="true" />

          <div className="home-page__pipeline-cell">
            <span className="home-page__pipeline-icon home-page__pipeline-icon--violet">
              <Clock />
            </span>
            <div>
              <span className="home-page__pipeline-label">Próxima generación</span>
              <strong className="home-page__pipeline-value">
                {formatFridayAt18(metrics.nextFriday)}
              </strong>
            </div>
          </div>
        </div>

        <div className="home-page__pipeline-badge-wrap">
          <span
            className={`home-page__pipeline-badge${
              metrics.pendingDrafts > 0
                ? " home-page__pipeline-badge--active"
                : " home-page__pipeline-badge--idle"
            }`}
          >
            <span className="home-page__pipeline-badge-dot" aria-hidden="true" />
            {loading
              ? "Sincronizando..."
              : metrics.pendingDrafts > 0
              ? `${metrics.pendingDrafts} ${
                  metrics.pendingDrafts === 1 ? "borrador" : "borradores"
                } por revisar`
              : "Sin pendientes"}
          </span>
        </div>
      </section>

      {(error || data?.partialError) && (
        <div className="home-page__error" role="status">
          <AlertCircle size={16} />
          <span>{error || "No pudimos cargar parte de la información del pipeline."}</span>
        </div>
      )}

      <section
        className="home-page__stats"
        aria-label="Indicadores del pipeline"
      >
        <StatCard
          icon={Users}
          accent="amber"
          label="Comunidades activas"
          value={loading ? null : metrics.activeCommunities}
        />
        <StatCard
          icon={Activity}
          accent="blue"
          label="Posts analizados"
          value={loading ? null : metrics.postsAnalyzed}
          hint={metrics.lastWeekOf ? "última semana" : null}
        />
        <StatCard
          icon={Sparkles}
          accent="violet"
          label="Temas generados"
          value={loading ? null : metrics.topicsGenerated}
          hint={metrics.lastWeekOf ? "última semana" : null}
        />
        <StatCard
          icon={Clock}
          accent="emerald"
          label="Pendientes de revisión"
          value={loading ? null : metrics.pendingDrafts}
        />
      </section>

      <GenerateWithAI />

      {showEmptyHint && (
        <p className="home-page__empty-hint">
          Aún no se ha generado el primer paquete. Usá el panel de arriba para
          generar borradores con IA, o esperá al próximo viernes a las 18:00.
        </p>
      )}

      <div className="home-page__grid">
        {SECTIONS.map((s, i) => {
          const Icon = s.icon;
          const badgeValue = badgeMap[s.badgeKey];
          const showBadge =
            !loading && typeof badgeValue === "number" && badgeValue > 0;
          const badgeLabel =
            s.badgeKey === "channels" ? `${badgeValue} canales` : badgeValue;
          return (
            <Link
              key={s.to}
              to={s.to}
              className={`home-page__card home-page__card--${s.accent}`}
            >
              <span className="home-page__card-icon" aria-hidden="true">
                <Icon />
              </span>
              <h2>{s.title}</h2>
              <p>{s.description}</p>
              {showBadge && (
                <span
                  className={`home-page__card-badge home-page__card-badge--${s.accent}`}
                  aria-label={`${badgeValue} ${s.title}`}
                >
                  {badgeLabel}
                </span>
              )}
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

function StatCard({ icon: Icon, accent, label, value, hint }) {
  return (
    <div className={`home-page__stat home-page__stat--${accent}`}>
      <span className="home-page__stat-icon" aria-hidden="true">
        <Icon />
      </span>
      <div className="home-page__stat-body">
        {value === null ? (
          <span className="home-page__stat-skeleton" aria-hidden="true" />
        ) : (
          <strong className="home-page__stat-value">{value}</strong>
        )}
        <span className="home-page__stat-label">{label}</span>
        {hint && <span className="home-page__stat-hint">{hint}</span>}
      </div>
    </div>
  );
}
