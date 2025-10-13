import React, { createContext, useContext, useState, ReactNode } from 'react';

interface User {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  permissions: string[];
}

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (userData: any) => Promise<void>;
  logout: () => void;
  updateUser: (userData: Partial<User>) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

// Demo user for frontend-only mode
const DEMO_USER: User = {
  id: '1',
  username: 'admin',
  email: 'admin@testplatform.com',
  firstName: 'Demo',
  lastName: 'Admin',
  role: 'SUPER_ADMIN',
  permissions: [
    'MANAGE_USERS', 'MANAGE_PROJECTS', 'MANAGE_TESTS', 
    'EXECUTE_TESTS', 'VIEW_REPORTS', 'MANAGE_CONFIGURATIONS'
  ]
};

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(false);

  const isAuthenticated = !!user;

  const login = async (email: string, password: string) => {
    setLoading(true);
    
    // Demo login - accept admin credentials or any credentials for demo
    if ((email === 'admin@testplatform.com' || email === 'admin') && password === 'admin123') {
      setTimeout(() => {
        setUser(DEMO_USER);
        localStorage.setItem('demo_token', 'demo-jwt-token');
        setLoading(false);
      }, 1000); // Simulate API delay
    } else {
      // For demo purposes, accept any credentials
      setTimeout(() => {
        setUser({
          ...DEMO_USER,
          email: email,
          username: email.split('@')[0]
        });
        localStorage.setItem('demo_token', 'demo-jwt-token');
        setLoading(false);
      }, 1000);
    }
  };

  const register = async (userData: any) => {
    setLoading(true);
    setTimeout(() => {
      setUser({
        id: '2',
        username: userData.username,
        email: userData.email,
        firstName: userData.firstName,
        lastName: userData.lastName,
        role: 'DEVELOPER',
        permissions: ['CREATE_TESTS', 'EXECUTE_TESTS', 'VIEW_REPORTS']
      });
      localStorage.setItem('demo_token', 'demo-jwt-token');
      setLoading(false);
    }, 1000);
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('demo_token');
  };

  const updateUser = (userData: Partial<User>) => {
    if (user) {
      setUser({ ...user, ...userData });
    }
  };

  // Auto-login for demo if token exists
  React.useEffect(() => {
    const token = localStorage.getItem('demo_token');
    if (token && !user) {
      setUser(DEMO_USER);
    }
  }, [user]);

  const value: AuthContextType = {
    user,
    isAuthenticated,
    loading,
    login,
    register,
    logout,
    updateUser,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};
