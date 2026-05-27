import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Mail, Lock, Eye, EyeOff, Shield, AlertCircle } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import AuthLayout from "./auth/AuthLayout";

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: "", password: "" });
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const onSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await register(form);
      navigate("/dashboard", { replace: true });
    } catch (err) {
      setError(err?.response?.data?.message || "No se pudo crear la cuenta");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AuthLayout>
      <div className="auth-form__head">
        <h1>Crear cuenta</h1>
        <p>Empieza a revisar los borradores semanales de tu comunidad.</p>
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
              placeholder="Mínimo 6 caracteres"
              autoComplete="new-password"
              minLength={6}
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
          <p className="auth-form__hint">Usa al menos 6 caracteres.</p>
        </label>

        {error && (
          <div className="auth-form__error">
            <AlertCircle />
            {error}
          </div>
        )}

        <button type="submit" className="auth-form__submit" disabled={submitting}>
          {submitting && <span className="auth-form__spinner" aria-hidden />}
          {submitting ? "Creando…" : "Crear cuenta"}
        </button>

        <p className="auth-form__trust">
          <Shield /> Conexión cifrada · HTTPS
        </p>
      </form>

      <p className="auth-form__switch">
        ¿Ya tienes cuenta?
        <Link to="/login">Iniciar sesión</Link>
      </p>
    </AuthLayout>
  );
}
