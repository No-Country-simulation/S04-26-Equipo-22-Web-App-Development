import { api } from "./client";

export const getWeeklyStatistics = async () => {
	const response = await api.get("/api/community-posts/weekly/statistics");

	return response.data;
};

export const getWeeklyDigest = async () => {
	const response = await api.get("/api/community-posts/weekly/digest");

	return response.data;
};
