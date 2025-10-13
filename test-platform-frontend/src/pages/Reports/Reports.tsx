import React from 'react';
import { Typography, Box, Card, CardContent, Grid } from '@mui/material';

const Reports: React.FC = () => {
  return (
    <Box>
      <Typography variant="h4" sx={{ mb: 3, fontWeight: 'bold' }}>
        Reports & Analytics
      </Typography>
      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Test Coverage Report
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Overall coverage: 94.2%
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Performance Metrics
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Average execution time: 2.3s
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Reports;
