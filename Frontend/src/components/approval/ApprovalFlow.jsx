import "./ApprovalFlow.css";

const steps = [
  {
    key: "GENERATED",
    title: "GENERADO",
    subtitle: "por IA",
  },

  {
    key: "IN_REVIEW",
    title: "EN REVISIÓN",
    subtitle: "por editor",
  },

  {
    key: "APPROVED",
    title: "APROBADO",
    subtitle: "listo para publicar",
  },

  {
    key: "PUBLISHED",
    title: "PUBLICADO",
    subtitle: "en canales",
  },
];

function ApprovalFlow({ draft }) {
  if (!draft) return null;

  const currentStep = steps.findIndex(
    (step) => step.key === draft.status
  );

  return (
    <div className="approval-flow">

      {steps.map((step, index) => (

        <div
          className="flow-wrapper"
          key={step.key}
        >

          <div
            className={`flow-card ${
              index <= currentStep
                ? `active ${step.key.toLowerCase()}`
                : ""
            }`}
          >

            <h3>
              {step.title}
            </h3>

            <p>
              {step.subtitle}
            </p>

          </div>

          {index < steps.length - 1 && (
            <span className="flow-arrow">
              →
            </span>
          )}

        </div>

      ))}

    </div>
  );
}

export default ApprovalFlow;