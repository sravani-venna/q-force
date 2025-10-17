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
  Checkbox,
  FormControlLabel,
  LinearProgress,
  Tooltip,
} from '@mui/material';
import {
  Send as SendIcon,
  GitHub as GitHubIcon,
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
  Info as InfoIcon,
  Code as CodeIcon,
  ExpandMore as ExpandMoreIcon,
  BugReport as BugReportIcon,
  Warning as WarningIcon,
  PriorityHigh as PriorityHighIcon,
  CheckBox as CheckBoxIcon,
  CheckBoxOutlineBlank as CheckBoxOutlineBlankIcon,
} from '@mui/icons-material';
import { toast } from 'react-toastify';
import { aiReviewService } from '../../services/prReviewService';

interface DiffComment {
  fileName: string;
  diffLine: number;
  comment: string;
  severity?: 'bug' | 'high' | 'medium' | 'low';
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
  const [selectedComments, setSelectedComments] = useState<Set<number>>(new Set());

  // Parse PR URL to extract owner, repo, and PR number
  const parsePrUrl = (url: string): { repoOwner: string; repoName: string; prNumber: number } | null => {
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
    setSelectedComments(new Set());

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
      
      // Add severity to comments (random for demo, should come from API)
      if (result.comments) {
        result.comments = result.comments.map((comment, index) => ({
          ...comment,
          severity: index === 0 ? 'bug' : index % 3 === 0 ? 'high' : index % 2 === 0 ? 'medium' : 'low'
        }));
      }
      
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

  const toggleCommentSelected = (index: number) => {
    const newSelected = new Set(selectedComments);
    if (newSelected.has(index)) {
      newSelected.delete(index);
    } else {
      newSelected.add(index);
    }
    setSelectedComments(newSelected);
  };

  const toggleSelectAll = () => {
    if (reviewResult?.comments) {
      if (selectedComments.size === reviewResult.comments.length) {
        setSelectedComments(new Set());
      } else {
        setSelectedComments(new Set(reviewResult.comments.map((_, i) => i)));
      }
    }
  };

  const handleApplyChanges = () => {
    const selectedCount = selectedComments.size;
    toast.success(`Applying ${selectedCount} suggested change${selectedCount !== 1 ? 's' : ''}...`);
    // TODO: Implement actual apply changes logic
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

  const getSeverityColor = (severity?: string) => {
    switch (severity) {
      case 'bug':
        return 'error';
      case 'high':
        return 'warning';
      case 'medium':
        return 'info';
      case 'low':
        return 'default';
      default:
        return 'default';
    }
  };

  const getSeverityLabel = (severity?: string) => {
    return severity ? severity.charAt(0).toUpperCase() + severity.slice(1) : 'Info';
  };

  // Calculate code quality score (simplified)
  const calculateCodeQualityScore = () => {
    if (!reviewResult?.comments) return 100;
    const bugCount = reviewResult.comments.filter(c => c.severity === 'bug').length;
    const highCount = reviewResult.comments.filter(c => c.severity === 'high').length;
    const mediumCount = reviewResult.comments.filter(c => c.severity === 'medium').length;
    
    const score = 100 - (bugCount * 15 + highCount * 10 + mediumCount * 5);
    return Math.max(0, Math.min(100, score));
  };

  // Extract file issues from review summary
  const extractFileIssues = () => {
    if (!reviewResult?.reviewSummary) return [];
    
    const lines = reviewResult.reviewSummary.split('\n');
    return lines
      .filter(line => line.includes('**') && line.includes('.java'))
      .map(line => {
        const match = line.match(/\*\*(.+?)\*\*:?\s*(.+)/);
        if (match) {
          return { file: match[1], issue: match[2] };
        }
        return null;
      })
      .filter(Boolean);
  };

  const codeQualityScore = reviewResult ? calculateCodeQualityScore() : 0;
  const fileIssues = reviewResult ? extractFileIssues() : [];

  return (
    <Box>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" gutterBottom fontWeight="bold">
          AI Review
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
            <Tooltip 
              title="Example: https://github.com/owner/repo/pull/123" 
              arrow
              placement="top"
              enterDelay={300}
            >
              <TextField
                fullWidth
                placeholder="Paste GitHub PR URL here..."
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
                sx={{
                  '& .MuiOutlinedInput-root': {
                    transition: 'all 0.3s ease',
                    '&:hover': {
                      boxShadow: '0 4px 12px rgba(0, 0, 0, 0.1)',
                      borderColor: 'primary.main',
                    },
                  },
                }}
              />
            </Tooltip>
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

      {/* Results Section */}
      {reviewResult && !loading && (
        <Box>
          <Grid container spacing={3}>
            {/* Left Column - Status and File Issues */}
            <Grid item xs={12} md={8}>
              {/* Status Card */}
              <Card sx={{ mb: 3, bgcolor: (theme) => {
                  const statusColor = getStatusColor(reviewResult.status);
                  return statusColor !== 'default' ? alpha(theme.palette[statusColor].main, 0.1) : alpha(theme.palette.grey[500], 0.1);
                }}}>
                <CardContent>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
                    <CheckCircleIcon sx={{ color: 'success.main' }} />
                    <Typography variant="h6">
                      Review Status: PR #{reviewResult.prNumber}
                    </Typography>
                    <Chip 
                      label={reviewResult.status ? reviewResult.status.toUpperCase().replace('_', ' ') : 'UNKNOWN'} 
                      color={getStatusColor(reviewResult.status) !== 'default' ? getStatusColor(reviewResult.status) as 'success' | 'error' | 'info' : undefined}
                      size="small"
                    />
                  </Box>
                  
                  {/* File Issues */}
                  {fileIssues.length > 0 && (
                    <Box sx={{ mt: 2 }}>
                      {fileIssues.map((issue: any, index: number) => (
                        <Paper key={index} sx={{ p: 2, mb: 2, bgcolor: 'background.paper' }}>
                          <Typography variant="body2" fontWeight="bold" color="primary" sx={{ mb: 0.5 }}>
                            {issue.file}
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            {issue.issue}
                          </Typography>
                        </Paper>
                      ))}
                    </Box>
                  )}
                </CardContent>
              </Card>

              {/* Comments Section */}
              {reviewResult?.comments && Array.isArray(reviewResult.comments) && reviewResult.comments.length > 0 && (
                <Card>
                  <CardContent>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <BugReportIcon color="primary" />
                        <Typography variant="h6">
                          Code Review Suggestions
                        </Typography>
                        <Chip label={reviewResult.comments?.length || 0} color="primary" size="small" />
                      </Box>
                      
                      <Box sx={{ display: 'flex', gap: 1 }}>
                        <Button
                          variant="outlined"
                          size="small"
                          onClick={toggleSelectAll}
                          startIcon={selectedComments.size === reviewResult.comments.length ? <CheckBoxIcon /> : <CheckBoxOutlineBlankIcon />}
                        >
                          Select All ({selectedComments.size}/{reviewResult.comments.length})
                        </Button>
                        <Button
                          variant="contained"
                          size="small"
                          disabled={selectedComments.size === 0}
                          onClick={handleApplyChanges}
                          startIcon={<CheckCircleIcon />}
                        >
                          Apply Changes ({selectedComments.size})
                        </Button>
                      </Box>
                    </Box>

                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                      {reviewResult.comments.map((comment, index) => (
                        <Paper
                          key={index}
                          elevation={2}
                          sx={{
                            borderLeft: 4,
                            borderColor: selectedComments.has(index) ? 'primary.main' : 'grey.300',
                            transition: 'all 0.3s ease',
                            bgcolor: selectedComments.has(index) ? alpha('#2196f3', 0.05) : 'background.paper',
                          }}
                        >
                          {/* Comment Header */}
                          <Box sx={{ p: 2, display: 'flex', alignItems: 'flex-start', gap: 2 }}>
                            <Checkbox
                              checked={selectedComments.has(index)}
                              onChange={() => toggleCommentSelected(index)}
                              sx={{ mt: -0.5 }}
                            />
                            
                            <Box sx={{ flex: 1 }}>
                              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                                <CodeIcon fontSize="small" color="action" />
                                <Typography variant="subtitle2" fontWeight="bold" sx={{ flex: 1 }}>
                                  {comment.fileName}
                                </Typography>
                                <Chip
                                  label={`Line ${comment.diffLine}`}
                                  size="small"
                                  variant="outlined"
                                />
                              </Box>

                              {/* Severity Badge */}
                              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                                <Chip
                                  label={getSeverityLabel(comment.severity)}
                                  color={getSeverityColor(comment.severity) as any}
                                  size="small"
                                  icon={comment.severity === 'bug' ? <BugReportIcon /> : 
                                        comment.severity === 'high' ? <WarningIcon /> : 
                                        <InfoIcon />}
                                />
                              </Box>

                              {/* Comment Text */}
                              <Typography 
                                variant="body2" 
                                color="text.secondary"
                                sx={{
                                  display: expandedComments.has(index) ? 'block' : '-webkit-box',
                                  WebkitLineClamp: expandedComments.has(index) ? 'unset' : 2,
                                  WebkitBoxOrient: 'vertical',
                                  overflow: 'hidden',
                                  textOverflow: 'ellipsis',
                                  whiteSpace: expandedComments.has(index) ? 'pre-wrap' : 'normal',
                                }}
                              >
                                {comment.comment}
                              </Typography>

                              {comment.comment.length > 100 && (
                                <Button
                                  size="small"
                                  onClick={() => toggleCommentExpanded(index)}
                                  sx={{ mt: 1 }}
                                  endIcon={expandedComments.has(index) ? <ExpandMoreIcon sx={{ transform: 'rotate(180deg)' }} /> : <ExpandMoreIcon />}
                                >
                                  {expandedComments.has(index) ? 'Show Less' : 'Show More'}
                                </Button>
                              )}
                            </Box>
                          </Box>
                        </Paper>
                      ))}
                    </Box>
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
            </Grid>

            {/* Right Column - Code Quality Score */}
            <Grid item xs={12} md={4}>
              <Card 
                sx={{ 
                  background: codeQualityScore >= 90 ? 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' :
                              codeQualityScore >= 70 ? 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' :
                              'linear-gradient(135deg, #fad0c4 0%, #ffd1ff 100%)',
                  color: 'white',
                  textAlign: 'center',
                }}
              >
                <CardContent>
                  <Typography variant="h3" fontWeight="bold" sx={{ mb: 1 }}>
                    {codeQualityScore}
                    <Typography component="span" variant="h5">
                      {' '}/ 100
                    </Typography>
                  </Typography>
                  <Typography variant="body2" sx={{ mb: 2, opacity: 0.9 }}>
                    Code Quality Score
                  </Typography>
                  
                  <Chip
                    icon={<PriorityHighIcon />}
                    label={codeQualityScore >= 90 ? "Excellent" : 
                           codeQualityScore >= 70 ? "High Priority" : 
                           "Needs Attention"}
                    sx={{ 
                      bgcolor: 'rgba(255, 255, 255, 0.3)',
                      color: 'white',
                      fontWeight: 'bold',
                    }}
                  />
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        </Box>
      )}

      {/* Error State */}
      {error && reviewResult?.status === 'error' && (
        <Alert severity="error" icon={<ErrorIcon />} sx={{ mb: 3 }}>
          <Typography variant="body2" fontWeight="bold">Review Failed</Typography>
          <Typography variant="body2">{reviewResult.errorMessage || error}</Typography>
        </Alert>
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
                  Click "Review" to analyze the PR with AI (powered by Llama 3.2)
                </Typography>
              </li>
              <li>
                <Typography variant="body2">
                  View AI-generated suggestions with severity ratings and quality scores
                </Typography>
              </li>
              <li>
                <Typography variant="body2">
                  Select and apply suggestions with one click
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
