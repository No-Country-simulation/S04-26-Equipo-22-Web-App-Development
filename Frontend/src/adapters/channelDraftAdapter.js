export function channelDraftAdapter(draft) {
	switch (draft.targetPlatform) {
		case "TWITTER":
			return {
				...draft,
				channel: "Twitter",
				user: "TalentCircle",
				username: "@talentcircle_dev",
				avatar: "https://i.pravatar.cc/150?img=15",
				verified: true,
				time: "2m",
			};

		case "LINKEDIN":
			return {
				...draft,
				channel: "LinkedIn",
				user: "TalentCircle",
				role: "Comunidad de desarrolladores",
				avatar: "https://i.pravatar.cc/150?img=12",
				time: "2m",
			};

		case "NEWSLETTER":
			return {
				...draft,
				channel: "Newsletter",
				subject: "🚀 Lo mejor de nuestra comunidad esta semana",
				
				time: "2m",
			};

		default:
			return draft;
	}
}
