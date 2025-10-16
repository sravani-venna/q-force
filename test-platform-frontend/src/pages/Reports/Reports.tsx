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
  Button,
  Menu,
  MenuItem,
  ListItemIcon,
  ListItemText,
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
  Download as DownloadIcon,
  ArrowDropDown as ArrowDropDownIcon,
  Description as DescriptionIcon,
  TableChart as TableChartIcon,
  PictureAsPdf as PictureAsPdfIcon,
} from '@mui/icons-material';

const Reports: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [dashboardData, setDashboardData] = useState<any>(null);
  const [testSuites, setTestSuites] = useState<any[]>([]);
  const [serviceTests, setServiceTests] = useState<any[]>([]);
  const [downloadAnchorEl, setDownloadAnchorEl] = useState<null | HTMLElement>(null);
  const downloadMenuOpen = Boolean(downloadAnchorEl);

  useEffect(() => {
    fetchReportsData();
  }, []);

  const fetchReportsData = async () => {
    try {
      const [statsRes, suitesRes, servicesRes] = await Promise.all([
        fetch('http://localhost:8080/api/dashboard/stats'),
        fetch('http://localhost:8080/api/test-suites'),
        fetch('http://localhost:8080/api/tests/services'),
      ]);

      const statsData = await statsRes.json();
      const suitesData = await suitesRes.json();
      const servicesData = await servicesRes.json();

      console.log('📊 Reports data fetched:', { 
        stats: statsData.data, 
        suites: suitesData.data,
        services: servicesData.data 
      });

      setDashboardData(statsData.data);
      setTestSuites(suitesData.data || []);
      setServiceTests(servicesData.data || []);
      setLoading(false);
    } catch (error) {
      console.error('Error fetching reports data:', error);
      setLoading(false);
    }
  };

  const handleDownloadClick = (event: React.MouseEvent<HTMLElement>) => {
    setDownloadAnchorEl(event.currentTarget);
  };

  const handleDownloadClose = () => {
    setDownloadAnchorEl(null);
  };

  const downloadJSON = () => {
    const reportData = {
      timestamp: new Date().toISOString(),
      summary: {
        totalTests: dashboardData?.totalTests || 0,
        passedTests: dashboardData?.passedTests || 0,
        failedTests: dashboardData?.failedTests || 0,
        passRate: `${((dashboardData?.passedTests || 0) / (dashboardData?.totalTests || 1) * 100).toFixed(1)}%`,
        executionTime: `${(dashboardData?.executionTime / 1000).toFixed(2)}s`,
        generatedTestSuites: dashboardData?.generatedTestSuites || 0,
      },
      testSuites: testSuites.map(suite => ({
        id: suite.id,
        name: suite.name,
        type: suite.type,
        status: suite.status,
        testCases: suite.testCases?.length || 0,
        passed: suite.testCases?.filter((t: any) => t.status === 'PASSED').length || 0,
        failed: suite.testCases?.filter((t: any) => t.status === 'FAILED').length || 0,
        pending: suite.testCases?.filter((t: any) => t.status === 'PENDING').length || 0,
      })),
      trends: dashboardData?.trendsData || [],
    };

    const blob = new Blob([JSON.stringify(reportData, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `test-report-${new Date().toISOString().split('T')[0]}.json`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
    handleDownloadClose();
  };

  const downloadCSV = () => {
    const totalTests = dashboardData?.totalTests || 0;
    const passedTests = dashboardData?.passedTests || 0;
    const failedTests = dashboardData?.failedTests || 0;
    const passRate = ((passedTests / (totalTests || 1)) * 100).toFixed(1);

    let csvContent = 'Test Report\n\n';
    csvContent += 'Summary\n';
    csvContent += 'Metric,Value\n';
    csvContent += `Total Tests,${totalTests}\n`;
    csvContent += `Passed Tests,${passedTests}\n`;
    csvContent += `Failed Tests,${failedTests}\n`;
    csvContent += `Pass Rate,${passRate}%\n`;
    csvContent += `Avg Execution Time,${(dashboardData?.executionTime / 1000).toFixed(2)}s\n`;
    csvContent += `Total Test Suites,${testSuites.length}\n\n`;

    csvContent += 'Test Suites\n';
    csvContent += 'ID,Name,Type,Status,Total Tests,Passed,Failed,Pending\n';
    testSuites.forEach(suite => {
      const passed = suite.testCases?.filter((t: any) => t.status === 'PASSED').length || 0;
      const failed = suite.testCases?.filter((t: any) => t.status === 'FAILED').length || 0;
      const pending = suite.testCases?.filter((t: any) => t.status === 'PENDING').length || 0;
      const total = suite.testCases?.length || 0;
      csvContent += `${suite.id},"${suite.name}",${suite.type},${suite.status},${total},${passed},${failed},${pending}\n`;
    });

    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `test-report-${new Date().toISOString().split('T')[0]}.csv`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
    handleDownloadClose();
  };

  const downloadPDF = () => {
    // For PDF, we'll create an HTML report and open it in a new window for printing
    const totalTests = dashboardData?.totalTests || 0;
    const passedTests = dashboardData?.passedTests || 0;
    const failedTests = dashboardData?.failedTests || 0;
    const passRate = ((passedTests / (totalTests || 1)) * 100).toFixed(1);

    const htmlContent = `
      <!DOCTYPE html>
      <html>
      <head>
        <title>Test Report - ${new Date().toLocaleDateString()}</title>
        <style>
          body { font-family: Arial, sans-serif; margin: 40px; color: #333; }
          h1 { color: #2196f3; border-bottom: 3px solid #2196f3; padding-bottom: 10px; }
          h2 { color: #666; margin-top: 30px; }
          .summary { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; margin: 20px 0; }
          .summary-card { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); 
                          color: white; padding: 20px; border-radius: 8px; }
          .summary-card h3 { margin: 0; font-size: 32px; }
          .summary-card p { margin: 5px 0 0 0; opacity: 0.9; }
          table { width: 100%; border-collapse: collapse; margin-top: 20px; }
          th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
          th { background-color: #2196f3; color: white; }
          tr:hover { background-color: #f5f5f5; }
          .status-passed { color: #4caf50; font-weight: bold; }
          .status-failed { color: #f44336; font-weight: bold; }
          .status-pending { color: #ff9800; font-weight: bold; }
          .footer { margin-top: 50px; text-align: center; color: #999; font-size: 12px; }
          @media print {
            .no-print { display: none; }
          }
        </style>
      </head>
      <body>
        <h1>🧪 Test Execution Report</h1>
        <p><strong>Generated:</strong> ${new Date().toLocaleString()}</p>
        
        <h2>📊 Summary</h2>
        <div class="summary">
          <div class="summary-card">
            <h3>${passRate}%</h3>
            <p>Pass Rate</p>
          </div>
          <div class="summary-card" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
            <h3>${(dashboardData?.executionTime / 1000).toFixed(2)}s</h3>
            <p>Avg Execution Time</p>
          </div>
          <div class="summary-card" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
            <h3>${totalTests}</h3>
            <p>Total Tests</p>
          </div>
        </div>

        <h2>📋 Test Results by Suite</h2>
        <table>
          <thead>
            <tr>
              <th>Suite Name</th>
              <th>Type</th>
              <th>Status</th>
              <th>Total</th>
              <th>Passed</th>
              <th>Failed</th>
              <th>Pending</th>
              <th>Pass Rate</th>
            </tr>
          </thead>
          <tbody>
            ${testSuites.map(suite => {
              const passed = suite.testCases?.filter((t: any) => t.status === 'PASSED').length || 0;
              const failed = suite.testCases?.filter((t: any) => t.status === 'FAILED').length || 0;
              const pending = suite.testCases?.filter((t: any) => t.status === 'PENDING').length || 0;
              const total = suite.testCases?.length || 0;
              const suitePassRate = total > 0 ? ((passed / total) * 100).toFixed(1) : '0';
              return `
                <tr>
                  <td>${suite.name}</td>
                  <td>${suite.type}</td>
                  <td class="status-${suite.status?.toLowerCase()}">${suite.status}</td>
                  <td>${total}</td>
                  <td class="status-passed">${passed}</td>
                  <td class="status-failed">${failed}</td>
                  <td class="status-pending">${pending}</td>
                  <td>${suitePassRate}%</td>
                </tr>
              `;
            }).join('')}
          </tbody>
        </table>

        <div class="footer">
          <p>Generated by Q-Force Test Platform | ${new Date().toLocaleDateString()}</p>
        </div>

        <div class="no-print" style="margin-top: 30px; text-align: center;">
          <button onclick="window.print()" style="padding: 10px 20px; background: #2196f3; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 16px;">
            Print / Save as PDF
          </button>
          <button onclick="window.close()" style="padding: 10px 20px; background: #666; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; margin-left: 10px;">
            Close
          </button>
        </div>
      </body>
      </html>
    `;

    const printWindow = window.open('', '_blank');
    if (printWindow) {
      printWindow.document.write(htmlContent);
      printWindow.document.close();
    }
    handleDownloadClose();
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

  // Performance metrics data - Use service-level aggregated data
  const performanceData = (serviceTests || []).map((service: any) => ({
    name: service.serviceName,
    passed: service.passedTests || 0,
    failed: service.failedTests || 0,
    tests: service.totalTestCases || 0,
    passRate: service.passRate || 0,
  }));

  // Execution time trends
  const trendData = dashboardData?.trendsData?.map((trend: any) => ({
    date: new Date(trend.date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
    passed: trend.passed,
    failed: trend.failed,
    total: trend.passed + trend.failed,
  })) || [];

  // Test suite distribution for pie chart - Use service-level data
  const suiteDistribution = (() => {
    const colors = ['#2196f3', '#4caf50', '#ff9800', '#f44336', '#9c27b0', '#00bcd4', '#8bc34a'];
    return (serviceTests || [])
      .map((service: any, index: number) => ({
        name: service.serviceName,
        shortName: service.serviceName.replace(' Service', ''),
        value: service.totalTestCases || 0,
        color: colors[index % colors.length],
        passed: service.passedTests || 0,
        failed: service.failedTests || 0,
        passRate: service.passRate || 0,
      }))
      .filter((s: any) => s.value > 0);
  })();

  const avgExecutionTime = dashboardData?.executionTime 
    ? (dashboardData.executionTime / 1000).toFixed(2) 
    : '0';

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" sx={{ fontWeight: 'bold' }}>
          Reports & Analytics
        </Typography>
        
        {/* Download Button */}
        <Button
          variant="contained"
          startIcon={<DownloadIcon />}
          endIcon={<ArrowDropDownIcon />}
          onClick={handleDownloadClick}
          sx={{
            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
            '&:hover': {
              background: 'linear-gradient(135deg, #764ba2 0%, #667eea 100%)',
            },
          }}
        >
          Download Report
        </Button>
        
        {/* Download Menu */}
        <Menu
          anchorEl={downloadAnchorEl}
          open={downloadMenuOpen}
          onClose={handleDownloadClose}
          anchorOrigin={{
            vertical: 'bottom',
            horizontal: 'right',
          }}
          transformOrigin={{
            vertical: 'top',
            horizontal: 'right',
          }}
        >
          <MenuItem onClick={downloadJSON}>
            <ListItemIcon>
              <DescriptionIcon fontSize="small" />
            </ListItemIcon>
            <ListItemText>Download as JSON</ListItemText>
          </MenuItem>
          <MenuItem onClick={downloadCSV}>
            <ListItemIcon>
              <TableChartIcon fontSize="small" />
            </ListItemIcon>
            <ListItemText>Download as CSV</ListItemText>
          </MenuItem>
          <MenuItem onClick={downloadPDF}>
            <ListItemIcon>
              <PictureAsPdfIcon fontSize="small" />
            </ListItemIcon>
            <ListItemText>Download as PDF</ListItemText>
          </MenuItem>
        </Menu>
      </Box>

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

        {/* Service-Wise Test Distribution */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <AssessmentIcon color="primary" />
                Service-Wise Test Distribution
              </Typography>
              <Box sx={{ height: 300 }}>
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={suiteDistribution}
                      cx="50%"
                      cy="50%"
                      labelLine={true}
                      label={({ shortName, percent }) => `${shortName}: ${(percent * 100).toFixed(0)}%`}
                      outerRadius={80}
                      fill="#8884d8"
                      dataKey="value"
                    >
                      {suiteDistribution.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.color} />
                      ))}
                    </Pie>
                    <Tooltip 
                      formatter={(value: any, name: any, props: any) => {
                        const entry = props.payload;
                        return [
                          `${value} tests (${entry.passed} passed, ${entry.failed} failed) - ${entry.passRate?.toFixed(1)}% pass rate`,
                          entry.name
                        ];
                      }}
                    />
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
