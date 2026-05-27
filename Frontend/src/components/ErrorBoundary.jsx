import { Component } from "react";
import { AlertCircle } from "lucide-react";

export class ErrorBoundary extends Component {
  state = { hasError: false, error: null };

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  render() {
    if (!this.state.hasError) return this.props.children;

    return (
      <div style={{ padding: 32, textAlign: "center", color: "#6b7280" }}>
        <AlertCircle size={40} style={{ marginBottom: 12, color: "#ef4444" }} />
        <h2 style={{ margin: "0 0 8px", color: "#111827" }}>Algo salio mal</h2>
        <p>Recarga la pagina o intenta de nuevo mas tarde.</p>
        <button
          onClick={() => this.setState({ hasError: false, error: null })}
          style={{
            marginTop: 16,
            padding: "8px 20px",
            borderRadius: 8,
            border: "1px solid #d1d5db",
            background: "#fff",
            cursor: "pointer",
          }}
        >
          Reintentar
        </button>
      </div>
    );
  }
}
