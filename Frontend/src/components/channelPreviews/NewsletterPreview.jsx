import "./NewsletterPreview.css";

function NewsletterPreview({ data }) {
	return (
		<article className="newsletter-card">

			<div className="newsletter-top">
<h2 className="newsletter-subject">
				{data.subject}
			</h2>
				<span className="newsletter-time">
					{data.time}
				</span>

			</div>

			

			<p className="newsletter-content">
				{data.content}
			</p>

			{
				data.image && (
					<img
						src={data.image}
						alt=""
						className="newsletter-image"
					/>
				)
			}

			

		</article>
	);
}

export default NewsletterPreview;