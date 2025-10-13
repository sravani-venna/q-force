import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Grid,
  Chip,
  LinearProgress,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Alert,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Divider,
  CircularProgress,
  Snackbar,
} from '@mui/material';
import {
  Add as AddIcon,
  Code as CodeIcon,
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
  Schedule as ScheduleIcon,
  TrendingUp as TrendingUpIcon,
  PlayArrow as PlayArrowIcon,
  ExpandMore as ExpandMoreIcon,
  ExpandLess as ExpandLessIcon,
  BugReport as BugReportIcon,
  Hub as IntegrationIcon,
  Timeline as TimelineIcon,
} from '@mui/icons-material';
import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

interface PullRequest {
  id: number;
  number: number;
  title: string;
  branch: string;
  author: string;
  status: 'OPEN' | 'MERGED' | 'CLOSED';
  createdAt: string;
  testsGenerated: number;
  testsPassed: number;
  testsFailed: number;
  coverage: number;
  generatedTests?: any[];
}

const PullRequests: React.FC = () => {
  const [pullRequests, setPullRequests] = useState<PullRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [open, setOpen] = useState(false);
  const [selectedPR, setSelectedPR] = useState<PullRequest | null>(null);
  const [newPRData, setNewPRData] = useState({
    number: '',
    title: '',
    branch: '',
    author: 'developer@testplatform.com',
    changedFiles: ''
  });
  const [runningTests, setRunningTests] = useState<number[]>([]); // Track which PRs are running tests
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [snackbarMessage, setSnackbarMessage] = useState('');
  const [expandedTestSuites, setExpandedTestSuites] = useState<{ [key: string]: boolean }>({});

  useEffect(() => {
    fetchPullRequests();
  }, []);

  const fetchPullRequests = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`${API_BASE_URL}/api/pull-requests`);
      if (response.data.success) {
        setPullRequests(response.data.data);
      }
    } catch (err: any) {
      setError('Failed to load pull requests');
      console.error('PR fetch error:', err);
    } finally {
      setLoading(false);
    }
  };

  const createPullRequest = async () => {
    try {
      if (!newPRData.number || !newPRData.title || !newPRData.branch) {
        setError('Please fill in all required fields');
        return;
      }

      const changedFiles = newPRData.changedFiles
        .split('\n')
        .filter(file => file.trim())
        .map(file => ({ filename: file.trim() }));

      const response = await axios.post(`${API_BASE_URL}/api/pull-requests`, {
        number: parseInt(newPRData.number),
        title: newPRData.title,
        branch: newPRData.branch,
        author: newPRData.author,
        changedFiles
      });

      if (response.data.success) {
        setOpen(false);
        setNewPRData({
          number: '',
          title: '',
          branch: '',
          author: 'developer@testplatform.com',
          changedFiles: ''
        });
        fetchPullRequests();
        setError(null);
      }
    } catch (err: any) {
      setError('Failed to create pull request');
      console.error('PR creation error:', err);
    }
  };

  const viewPRDetails = async (pr: PullRequest) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/api/pull-requests/${pr.id}`);
      if (response.data.success) {
        setSelectedPR(response.data.data);
      }
    } catch (err: any) {
      console.error('Failed to fetch PR details:', err);
    }
  };

  const runTests = async (pr: PullRequest) => {
    try {
      // Add PR to running tests list
      setRunningTests(prev => [...prev, pr.id]);
      setError(null);
      
      console.log(`🧪 Running tests for PR #${pr.number}: ${pr.title}`);
      
      // Call test execution API
      const response = await axios.post(`${API_BASE_URL}/api/test-executions`, {
        suiteId: `pr-${pr.number}`,
        prNumber: pr.number,
        branch: pr.branch,
        action: 'run'
      });
      
      if (response.data.success) {
        // Simulate test execution delay
        setTimeout(async () => {
          // Refresh PR data to get updated results
          await fetchPullRequests();
          
          // Remove from running tests
          setRunningTests(prev => prev.filter(id => id !== pr.id));
          
          // Show success message
          setSnackbarMessage(`✅ Tests completed for PR #${pr.number}`);
          setSnackbarOpen(true);
          console.log(`✅ Tests completed for PR #${pr.number}`);
        }, 3000); // 3 second simulation
      } else {
        setRunningTests(prev => prev.filter(id => id !== pr.id));
        setError('Failed to run tests');
      }
    } catch (err: any) {
      setRunningTests(prev => prev.filter(id => id !== pr.id));
      setError('Failed to run tests: ' + (err.message || 'Unknown error'));
      console.error('Test execution error:', err);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'OPEN': return 'primary';
      case 'MERGED': return 'success';
      case 'CLOSED': return 'error';
      default: return 'default';
    }
  };

  const getPassRateColor = (passRate: number) => {
    if (passRate >= 90) return 'success';
    if (passRate >= 70) return 'warning';
    return 'error';
  };

  const toggleTestSuiteExpansion = (testSuiteKey: string) => {
    setExpandedTestSuites(prev => ({
      ...prev,
      [testSuiteKey]: !prev[testSuiteKey]
    }));
  };

  const getTestTypeIcon = (testType: string) => {
    switch (testType) {
      case 'UNIT': return <BugReportIcon color="primary" />;
      case 'INTEGRATION': return <IntegrationIcon color="secondary" />;
      case 'E2E': return <TimelineIcon color="success" />;
      default: return <CodeIcon />;
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '200px' }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" sx={{ fontWeight: 'bold' }}>
          Pull Requests
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => setOpen(true)}
        >
          Create PR
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      <Grid container spacing={3}>
        {pullRequests.map((pr) => {
          const passRate = pr.testsGenerated > 0 
            ? ((pr.testsPassed / pr.testsGenerated) * 100).toFixed(1)
            : '0';
          
          return (
            <Grid item xs={12} md={6} key={pr.id}>
              <Card sx={{ height: '100%' }}>
                <CardContent>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
                    <Box>
                      <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                        #{pr.number} {pr.title}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        {pr.branch} by {pr.author}
                      </Typography>
                    </Box>
                    <Chip
                      label={pr.status}
                      color={getStatusColor(pr.status) as any}
                      size="small"
                    />
                  </Box>

                  <Box sx={{ mb: 2 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                      <Typography variant="body2">
                        Tests: {pr.testsPassed}/{pr.testsGenerated}
                      </Typography>
                      <Typography variant="body2" color={getPassRateColor(parseFloat(passRate))}>
                        {passRate}% Pass Rate
                      </Typography>
                    </Box>
                    <LinearProgress
                      variant="determinate"
                      value={parseFloat(passRate)}
                      color={getPassRateColor(parseFloat(passRate)) as any}
                      sx={{ height: 8, borderRadius: 4 }}
                    />
                  </Box>

                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <TrendingUpIcon sx={{ fontSize: 16 }} color="action" />
                      <Typography variant="body2">
                        Coverage: {pr.coverage.toFixed(1)}%
                      </Typography>
                    </Box>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <CodeIcon sx={{ fontSize: 16 }} color="action" />
                      <Typography variant="body2">
                        {pr.testsGenerated} tests
                      </Typography>
                    </Box>
                  </Box>

                  <Box sx={{ display: 'flex', gap: 1 }}>
                    <Button
                      size="small"
                      variant="contained"
                      color="info"
                      startIcon={<CodeIcon />}
                      onClick={() => viewPRDetails(pr)}
                    >
                      View Test Cases
                    </Button>
                    {pr.status === 'OPEN' && (
                      <Button
                        size="small"
                        variant="contained"
                        startIcon={runningTests.includes(pr.id) ? <CircularProgress size={16} /> : <PlayArrowIcon />}
                        onClick={() => runTests(pr)}
                        disabled={runningTests.includes(pr.id)}
                      >
                        {runningTests.includes(pr.id) ? 'Running...' : 'Run Tests'}
                      </Button>
                    )}
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          );
        })}
      </Grid>

      {/* Create PR Dialog */}
      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>Create New Pull Request</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Create a pull request to automatically generate test cases for your changes.
          </Typography>
          
          <Grid container spacing={2}>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="PR Number"
                type="number"
                value={newPRData.number}
                onChange={(e) => setNewPRData({...newPRData, number: e.target.value})}
                margin="normal"
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Branch Name"
                value={newPRData.branch}
                onChange={(e) => setNewPRData({...newPRData, branch: e.target.value})}
                margin="normal"
                placeholder="feature/new-feature"
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="PR Title"
                value={newPRData.title}
                onChange={(e) => setNewPRData({...newPRData, title: e.target.value})}
                margin="normal"
                placeholder="Add user validation service"
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Author"
                value={newPRData.author}
                onChange={(e) => setNewPRData({...newPRData, author: e.target.value})}
                margin="normal"
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Changed Files (one per line)"
                multiline
                rows={4}
                value={newPRData.changedFiles}
                onChange={(e) => setNewPRData({...newPRData, changedFiles: e.target.value})}
                margin="normal"
                placeholder={`UserService.java
UserController.java
UserRepository.java`}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancel</Button>
          <Button onClick={createPullRequest} variant="contained">
            Create PR & Generate Tests
          </Button>
        </DialogActions>
      </Dialog>

      {/* PR Details Dialog */}
      <Dialog
        open={!!selectedPR}
        onClose={() => setSelectedPR(null)}
        maxWidth="xl"
        fullWidth
        scroll="body"
        PaperProps={{
          sx: { 
            minHeight: '90vh',
            maxHeight: '95vh'
          }
        }}
      >
        <DialogTitle>
          PR #{selectedPR?.number}: {selectedPR?.title}
        </DialogTitle>
        <DialogContent>
          {selectedPR && (
            <Box>
              <Grid container spacing={3} sx={{ mb: 3 }}>
                <Grid item xs={6}>
                  <Typography variant="subtitle2" color="text.secondary">Branch</Typography>
                  <Typography variant="body1">{selectedPR.branch}</Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="subtitle2" color="text.secondary">Author</Typography>
                  <Typography variant="body1">{selectedPR.author}</Typography>
                </Grid>
                <Grid item xs={4}>
                  <Typography variant="subtitle2" color="text.secondary">Tests Generated</Typography>
                  <Typography variant="h6" color="primary">{selectedPR.testsGenerated}</Typography>
                </Grid>
                <Grid item xs={4}>
                  <Typography variant="subtitle2" color="text.secondary">Pass Rate</Typography>
                  <Typography variant="h6" color="success.main">
                    {selectedPR.testsGenerated > 0 
                      ? `${((selectedPR.testsPassed / selectedPR.testsGenerated) * 100).toFixed(1)}%`
                      : '0%'
                    }
                  </Typography>
                </Grid>
                <Grid item xs={4}>
                  <Typography variant="subtitle2" color="text.secondary">Coverage</Typography>
                  <Typography variant="h6" color="info.main">{selectedPR.coverage.toFixed(1)}%</Typography>
                </Grid>
              </Grid>

              {selectedPR.generatedTests && selectedPR.generatedTests.length > 0 && (
                <Box>
                  <Typography variant="h5" sx={{ mb: 3, fontWeight: 'bold', color: 'primary.main' }}>
                    🧪 Generated Test Cases ({selectedPR.generatedTests.length} Test Suites)
                  </Typography>
                  
                  {/* Group tests by type */}
                  {['UNIT', 'INTEGRATION', 'E2E'].map(testType => {
                    const testsOfType = selectedPR.generatedTests!.filter(test => test.type === testType);
                    if (testsOfType.length === 0) return null;
                    
                    return (
                      <Card key={testType} sx={{ mb: 3, border: 2, borderColor: testType === 'UNIT' ? 'primary.main' : testType === 'INTEGRATION' ? 'secondary.main' : 'success.main' }}>
                        <CardContent>
                          <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                            {getTestTypeIcon(testType)}
                            <Typography variant="h6" sx={{ ml: 1, fontWeight: 'bold' }}>
                              {testType} Tests ({testsOfType.length} suites)
                            </Typography>
                          </Box>
                          
                          {testsOfType.map((testSuite, index) => {
                            const testSuiteKey = `${testType}-${testSuite.id}-${index}`;
                            const isExpanded = expandedTestSuites[testSuiteKey];
                            
                            return (
                              <Card key={testSuiteKey} sx={{ mb: 2, bgcolor: 'grey.50' }}>
                                <CardContent sx={{ pb: 1 }}>
                                  <Box 
                                    sx={{ 
                                      display: 'flex', 
                                      alignItems: 'center', 
                                      cursor: 'pointer',
                                      '&:hover': { bgcolor: 'grey.100' },
                                      p: 1,
                                      borderRadius: 1
                                    }}
                                    onClick={() => toggleTestSuiteExpansion(testSuiteKey)}
                                  >
                                    <Box sx={{ display: 'flex', alignItems: 'center', flex: 1 }}>
                                      <CodeIcon sx={{ mr: 1 }} />
                                      <Box>
                                        <Typography variant="subtitle1" sx={{ fontWeight: 'bold' }}>
                                          📁 {testSuite.filePath}
                                        </Typography>
                                        <Typography variant="body2" color="text.secondary">
                                          {testSuite.testCases.length} test cases • Generated: {new Date(testSuite.generatedAt).toLocaleString()}
                                        </Typography>
                                      </Box>
                                    </Box>
                                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                      <Chip
                                        label={testSuite.status}
                                        color="success"
                                        size="small"
                                      />
                                      {isExpanded ? <ExpandLessIcon /> : <ExpandMoreIcon />}
                                    </Box>
                                  </Box>
                                  
                                  {isExpanded && (
                                    <Box sx={{ mt: 2, pl: 2 }}>
                                      <Typography variant="subtitle2" sx={{ mb: 1, fontWeight: 'bold' }}>
                                        Individual Test Cases:
                                      </Typography>
                                      <List dense>
                                        {testSuite.testCases.map((testCase: any, tcIndex: number) => (
                                          <ListItem 
                                            key={tcIndex}
                                            sx={{ 
                                              bgcolor: 'white', 
                                              mb: 1, 
                                              borderRadius: 1,
                                              border: 1,
                                              borderColor: 'divider'
                                            }}
                                          >
                                            <ListItemIcon>
                                              <CheckCircleIcon 
                                                color={testCase.priority === 'HIGH' ? 'error' : testCase.priority === 'MEDIUM' ? 'warning' : 'success'}
                                                fontSize="small"
                                              />
                                            </ListItemIcon>
                                            <ListItemText
                                              primary={
                                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                                  <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                                                    {testCase.name}
                                                  </Typography>
                                                  <Chip
                                                    label={testCase.priority}
                                                    size="small"
                                                    color={testCase.priority === 'HIGH' ? 'error' : testCase.priority === 'MEDIUM' ? 'warning' : 'default'}
                                                  />
                                                </Box>
                                              }
                                              secondary={testCase.description}
                                            />
                                          </ListItem>
                                        ))}
                                      </List>
                                    </Box>
                                  )}
                                </CardContent>
                              </Card>
                            );
                          })}
                        </CardContent>
                      </Card>
                    );
                  })}
                </Box>
              )}
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setSelectedPR(null)}>Close</Button>
        </DialogActions>
      </Dialog>

      {/* Success Snackbar */}
      <Snackbar
        open={snackbarOpen}
        autoHideDuration={6000}
        onClose={() => setSnackbarOpen(false)}
        message={snackbarMessage}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      />
    </Box>
  );
};

export default PullRequests;
