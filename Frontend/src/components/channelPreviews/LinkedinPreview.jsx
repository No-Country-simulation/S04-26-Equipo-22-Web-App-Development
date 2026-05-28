import "./LinkedinPreview.css";
import {
	ThumbsUp,
	MessageCircle,
	Repeat2,
	Send,
	Globe
} from "lucide-react";
function LinkedinPreview({ data }) {
	if (!data) return null;
	return (
		<article className="linkedin-card">

			{/* HEADER */}
			<div className="linkedin-header">

				<div className="linkedin-user">

					<img
						src={data.avatar || ""}
						alt={data.user || ""}
						className="linkedin-avatar"
					/>

					<div className="linkedin-user-info">

						<h3>{data.user || ""}</h3>

						<span className="linkedin-role">
							{data.role || ""}
						</span>

						<p className="linkedin-time">
							{data.time || ""} · <Globe />
						</p>

					</div>

				</div>



			</div>

			{/* CONTENT */}
			<p className="linkedin-content">
				{data.content || ""}
			</p>

			{/* IMAGE */}
			{
				data.image && (
					<img
						src={data.image}
						alt=""
						className="linkedin-post-image"
					/>
				)
			}

			{/* STATS */}
			<div className="linkedin-stats">

				<div className="linkedin-reactions">

					<div className="linkedin-reaction-icons">

						<span className="like"><ThumbsUp /></span>
						<span className="retweet"><Repeat2 /></span>
						<span className="send"><Send /></span>

					</div>

					<span>124</span>

				</div>

				<div>
					12 comentarios • 4 compartidos
				</div>

			</div>

			{/* ACTIONS */}
			<div className="linkedin-actions">

				<button className="linkedin-action-btn">
					<ThumbsUp />
	<span>Recomendar</span>
				</button>

				<button className="linkedin-action-btn">
					<MessageCircle />
	<span>Comentar</span>
				</button>

				<button className="linkedin-action-btn">
				<Repeat2 />
	<span>Compartir</span>
				</button>

			

			</div>

		</article>
	);
}

export default LinkedinPreview;