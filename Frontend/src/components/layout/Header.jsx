import { useEffect, useRef, useState } from "react";
import { NavLink, useNavigate, useLocation } from "react-router-dom";
import {
  Home,
  Users,
  FileEdit,
  CheckCircle2,
  Eye,
  Activity,
  History,
  Settings as SettingsIcon,
  ChevronDown,
  LogOut,
  Menu,
  X,
  Sparkles,
} from "lucide-react";
import { useAuth } from "../../hooks/useAuth";
import "./Header.css";

const NAV_ITEMS = [
  { to: "/dashboard",   label: "Inicio",       icon: Home,         end: true },
  { to: "/communities", label: "Comunidades",  icon: Users },
  { to: "/drafts",      label: "Borradores",   icon: FileEdit },
  { to: "/approval",    label: "Aprobación",   icon: CheckCircle2 },
  { to: "/preview",     label: "Preview",      icon: Eye },
  { to: "/job-status",  label: "Estado",       icon: Activity },
  { to: "/history",     label: "Historial",    icon: History },
  { to: "/settings",    label: "Configuración", icon: SettingsIcon },
];

function getInitials(email) {
  if (!email) return "·";
  const handle = email.split("@")[0] || "";
  const parts = handle.split(/[._-]+/).filter(Boolean);
  if (parts.length === 0) return handle.slice(0, 2).toUpperCase();
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
  return (parts[0][0] + parts[1][0]).toUpperCase();
}

export default function Header() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [menuOpen, setMenuOpen] = useState(false);   // mobile nav drawer
  const [userOpen, setUserOpen] = useState(false);   // user dropdown
  const userMenuRef = useRef(null);

  // close menus on route change
  useEffect(() => {
    setMenuOpen(false);
    setUserOpen(false);
  }, [location.pathname]);

  // click-outside for user dropdown
  useEffect(() => {
    if (!userOpen) return;
    const onClick = (e) => {
      if (userMenuRef.current && !userMenuRef.current.contains(e.target)) {
        setUserOpen(false);
      }
    };
    document.addEventListener("mousedown", onClick);
    return () => document.removeEventListener("mousedown", onClick);
  }, [userOpen]);

  // escape closes drawer
  useEffect(() => {
    if (!menuOpen) return;
    const onKey = (e) => e.key === "Escape" && setMenuOpen(false);
    document.addEventListener("keydown", onKey);
    return () => document.removeEventListener("keydown", onKey);
  }, [menuOpen]);

  const onLogout = async () => {
    setUserOpen(false);
    await logout();
    navigate("/login", { replace: true });
  };

  return (
    <header className="app-header">
      <div className="app-header__inner">
        <NavLink to="/dashboard" className="app-header__brand" aria-label="TalentCircle">
          <span className="app-header__mark" aria-hidden="true">
            <Sparkles size={14} />
          </span>
          <span className="app-header__brand-text">TalentCircle</span>
        </NavLink>

        <button
          type="button"
          className="app-header__burger"
          onClick={() => setMenuOpen((v) => !v)}
          aria-label={menuOpen ? "Cerrar menú" : "Abrir menú"}
          aria-expanded={menuOpen}
        >
          {menuOpen ? <X size={18} /> : <Menu size={18} />}
        </button>

        <nav
          className={`app-header__nav${menuOpen ? " is-open" : ""}`}
          aria-label="Navegación principal"
        >
          {NAV_ITEMS.map(({ to, label, end, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              className={({ isActive }) =>
                "app-header__link" + (isActive ? " is-active" : "")
              }
            >
              <Icon size={14} aria-hidden="true" />
              <span>{label}</span>
            </NavLink>
          ))}
        </nav>

        <div className="app-header__user" ref={userMenuRef}>
          {user && (
            <>
              <button
                type="button"
                className={`app-header__avatar-btn${userOpen ? " is-open" : ""}`}
                onClick={() => setUserOpen((v) => !v)}
                aria-haspopup="menu"
                aria-expanded={userOpen}
                aria-label="Abrir menú de usuario"
              >
                <span className="app-header__avatar" aria-hidden="true">
                  {getInitials(user.email)}
                </span>
                <ChevronDown size={14} aria-hidden="true" className="app-header__caret" />
              </button>

              {userOpen && (
                <div className="app-header__user-menu" role="menu">
                  <div className="app-header__user-meta">
                    <span className="app-header__user-email">{user.email}</span>
                    <span className={`app-header__user-role role--${(user.role || "").toLowerCase()}`}>
                      {user.role}
                    </span>
                  </div>
                  <button
                    type="button"
                    onClick={onLogout}
                    className="app-header__user-action"
                    role="menuitem"
                  >
                    <LogOut size={14} aria-hidden="true" />
                    <span>Cerrar sesión</span>
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </header>
  );
}
