import "./TwitterPreview.css";



import {
	FaRegComment,
	FaRetweet,
	FaRegHeart,
	FaChartBar
} from "react-icons/fa";

import { MdVerified } from "react-icons/md";

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
								<MdVerified className="twitter-verified" />
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
						<FaRegComment />
						<span>12</span>
					</div>

					<div className="twitter-action">
						<FaRetweet />
						<span>34</span>
					</div>

					<div className="twitter-action">
						<FaRegHeart />
						<span>281</span>
					</div>

					<div className="twitter-action">
						<FaChartBar />
						<span>8.2k</span>
					</div>

				</div>

			</div>

		</article>
	);
}

export default TwitterPreview;