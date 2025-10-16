import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

// Create axios instance with common configuration
const apiClient = axios.create({
  baseURL: `${API_BASE_URL}/api`,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: false,
});

// Add request interceptor to include token
apiClient.interceptors.request.use(
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
apiClient.interceptors.response.use(
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

// Dashboard API
export const dashboardService = {
  async getStats() {
    try {
      const response = await apiClient.get('/dashboard/stats');
      return response.data;
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to fetch dashboard stats');
    }
  },

  async getRealTestCases() {
    try {
      const response = await apiClient.get('/dashboard/real-tests');
      return response.data;
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to fetch real test cases');
    }
  },
};

// Test Suites API
export const testSuiteService = {
  async getAll() {
    try {
      const response = await apiClient.get('/test-suites');
      return response.data;
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to fetch test suites');
    }
  },

  async getById(id: string) {
    try {
      const response = await apiClient.get(`/test-suites/${id}`);
      return response.data;
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to fetch test suite');
    }
  },

  async create(testSuite: any) {
    try {
      const response = await apiClient.post('/test-suites', testSuite);
      return response.data;
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to create test suite');
    }
  },

  async update(id: string, testSuite: any) {
    try {
      const response = await apiClient.put(`/test-suites/${id}`, testSuite);
      return response.data;
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to update test suite');
    }
  },

  async delete(id: string) {
    try {
      const response = await apiClient.delete(`/test-suites/${id}`);
      return response.data;
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to delete test suite');
    }
  },

  async getServiceLevelTests() {
    try {
      const response = await apiClient.get('/tests/services');
      return response.data;
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to fetch service-level tests');
    }
  },
};

// Test Executions API
export const testExecutionService = {
  async getAll() {
    try {
      const response = await apiClient.get('/test-executions');
      return response.data;
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to fetch test executions');
    }
  },

  async getById(id: string) {
    try {
      const response = await apiClient.get(`/test-executions/${id}`);
      return response.data;
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to fetch test execution');
    }
  },

  async start(suiteId: string) {
    try {
      console.log('🚀 API: Starting test execution for suite:', suiteId);
      const response = await apiClient.post('/test-executions', { suiteId });
      console.log('✅ API: Test execution response:', response.data);
      return response.data;
    } catch (error: any) {
      console.error('❌ API: Test execution failed:', error);
      throw new Error(error.response?.data?.message || 'Failed to start test execution');
    }
  },

  async stop(id: string) {
    try {
      const response = await apiClient.post(`/test-executions/${id}/stop`);
      return response.data;
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to stop test execution');
    }
  },

  async getDetailedTestCaseResults(status?: string) {
    try {
      const url = status ? `/test-executions/test-cases?status=${status}` : '/test-executions/test-cases';
      const response = await apiClient.get(url);
      return response.data;
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to fetch detailed test case results');
    }
  },

  async getTestCaseResultsSummary() {
    try {
      const response = await apiClient.get('/test-executions/test-cases/summary');
      return response.data;
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to fetch test case results summary');
    }
  },
};

// Test Generation API
export const testGenerationService = {
  async generateTests(request: any) {
    try {
      const response = await apiClient.post('/test-generation/generate', request);
      return response.data;
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to generate tests');
    }
  },

  async analyzeCode(codeSnippet: string) {
    try {
      const response = await apiClient.post('/test-generation/analyze', { codeSnippet });
      return response.data;
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to analyze code');
    }
  },

  async getTemplates() {
    try {
      const response = await apiClient.get('/test-generation/templates');
      return response.data;
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to fetch templates');
    }
  },
};

// Reports API
export const reportsService = {
  async getCoverageReport(suiteId?: string) {
    try {
      const url = suiteId ? `/reports/coverage?suiteId=${suiteId}` : '/reports/coverage';
      const response = await apiClient.get(url);
      return response.data;
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to fetch coverage report');
    }
  },

  async getPerformanceReport(suiteId?: string) {
    try {
      const url = suiteId ? `/reports/performance?suiteId=${suiteId}` : '/reports/performance';
      const response = await apiClient.get(url);
      return response.data;
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to fetch performance report');
    }
  },

  async getTrendsReport(period: 'week' | 'month' | 'quarter' = 'week') {
    try {
      const response = await apiClient.get(`/reports/trends?period=${period}`);
      return response.data;
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to fetch trends report');
    }
  },

  async exportReport(type: 'pdf' | 'excel', reportType: string, filters?: any) {
    try {
      const response = await apiClient.post('/reports/export', {
        type,
        reportType,
        filters,
      }, {
        responseType: 'blob',
      });
      return response.data;
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to export report');
    }
  },
};

// Users API (for settings/profile)
export const userService = {
  async getProfile() {
    try {
      const response = await apiClient.get('/users/profile');
      return response.data;
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to fetch profile');
    }
  },

  async updateProfile(profile: any) {
    try {
      const response = await apiClient.put('/users/profile', profile);
      return response.data;
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to update profile');
    }
  },

  async changePassword(passwordData: any) {
    try {
      const response = await apiClient.post('/users/change-password', passwordData);
      return response.data;
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to change password');
    }
  },
};

const apiService = {
  dashboard: dashboardService,
  testSuites: testSuiteService,
  testExecutions: testExecutionService,
  testGeneration: testGenerationService,
  reports: reportsService,
  users: userService,
};

export default apiService;
