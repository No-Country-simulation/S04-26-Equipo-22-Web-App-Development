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

			<button className="newsletter-button">
				{data.buttonText}
			</button>

		</article>
	);
}

export default NewsletterPreview;