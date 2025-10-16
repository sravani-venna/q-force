import React, { useState, useEffect, useRef } from 'react';
import {
  Grid,
  Card,
  CardContent,
  Typography,
  Box,
  Chip,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  CircularProgress,
  Button,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  IconButton,
  Fade,
  Slide,
  Zoom,
  Skeleton,
  LinearProgress,
} from '@mui/material';
import { dashboardService, testSuiteService, testExecutionService } from '../../services/apiService';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  BarChart,
  Bar
} from 'recharts';
import {
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
  CheckCircle as CheckCircleIcon,
  PlayArrow as PlayArrowIcon,
  Code as CodeIcon,
  Assessment as AssessmentIcon,
  MergeType as GitMergeIcon,
  AutoAwesome as AutoAwesomeIcon,
  Close as CloseIcon,
  Visibility as VisibilityIcon,
} from '@mui/icons-material';

// Enhanced chart colors with gradients
const CHART_COLORS = ['#1976d2', '#dc004e', '#9c27b0', '#2e7d32', '#ed6c02'];
const GRADIENT_COLORS = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
];

const StatCard: React.FC<{
  title: string;
  value: string | number;
  change?: number;
  icon: React.ReactNode;
  color: string;
  clickable?: boolean;
  onClick?: () => void;
}> = ({ title, value, change, icon, color, clickable = false, onClick }) => (
  <Card 
    sx={{ 
      height: '100%',
      cursor: clickable ? 'pointer' : 'default',
      transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
      borderRadius: 3,
      position: 'relative',
      overflow: 'hidden',
      '&:before': {
        content: '""',
        position: 'absolute',
        top: 0,
        left: 0,
        right: 0,
        height: '4px',
        background: `linear-gradient(90deg, ${color} 0%, ${color}80 100%)`,
      },
      '&:hover': clickable ? {
        transform: 'translateY(-8px) scale(1.02)',
        boxShadow: `0 20px 40px rgba(0,0,0,0.1), 0 0 0 1px ${color}20`,
        '& .stat-icon': {
          transform: 'scale(1.1) rotate(5deg)',
        }
      } : {}
    }}
    onClick={clickable ? onClick : undefined}
  >
    <CardContent sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <Box sx={{ flex: 1 }}>
          <Typography 
            color="textSecondary" 
            gutterBottom 
            variant="overline" 
            sx={{ 
              fontWeight: 600,
              letterSpacing: 1,
              textTransform: 'uppercase',
              fontSize: '0.75rem'
            }}
          >
            {title}
          </Typography>
          <Typography 
            variant="h3" 
            sx={{ 
              color,
              fontWeight: 700,
              background: `linear-gradient(135deg, ${color} 0%, ${color}80 100%)`,
              backgroundClip: 'text',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
              mb: 1
            }}
          >
            {value}
          </Typography>
          {change !== undefined && (
            <Box sx={{ display: 'flex', alignItems: 'center', mt: 1 }}>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                {change >= 0 ? (
                  <TrendingUpIcon sx={{ color: 'success.main', mr: 0.5 }} fontSize="small" />
                ) : (
                  <TrendingDownIcon sx={{ color: 'error.main', mr: 0.5 }} fontSize="small" />
                )}
                <Typography 
                  variant="body2" 
                  color={change >= 0 ? 'success.main' : 'error.main'}
                  sx={{ fontWeight: 600 }}
                >
                  {Math.abs(change)}%
                </Typography>
              </Box>
            </Box>
          )}
        </Box>
        <Box 
          className="stat-icon"
          sx={{ 
            color, 
            opacity: 0.9,
            transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
            filter: 'drop-shadow(0 4px 8px rgba(0,0,0,0.1))',
            display: 'flex',
            alignItems: 'center',
            gap: 1
          }}
        >
          {icon}
          {clickable && <VisibilityIcon sx={{ fontSize: 20, color: 'text.secondary' }} />}
        </Box>
      </Box>
    </CardContent>
  </Card>
);

