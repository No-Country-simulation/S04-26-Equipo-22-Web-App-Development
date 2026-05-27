import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { Mail, Lock, Eye, EyeOff, Shield, AlertCircle } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import AuthLayout from "./auth/AuthLayout";

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ email: "", password: "" });
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const from = location.state?.from?.pathname || "/";

  const onSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await login(form);
      navigate(from, { replace: true });
    } catch (err) {
      setError(err?.response?.data?.message || "Credenciales inválidas");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AuthLayout>
      <div className="auth-form__head">
        <h1>Bienvenido de nuevo</h1>
        <p>Inicia sesión para revisar los borradores de esta semana.</p>
      </div>

      <form onSubmit={onSubmit} noValidate>
        <label className="auth-form__field">
          <span className="auth-form__label">Email</span>
          <span className="auth-form__input-wrap">
            <input
              type="email"
              className="auth-form__input"
              placeholder="tu@email.com"
              autoComplete="email"
              required
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
            />
            <span className="auth-form__icon"><Mail /></span>
          </span>
        </label>

        <label className="auth-form__field">
          <span className="auth-form__label">Contraseña</span>
          <span className="auth-form__input-wrap">
            <input
              type={showPassword ? "text" : "password"}
              className="auth-form__input auth-form__input--password"
              placeholder="••••••••"
              autoComplete="current-password"
              required
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
            />
            <span className="auth-form__icon"><Lock /></span>
            <button
              type="button"
              className="auth-form__toggle"
              onClick={() => setShowPassword((v) => !v)}
              aria-label={showPassword ? "Ocultar contraseña" : "Mostrar contraseña"}
              tabIndex={-1}
            >
              {showPassword ? <EyeOff /> : <Eye />}
            </button>
          </span>
        </label>

        {error && (
          <div className="auth-form__error">
            <AlertCircle />
            {error}
          </div>
        )}

        <button type="submit" className="auth-form__submit" disabled={submitting}>
          {submitting && <span className="auth-form__spinner" aria-hidden />}
          {submitting ? "Entrando…" : "Entrar"}
        </button>

        <p className="auth-form__trust">
          <Shield /> Conexión cifrada · HTTPS
        </p>
      </form>

      <p className="auth-form__switch">
        ¿No tienes cuenta?
        <Link to="/register">Crear una</Link>
      </p>
    </AuthLayout>
  );
}
