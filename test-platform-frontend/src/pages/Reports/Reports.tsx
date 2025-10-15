import React, { useState, useEffect } from 'react';
import {
  Typography,
  Box,
  Card,
  CardContent,
  Grid,
  CircularProgress,
  Paper,
  Chip,
  Stack,
} from '@mui/material';
import {
  PieChart,
  Pie,
  BarChart,
  Bar,
  LineChart,
  Line,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  RadialBarChart,
  RadialBar,
} from 'recharts';
import {
  TrendingUp as TrendingUpIcon,
  Speed as SpeedIcon,
  CheckCircle as CheckCircleIcon,
  Assessment as AssessmentIcon,
} from '@mui/icons-material';

const Reports: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [dashboardData, setDashboardData] = useState<any>(null);
  const [testSuites, setTestSuites] = useState<any[]>([]);

  useEffect(() => {
    fetchReportsData();
  }, []);

  const fetchReportsData = async () => {
    try {
      const [statsRes, suitesRes] = await Promise.all([
        fetch('http://localhost:8080/api/dashboard/stats'),
        fetch('http://localhost:8080/api/tests/suites'),
      ]);

      const statsData = await statsRes.json();
      const suitesData = await suitesRes.json();

      setDashboardData(statsData.data);
      setTestSuites(suitesData.data || []);
      setLoading(false);
    } catch (error) {
      console.error('Error fetching reports data:', error);
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '400px' }}>
        <CircularProgress />
      </Box>
    );
  }

  // Calculate coverage metrics
  const totalTests = dashboardData?.totalTests || 0;
  const passedTests = dashboardData?.passedTests || 0;
  const failedTests = dashboardData?.failedTests || 0;
  const passRate = totalTests > 0 ? ((passedTests / totalTests) * 100).toFixed(1) : '0';
  const coverage = parseFloat(passRate);

  // Coverage data for radial chart
  const coverageData = [
    {
      name: 'Coverage',
      value: coverage,
      fill: coverage >= 80 ? '#4caf50' : coverage >= 60 ? '#ff9800' : '#f44336',
    },
  ];

  // Performance metrics data - Group by service (combine UNIT and INTEGRATION)
  const performanceData = (() => {
    const serviceGroups: { [key: string]: { passed: number; failed: number; tests: number } } = {};
    
    testSuites.forEach((suite) => {
      // Extract service name (e.g., "ProjectService" from "ProjectService UNIT Tests")
      const serviceName = suite.name
        ?.replace(/\s*(UNIT|INTEGRATION)\s*Tests?/gi, '')
        ?.trim();
      
      if (serviceName) {
        if (!serviceGroups[serviceName]) {
          serviceGroups[serviceName] = { passed: 0, failed: 0, tests: 0 };
        }
        
        const passed = suite.testCases?.filter((t: any) => t.status === 'PASSED').length || 0;
        const failed = suite.testCases?.filter((t: any) => t.status === 'FAILED').length || 0;
        const tests = suite.testCases?.length || 0;
        
        serviceGroups[serviceName].passed += passed;
        serviceGroups[serviceName].failed += failed;
        serviceGroups[serviceName].tests += tests;
      }
    });
    
    return Object.entries(serviceGroups).map(([name, metrics]) => ({
      name,
      ...metrics,
    }));
  })();

  // Execution time trends
  const trendData = dashboardData?.trendsData?.map((trend: any) => ({
    date: new Date(trend.date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
    passed: trend.passed,
    failed: trend.failed,
    total: trend.passed + trend.failed,
  })) || [];

  // Test suite distribution for pie chart - Group by service (combine UNIT and INTEGRATION)
  const suiteDistribution = (() => {
    const serviceGroups: { [key: string]: number } = {};
    
    testSuites.forEach((suite) => {
      // Extract service name (e.g., "ProjectService" from "ProjectService UNIT Tests")
      const serviceName = suite.name
        ?.replace(/\s*(UNIT|INTEGRATION)\s*Tests?/gi, '')
        ?.trim();
      
      if (serviceName) {
        const testCount = suite.testCases?.length || 0;
        serviceGroups[serviceName] = (serviceGroups[serviceName] || 0) + testCount;
      }
    });
    
    const colors = ['#2196f3', '#4caf50', '#ff9800', '#f44336', '#9c27b0', '#00bcd4', '#8bc34a'];
    return Object.entries(serviceGroups)
      .map(([name, value], index) => ({
        name,
        value,
        color: colors[index % colors.length],
      }))
      .filter(s => s.value > 0);
  })();

  const avgExecutionTime = dashboardData?.executionTime 
    ? (dashboardData.executionTime / 1000).toFixed(2) 
    : '0';

  return (
    <Box>
      <Typography variant="h4" sx={{ mb: 3, fontWeight: 'bold' }}>
        Reports & Analytics
      </Typography>

      {/* Summary Cards */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Paper sx={{ p: 2, background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' }}>
            <Stack direction="row" alignItems="center" spacing={2}>
              <CheckCircleIcon sx={{ fontSize: 40, color: 'white' }} />
              <Box>
                <Typography variant="h4" sx={{ color: 'white', fontWeight: 'bold' }}>
                  {passRate}%
                </Typography>
                <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.9)' }}>
                  Pass Rate
                </Typography>
              </Box>
            </Stack>
          </Paper>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Paper sx={{ p: 2, background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' }}>
            <Stack direction="row" alignItems="center" spacing={2}>
              <SpeedIcon sx={{ fontSize: 40, color: 'white' }} />
              <Box>
                <Typography variant="h4" sx={{ color: 'white', fontWeight: 'bold' }}>
                  {avgExecutionTime}s
                </Typography>
                <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.9)' }}>
                  Avg Execution
                </Typography>
              </Box>
            </Stack>
          </Paper>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Paper sx={{ p: 2, background: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' }}>
            <Stack direction="row" alignItems="center" spacing={2}>
              <AssessmentIcon sx={{ fontSize: 40, color: 'white' }} />
              <Box>
                <Typography variant="h4" sx={{ color: 'white', fontWeight: 'bold' }}>
                  {totalTests}
                </Typography>
                <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.9)' }}>
                  Total Tests
                </Typography>
              </Box>
            </Stack>
          </Paper>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Paper sx={{ p: 2, background: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)' }}>
            <Stack direction="row" alignItems="center" spacing={2}>
              <TrendingUpIcon sx={{ fontSize: 40, color: 'white' }} />
              <Box>
                <Typography variant="h4" sx={{ color: 'white', fontWeight: 'bold' }}>
                  {testSuites.length}
                </Typography>
                <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.9)' }}>
                  Test Suites
                </Typography>
              </Box>
            </Stack>
          </Paper>
        </Grid>
      </Grid>

      {/* Charts Section */}
      <Grid container spacing={3}>
        {/* Test Coverage Chart */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <CheckCircleIcon color="primary" />
                Test Coverage Report
              </Typography>
              <Box sx={{ height: 300, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
                <ResponsiveContainer width="100%" height="100%">
                  <RadialBarChart
                    cx="50%"
                    cy="50%"
                    innerRadius="60%"
                    outerRadius="90%"
                    data={coverageData}
                    startAngle={180}
                    endAngle={0}
                  >
                    <RadialBar
                      background
                      dataKey="value"
                      cornerRadius={10}
                    />
                    <text
                      x="50%"
                      y="50%"
                      textAnchor="middle"
                      dominantBaseline="middle"
                      className="progress-label"
                      style={{ fontSize: '32px', fontWeight: 'bold', fill: coverageData[0].fill }}
                    >
                      {coverage}%
                    </text>
                    <text
                      x="50%"
                      y="60%"
                      textAnchor="middle"
                      dominantBaseline="middle"
                      style={{ fontSize: '14px', fill: '#666' }}
                    >
                      Pass Rate
                    </text>
                  </RadialBarChart>
                </ResponsiveContainer>
                <Stack direction="row" spacing={2} sx={{ mt: 2 }}>
                  <Chip label={`${passedTests} Passed`} color="success" size="small" />
                  <Chip label={`${failedTests} Failed`} color="error" size="small" />
                  <Chip label={`${totalTests - passedTests - failedTests} Pending`} size="small" />
                </Stack>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Performance Metrics Chart */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <SpeedIcon color="primary" />
                Performance Metrics by Suite
              </Typography>
              <Box sx={{ height: 300 }}>
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={performanceData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="name" angle={-45} textAnchor="end" height={80} />
                    <YAxis />
                    <Tooltip />
                    <Legend />
                    <Bar dataKey="passed" fill="#4caf50" name="Passed" />
                    <Bar dataKey="failed" fill="#f44336" name="Failed" />
                  </BarChart>
                </ResponsiveContainer>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Test Execution Trends */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <TrendingUpIcon color="primary" />
                Test Execution Trends
              </Typography>
              <Box sx={{ height: 300 }}>
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={trendData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="date" />
                    <YAxis />
                    <Tooltip />
                    <Legend />
                    <Line type="monotone" dataKey="passed" stroke="#4caf50" strokeWidth={2} name="Passed" />
                    <Line type="monotone" dataKey="failed" stroke="#f44336" strokeWidth={2} name="Failed" />
                    <Line type="monotone" dataKey="total" stroke="#2196f3" strokeWidth={2} name="Total" />
                  </LineChart>
                </ResponsiveContainer>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Test Suite Distribution */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <AssessmentIcon color="primary" />
                Test Suite Distribution
              </Typography>
              <Box sx={{ height: 300 }}>
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={suiteDistribution}
                      cx="50%"
                      cy="50%"
                      labelLine={false}
                      label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                      outerRadius={80}
                      fill="#8884d8"
                      dataKey="value"
                    >
                      {suiteDistribution.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.color} />
                      ))}
                    </Pie>
                    <Tooltip />
                  </PieChart>
                </ResponsiveContainer>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Reports;
