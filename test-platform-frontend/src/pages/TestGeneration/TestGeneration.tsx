import React, { useState } from 'react';
import { 
  Typography, 
  Box, 
  Card, 
  CardContent, 
  Button, 
  Grid, 
  TextField,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
  CircularProgress,
  Chip
} from '@mui/material';
import { testGenerationService } from '../../services/apiService';

const TestGeneration: React.FC = () => {
  const [open, setOpen] = useState(false);
  const [testType, setTestType] = useState<'UNIT' | 'INTEGRATION' | 'E2E'>('UNIT');
  const [codeInput, setCodeInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<any>(null);
  const [error, setError] = useState<string | null>(null);

  const handleGenerate = async (type: 'UNIT' | 'INTEGRATION' | 'E2E') => {
    setTestType(type);
    setOpen(true);
    setResult(null);
    setError(null);
  };

  const handleSubmit = async () => {
    if (!codeInput.trim()) {
      setError('Please provide code to generate tests for');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const response = await testGenerationService.generateTests({
        type: testType,
        code: codeInput,
        language: 'java',
        framework: 'junit'
      });

      if (response.success) {
        setResult(response);
      } else {
        setError(response.message || 'Failed to generate tests');
      }
    } catch (err: any) {
      setError(err.message || 'Failed to generate tests');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setOpen(false);
    setCodeInput('');
    setResult(null);
    setError(null);
  };

  return (
    <Box>
      <Typography variant="h4" sx={{ mb: 3, fontWeight: 'bold' }}>
        Test Generation - AI Powered
      </Typography>
      
      <Alert severity="info" sx={{ mb: 3 }}>
        Generate comprehensive tests using AI. Simply provide your code and get high-quality test cases instantly.
      </Alert>

      <Grid container spacing={3}>
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Unit Tests
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Generate comprehensive unit tests for your Java classes with mocking and assertions
              </Typography>
              <Button 
                variant="contained" 
                fullWidth
                onClick={() => handleGenerate('UNIT')}
              >
                Generate Unit Tests
              </Button>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Integration Tests
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Create integration tests for your APIs and services with database interactions
              </Typography>
              <Button 
                variant="contained" 
                fullWidth
                onClick={() => handleGenerate('INTEGRATION')}
              >
                Generate Integration Tests
              </Button>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                E2E Tests
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Generate end-to-end tests for complete user workflows using Selenium
              </Typography>
              <Button 
                variant="contained" 
                fullWidth
                onClick={() => handleGenerate('E2E')}
              >
                Generate E2E Tests
              </Button>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Generation Dialog */}
      <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
        <DialogTitle>
          Generate {testType} Tests
        </DialogTitle>
        <DialogContent>
          {!result && (
            <>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Paste your Java code below and we'll generate comprehensive {testType.toLowerCase()} tests for you.
              </Typography>
              
              {error && (
                <Alert severity="error" sx={{ mb: 2 }}>
                  {error}
                </Alert>
              )}

              <TextField
                fullWidth
                multiline
                rows={10}
                variant="outlined"
                placeholder="Paste your Java code here..."
                value={codeInput}
                onChange={(e) => setCodeInput(e.target.value)}
                disabled={loading}
              />
            </>
          )}

          {loading && (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', py: 4 }}>
              <CircularProgress />
              <Typography sx={{ ml: 2 }}>Generating tests...</Typography>
            </Box>
          )}

          {result && (
            <Box>
              <Alert severity="success" sx={{ mb: 2 }}>
                Successfully generated {result.testCases?.length || 0} test cases!
              </Alert>
              
              <Typography variant="h6" sx={{ mb: 2 }}>Generated Test Cases:</Typography>
              
              {result.testCases?.map((testCase: any, index: number) => (
                <Card key={index} sx={{ mb: 2 }}>
                  <CardContent>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
                      <Typography variant="subtitle1" fontWeight="bold">
                        {testCase.name}
                      </Typography>
                      <Chip 
                        label={testCase.priority} 
                        size="small" 
                        color={testCase.priority === 'HIGH' ? 'error' : testCase.priority === 'MEDIUM' ? 'warning' : 'default'}
                      />
                    </Box>
                    <Chip label={testCase.type} size="small" sx={{ mb: 1 }} />
                    <Typography variant="body2" color="text.secondary">
                      This test case validates the behavior and functionality of your code.
                    </Typography>
                  </CardContent>
                </Card>
              ))}
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose}>
            {result ? 'Close' : 'Cancel'}
          </Button>
          {!result && !loading && (
            <Button 
              onClick={handleSubmit} 
              variant="contained"
              disabled={!codeInput.trim()}
            >
              Generate Tests
            </Button>
          )}
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default TestGeneration;