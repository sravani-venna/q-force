import React from 'react';
import { Typography, Box, Card, CardContent, Button, Grid, Chip } from '@mui/material';

const TestExecution: React.FC = () => {
  return (
    <Box>
      <Typography variant="h4" sx={{ mb: 3, fontWeight: 'bold' }}>
        Test Execution
      </Typography>
      <Grid container spacing={3}>
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Recent Test Executions
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Monitor and manage your test executions
              </Typography>
              <Box sx={{ mt: 2, display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                <Chip label="Payment Service - Running" color="warning" />
                <Chip label="User API - Passed" color="success" />
                <Chip label="Order Service - Failed" color="error" />
              </Box>
              <Button variant="contained" sx={{ mt: 2 }}>
                Start New Execution
              </Button>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default TestExecution;
