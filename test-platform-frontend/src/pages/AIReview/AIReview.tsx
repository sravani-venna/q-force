import React, { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  TextField,
  Button,
  CircularProgress,
  Alert,
  Chip,
  Divider,
  Paper,
  Grid,
  IconButton,
  Collapse,
  alpha,
} from '@mui/material';
import {
  Send as SendIcon,
  GitHub as GitHubIcon,
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
  Info as InfoIcon,
  Code as CodeIcon,
  ExpandMore as ExpandMoreIcon,
  ExpandLess as ExpandLessIcon,
  BugReport as BugReportIcon,
} from '@mui/icons-material';
import { toast } from 'react-toastify';
import { aiReviewService } from '../../services/prReviewService';

interface DiffComment {
  fileName: string;
  diffLine: number;
  comment: string;
}

interface ReviewResponse {
  status: 'success' | 'error' | 'no_suggestions';
  prNumber: number;
  reviewSummary: string | null;
  comments: DiffComment[];
  errorMessage: string | null;
}

const AIReview: React.FC = () => {
  const [prUrl, setPrUrl] = useState('');
  const [loading, setLoading] = useState(false);
  const [reviewResult, setReviewResult] = useState<ReviewResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [expandedComments, setExpandedComments] = useState<Set<number>>(new Set());

  // Parse PR URL to extract owner, repo, and PR number
  const parsePrUrl = (url: string): { repoOwner: string; repoName: string; prNumber: number } | null => {
    // Support both https://github.com/owner/repo/pull/123 and owner/repo/pull/123 formats
    const githubRegex = /(?:https?:\/\/)?(?:www\.)?github\.com\/([^\/]+)\/([^\/]+)\/pull\/(\d+)/;
    const match = url.match(githubRegex);

    if (match) {
      return {
        repoOwner: match[1],
        repoName: match[2],
        prNumber: parseInt(match[3], 10),
      };
    }

    return null;
  };

  const handleReview = async () => {
    setError(null);
    setReviewResult(null);

    if (!prUrl.trim()) {
      setError('Please enter a GitHub PR URL');
      return;
    }

    const parsedData = parsePrUrl(prUrl);
    if (!parsedData) {
      setError('Invalid GitHub PR URL format. Expected: https://github.com/owner/repo/pull/123');
      return;
    }

    setLoading(true);

    try {
      const result = await aiReviewService.reviewPullRequest(parsedData);
      setReviewResult(result);

      if (result.status === 'success') {
        toast.success(`Successfully reviewed PR #${result.prNumber}!`);
      } else if (result.status === 'no_suggestions') {
        toast.info('No suggestions found for this PR');
      } else {
        toast.warning('Review completed with warnings');
      }
    } catch (err: any) {
      const errorMsg = err.message || 'Failed to review pull request';
      setError(errorMsg);
      toast.error(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  const toggleCommentExpanded = (index: number) => {
    const newExpanded = new Set(expandedComments);
    if (newExpanded.has(index)) {
      newExpanded.delete(index);
    } else {
      newExpanded.add(index);
    }
    setExpandedComments(newExpanded);
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'success':
        return 'success';
      case 'error':
        return 'error';
      case 'no_suggestions':
        return 'info';
      default:
        return 'default';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'success':
        return <CheckCircleIcon />;
      case 'error':
        return <ErrorIcon />;
      case 'no_suggestions':
        return <InfoIcon />;
      default:
        return <InfoIcon />;
    }
  };

  return (
    <Box>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" gutterBottom fontWeight="bold">
          AI PR Review
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Get AI-powered code review suggestions for your GitHub pull requests using local CodeGemma
        </Typography>
      </Box>

      {/* Input Section */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <GitHubIcon />
            Pull Request URL
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Enter the full GitHub PR URL (e.g., https://github.com/owner/repo/pull/123)
          </Typography>
          
          <Box sx={{ display: 'flex', gap: 2, alignItems: 'flex-start' }}>
            <TextField
              fullWidth
              placeholder="https://github.com/owner/repo/pull/123"
              value={prUrl}
              onChange={(e) => setPrUrl(e.target.value)}
              onKeyPress={(e) => {
                if (e.key === 'Enter' && !loading) {
                  handleReview();
                }
              }}
              disabled={loading}
              variant="outlined"
              size="medium"
              error={!!error && !reviewResult}
              helperText={error && !reviewResult ? error : ''}
            />
            <Button
              variant="contained"
              size="large"
              onClick={handleReview}
              disabled={loading}
              endIcon={loading ? <CircularProgress size={20} /> : <SendIcon />}
              sx={{ minWidth: 140, height: 56 }}
            >
              {loading ? 'Reviewing...' : 'Review'}
            </Button>
          </Box>
        </CardContent>
      </Card>

      {/* Loading State */}
      {loading && (
        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', py: 6 }}>
          <CircularProgress size={60} />
          <Typography variant="body1" sx={{ mt: 2 }} color="text.secondary">
            Analyzing pull request with AI...
          </Typography>
          <Typography variant="caption" color="text.secondary" sx={{ mt: 1 }}>
            This may take a few moments
          </Typography>
        </Box>
      )}

      {/* Error State */}
      {error && reviewResult?.status === 'error' && (
        <Alert severity="error" icon={<ErrorIcon />} sx={{ mb: 3 }}>
          <Typography variant="body2" fontWeight="bold">Review Failed</Typography>
          <Typography variant="body2">{reviewResult.errorMessage || error}</Typography>
        </Alert>
      )}

      {/* Results Section */}
      {reviewResult && !loading && (
        <Box>
          {/* Status Card */}
          <Card sx={{ mb: 3, bgcolor: (theme) => {
              const statusColor = getStatusColor(reviewResult.status);
              return statusColor !== 'default' ? alpha(theme.palette[statusColor].main, 0.1) : alpha(theme.palette.grey[500], 0.1);
            }}}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
                <Box sx={{ color: (theme) => {
                  const statusColor = getStatusColor(reviewResult.status);
                  return statusColor !== 'default' ? theme.palette[statusColor].main : theme.palette.grey[600];
                }}}>
                  {getStatusIcon(reviewResult.status)}
                </Box>
                <Typography variant="h6">
                  Review Status: PR #{reviewResult.prNumber}
                </Typography>
                <Chip 
                  label={reviewResult.status ? reviewResult.status.toUpperCase() : 'UNKNOWN'} 
                  color={getStatusColor(reviewResult.status) !== 'default' ? getStatusColor(reviewResult.status) as 'success' | 'error' | 'info' : undefined}
                  size="small"
                />
              </Box>
              
              {reviewResult.reviewSummary && (
                <Paper sx={{ p: 2, bgcolor: 'background.paper' }}>
                  <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>
                    {reviewResult.reviewSummary}
                  </Typography>
                </Paper>
              )}
            </CardContent>
          </Card>

          {/* Comments Section */}
          {reviewResult?.comments && Array.isArray(reviewResult.comments) && reviewResult.comments.length > 0 && (
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 3 }}>
                  <BugReportIcon color="primary" />
                  <Typography variant="h6">
                    Code Review Suggestions
                  </Typography>
                  <Chip label={reviewResult.comments?.length || 0} color="primary" size="small" />
                </Box>

                <Grid container spacing={2}>
                  {reviewResult.comments.map((comment, index) => (
                    <Grid item xs={12} key={index}>
                      <Paper
                        elevation={2}
                        sx={{
                          p: 2,
                          borderLeft: 4,
                          borderColor: 'primary.main',
                          transition: 'all 0.3s ease',
                          '&:hover': {
                            boxShadow: 6,
                            transform: 'translateY(-2px)',
                          },
                        }}
                      >
                        {/* Comment Header */}
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
                          <Box sx={{ flex: 1 }}>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                              <CodeIcon fontSize="small" color="action" />
                              <Typography variant="subtitle1" fontWeight="bold">
                                {comment.fileName}
                              </Typography>
                              <Chip
                                label={`Line ${comment.diffLine}`}
                                size="small"
                                variant="outlined"
                                sx={{ ml: 1 }}
                              />
                            </Box>
                          </Box>
                          <IconButton
                            size="small"
                            onClick={() => toggleCommentExpanded(index)}
                            sx={{ ml: 1 }}
                          >
                            {expandedComments.has(index) ? <ExpandLessIcon /> : <ExpandMoreIcon />}
                          </IconButton>
                        </Box>

                        {/* Comment Preview */}
                        <Typography 
                          variant="body2" 
                          color="text.secondary"
                          sx={{
                            display: expandedComments.has(index) ? 'none' : '-webkit-box',
                            WebkitLineClamp: 2,
                            WebkitBoxOrient: 'vertical',
                            overflow: 'hidden',
                            textOverflow: 'ellipsis',
                          }}
                        >
                          {comment.comment}
                        </Typography>

                        {/* Expanded Comment */}
                        <Collapse in={expandedComments.has(index)}>
                          <Box sx={{ mt: 2 }}>
                            <Divider sx={{ mb: 2 }} />
                            <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>
                              {comment.comment}
                            </Typography>
                          </Box>
                        </Collapse>
                      </Paper>
                    </Grid>
                  ))}
                </Grid>
              </CardContent>
            </Card>
          )}

          {/* No Comments Found */}
          {reviewResult?.comments && reviewResult.comments.length === 0 && reviewResult.status !== 'error' && (
            <Card>
              <CardContent sx={{ textAlign: 'center', py: 6 }}>
                <CheckCircleIcon sx={{ fontSize: 60, color: 'success.main', mb: 2 }} />
                <Typography variant="h6" gutterBottom>
                  No Issues Found
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  The AI review found no suggestions for improvement in this pull request.
                </Typography>
              </CardContent>
            </Card>
          )}
        </Box>
      )}

      {/* Help Section */}
      {!reviewResult && !loading && (
        <Card sx={{ mt: 3, bgcolor: 'info.main', color: 'white' }}>
          <CardContent>
            <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <InfoIcon />
              How to Use
            </Typography>
            <Box component="ul" sx={{ m: 0, pl: 3 }}>
              <li>
                <Typography variant="body2">
                  Paste a GitHub pull request URL (e.g., https://github.com/owner/repo/pull/123)
                </Typography>
              </li>
              <li>
                <Typography variant="body2">
                  Click "Review" to analyze the PR with AI (powered by CodeGemma)
                </Typography>
              </li>
              <li>
                <Typography variant="body2">
                  View AI-generated suggestions for each file and line in the diff
                </Typography>
              </li>
              <li>
                <Typography variant="body2">
                  All reviews run locally - no data is sent to external APIs
                </Typography>
              </li>
            </Box>
          </CardContent>
        </Card>
      )}
    </Box>
  );
};

export default AIReview;

