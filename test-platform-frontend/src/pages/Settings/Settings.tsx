import React from 'react';
import { Typography, Box, Card, CardContent, Grid } from '@mui/material';

const Settings: React.FC = () => {
  return (
    <Box>
      <Typography variant="h4" sx={{ mb: 3, fontWeight: 'bold' }}>
        Settings
      </Typography>
      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Profile Settings
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Manage your account settings and preferences
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                System Configuration
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Configure system-wide test platform settings
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Settings;
