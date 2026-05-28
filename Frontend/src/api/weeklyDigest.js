import { api } from "./client";

export const getWeeklyStatistics = async () => {
	const response = await api.get("/api/community-posts/weekly/statistics");
	return response.data;
};

export const getWeeklyDigest = async () => {
	const response = await api.get("/api/community-posts/weekly/digest");
	return response.data;
};

export const generateDigestWithAI = async (communityId) => {
	const response = await api.post(
		`/api/weekly-digests/generate-with-ai?communityId=${communityId}`
	);
	return response.data;
};

export const regenerateDrafts = async (digestId) => {
	const response = await api.post(
		`/api/weekly-digests/${digestId}/regenerate-drafts`
	);
	return response.data;
};
