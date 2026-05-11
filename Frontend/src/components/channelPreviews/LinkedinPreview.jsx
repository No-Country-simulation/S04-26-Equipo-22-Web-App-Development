import "./LinkedinPreview.css";
import {
	FaRegThumbsUp,
	FaRegCommentDots,
	FaRetweet,
	FaPaperPlane,
	FaGlobeAmericas
} from "react-icons/fa";
function LinkedinPreview({ data }) {
	return (
		<article className="linkedin-card">

			{/* HEADER */}
			<div className="linkedin-header">

				<div className="linkedin-user">

					<img
						src={data.avatar}
						alt={data.user}
						className="linkedin-avatar"
					/>

					<div className="linkedin-user-info">

						<h3>{data.user}</h3>

						<span className="linkedin-role">
							{data.role}
						</span>

						<p className="linkedin-time">
							{data.time} · <FaGlobeAmericas />	
						</p>

					</div>

				</div>

				

			</div>

			{/* CONTENT */}
			<p className="linkedin-content">
				{data.content}
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

						<span className="like"><FaRegThumbsUp /></span>
						<span className="retweet"><FaRetweet /></span>
						<span className="send"><FaPaperPlane /></span>

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
					<FaRegThumbsUp />
	<span>Recomendar</span>
				</button>

				<button className="linkedin-action-btn">
					<FaRegCommentDots />
	<span>Comentar</span>
				</button>

				<button className="linkedin-action-btn">
				<FaRetweet />
	<span>Compartir</span>
				</button>

			

			</div>

		</article>
	);
}

export default LinkedinPreview;