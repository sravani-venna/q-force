import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

// Create axios instance for PR review API
const prReviewClient = axios.create({
  baseURL: `${API_BASE_URL}/api/review`,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: false,
});

// Add request interceptor to include token
prReviewClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Add response interceptor for error handling
prReviewClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // Token expired, redirect to login
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// PR Review Request Interface
export interface PrReviewRequest {
  repoOwner: string;
  repoName: string;
  prNumber: number;
}

// Diff Comment Interface
export interface DiffComment {
  fileName: string;
  diffLine: number;
  comment: string;
}

// PR Review Response Interface
export interface PrReviewResponse {
  status: 'success' | 'error' | 'no_suggestions';
  prNumber: number;
  reviewSummary: string | null;
  comments: DiffComment[];
  errorMessage: string | null;
}

// AI Review Service
export const aiReviewService = {
  /**
   * Review a pull request using AI
   * @param request - PR review request containing repo owner, name, and PR number
   * @returns Review response with comments and summary
   */
  async reviewPullRequest(request: PrReviewRequest): Promise<PrReviewResponse> {
    try {
      const response = await prReviewClient.post<{ success: boolean; data: PrReviewResponse; message: string }>('/pr', request);
      return response.data.data;  // Extract data from ApiResponse wrapper
    } catch (error: any) {
      // Handle specific error cases
      if (error.response?.data) {
        const errorData = error.response.data;
        
        // If backend returns a structured error response
        if (errorData.status === 'error') {
          throw new Error(errorData.errorMessage || errorData.reviewSummary || 'Failed to review pull request');
        }
      }
      
      // Generic error handling
      throw new Error(
        error.response?.data?.message || 
        error.message || 
        'Failed to review pull request. Please check your connection and try again.'
      );
    }
  },

  /**
   * Check health of PR review service
   * @returns Health status
   */
  async checkHealth(): Promise<{ status: string; message: string }> {
    try {
      const response = await prReviewClient.get('/health');
      return response.data;
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to check service health');
    }
  },
};

export default aiReviewService;

