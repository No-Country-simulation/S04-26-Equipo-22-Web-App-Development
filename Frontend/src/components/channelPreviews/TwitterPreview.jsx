import "./TwitterPreview.css";



import {
	MessageCircle,
	Repeat2,
	Heart,
	ChartBar,
	BadgeCheck
} from "lucide-react";

function TwitterPreview({ data }) {
	return (
		<article className="twitter-card">

			<img
				src={data.avatar}
				alt={data.user}
				className="twitter-avatar"
			/>

			<div className="twitter-body">

				{/* HEADER */}
				<div className="twitter-header">

					<div className="twitter-user-info">

						<h3>{data.user}</h3>

						{
							data.verified && (
								<BadgeCheck className="twitter-verified" />
							)
						}

						<span className="twitter-username">
							{data.username}
						</span>

						

						<span className="twitter-time">
							{data.time}
						</span>

					</div>

					

				</div>

				{/* CONTENT */}
				<p className="twitter-content">
					{data.content}
				</p>

				{/* IMAGE */}
				{
					data.image && (
						<img
							src={data.image}
							alt=""
							className="twitter-post-image"
						/>
					)
				}

				{/* ACTIONS */}
				<div className="twitter-actions">

					<div className="twitter-action">
						<MessageCircle />
						<span>12</span>
					</div>

					<div className="twitter-action">
						<Repeat2 />
						<span>34</span>
					</div>

					<div className="twitter-action">
						<Heart />
						<span>281</span>
					</div>

					<div className="twitter-action">
						<ChartBar />
						<span>8.2k</span>
					</div>

				</div>

			</div>

		</article>
	);
}

export default TwitterPreview;