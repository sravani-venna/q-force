import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';


interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

interface AuthResponse {
  token: string;
  refreshToken: string;
  user: {
    id: string;
    username: string;
    email: string;
    firstName: string;
    lastName: string;
    role: string;
    permissions: string[];
  };
}

interface User {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  permissions: string[];
}

class AuthService {
  private apiClient = axios.create({
    baseURL: `${API_BASE_URL}/api/auth`,
    headers: {
      'Content-Type': 'application/json',
    },
  });

  constructor() {
    // Add request interceptor to include token
    this.apiClient.interceptors.request.use(
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
    this.apiClient.interceptors.response.use(
      (response) => response,
      async (error) => {
        if (error.response?.status === 401) {
          // Token expired, try to refresh
          try {
            await this.refreshToken();
            // Retry the original request
            return this.apiClient.request(error.config);
          } catch (refreshError) {
            // Refresh failed, logout user
            this.logout();
            window.location.href = '/login';
          }
        }
        return Promise.reject(error);
      }
    );
  }

  async login(email: string, password: string): Promise<AuthResponse> {
    try {
      const response = await this.apiClient.post<any>('/login', {
        email,
        password,
      });
      
      if (response.data.success) {
        // Convert backend response to expected format
        const authData: AuthResponse = {
          token: response.data.data.token,
          refreshToken: response.data.data.token, // Use same token for simplicity
          user: {
            id: response.data.data.user.id.toString(),
            username: response.data.data.user.name,
            email: response.data.data.user.email,
            firstName: response.data.data.user.name.split(' ')[0] || '',
            lastName: response.data.data.user.name.split(' ')[1] || '',
            role: response.data.data.user.role,
            permissions: ['CREATE_TESTS', 'EXECUTE_TESTS', 'VIEW_REPORTS', 'VIEW_PRS', 'MANAGE_USERS']
          }
        };
        
        // Store tokens
        localStorage.setItem('token', authData.token);
        localStorage.setItem('refreshToken', authData.refreshToken);
        
        return authData;
      } else {
        throw new Error(response.data.message || 'Login failed');
      }
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Login failed');
    }
  }

  async register(userData: RegisterRequest): Promise<AuthResponse> {
    try {
      const response = await this.apiClient.post<{ data: AuthResponse }>('/register', userData);
      
      return response.data.data;
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Registration failed');
    }
  }

  async refreshToken(): Promise<AuthResponse> {
    try {
      const refreshToken = localStorage.getItem('refreshToken');
      if (!refreshToken) {
        throw new Error('No refresh token available');
      }

      const response = await this.apiClient.post<{ data: AuthResponse }>('/refresh', {
        refreshToken,
      });

      const authData = response.data.data;
      localStorage.setItem('token', authData.token);
      localStorage.setItem('refreshToken', authData.refreshToken);

      return authData;
    } catch (error: any) {
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      throw new Error(error.response?.data?.message || 'Token refresh failed');
    }
  }

  async getCurrentUser(): Promise<User> {
    try {
      const response = await this.apiClient.get<{ data: User }>('/me');
      return response.data.data;
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to get user info');
    }
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
  }

  isAuthenticated(): boolean {
    return !!localStorage.getItem('token');
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }
}

export const authService = new AuthService();