const Dashboard: React.FC = () => {
  const [dashboardData, setDashboardData] = useState<any>(null);
  const [testSuites, setTestSuites] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [dataSource, setDataSource] = useState<'api' | 'fallback'>('api');
  const [testDetailsOpen, setTestDetailsOpen] = useState(false);
  const [runningTestsOpen, setRunningTestsOpen] = useState(false);
  const [executingTests, setExecutingTests] = useState(false);
  const [runningTestSuites, setRunningTestSuites] = useState<any[]>([]);
  const [realTestCases, setRealTestCases] = useState<any>(null);
  const [detailedTestCasesOpen, setDetailedTestCasesOpen] = useState(false);
  const [detailedTestCases, setDetailedTestCases] = useState<any[]>([]);
  const [testCaseStatus, setTestCaseStatus] = useState<string>('');
  const [loadingTestCases, setLoadingTestCases] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  // Function to execute tests for entire codebase
  const executeCodebaseTests = async () => {
    try {
      setExecutingTests(true);
      console.log('🧪 Starting codebase-wide test execution...');
      console.log('📊 Test suites to execute:', testSuites);
      
      // Mark all test suites as running
      const runningSuites = testSuites.map(suite => ({
        ...suite,
        status: 'RUNNING',
        startedAt: new Date().toISOString(),
        progress: 0
      }));
      setRunningTestSuites(runningSuites);
      console.log('🏃‍♂️ Marked test suites as running:', runningSuites);
      
      // Execute tests for all test suites
      const executionPromises = testSuites.map(async (suite, index) => {
        try {
          console.log(`🚀 Executing test suite ${suite.id} (${suite.name})...`);
          const result = await testExecutionService.start(suite.id);
          console.log(`✅ Test suite ${suite.id} execution started:`, result);
          return result;
        } catch (error) {
          console.error(`❌ Failed to execute test suite ${suite.id}:`, error);
          throw error;
        }
      });
      
      // Wait for all executions to start
      const results = await Promise.all(executionPromises);
      console.log('🎉 All test executions started:', results);
      
      // Poll for test status updates
      let pollCount = 0;
      const maxPolls = 24; // Poll for up to 2 minutes (24 * 5 seconds)
      
      const pollInterval = setInterval(async () => {
        pollCount++;
        console.log(`🔄 Polling for test status (attempt ${pollCount}/${maxPolls})...`);
        
        try {
          // Fetch updated test suites
          const response = await testSuiteService.getAll();
          const updatedSuites = response.data || [];
          
          // Update running test suites with latest status
          const stillRunning = runningSuites
            .map(runningSuite => {
              const updated = updatedSuites.find((s: any) => s.id === runningSuite.id);
              if (updated) {
                return {
                  ...runningSuite,
                  ...updated,
                  startedAt: runningSuite.startedAt // Keep original start time
                };
              }
              return runningSuite;
            })
            .filter((suite: any) => suite.status === 'RUNNING' || suite.status === 'PENDING');
          
          setRunningTestSuites(stillRunning);
          console.log(`📊 Still running: ${stillRunning.length} suites`);
          
          // Stop polling if all tests are complete or max polls reached
          if (stillRunning.length === 0 || pollCount >= maxPolls) {
            clearInterval(pollInterval);
            console.log('✅ Test execution polling complete');
            setRunningTestSuites([]);
            setExecutingTests(false);
            
            // Refresh dashboard data
            await fetchDashboardData();
          }
        } catch (error) {
          console.error('❌ Error polling test status:', error);
        }
      }, 5000); // Poll every 5 seconds
      
      console.log('✅ Codebase test execution initiated');
    } catch (error) {
      console.error('❌ Failed to execute codebase tests:', error);
      setError('Failed to execute codebase tests: ' + (error as Error).message);
      setRunningTestSuites([]);
      setExecutingTests(false);
    }
  };

  // Helper function to calculate test type distribution
  const getTestTypeDistribution = () => {
    const typeCount: { [key: string]: number } = {};
    (testSuites || []).forEach(suite => {
      typeCount[suite.type] = (typeCount[suite.type] || 0) + 1;
    });

    return Object.entries(typeCount).map(([type, count], index) => ({
      name: type,
      value: count,
      color: CHART_COLORS[index % CHART_COLORS.length]
    }));
  };

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      console.log('🚀 Fetching dashboard data...');
      console.log('🔑 Token in localStorage:', !!localStorage.getItem('token'));
      
      // Test direct API call first
      try {
        const [directStatsResponse, directSuitesResponse] = await Promise.all([
          fetch('http://localhost:8080/api/dashboard/stats'),
          fetch('http://localhost:8080/api/test-suites')
        ]);
        
        const directStatsData = await directStatsResponse.json();
        const directSuitesData = await directSuitesResponse.json();
        
        console.log('🔍 Direct API call results:');
        console.log('📊 Stats:', directStatsData);
        console.log('📝 Suites:', directSuitesData);
        
        if (directStatsData.success) {
          setDashboardData(directStatsData.data);
          setDataSource('api');
          console.log('✅ Using direct API stats data:', directStatsData.data);
        }
        
        if (directSuitesData.success) {
          setTestSuites(directSuitesData.data);
          setDataSource('api');
          console.log('✅ Using direct API suites data:', directSuitesData.data);
          console.log('📊 Test suites count:', directSuitesData.data.length);
          directSuitesData.data.forEach((suite: any, index: number) => {
            console.log(`📝 Suite ${index + 1}: ${suite.name} - ${suite.testCases?.length || 0} test cases`);
          });
        }
      } catch (directError) {
        console.error('❌ Direct API call failed:', directError);
      }
      
      // Fetch dashboard stats, test suites, and real test cases in parallel
      const [statsResponse, suitesResponse, realTestCasesResponse] = await Promise.all([
        dashboardService.getStats(),
        testSuiteService.getAll(),
        dashboardService.getRealTestCases()
      ]);

      console.log('📊 Stats response:', statsResponse);
      console.log('📝 Suites response:', suitesResponse);
      console.log('🎯 Real test cases response:', realTestCasesResponse);

      if (statsResponse && statsResponse.success) {
        setDashboardData(statsResponse.data);
        setDataSource('api');
        console.log('✅ Dashboard data set from API:', statsResponse.data);
      } else {
        console.warn('⚠️ Stats response not successful:', statsResponse);
      }
      
      if (suitesResponse && suitesResponse.success) {
        setTestSuites(suitesResponse.data);
        setDataSource('api');
        console.log('✅ Test suites set from API:', suitesResponse.data);
      } else {
        console.warn('⚠️ Suites response not successful:', suitesResponse);
      }

      if (realTestCasesResponse && realTestCasesResponse.success) {
        setRealTestCases(realTestCasesResponse.data);
        console.log('✅ Real test cases data set from API:', realTestCasesResponse.data);
      } else {
        console.warn('⚠️ Real test cases response not successful:', realTestCasesResponse);
      }

      // If no data was loaded, show error instead of fallback data
      if (!statsResponse?.success && !suitesResponse?.success) {
        console.log('📊 API calls failed - no fallback data used');
        setDataSource('api');
        setError('Failed to load dashboard data from API');
        return;
      }
    } catch (error) {
      console.error('❌ Error fetching dashboard data:', error);
      setError('Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  const handleRetry = () => {
    setError(null);
    setDataSource('api');
    window.location.reload(); // Simple retry by reloading
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  if (loading) {
    return (
      <Box 
        sx={{ 
          display: 'flex', 
          flexDirection: 'column',
          justifyContent: 'center', 
          alignItems: 'center', 
          height: '100vh'
        }}
      >
        <Box sx={{ textAlign: 'center', zIndex: 1, position: 'relative' }}>
          <CircularProgress 
            size={60} 
            thickness={4}
            sx={{ 
              color: 'white',
              mb: 3,
              filter: 'drop-shadow(0 4px 8px rgba(0,0,0,0.2))'
            }} 
          />
          <Typography 
            variant="h5" 
            sx={{ 
              color: 'white',
              fontWeight: 600,
              mb: 1
            }}
          >
            Loading Dashboard...
          </Typography>
          <Typography 
            variant="body1" 
            sx={{ 
              color: 'rgba(255,255,255,0.8)',
              fontWeight: 400
            }}
          >
            Fetching your test analytics
          </Typography>
        </Box>
      </Box>
    );
  }

  if (error) {
    return (
      <Box 
        sx={{ 
          display: 'flex', 
          flexDirection: 'column',
          justifyContent: 'center', 
          alignItems: 'center', 
          height: '100vh',
          p: 3
        }}
      >
        <Box sx={{ textAlign: 'center', zIndex: 1, position: 'relative', maxWidth: 500 }}>
          <Typography 
            variant="h4" 
            sx={{ 
              color: 'white',
              fontWeight: 700,
              mb: 2
            }}
          >
            ⚠️ Error Loading Dashboard
          </Typography>
          <Typography 
            variant="body1" 
            sx={{ 
              color: 'rgba(255,255,255,0.8)',
              fontWeight: 400,
              mb: 3
            }}
          >
            {error}
          </Typography>
          <Button 
            variant="contained"
            onClick={handleRetry}
            sx={{
              background: 'linear-gradient(135deg, #4caf50 0%, #2e7d32 100%)',
              color: 'white',
              fontWeight: 600,
              px: 4,
              py: 1.5,
              borderRadius: 2,
              boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
              '&:hover': {
                transform: 'translateY(-2px)',
                boxShadow: '0 6px 16px rgba(0,0,0,0.2)'
              }
            }}
          >
            🔄 Retry
          </Button>
        </Box>
      </Box>
    );
  }

  // Calculate additional stats from real data
  const passRate = dashboardData ? 
    ((dashboardData.passedTests / (dashboardData.passedTests + dashboardData.failedTests)) * 100).toFixed(1) : '0';
  
  // Calculate rounded coverage percentage
  const coveragePercentage = dashboardData ? 
    Math.round(dashboardData.coverage || 0) : 0;
  
  const runningTests = (testSuites || []).filter(suite => suite.status === 'RUNNING').length;

  // Function to fetch detailed test cases by status
  const fetchDetailedTestCases = async (status: string) => {
    try {
      setLoadingTestCases(true);
      setTestCaseStatus(status);
      console.log('🔍 Fetching detailed test cases for status:', status);
      
      const response = await testExecutionService.getDetailedTestCaseResults(status);
      console.log('📊 Detailed test cases response:', response);
      
      if (response && response.success) {
        setDetailedTestCases(response.data || []);
        setDetailedTestCasesOpen(true);
        console.log('✅ Loaded detailed test cases:', response.data?.length || 0);
      } else {
        console.warn('⚠️ No detailed test cases found for status:', status);
        setDetailedTestCases([]);
        setDetailedTestCasesOpen(true);
      }
    } catch (error) {
      console.error('❌ Error fetching detailed test cases:', error);
      setError('Failed to fetch detailed test cases: ' + (error as Error).message);
    } finally {
      setLoadingTestCases(false);
    }
  };

  return (
    <Box 
      sx={{ 
        p: 3,
        minHeight: '100vh'
      }}
    >
      {/* Header */}
      <Box 
        sx={{ 
          display: 'flex', 
          justifyContent: 'space-between', 
          alignItems: 'center', 
          mb: 4,
          position: 'relative',
          zIndex: 1
        }}
      >
        <Box>
          <Typography 
            variant="h3" 
            component="h1"
            sx={{
              fontWeight: 700,
              color: 'text.primary',
              mb: 1
            }}
          >
            Dashboard
          </Typography>
          <Typography 
            variant="body1" 
            sx={{ 
              color: 'text.secondary',
              fontWeight: 500
            }}
          >
            Test Platform Analytics & Insights
          </Typography>
        </Box>
      </Box>

      {/* Main Metrics Row */}
      <Grid container spacing={4} sx={{ mb: 4, position: 'relative', zIndex: 1 }}>
        <Grid item xs={12} sm={6} lg={3}>
          <StatCard
            title="Total Tests"
            value={dashboardData?.totalTests?.toLocaleString() || '0'}
            change={12}
            icon={<CodeIcon sx={{ fontSize: 40 }} />}
            color="#1976d2"
            clickable={true}
            onClick={() => setTestDetailsOpen(true)}
          />
        </Grid>
        <Grid item xs={12} sm={6} lg={3}>
          <StatCard
            title="Pass Rate"
            value={`${passRate}%`}
            change={2.1}
            icon={<CheckCircleIcon sx={{ fontSize: 40 }} />}
            color="#2e7d32"
            clickable={true}
            onClick={() => fetchDetailedTestCases('PASSED')}
          />
        </Grid>
        <Grid item xs={12} sm={6} lg={3}>
          <StatCard
            title="Failed Tests"
            value={dashboardData?.failedTests || 0}
            icon={<CheckCircleIcon sx={{ fontSize: 40 }} />}
            color="#d32f2f"
            clickable={true}
            onClick={() => fetchDetailedTestCases('FAILED')}
          />
        </Grid>
        <Grid item xs={12} sm={6} lg={3}>
          <StatCard
            title="Running Tests"
            value={runningTestSuites.length}
            icon={<PlayArrowIcon sx={{ fontSize: 40 }} />}
            color="#9c27b0"
            clickable={true}
            onClick={() => setRunningTestsOpen(true)}
          />
        </Grid>
      </Grid>

      {/* PR Metrics Row */}
      <Grid container spacing={4} sx={{ mb: 4, position: 'relative', zIndex: 1 }}>
        <Grid item xs={12} sm={6} lg={3}>
          <StatCard
            title="Active PRs"
            value={dashboardData?.activePRs || 0}
            icon={<GitMergeIcon sx={{ fontSize: 40 }} />}
            color="#1976d2"
          />
        </Grid>
        <Grid item xs={12} sm={6} lg={3}>
          <StatCard
            title="Merged PRs"
            value={dashboardData?.mergedPRs || 0}
            icon={<CheckCircleIcon sx={{ fontSize: 40 }} />}
            color="#2e7d32"
          />
        </Grid>
        <Grid item xs={12} sm={6} lg={3}>
          <StatCard
            title="Test Suites Generated"
            value={dashboardData?.generatedTestSuites || 0}
            icon={<AutoAwesomeIcon sx={{ fontSize: 40 }} />}
            color="#ed6c02"
          />
        </Grid>
        <Grid item xs={12} sm={6} lg={3}>
          <StatCard
            title="Coverage"
            value={`${coveragePercentage}%`}
            icon={<AssessmentIcon sx={{ fontSize: 40 }} />}
            color="#9c27b0"
          />
        </Grid>
      </Grid>

      {/* Charts Row */}
      <Grid container spacing={4} sx={{ mb: 4, position: 'relative', zIndex: 1 }}>
        <Grid item xs={12} lg={6}>
          <Card
            sx={{
              borderRadius: 3,
              boxShadow: '0 8px 32px rgba(0,0,0,0.1)',
              transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
              '&:hover': {
                transform: 'translateY(-4px)',
                boxShadow: '0 12px 40px rgba(0,0,0,0.15)'
              }
            }}
          >
            <CardContent sx={{ p: 3 }}>
              <Typography 
                variant="h6" 
                gutterBottom
                sx={{
                  fontWeight: 600,
                  color: 'text.primary',
                  mb: 3
                }}
              >
                📈 Test Execution Trends
              </Typography>
              <Box sx={{ height: 320, p: 2 }}>
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={dashboardData?.trendsData || []}>
                    <CartesianGrid strokeDasharray="3 3" stroke="rgba(0,0,0,0.1)" />
                    <XAxis 
                      dataKey="date" 
                      tick={{ fill: 'rgba(0,0,0,0.6)', fontSize: 12 }}
                    />
                    <YAxis 
                      tick={{ fill: 'rgba(0,0,0,0.6)', fontSize: 12 }}
                    />
                    <Tooltip 
                      contentStyle={{
                        background: 'rgba(0,0,0,0.8)',
                        border: 'none',
                        borderRadius: 8,
                        color: 'white'
                      }}
                    />
                    <Legend 
                      wrapperStyle={{ color: 'rgba(0,0,0,0.6)' }}
                    />
                    <Line 
                      type="monotone" 
                      dataKey="passed" 
                      stroke="#4caf50" 
                      strokeWidth={3}
                      name="Passed Tests"
                      dot={{ r: 6, fill: '#4caf50' }}
                    />
                    <Line 
                      type="monotone" 
                      dataKey="failed" 
                      stroke="#f44336" 
                      strokeWidth={3}
                      name="Failed Tests"
                      dot={{ r: 6, fill: '#f44336' }}
                    />
                    <Line 
                      type="monotone" 
                      dataKey="coverage" 
                      stroke="#2196f3" 
                      strokeWidth={3}
                      name="Coverage %"
                      dot={{ r: 6, fill: '#2196f3' }}
                    />
                  </LineChart>
                </ResponsiveContainer>
              </Box>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} lg={6}>
          <Card
            sx={{
              borderRadius: 3,
              boxShadow: '0 8px 32px rgba(0,0,0,0.1)',
              transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
              '&:hover': {
                transform: 'translateY(-4px)',
                boxShadow: '0 12px 40px rgba(0,0,0,0.15)'
              }
            }}
          >
            <CardContent sx={{ p: 3 }}>
              <Typography 
                variant="h6" 
                gutterBottom
                sx={{
                  fontWeight: 600,
                  color: 'text.primary',
                  mb: 3
                }}
              >
                🥧 Test Suite Distribution
              </Typography>
              <Box sx={{ height: 320, p: 2, backgroundColor: 'white', borderRadius: 1 }}>
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart style={{ backgroundColor: 'white' }}>
                    <Pie
                      data={(() => {
                        // Group test suites by service name
                        const serviceGroups: { [key: string]: number } = {};
                        
                        testSuites?.forEach((suite: any) => {
                          // Extract service name (e.g., "ProjectService" from "ProjectService UNIT Tests")
                          const serviceName = suite.name
                            ?.replace(/\s*(UNIT|INTEGRATION)\s*Tests?/gi, '')
                            ?.trim();
                          
                          if (serviceName) {
                            const testCount = suite.testCases ? suite.testCases.length : (suite.testCount || 0);
                            serviceGroups[serviceName] = (serviceGroups[serviceName] || 0) + testCount;
                          }
                        });
                        
                        // Convert to array format for chart
                        return Object.entries(serviceGroups).map(([name, count]) => ({
                          name,
                          value: count,
                          tests: count
                        }));
                      })()}
                      cx="50%"
                      cy="50%"
                      labelLine={false}
                      label={false}
                      outerRadius={80}
                      fill="#8884d8"
                      dataKey="value"
                    >
                      {(() => {
                        const serviceGroups: { [key: string]: number } = {};
                        testSuites?.forEach((suite: any) => {
                          const serviceName = suite.name?.replace(/\s*(UNIT|INTEGRATION)\s*Tests?/gi, '')?.trim();
                          if (serviceName) {
                            serviceGroups[serviceName] = (serviceGroups[serviceName] || 0) + 1;
                          }
                        });
                        const colors = ['#4caf50', '#2196f3', '#ff9800', '#f44336', '#9c27b0', '#00bcd4'];
                        return Object.keys(serviceGroups).map((key, index) => (
                          <Cell key={`cell-${index}`} fill={colors[index % colors.length]} />
                        ));
                      })()}
                    </Pie>
                    <Tooltip 
                      contentStyle={{
                        background: 'white',
                        border: '1px solid #e0e0e0',
                        borderRadius: 8,
                        color: 'black',
                        boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
                      }}
                      formatter={(value: any, name: any, props: any) => [
                        `${value} tests`, 
                        props.payload.name
                      ]}
                    />
                    <Legend 
                      verticalAlign="bottom" 
                      height={80}
                      formatter={(value: any) => value}
                      wrapperStyle={{ 
                        fontSize: '12px', 
                        padding: '5px',
                        color: 'rgba(0,0,0,0.6)'
                      }}
                      layout="horizontal"
                    />
                  </PieChart>
                </ResponsiveContainer>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Test Results Overview */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Test Results Overview
              </Typography>
              <Box sx={{ height: 320, p: 2 }}>
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={[
                    { name: 'Total', value: dashboardData?.totalTests || 0, fill: '#1976d2' },
                    { name: 'Passed', value: dashboardData?.passedTests || 0, fill: '#2e7d32' },
                    { name: 'Failed', value: dashboardData?.failedTests || 0, fill: '#d32f2f' },
                    { name: 'Suites', value: dashboardData?.generatedTestSuites || 0, fill: '#ed6c02' }
                  ]}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="name" />
                    <YAxis />
                    <Tooltip />
                    <Bar dataKey="value" />
                  </BarChart>
                </ResponsiveContainer>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Recent PRs */}
      {dashboardData?.recentPRs && dashboardData.recentPRs.length > 0 && (
        <Card sx={{ mb: 3 }}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Recent Pull Requests
            </Typography>
            <List>
              {dashboardData.recentPRs.map((pr: any) => (
                <ListItem key={pr.id}>
                  <ListItemIcon>
                    <GitMergeIcon />
                  </ListItemIcon>
                  <ListItemText
                    primary={`PR #${pr.number}: ${pr.title}`}
                    secondary={`Status: ${pr.status} | Tests: ${pr.testsGenerated} | Pass Rate: ${pr.passRate}%`}
                  />
                </ListItem>
              ))}
            </List>
          </CardContent>
        </Card>
      )}

      {/* Test Details Dialog */}
      <Dialog
        open={testDetailsOpen}
        onClose={() => setTestDetailsOpen(false)}
        maxWidth="lg"
        fullWidth
        PaperProps={{
          sx: { 
            minHeight: '70vh',
            maxHeight: '90vh'
          }
        }}
      >
        <DialogTitle>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography variant="h6">
              Test Details - {dashboardData?.totalTests || 0} Total Tests
            </Typography>
            <IconButton onClick={() => setTestDetailsOpen(false)}>
              <CloseIcon />
            </IconButton>
          </Box>
        </DialogTitle>
        <DialogContent>
          <Box sx={{ mb: 3 }}>
            <Grid container spacing={2}>
              <Grid item xs={3}>
                <Card sx={{ textAlign: 'center', p: 2 }}>
                  <Typography variant="h4" color="primary.main">
                    {dashboardData?.totalTests || 0}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Total
                  </Typography>
                </Card>
              </Grid>
              <Grid item xs={3}>
                <Card sx={{ textAlign: 'center', p: 2 }}>
                  <Typography variant="h4" color="success.main">
                    {dashboardData?.passedTests || 0}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Passed
                  </Typography>
                </Card>
              </Grid>
              <Grid item xs={3}>
                <Card sx={{ textAlign: 'center', p: 2 }}>
                  <Typography variant="h4" color="error.main">
                    {dashboardData?.failedTests || 0}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Failed
                  </Typography>
                </Card>
              </Grid>
              <Grid item xs={3}>
                <Card sx={{ textAlign: 'center', p: 2 }}>
                  <Typography variant="h4" color="warning.main">
                    {dashboardData?.runningTests || 0}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Running
                  </Typography>
                </Card>
              </Grid>
            </Grid>
          </Box>

          <Typography variant="h6" sx={{ mb: 2 }}>
            Test Suites ({testSuites.length})
          </Typography>
          
          <TableContainer component={Paper} sx={{ maxHeight: 400 }}>
            <Table stickyHeader>
              <TableHead>
                <TableRow>
                  <TableCell>Suite Name</TableCell>
                  <TableCell>Type</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Tests</TableCell>
                  <TableCell>Coverage</TableCell>
                  <TableCell>Last Run</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {testSuites.map((suite) => (
                  <TableRow key={suite.id}>
                    <TableCell>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <AssessmentIcon 
                          color={suite.status === 'COMPLETED' ? 'success' : 
                                 suite.status === 'RUNNING' ? 'primary' : 'action'} 
                        />
                        <Typography variant="body2" fontWeight="medium">
                          {suite.name}
                        </Typography>
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Chip 
                        label={suite.type} 
                        size="small" 
                        color={suite.type === 'UNIT' ? 'primary' : 
                               suite.type === 'INTEGRATION' ? 'secondary' : 'default'}
                      />
                    </TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        label={suite.status}
                        color={suite.status === 'COMPLETED' ? 'success' : 
                               suite.status === 'RUNNING' ? 'primary' : 'default'}
                        variant="outlined"
                      />
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2">
                        {suite.testCases?.length || 0} tests
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2">
                        {suite.coverage}%
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" color="text.secondary">
                        {suite.lastRun ? new Date(suite.lastRun).toLocaleDateString() : 'Never'}
                      </Typography>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>

          {testSuites.length === 0 && (
            <Box sx={{ textAlign: 'center', py: 4 }}>
              <AssessmentIcon sx={{ fontSize: 64, color: 'text.secondary', mb: 2 }} />
              <Typography variant="h6" color="text.secondary">
                No test suites found
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Generate some tests to see them here
              </Typography>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setTestDetailsOpen(false)}>
            Close
          </Button>
        </DialogActions>
      </Dialog>

      {/* Running Tests Dialog */}
      <Dialog
        open={runningTestsOpen}
        onClose={() => setRunningTestsOpen(false)}
        maxWidth="lg"
        fullWidth
        PaperProps={{
          sx: { 
            minHeight: '70vh',
            maxHeight: '90vh'
          }
        }}
      >
        <DialogTitle>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography variant="h6">
              Running Tests & Codebase Execution
            </Typography>
            <IconButton onClick={() => setRunningTestsOpen(false)}>
              <CloseIcon />
            </IconButton>
          </Box>
        </DialogTitle>
        <DialogContent>
          <Box sx={{ mb: 3 }}>
            <Grid container spacing={2}>
              <Grid item xs={3}>
                <Card sx={{ textAlign: 'center', p: 2 }}>
                  <Typography variant="h4" color="primary.main">
                    {runningTestSuites.length}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Currently Running
                  </Typography>
                </Card>
              </Grid>
              <Grid item xs={3}>
                <Card sx={{ textAlign: 'center', p: 2 }}>
                  <Typography variant="h4" color="success.main">
                    {dashboardData?.passedTests || 0}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Passed
                  </Typography>
                </Card>
              </Grid>
              <Grid item xs={3}>
                <Card sx={{ textAlign: 'center', p: 2 }}>
                  <Typography variant="h4" color="error.main">
                    {dashboardData?.failedTests || 0}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Failed
                  </Typography>
                </Card>
              </Grid>
              <Grid item xs={3}>
                <Card sx={{ textAlign: 'center', p: 2 }}>
                  <Typography variant="h4" color="warning.main">
                    {testSuites.length}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Total Suites
                  </Typography>
                </Card>
              </Grid>
            </Grid>
          </Box>

          <Box sx={{ mb: 3 }}>
            <Typography variant="h6" sx={{ mb: 2 }}>
              Execute Tests for Entire Codebase
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              Run all test suites across your entire codebase to ensure comprehensive coverage.
            </Typography>
            <Button
              variant="contained"
              color="primary"
              size="large"
              startIcon={executingTests ? <CircularProgress size={20} /> : <PlayArrowIcon />}
              onClick={executeCodebaseTests}
              disabled={executingTests}
              sx={{ mb: 2 }}
            >
              {executingTests ? 'Executing Tests... (1-2 min)' : 'Run All Tests'}
            </Button>
            {executingTests && (
              <Alert severity="info" sx={{ mt: 2 }}>
                Running tests across all {testSuites.length} test suites. This may take a few minutes...
              </Alert>
            )}
          </Box>

          <Typography variant="h6" sx={{ mb: 2 }}>
            Currently Running Test Suites
          </Typography>
          
          {runningTestSuites.length > 0 && (
            <Alert severity="info" sx={{ mb: 2 }}>
              {runningTestSuites.length} test suites are currently running. This may take 1-2 minutes to complete.
            </Alert>
          )}
          
          <TableContainer component={Paper} sx={{ maxHeight: 400 }}>
            <Table stickyHeader>
              <TableHead>
                <TableRow>
                  <TableCell>Suite Name</TableCell>
                  <TableCell>Type</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Progress</TableCell>
                  <TableCell>Coverage</TableCell>
                  <TableCell>Started</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {runningTestSuites.map((suite) => (
                  <TableRow key={suite.id}>
                    <TableCell>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <PlayArrowIcon color="primary" />
                        <Typography variant="body2" fontWeight="medium">
                          {suite.name}
                        </Typography>
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Chip 
                        label={suite.type} 
                        size="small" 
                        color={suite.type === 'UNIT' ? 'primary' : 
                               suite.type === 'INTEGRATION' ? 'secondary' : 'default'}
                      />
                    </TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        label="RUNNING"
                        color="primary"
                        variant="outlined"
                      />
                    </TableCell>
                    <TableCell>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <CircularProgress size={16} />
                        <Typography variant="body2">
                          In Progress
                        </Typography>
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2">
                        {suite.coverage}%
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" color="text.secondary">
                        {suite.startedAt ? new Date(suite.startedAt).toLocaleTimeString() : 'Just now'}
                      </Typography>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>

          {runningTestSuites.length === 0 && (
            <Box sx={{ textAlign: 'center', py: 4 }}>
              <PlayArrowIcon sx={{ fontSize: 64, color: 'text.secondary', mb: 2 }} />
              <Typography variant="h6" color="text.secondary">
                No tests currently running
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Click "Run All Tests" to execute tests across your entire codebase
              </Typography>
            </Box>
          )}

          {/* Completed Test Cases Section */}
          <Box sx={{ mt: 4 }}>
            <Typography variant="h6" sx={{ mb: 2 }}>
              Recently Completed Test Cases
            </Typography>
            
            <TableContainer component={Paper} sx={{ maxHeight: 300 }}>
              <Table stickyHeader>
                <TableHead>
                  <TableRow>
                    <TableCell>Test Case</TableCell>
                    <TableCell>Suite</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Duration</TableCell>
                    <TableCell>Completed</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {(() => {
                    // Extract all test cases from completed test suites
                    const completedTestCases: any[] = [];
                    testSuites
                      .filter(suite => suite.status === 'COMPLETED' && suite.testCases && suite.testCases.length > 0)
                      .forEach(suite => {
                        suite.testCases.forEach((testCase: any) => {
                          completedTestCases.push({
                            ...testCase,
                            suiteName: suite.name,
                            suiteType: suite.type,
                            lastRun: suite.lastRun
                          });
                        });
                      });
                    
                    // Sort by most recent and take top 10
                    const recentTests = completedTestCases
                      .sort((a, b) => {
                        if (!a.lastRun) return 1;
                        if (!b.lastRun) return -1;
                        return new Date(b.lastRun).getTime() - new Date(a.lastRun).getTime();
                      })
                      .slice(0, 10);
                    
                    if (recentTests.length === 0) {
                      return (
                        <TableRow>
                          <TableCell colSpan={5} align="center">
                            <Typography variant="body2" color="text.secondary" sx={{ py: 2 }}>
                              No completed test cases yet. Run tests to see results here.
                            </Typography>
                          </TableCell>
                        </TableRow>
                      );
                    }
                    
                    return recentTests.map((testCase, index) => (
                      <TableRow key={index}>
                        <TableCell>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            {testCase.status === 'PASSED' ? (
                              <CheckCircleIcon color="success" fontSize="small" />
                            ) : (
                              <CloseIcon color="error" fontSize="small" />
                            )}
                            <Typography variant="body2" fontWeight="medium">
                              {testCase.name}
                            </Typography>
                          </Box>
                        </TableCell>
                        <TableCell>
                          <Typography variant="body2" sx={{ fontSize: '0.85rem' }}>
                            {testCase.suiteName}
                          </Typography>
                        </TableCell>
                        <TableCell>
                          <Chip
                            size="small"
                            label={testCase.status}
                            color={testCase.status === 'PASSED' ? 'success' : 
                                   testCase.status === 'FAILED' ? 'error' : 'default'}
                            variant="outlined"
                          />
                        </TableCell>
                        <TableCell>
                          <Typography variant="body2">
                            {testCase.suiteType}
                          </Typography>
                        </TableCell>
                        <TableCell>
                          <Typography variant="body2" color="text.secondary">
                            {testCase.lastRun ? new Date(testCase.lastRun).toLocaleTimeString() : 'Just now'}
                          </Typography>
                        </TableCell>
                      </TableRow>
                    ));
                  })()}
                </TableBody>
              </Table>
            </TableContainer>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRunningTestsOpen(false)}>
            Close
          </Button>
        </DialogActions>
      </Dialog>

      {/* Detailed Test Cases Dialog */}
      <Dialog
        open={detailedTestCasesOpen}
        onClose={() => setDetailedTestCasesOpen(false)}
        maxWidth="lg"
        fullWidth
        PaperProps={{
          sx: { 
            minHeight: '70vh',
            maxHeight: '90vh'
          }
        }}
      >
        <DialogTitle>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography variant="h6">
              {testCaseStatus === 'PASSED' ? 'Passed Test Cases' : 
               testCaseStatus === 'FAILED' ? 'Failed Test Cases' : 
               'All Test Cases'} ({detailedTestCases.length})
            </Typography>
            <IconButton onClick={() => setDetailedTestCasesOpen(false)}>
              <CloseIcon />
            </IconButton>
          </Box>
        </DialogTitle>
        <DialogContent>
          {loadingTestCases ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', py: 4 }}>
              <CircularProgress />
              <Typography sx={{ ml: 2 }}>Loading test cases...</Typography>
            </Box>
          ) : detailedTestCases.length > 0 ? (
            <TableContainer component={Paper} sx={{ maxHeight: 500 }}>
              <Table stickyHeader>
                <TableHead>
                  <TableRow>
                    <TableCell>Test Case</TableCell>
                    <TableCell>Suite</TableCell>
                    <TableCell>Type</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Priority</TableCell>
                    <TableCell>Execution Time</TableCell>
                    <TableCell>Executed At</TableCell>
                    <TableCell>Error Message</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {detailedTestCases.map((testCase) => (
                    <TableRow key={testCase.id}>
                      <TableCell>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <CheckCircleIcon 
                            color={testCase.status === 'PASSED' ? 'success' : 
                                   testCase.status === 'FAILED' ? 'error' : 'action'} 
                          />
                          <Box>
                            <Typography variant="body2" fontWeight="medium">
                              {testCase.name}
                            </Typography>
                            {testCase.description && (
                              <Typography variant="caption" color="text.secondary">
                                {testCase.description}
                              </Typography>
                            )}
                          </Box>
                        </Box>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2">
                          {testCase.suiteName}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Chip 
                          label={testCase.type} 
                          size="small" 
                          color={testCase.type === 'UNIT' ? 'primary' : 
                                 testCase.type === 'INTEGRATION' ? 'secondary' : 'default'}
                        />
                      </TableCell>
                      <TableCell>
                        <Chip
                          size="small"
                          label={testCase.status}
                          color={testCase.status === 'PASSED' ? 'success' : 
                                 testCase.status === 'FAILED' ? 'error' : 'default'}
                          variant="outlined"
                        />
                      </TableCell>
                      <TableCell>
                        <Chip
                          size="small"
                          label={testCase.priority}
                          color={testCase.priority === 'HIGH' ? 'error' : 
                                 testCase.priority === 'MEDIUM' ? 'warning' : 'default'}
                          variant="outlined"
                        />
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2">
                          {testCase.executionTime ? `${testCase.executionTime}ms` : 'N/A'}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" color="text.secondary">
                          {testCase.executedAt ? new Date(testCase.executedAt).toLocaleString() : 'Never'}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        {testCase.errorMessage ? (
                          <Typography variant="body2" color="error.main" sx={{ maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis' }}>
                            {testCase.errorMessage}
                          </Typography>
                        ) : (
                          <Typography variant="body2" color="text.secondary">
                            No errors
                          </Typography>
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          ) : (
            <Box sx={{ textAlign: 'center', py: 4 }}>
              <CheckCircleIcon sx={{ fontSize: 64, color: 'text.secondary', mb: 2 }} />
              <Typography variant="h6" color="text.secondary">
                No {testCaseStatus.toLowerCase()} test cases found
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {testCaseStatus === 'PASSED' ? 'All tests are failing or pending' : 
                 testCaseStatus === 'FAILED' ? 'All tests are passing or pending' : 
                 'No test cases available'}
              </Typography>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDetailedTestCasesOpen(false)}>
            Close
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Dashboard;