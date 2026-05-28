import { useRef } from "react";
import { Link } from "react-router-dom";
import { Sparkles, FileText, CheckCircle2, Download, ArrowRight, Users, BarChart3, Zap, Shield, Globe } from "lucide-react";
import gsap from "gsap";
import { ScrollTrigger } from "gsap/ScrollTrigger";
import { useGSAP } from "@gsap/react";
import { useAuth } from "../hooks/useAuth";
import "./LandingPage.css";

gsap.registerPlugin(ScrollTrigger);

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
  { icon: Sparkles, label: "IA con Gemini", desc: "Genera resúmenes y borradores adaptados a cada canal.", accent: "violet" },
  { icon: Users, label: "Multi-comunidad", desc: "Monitorea múltiples comunidades en Discord, Slack, Telegram.", accent: "blue" },
  { icon: CheckCircle2, label: "Flujo editorial", desc: "Revisión, edición y aprobación antes de publicar.", accent: "emerald" },
  { icon: Zap, label: "Rápido y eficiente", desc: "Genera contenido semanal en minutos, no en horas.", accent: "amber" },
  { icon: Shield, label: "Control total", desc: "Nada se publica sin tu aprobación. Vos tenés la última palabra.", accent: "rose" },
  { icon: Globe, label: "Multi-canal", desc: "Newsletter, LinkedIn y X desde un solo panel de control.", accent: "cyan" },
];

const STATS = [
  { value: "3x", label: "más rápido que crear contenido manual" },
  { value: "100%", label: "control editorial antes de publicar" },
  { value: "3", label: "canales generados simultáneamente" },
];

