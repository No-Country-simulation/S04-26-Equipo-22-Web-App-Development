import { useState } from "react";

import "./ChannelPreview.css";
import { previewData } from "../data/previewData";

import LinkedinPreview from "../components/channelPreviews/LinkedinPreview";
import TwitterPreview from "../components/channelPreviews/TwitterPreview";
import NewsletterPreview from "../components/channelPreviews/NewsletterPreview";

function ChannelPreview() {

const [activeTab, setActiveTab] = useState("All");
  const renderPreview = (preview) => {

    switch (preview.channel) {

      case "LinkedIn":
        return <LinkedinPreview data={preview} />;

      case "Twitter":
        return <TwitterPreview data={preview} />;

      case "Newsletter":
        return <NewsletterPreview data={preview} />;

      default:
        return null;
    }
  };

  return (

    <div className="preview-page">

      <header className="preview-header">

        <button className="back-button">
          ← Volver
        </button>

        <h1 className="preview-title">
          Vista previa del borrador
        </h1>

      </header>

      <nav className="preview-tabs">
<button
  className={activeTab === "All" ? "active-tab" : ""}
  onClick={() => setActiveTab("All")}
>
  Todos
</button> 
      <button
  className={activeTab === "Newsletter" ? "active-tab" : ""}
  onClick={() => setActiveTab("Newsletter")}
>
  Newsletter
</button>

<button
  className={activeTab === "LinkedIn" ? "active-tab" : ""}
  onClick={() => setActiveTab("LinkedIn")}
>
  LinkedIn
</button>

<button
  className={activeTab === "Twitter" ? "active-tab" : ""}
  onClick={() => setActiveTab("Twitter")}
>
  Twitter
</button>

      </nav>

      <section className="preview-grid">

       {previewData
  .filter(
    (preview) =>
      activeTab === "All" ||
      preview.channel === activeTab
  )
  .map((preview) => (
            <div className="preview-card" key={preview.id}>

              <h3 className="preview-card-title">
                {preview.channel}
              </h3>

              {renderPreview(preview)}

            </div>

          ))}

      </section>

    </div>
  );
}

export default ChannelPreview;