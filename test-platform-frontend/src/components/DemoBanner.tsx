import React from 'react';
import { Alert, AlertTitle, Box, Chip } from '@mui/material';

const DemoBanner: React.FC = () => {
  return (
    <Box sx={{ mb: 2 }}>
      <Alert severity="info" sx={{ display: 'flex', alignItems: 'center' }}>
        <AlertTitle sx={{ mb: 0, mr: 2 }}>
          <Chip label="DEMO MODE" color="primary" size="small" sx={{ mr: 1 }} />
          Frontend Only
        </AlertTitle>
        You're viewing the Test Platform interface in demo mode. 
        To enable full functionality with real authentication and data, 
        please start the backend services.
      </Alert>
    </Box>
  );
};

export default DemoBanner;
