# Test Platform Frontend

A modern, responsive React application for the Test Validation Platform providing automated test management, Pull Request integration, and real-time analytics.

## 🚀 Features

- **📊 Interactive Dashboard**: Real-time metrics, test statistics, and trend analysis
- **🔄 Pull Request Management**: Create, view, and manage PRs with automated test generation
- **🧪 Test Case Viewer**: Enhanced UI for viewing generated Unit, Integration, and E2E tests
- **⚡ Test Execution**: Run tests with real-time progress tracking
- **📈 Analytics & Reporting**: Comprehensive insights and coverage reports
- **🔐 Authentication**: Secure login with role-based access control
- **📱 Responsive Design**: Mobile-first design using Material-UI
- **🌙 Modern UI**: Clean, professional interface with smooth animations

## 🛠️ Tech Stack

- **Framework**: React 18+ with TypeScript
- **UI Library**: Material-UI (MUI) v5
- **State Management**: React Context API
- **HTTP Client**: Axios for API communication
- **Routing**: React Router v6
- **Charts**: Chart.js / Recharts for data visualization
- **Icons**: Material-UI Icons
- **Build Tool**: Create React App

## 📁 Project Structure

```
test-platform-frontend/
├── public/
│   ├── index.html          # Main HTML template
│   ├── manifest.json       # PWA manifest
│   └── favicon.ico         # App favicon
├── src/
│   ├── components/         # Reusable UI components
│   │   └── Layout/        # Layout components
│   ├── pages/             # Page components
│   │   ├── Dashboard/     # Dashboard page
│   │   ├── PullRequests/  # PR management
│   │   ├── TestGeneration/ # Test generation
│   │   ├── TestExecution/ # Test execution
│   │   ├── Reports/       # Analytics & reports
│   │   ├── Settings/      # Application settings
│   │   └── Auth/          # Authentication pages
│   ├── contexts/          # React contexts
│   ├── services/          # API service layer
│   ├── utils/             # Utility functions
│   ├── App.tsx            # Main app component
│   └── index.tsx          # Application entry point
├── package.json           # Dependencies and scripts
├── tsconfig.json          # TypeScript configuration
└── README.md             # This file
```

## 🛠️ Installation

### Prerequisites

- Node.js 16+
- npm 8+ or yarn 1.22+
- Backend API running (see test-platform-backend)

### Setup

1. **Navigate to frontend directory:**
   ```bash
   cd test-platform-frontend
   ```

2. **Install dependencies:**
   ```bash
   npm install
   # or
   yarn install
   ```

3. **Configure environment:**
   ```bash
   # Create .env file with backend URL
   echo "REACT_APP_API_URL=http://localhost:8080" > .env
   ```

4. **Start development server:**
   ```bash
   npm start
   # or
   yarn start
   ```

5. **Open in browser:**
   - Navigate to `http://localhost:3000`
   - Login with: `admin@testplatform.com` / `admin123`

## 🌐 Available Scripts

```bash
npm start          # Start development server
npm run build      # Create production build
npm test           # Run tests
npm run eject      # Eject from Create React App (⚠️ irreversible)
```

## 🔧 Configuration

### Environment Variables

Create a `.env` file in the root directory:

```bash
# Backend API Configuration
REACT_APP_API_URL=http://localhost:8080
REACT_APP_API_TIMEOUT=10000

# Application Settings
REACT_APP_NAME=Test Platform
REACT_APP_VERSION=1.0.0

# Feature Flags
REACT_APP_ENABLE_MOCK_DATA=false
REACT_APP_ENABLE_DEBUG_MODE=false
```

### API Integration

The frontend communicates with the backend through a centralized API service layer:

- **Base URL**: Configured via `REACT_APP_API_URL`
- **Authentication**: JWT tokens stored in localStorage
- **Error Handling**: Centralized error handling with user-friendly messages
- **Loading States**: Consistent loading indicators across the app

## 📊 Key Features

### Dashboard
- Real-time test statistics
- PR metrics and trends
- Interactive charts and graphs
- Recent activity feed

### Pull Request Management
- Create new PRs with automatic test generation
- View PR details with associated test cases
- Execute tests with real-time progress
- Enhanced test case viewer with expandable sections

### Test Case Viewer
- Color-coded test types (Unit, Integration, E2E)
- Expandable test suites
- Priority indicators (High, Medium, Low)
- Detailed test descriptions

### Authentication
- Secure JWT-based authentication
- Role-based access control
- Demo mode for development
- Persistent login sessions

## 🎨 UI/UX Features

- **Material Design**: Consistent Material-UI components
- **Responsive Layout**: Works on desktop, tablet, and mobile
- **Dark/Light Theme**: Theme support (future enhancement)
- **Loading States**: Smooth loading animations
- **Error Boundaries**: Graceful error handling
- **Accessibility**: WCAG compliant components

## 🔗 API Endpoints Used

The frontend integrates with these backend endpoints:

- `POST /api/auth/login` - User authentication
- `GET /api/dashboard/stats` - Dashboard statistics
- `GET /api/pull-requests` - List pull requests
- `GET /api/pull-requests/:id` - Get PR details with tests
- `POST /api/pull-requests` - Create new PR
- `POST /api/tests/execute` - Execute tests
- `GET /api/tests/suites` - Get test suites

## 🚀 Deployment

### Production Build

```bash
npm run build
```

### Docker Deployment

```dockerfile
FROM node:16-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
RUN npm run build
EXPOSE 3000
CMD ["npm", "start"]
```

### Environment Setup

For production deployment:

1. Set `REACT_APP_API_URL` to your backend URL
2. Configure HTTPS for security
3. Set up CDN for static assets
4. Enable gzip compression

## 🧪 Testing

```bash
# Run all tests
npm test

# Run tests with coverage
npm test -- --coverage

# Run tests in watch mode
npm test -- --watch
```

## 🔮 Future Enhancements

- **Real-time Updates**: WebSocket integration for live updates
- **Advanced Filtering**: Enhanced search and filter capabilities
- **Theme Customization**: User-configurable themes
- **Internationalization**: Multi-language support
- **Offline Support**: PWA capabilities for offline use
- **Advanced Analytics**: More detailed reporting and insights

## 🤝 Development

### Code Style

- **TypeScript**: Strict typing enabled
- **ESLint**: Code linting and formatting
- **Prettier**: Consistent code formatting
- **Component Structure**: Functional components with hooks

### Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if needed
5. Submit a pull request

## 📄 License

MIT License - see LICENSE file for details

## 🆘 Support

- **Issues**: GitHub Issues
- **Documentation**: In-app help sections
- **Email**: frontend-support@testplatform.com

---

**🚀 Build amazing testing workflows with our modern React frontend!**