export default function LandingPage() {
  const { isAuthenticated } = useAuth();
  const containerRef = useRef(null);

  useGSAP(() => {
    const container = containerRef.current;
    if (!container) return;

    gsap.fromTo(".landing__orb",
      { scale: 0, opacity: 0 },
      { scale: 1, opacity: 1, duration: 2, stagger: 0.2, ease: "elastic.out(1, 0.4)", clearProps: "transform" }
    );

    gsap.utils.toArray(".landing__orb").forEach((orb) => {
      gsap.to(orb, {
        y: "random(-50, 50)",
        x: "random(-40, 40)",
        duration: "random(5, 8)",
        repeat: -1,
        yoyo: true,
        ease: "sine.inOut",
      });
    });

    const heroTl = gsap.timeline({ defaults: { ease: "power3.out", clearProps: "all" } });

    heroTl.fromTo(".landing__nav",
      { y: -30, opacity: 0 },
      { y: 0, opacity: 1, duration: 0.6, clearProps: "all" }
    );

    heroTl.fromTo(".landing__badge",
      { scale: 0, opacity: 0 },
      { scale: 1, opacity: 1, duration: 0.5, ease: "back.out(3)", clearProps: "all" },
      0.2
    );

    heroTl.fromTo(".landing__hero-title-line",
      { y: 80, opacity: 0, rotateX: 40 },
      { y: 0, opacity: 1, rotateX: 0, duration: 0.9, stagger: 0.12, ease: "power4.out", clearProps: "all" },
      0.3
    );

    heroTl.fromTo(".landing__hero p",
      { y: 30, opacity: 0 },
      { y: 0, opacity: 1, duration: 0.7, clearProps: "all" },
      0.7
    );

    heroTl.fromTo(".landing__hero-actions a",
      { y: 20, opacity: 0, scale: 0.9 },
      { y: 0, opacity: 1, scale: 1, duration: 0.5, stagger: 0.1, ease: "back.out(2)", clearProps: "all" },
      0.9
    );

    heroTl.fromTo(".landing__hero-glow",
      { scale: 0, opacity: 0 },
      { scale: 1, opacity: 1, duration: 1.2, ease: "power2.out" },
      0.4
    );

    gsap.to(".landing__hero-title-gradient", {
      backgroundPosition: "200% center",
      duration: 4,
      repeat: -1,
      ease: "none",
    });

    gsap.fromTo(".landing__stats-item",
      { y: 40, opacity: 0, scale: 0.8 },
      {
        y: 0, opacity: 1, scale: 1,
        scrollTrigger: { trigger: ".landing__stats", start: "top 85%", toggleActions: "play none none none" },
        duration: 0.7, stagger: 0.15, ease: "back.out(1.7)", clearProps: "all",
      }
    );

    container.querySelectorAll(".landing__stats-value").forEach((el) => {
      const text = el.textContent;
      const numMatch = text.match(/\d+/);
      if (!numMatch) return;
      const target = parseInt(numMatch[0], 10);
      const suffix = text.replace(/\d+/, "");
      const proxy = { val: 0 };
      gsap.to(proxy, {
        val: target,
        duration: 2,
        ease: "power2.out",
        snap: { val: 1 },
        scrollTrigger: { trigger: el, start: "top 85%", toggleActions: "play none none none" },
        onUpdate: () => { el.textContent = Math.round(proxy.val) + suffix; },
      });
    });

    gsap.fromTo(".landing__how h2",
      { y: 40, opacity: 0 },
      {
        y: 0, opacity: 1,
        scrollTrigger: { trigger: ".landing__how", start: "top 80%", toggleActions: "play none none none" },
        duration: 0.7, clearProps: "all",
      }
    );

    gsap.utils.toArray(".landing__step").forEach((step, i) => {
      gsap.fromTo(step,
        { y: 60, opacity: 0, scale: 0.9 },
        {
          y: 0, opacity: 1, scale: 1,
          scrollTrigger: { trigger: step, start: "top 88%", toggleActions: "play none none none" },
          duration: 0.8, delay: i * 0.1, ease: "back.out(1.4)", clearProps: "all",
        }
      );
    });

    gsap.fromTo(".landing__features-title",
      { y: 30, opacity: 0 },
      {
        y: 0, opacity: 1,
        scrollTrigger: { trigger: ".landing__features-title", start: "top 85%", toggleActions: "play none none none" },
        duration: 0.7, clearProps: "all",
      }
    );

    gsap.utils.toArray(".landing__feature").forEach((feat, i) => {
      gsap.fromTo(feat,
        { y: 50, opacity: 0, scale: 0.92 },
        {
          y: 0, opacity: 1, scale: 1,
          scrollTrigger: { trigger: feat, start: "top 90%", toggleActions: "play none none none" },
          duration: 0.7, delay: i * 0.08, ease: "back.out(1.4)", clearProps: "all",
        }
      );
    });

    gsap.fromTo(".landing__cta-section",
      { y: 50, opacity: 0 },
      {
        y: 0, opacity: 1,
        scrollTrigger: { trigger: ".landing__cta-section", start: "top 85%", toggleActions: "play none none none" },
        duration: 0.8, ease: "power3.out", clearProps: "all",
      }
    );

  }, { scope: containerRef });

  return (
    <div className="landing" ref={containerRef}>
      <div className="landing__orb landing__orb--1" aria-hidden="true" />
      <div className="landing__orb landing__orb--2" aria-hidden="true" />
      <div className="landing__orb landing__orb--3" aria-hidden="true" />
      <div className="landing__orb landing__orb--4" aria-hidden="true" />

      <nav className="landing__nav">
        <span className="landing__logo">
          <Sparkles size={18} className="landing__logo-icon" />
          TalentCircle
        </span>
        <div className="landing__nav-links">
          {isAuthenticated ? (
            <Link to="/dashboard" className="landing__nav-cta">Ir al panel <ArrowRight size={14} /></Link>
          ) : (
            <>
              <Link to="/login" className="landing__nav-link">Iniciar sesión</Link>
              <Link to="/register" className="landing__nav-cta">Registrarse</Link>
            </>
          )}
        </div>
      </nav>

      <header className="landing__hero">
        <div className="landing__hero-glow" aria-hidden="true" />
        <span className="landing__badge">
          <span className="landing__badge-dot" />
          Pipeline de contenido con IA
        </span>
        <h1>
          <span className="landing__hero-title-line">Transforma la actividad</span>
          <span className="landing__hero-title-line">de tu comunidad en</span>
          <span className="landing__hero-title-line">
            <span className="landing__hero-title-gradient">contenido listo para publicar</span>
          </span>
        </h1>
        <p>
          TalentCircle monitorea tus comunidades técnicas, identifica las contribuciones más
          relevantes con IA, y genera borradores diferenciados para Newsletter, LinkedIn y X
          — listos para tu revisión.
        </p>
        <div className="landing__hero-actions">
          {isAuthenticated ? (
            <Link to="/dashboard" className="landing__btn-primary">
              Ir al panel <ArrowRight size={16} />
            </Link>
          ) : (
            <>
              <Link to="/register" className="landing__btn-primary">
                Empezar gratis <ArrowRight size={16} />
              </Link>
              <Link to="/login" className="landing__btn-secondary">Ya tengo cuenta</Link>
            </>
          )}
        </div>
      </header>

      <section className="landing__stats">
        {STATS.map((s, i) => (
          <div className="landing__stats-item" key={i}>
            <span className="landing__stats-value">{s.value}</span>
            <span className="landing__stats-label">{s.label}</span>
          </div>
        ))}
      </section>

      <section className="landing__how">
        <h2>Cómo funciona</h2>
        <div className="landing__steps">
          {STEPS.map((step, i) => (
            <div className="landing__step" key={i}>
              <div className="landing__step-number">{i + 1}</div>
              <div className="landing__step-icon"><step.icon size={24} /></div>
              <h3>{step.title}</h3>
              <p>{step.desc}</p>
              {i < STEPS.length - 1 && (
                <span className="landing__step-connector" aria-hidden="true" />
              )}
            </div>
          ))}
        </div>
      </section>

      <section className="landing__features-section">
        <h2 className="landing__features-title">Todo lo que necesitás</h2>
        <div className="landing__features">
          {FEATURES.map((f, i) => (
            <div className={`landing__feature landing__feature--${f.accent}`} key={i}>
              <span className="landing__feature-icon"><f.icon size={22} /></span>
              <h3>{f.label}</h3>
              <p>{f.desc}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="landing__cta-section">
        <div className="landing__cta-glow" aria-hidden="true" />
        <h2>Dejá de compilar contenido a mano</h2>
        <p>Automatizá el proceso y publicá contenido de calidad cada semana.</p>
        {isAuthenticated ? (
          <Link to="/dashboard" className="landing__btn-primary landing__btn-primary--lg">
            Ir al panel <ArrowRight size={18} />
          </Link>
        ) : (
          <Link to="/register" className="landing__btn-primary landing__btn-primary--lg">
            Crear cuenta gratis <ArrowRight size={18} />
          </Link>
        )}
      </section>

      <footer className="landing__footer">
        <span>TalentCircle © {new Date().getFullYear()}</span>
      </footer>
    </div>
  );
}
