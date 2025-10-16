import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { dashboardService } from '../services/apiService';

interface Repository {
  id: string;
  name: string;
  testCount: string;
}

interface RepositoryContextType {
  repositories: Repository[];
  selectedRepository: string;
  setSelectedRepository: (repoId: string) => void;
  loading: boolean;
}

const RepositoryContext = createContext<RepositoryContextType | undefined>(undefined);

export const RepositoryProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [repositories, setRepositories] = useState<Repository[]>([]);
  const [selectedRepository, setSelectedRepository] = useState<string>('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchRepositories();
  }, []);

  const fetchRepositories = async () => {
    try {
      setLoading(true);
      console.log('📚 Fetching available repositories...');
      const response = await dashboardService.getRepositories();
      console.log('📦 Repositories response:', response);
      
      if (response && response.success && response.data) {
        setRepositories(response.data);
        // Set default repository if none selected
        if (response.data.length > 0 && !selectedRepository) {
          setSelectedRepository(response.data[0].id);
        }
      }
    } catch (error) {
      console.error('❌ Error fetching repositories:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <RepositoryContext.Provider
      value={{
        repositories,
        selectedRepository,
        setSelectedRepository,
        loading,
      }}
    >
      {children}
    </RepositoryContext.Provider>
  );
};

export const useRepository = (): RepositoryContextType => {
  const context = useContext(RepositoryContext);
  if (context === undefined) {
    throw new Error('useRepository must be used within a RepositoryProvider');
  }
  return context;
};

