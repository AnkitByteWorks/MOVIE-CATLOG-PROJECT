import axios from "axios";
import { toast } from "sonner";
import { getAccessToken } from "../auth";

export const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8082",
  timeout: 15000,
  headers: { "Content-Type": "application/json" },
});

export const watchlogApi = axios.create({
  baseURL: process.env.NEXT_PUBLIC_WATCHLOG_API_URL || "http://localhost:8083",
  timeout: 15000,
  headers: { "Content-Type": "application/json" },
});

const attachInterceptors = (instance: typeof api, name: string) => {
  instance.interceptors.request.use((config) => {
    const token = getAccessToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    if (process.env.NODE_ENV === "development") {
      console.debug(`[${name}] ${config.method?.toUpperCase()} ${config.url}`);
    }
    return config;
  });

  instance.interceptors.response.use(
    (response) => response,
    (error) => {
      const status = error.response?.status;
      const message =
        error.response?.data?.message ||
        error.message ||
        "Something went wrong";

      if (status === 404) {
        // silent — often expected
      } else if (status === 401) {
        // Unauthenticated
      } else if (status >= 500) {
        toast.error("Server error. Please try again.");
      } else if (error.code === "ECONNABORTED") {
        toast.error("Request timed out. Check your connection.");
      } else if (!error.response) {
        console.warn(`[${name}] Network error — is the backend running?`, message);
      }

      return Promise.reject(error);
    }
  );
};

attachInterceptors(api, "API");
attachInterceptors(watchlogApi, "WATCHLOG");

export default api;
