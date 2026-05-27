import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import "./Header.css";

const NAV_ITEMS = [
  { to: "/dashboard", label: "Inicio", end: true },
  { to: "/communities", label: "Comunidades" },
  { to: "/drafts", label: "Borradores" },
  { to: "/approval", label: "Aprobación" },
  { to: "/preview", label: "Preview" },
  { to: "/job-status", label: "Estado del trabajo" },
  { to: "/history", label: "Historial" },
  { to: "/settings", label: "Configuración" },
];

export default function Header() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const onLogout = async () => {
    await logout();
    navigate("/login", { replace: true });
  };

  return (
    <header className="app-header">
      <div className="app-header__brand">TalentCircle</div>
      <nav className="app-header__nav">
        {NAV_ITEMS.map(({ to, label, end }) => (
          <NavLink
            key={to}
            to={to}
            end={end}
            className={({ isActive }) =>
              "app-header__link" + (isActive ? " is-active" : "")
            }
          >
            {label}
          </NavLink>
        ))}
      </nav>
      <div className="app-header__user">
        {user && (
          <>
            <span className="app-header__email">{user.email}</span>
            <span className="app-header__role">{user.role}</span>
            <button type="button" onClick={onLogout} className="app-header__logout">
              Salir
            </button>
          </>
        )}
      </div>
    </header>
  );
}
