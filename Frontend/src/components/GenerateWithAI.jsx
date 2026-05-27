import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Sparkles, AlertCircle, CheckCircle2, Loader } from "lucide-react";
import * as communitiesApi from "../api/communities";
import { generateDigestWithAI } from "../api/weeklyDigest";
import { useFetch } from "../hooks/useFetch";
import "./GenerateWithAI.css";

export default function GenerateWithAI() {
  const navigate = useNavigate();
  const { data: rawCommunities } = useFetch(() => communitiesApi.listCommunities({ onlyActive: true }));
  const communities = Array.isArray(rawCommunities) ? rawCommunities : [];

  const [selectedCommunity, setSelectedCommunity] = useState("");
  const [generating, setGenerating] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  const handleGenerate = async () => {
    if (!selectedCommunity) return;
    setGenerating(true);
    setError(null);
    setResult(null);
    try {
      const digest = await generateDigestWithAI(Number(selectedCommunity));
      setResult(digest);
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || "Error al generar con IA";
      setError(msg);
    } finally {
      setGenerating(false);
    }
  };

  return (
    <section className="generate-ai">
      <header className="generate-ai__header">
        <Sparkles size={18} />
        <h2>Generar borradores con IA</h2>
      </header>
      <p className="generate-ai__desc">
        Selecciona una comunidad y la IA analizará la actividad de la semana para
        generar borradores para Newsletter, LinkedIn y X.
      </p>

      <div className="generate-ai__controls">
        <select
          className="generate-ai__select"
          value={selectedCommunity}
          onChange={(e) => setSelectedCommunity(e.target.value)}
          disabled={generating}
        >
          <option value="">Seleccionar comunidad…</option>
          {communities.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name} ({c.platform})
            </option>
          ))}
        </select>
        <button
          className="generate-ai__btn"
          onClick={handleGenerate}
          disabled={!selectedCommunity || generating}
        >
          {generating ? (
            <>
              <Loader size={15} className="generate-ai__spinner" />
              Generando…
            </>
          ) : (
            <>
              <Sparkles size={15} />
              Generar con IA
            </>
          )}
        </button>
      </div>

      {error && (
        <div className="generate-ai__error">
          <AlertCircle size={15} />
          <span>{error}</span>
        </div>
      )}

      {result && (
        <div className="generate-ai__success">
          <CheckCircle2 size={15} />
          <span>
            Digest generado para <strong>{result.communityName}</strong> — semana del {result.weekStart}
          </span>
          <button
            className="generate-ai__link"
            onClick={() => navigate(`/approval/${result.id}`)}
          >
            Ver borradores →
          </button>
        </div>
      )}
    </section>
  );
}
