# 🏗️ Test Platform - Visual Architecture Diagram

## System Overview

```
╔═══════════════════════════════════════════════════════════════════════╗
║                         TEST PLATFORM SYSTEM                          ║
║                    (Automated Test Generation & Analysis)             ║
╚═══════════════════════════════════════════════════════════════════════╝
```

---

## 1. High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                                                                         │
│                          👤 USER (Browser)                              │
│                     http://localhost:3000                               │
│                                                                         │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │
                                 │ HTTP/REST
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                       ⚛️  FRONTEND LAYER                                │
│                    React 18 + TypeScript                                │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │  📱 Pages                                                         │  │
│  │  ├─ Dashboard      (Stats, Charts, Repository Selector)          │  │
│  │  ├─ Test Generation (Manual/Auto Test Creation)                  │  │
│  │  ├─ AI Review      (PR Analysis with CodeGemma)                  │  │
│  │  ├─ Reports        (Download: PDF/CSV/JSON)                      │  │
│  │  └─ Settings       (Configuration)                               │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │  🎨 Components                                                    │  │
│  │  ├─ Layout         (Sidebar, Header)                             │  │
│  │  ├─ Charts         (Recharts: Pie, Line, Bar)                    │  │
│  │  ├─ Tables         (MUI DataGrid)                                │  │
│  │  └─ Modals         (Test Details, Running Tests)                 │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │  🔄 State Management                                              │  │
│  │  ├─ RepositoryContext (Global: repositories, selected)           │  │
│  │  ├─ useState         (Local component state)                     │  │
│  │  └─ useEffect        (Side effects, data fetching)               │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│  Port: 3000 | Framework: React 18 | UI: Material-UI v5                │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │
                                 │ API Calls
                                 │ (apiService.ts)
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                       ☕ BACKEND LAYER                                   │
│                   Spring Boot 3.2 + Java 17                             │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │  🎯 REST Controllers                                              │  │
│  │  ├─ DashboardController     (/api/dashboard/*)                   │  │
│  │  │   ├─ GET /stats?repository=                                   │  │
│  │  │   ├─ GET /repositories                                        │  │
│  │  │   └─ GET /metrics                                             │  │
│  │  ├─ TestController           (/api/tests/*)                      │  │
│  │  │   ├─ GET /services?repository=                                │  │
│  │  │   └─ POST /generate                                           │  │
│  │  ├─ TestExecutionController  (/api/test-executions/*)            │  │
│  │  │   └─ GET /test-cases?repository=&status=                      │  │
│  │  ├─ HealthController         (/api/test-suites)                  │  │
│  │  │   └─ GET ?repository=                                         │  │
│  │  └─ AIReviewController       (/api/ai-pr-review)                 │  │
│  │      └─ POST (analyze GitHub PR)                                 │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │  🔧 Service Layer                                                 │  │
│  │  ├─ TestGenerationService                                        │  │
│  │  │   ├─ scanRepositoryForTests()                                 │  │
│  │  │   ├─ extractTestCasesFromFile()                               │  │
│  │  │   ├─ createTestSuiteFromFile()                                │  │
│  │  │   └─ Map<String, List<TestSuite>> repositoryTests             │  │
│  │  ├─ TestExecutionService                                         │  │
│  │  │   └─ getDetailedTestCaseResults()                             │  │
│  │  ├─ MultiRepositoryService                                       │  │
│  │  │   ├─ getEnabledRepositories()                                 │  │
│  │  │   └─ getDefaultRepository()                                   │  │
│  │  ├─ GitHubApiClient                                              │  │
│  │  │   ├─ fetchPrDiffFiles()                                       │  │
│  │  │   └─ postInlineComment()                                      │  │
│  │  └─ CodeGemmaService                                             │  │
│  │      └─ analyzePrChanges()                                       │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │  📊 Data Models                                                   │  │
│  │  ├─ TestSuite (id, name, type, testCases, coverage)              │  │
│  │  ├─ TestCase  (id, name, type, status, priority, errorMsg)       │  │
│  │  ├─ Repository (id, name, path, enabled)                         │  │
│  │  └─ DashboardStatsDTO (trends, stats, recentPRs)                 │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│  Port: 8080 | Framework: Spring Boot | ORM: JPA (H2 In-Memory)        │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │
                                 │ File I/O, Git, External APIs
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    🌐 EXTERNAL SYSTEMS & DATA                           │
│  ┌──────────────────┐  ┌──────────────────┐  ┌────────────────────┐   │
│  │  📂 File System  │  │  🐙 GitHub API   │  │  🤖 Ollama LLM     │   │
│  │                  │  │                  │  │  (CodeGemma)       │   │
│  │  Kepler App:     │  │  PR Analysis:    │  │                    │   │
│  │  /Desktop/       │  │  • Fetch Files   │  │  • AI Code Review  │   │
│  │  kepler-app/     │  │  • Post Comments │  │  • Generate Tests  │   │
│  │  ├─ project-svc  │  │  • Get Commits   │  │  • Suggestions     │   │
│  │  ├─ contrib-svc  │  │                  │  │                    │   │
│  │  ├─ work-svc     │  │  Requires:       │  │  Port: 11434       │   │
│  │  └─ public-api   │  │  GITHUB_TOKEN    │  │  Model: llama3.2   │   │
│  │                  │  │                  │  │                    │   │
│  │  Shared Svc:     │  │                  │  │                    │   │
│  │  /Desktop/       │  │                  │  │                    │   │
│  │  shared-services/│  │                  │  │                    │   │
│  └──────────────────┘  └──────────────────┘  └────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Data Flow - Repository Selection

```
┌─────────────────────────────────────────────────────────────────────┐
│  Step 1: Application Initialization                                 │
└─────────────────────────────────────────────────────────────────────┘
                           │
                           ▼
    ┌─────────────────────────────────────────────────┐
    │  App.tsx wraps with <RepositoryProvider>        │
    └─────────────────────────────────────────────────┘
                           │
                           ▼
    ┌─────────────────────────────────────────────────┐
    │  RepositoryContext.tsx                          │
    │  • Fetches: GET /api/dashboard/repositories     │
    │  • Stores: repositories[], selectedRepository   │
    │  • Provides: setSelectedRepository()            │
    └─────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────────┐
│  Step 2: Backend Returns Repository List                           │
└─────────────────────────────────────────────────────────────────────┘
                           │
                           ▼
    ┌─────────────────────────────────────────────────┐
    │  DashboardController.getRepositories()          │
    │  ↓                                              │
    │  MultiRepositoryService.getEnabledRepositories()│
    │  ↓                                              │
    │  Returns:                                       │
    │  [                                              │
    │    {id: "shared-services", name: "Shared       │
    │     Services", testCount: 19},                 │
    │    {id: "kepler-app", name: "Kepler App",      │
    │     testCount: 69}                             │
    │  ]                                              │
    └─────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────────┐
│  Step 3: User Selects Repository                                   │
└─────────────────────────────────────────────────────────────────────┘
                           │
                           ▼
    ┌─────────────────────────────────────────────────┐
    │  Dashboard.tsx                                  │
    │  • User clicks dropdown                         │
    │  • Selects "Kepler App"                         │
    │  • Calls: setSelectedRepository("kepler-app")   │
    └─────────────────────────────────────────────────┘
                           │
                           ▼
    ┌─────────────────────────────────────────────────┐
    │  Context broadcasts change                      │
    │  • selectedRepository = "kepler-app"            │
    │  • All subscribers notified                     │
    └─────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────────┐
│  Step 4: Components React to Change                                │
└─────────────────────────────────────────────────────────────────────┘
                           │
          ┌────────────────┼────────────────┐
          │                │                │
          ▼                ▼                ▼
    ┌─────────┐      ┌──────────┐    ┌──────────┐
    │Dashboard│      │ Reports  │    │  Others  │
    │ Page    │      │ Page     │    │          │
    └─────────┘      └──────────┘    └──────────┘
          │                │                │
          │                │                │
          │ useEffect      │ useEffect      │
          │ triggers       │ triggers       │
          │                │                │
          ▼                ▼                ▼
    ┌─────────────────────────────────────────────┐
    │  Parallel API Calls (with repository param) │
    │  • GET /api/dashboard/stats?repository=     │
    │  • GET /api/test-suites?repository=         │
    │  • GET /api/tests/services?repository=      │
    │  • GET /api/test-executions?repository=     │
    └─────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────────┐
│  Step 5: Backend Processes Request                                 │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 3. Test Scanning & Extraction Flow

```
┌────────────────────────────────────────────────────────────────────┐
│  TestGenerationService.scanRepositoryForTests("kepler-app")        │
└────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
          ┌───────────────────────────────────────┐
          │  Load Config from application.yml     │
          │  • path: /Desktop/kepler-app          │
          │  • services: [project, contrib,       │
          │              work, public-api]        │
          └───────────────────────────────────────┘
                              │
                              ▼
          ┌───────────────────────────────────────┐
          │  For each service directory:          │
          │  • project-service/                   │
          │  • contributor-service/               │
          │  • work-service/                      │
          │  • public-api-service/                │
          └───────────────────────────────────────┘
                              │
                              ▼
          ┌───────────────────────────────────────┐
          │  Find all *Test.java files            │
          │  (Recursive, max depth: 25)           │
          │                                       │
          │  Example files found:                 │
          │  • ProjectControllerTest.java         │
          │  • ContributorServiceTest.java        │
          │  • WorkServiceIntegrationTest.java    │
          │  • PublicApiControllerTest.java       │
          └───────────────────────────────────────┘
                              │
                              ▼
          ┌───────────────────────────────────────┐
          │  For each test file:                  │
          │  BufferedReader.readLine()            │
          └───────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│  Extract Test Methods                                               │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  @Test                                  ← Line matches "@Test"      │
│  public void testCreateProject() {      ← Extract method name      │
│      ...                                                            │
│  }                                                                  │
│                                                                     │
│  Create TestCase:                                                   │
│  • name: "testCreateProject"                                        │
│  • type: UNIT (from filename/path)                                 │
│  • status: PASSED/FAILED (90% pass rate)                           │
│  • priority: HIGH/MEDIUM/LOW (based on name patterns)              │
│  • executionTime: 100-600ms (random)                               │
│  • errorMessage: (if failed, realistic error)                      │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
          ┌───────────────────────────────────────┐
          │  Group into TestSuite                 │
          │  • fileName: ProjectControllerTest    │
          │  • testCases: [10 test methods]       │
          │  • totalTests: 10                     │
          │  • passedTests: 9                     │
          │  • failedTests: 1                     │
          │  • coverage: 87.3% (realistic random) │
          └───────────────────────────────────────┘
                              │
                              ▼
          ┌───────────────────────────────────────┐
          │  Extract Service from File Path       │
          │                                       │
          │  Path: /project-service/src/test/...  │
          │  → Service: "Project Service"         │
          │                                       │
          │  Path: /contributor-service/...       │
          │  → Service: "Contributor Service"     │
          └───────────────────────────────────────┘
                              │
                              ▼
          ┌───────────────────────────────────────┐
          │  Aggregate by Service                 │
          │                                       │
          │  Project Service:                     │
          │    • 14 test suites                   │
          │    • 128 total tests                  │
          │                                       │
          │  Contributor Service:                 │
          │    • 16 test suites                   │
          │    • 132 total tests                  │
          │                                       │
          │  Work Service:                        │
          │    • 23 test suites                   │
          │    • 133 total tests                  │
          │                                       │
          │  Public API Service:                  │
          │    • 16 test suites                   │
          │    • 81 total tests                   │
          └───────────────────────────────────────┘
                              │
                              ▼
          ┌───────────────────────────────────────┐
          │  Store in Repository Map              │
          │                                       │
          │  repositoryTests.put(                 │
          │    "kepler-app",                      │
          │    [69 TestSuite objects]             │
          │  )                                    │
          └───────────────────────────────────────┘
                              │
                              ▼
          ┌───────────────────────────────────────┐
          │  Return to Controller                 │
          │  • JSON serialized                    │
          │  • Sent to Frontend                   │
          └───────────────────────────────────────┘
```

---

## 4. Multi-Repository Configuration

```
┌─────────────────────────────────────────────────────────────────────┐
│  application.yml                                                    │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  app:                                                               │
│    multi-repository:                                                │
│      enabled: true                                                  │
│      default-repository: shared-services                            │
│      repositories:                                                  │
│                                                                     │
│        - id: shared-services                                        │
│          name: "Shared Services"                                    │
│          remote-url: git@github.com:../shared-services.git          │
│          path: /Users/smamidala/Desktop/shared-services             │
│          branch: main                                               │
│          language: java                                             │
│          framework: spring-boot                                     │
│          enabled: true                                              │
│                                                                     │
│        - id: kepler-app                                             │
│          name: "Kepler App"                                         │
│          remote-url: git@github.com:../kepler-app.git               │
│          path: /Users/smamidala/Desktop/kepler-app                  │
│          branch: release                                            │
│          language: java                                             │
│          framework: spring-boot                                     │
│          enabled: true                                              │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
          ┌───────────────────────────────────────┐
          │  MultiRepositoryService               │
          │  @PostConstruct init()                │
          │  • Loads config on startup            │
          │  • Validates paths exist              │
          │  • Sets default repository            │
          └───────────────────────────────────────┘
                              │
                              ▼
          ┌───────────────────────────────────────┐
          │  TestGenerationService                │
          │  @PostConstruct init()                │
          │  • Calls scanRepositoryForTests()     │
          │  • For each enabled repository        │
          │  • Stores in:                         │
          │    Map<String, List<TestSuite>>       │
          │    repositoryTests                    │
          └───────────────────────────────────────┘
                              │
                              ▼
          ┌───────────────────────────────────────┐
          │  Memory Storage                       │
          │  {                                    │
          │    "kepler-app": [69 TestSuites],     │
          │    "shared-services": [19 TestSuites] │
          │  }                                    │
          └───────────────────────────────────────┘
```

---

## 5. API Request/Response Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│  Frontend: Dashboard Component                                     │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
          ┌───────────────────────────────────────┐
          │  apiService.ts                        │
          │  dashboardService.getStats(           │
          │    repository: "kepler-app"           │
          │  )                                    │
          └───────────────────────────────────────┘
                              │
                              ▼ HTTP GET
          ┌───────────────────────────────────────┐
          │  http://localhost:8080                │
          │  /api/dashboard/stats                 │
          │  ?repository=kepler-app               │
          └───────────────────────────────────────┘
                              │
                              ▼
          ┌───────────────────────────────────────┐
          │  @GetMapping("/stats")                │
          │  DashboardController                  │
          │  .getDashboardStats(repository)       │
          └───────────────────────────────────────┘
                              │
                              ▼
          ┌───────────────────────────────────────┐
          │  testGenerationService                │
          │  .getAllTests("kepler-app")           │
          │  ↓                                    │
          │  Returns: List<TestSuite>             │
          │  [69 test suites]                     │
          └───────────────────────────────────────┘
                              │
                              ▼
          ┌───────────────────────────────────────┐
          │  Calculate Statistics:                │
          │  • totalTests: 474                    │
          │  • passedTests: 436                   │
          │  • failedTests: 38                    │
          │  • coverage: 87.5%                    │
          │  • Generate trends (3 days)           │
          └───────────────────────────────────────┘
                              │
                              ▼
          ┌───────────────────────────────────────┐
          │  Create DashboardStatsDTO             │
          │  {                                    │
          │    totalTests: 474,                   │
          │    passedTests: 436,                  │
          │    failedTests: 38,                   │
          │    coverage: 87.5,                    │
          │    trendsData: [...]                  │
          │  }                                    │
          └───────────────────────────────────────┘
                              │
                              ▼ JSON Response
          ┌───────────────────────────────────────┐
          │  ApiResponse.success(statsDTO)        │
          │  HTTP 200 OK                          │
          └───────────────────────────────────────┘
                              │
                              ▼
          ┌───────────────────────────────────────┐
          │  Frontend receives JSON               │
          │  • Updates state                      │
          │  • Triggers re-render                 │
          │  • Updates charts/tables              │
          └───────────────────────────────────────┘
```

---

## 6. Component Hierarchy

```
App.tsx
 │
 ├─ RepositoryProvider (Context)
 │   └─ [repositories, selectedRepository, setSelectedRepository]
 │
 └─ Router
     │
     ├─ Layout
     │   ├─ Sidebar (Navigation)
     │   └─ Main Content Area
     │
     └─ Routes
         │
         ├─ /dashboard → Dashboard.tsx
         │   │
         │   ├─ useRepository() hook
         │   ├─ Repository Selector Dropdown
         │   ├─ Stat Cards (4 cards: Total, Passed, Failed, Running)
         │   ├─ Test Execution Trends (Line Chart)
         │   ├─ Service-Wise Distribution (Pie Chart)
         │   ├─ Test Results Table
         │   ├─ Running Tests Modal
         │   └─ Test Details Modal
         │
         ├─ /test-generation → TestGeneration.tsx
         │   ├─ Manual Test Creation Form
         │   ├─ Batch Test Generation
         │   └─ Test Suite Configuration
         │
         ├─ /ai-review → AIReview.tsx
         │   ├─ PR URL Input (with tooltip)
         │   ├─ Review Results Display
         │   └─ AI Suggestions List
         │
         ├─ /reports → Reports.tsx
         │   ├─ useRepository() hook (shared context)
         │   ├─ Statistics Summary
         │   ├─ Service-Wise Breakdown
         │   ├─ Test Execution Trends Chart
         │   └─ Download Buttons (PDF, CSV, JSON)
         │
         └─ /settings → Settings.tsx
             ├─ Repository Configuration
             └─ GitHub Token Setup
```

---

## 7. Technology Stack

```
┌─────────────────────────────────────────────────────────────────────┐
│                          TECHNOLOGY STACK                           │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  FRONTEND                                                           │
│  ├─ React 18              (UI Framework)                            │
│  ├─ TypeScript            (Type Safety)                             │
│  ├─ Material-UI v5        (Component Library)                       │
│  ├─ Recharts              (Data Visualization)                      │
│  ├─ React Router v6       (Navigation)                              │
│  └─ Axios                 (HTTP Client)                             │
│                                                                     │
│  BACKEND                                                            │
│  ├─ Spring Boot 3.2       (Framework)                               │
│  ├─ Java 17               (Language)                                │
│  ├─ Spring Web            (REST APIs)                               │
│  ├─ Spring Data JPA       (Data Access)                             │
│  ├─ H2 Database           (In-Memory DB)                            │
│  ├─ Maven                 (Build Tool)                              │
│  └─ Lombok                (Code Generation)                         │
│                                                                     │
│  EXTERNAL SERVICES                                                  │
│  ├─ GitHub API            (PR Analysis)                             │
│  ├─ Ollama                (Local LLM Server)                        │
│  └─ CodeGemma             (AI Model)                                │
│                                                                     │
│  TESTING & COVERAGE                                                 │
│  ├─ JUnit 5               (Unit Testing)                            │
│  ├─ JaCoCo                (Code Coverage)                           │
│  └─ Spring Test           (Integration Testing)                     │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 8. Key Features Summary

```
┌─────────────────────────────────────────────────────────────────────┐
│  ✅ Multi-Repository Support                                        │
│     • Configure multiple Git repositories                           │
│     • Switch between repos with dropdown                            │
│     • Shared state across all components                            │
│                                                                     │
│  ✅ Real Test File Scanning                                         │
│     • Scans actual Java test files                                  │
│     • Extracts @Test annotations                                    │
│     • Parses method names and metadata                              │
│                                                                     │
│  ✅ Service-Wise Aggregation                                        │
│     • Groups tests by microservice                                  │
│     • Calculates per-service metrics                                │
│     • Visualizes distribution with charts                           │
│                                                                     │
│  ✅ Realistic Data Simulation                                       │
│     • 90% pass rate (configurable)                                  │
│     • 70-95% coverage range                                         │
│     • Varied priorities (HIGH/MEDIUM/LOW)                           │
│     • Realistic error messages for failures                         │
│                                                                     │
│  ✅ AI-Powered PR Review                                            │
│     • Analyzes GitHub pull requests                                 │
│     • Uses local CodeGemma LLM                                      │
│     • Provides code quality suggestions                             │
│                                                                     │
│  ✅ Comprehensive Reporting                                         │
│     • Download reports in PDF/CSV/JSON                              │
│     • Repository-specific reports                                   │
│     • Includes trends and statistics                                │
│                                                                     │
│  ✅ Interactive Dashboard                                           │
│     • Real-time statistics                                          │
│     • Interactive charts (Recharts)                                 │
│     • Drill-down into test details                                  │
│     • Modal views for detailed data                                 │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 9. Deployment Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                       DEVELOPMENT ENVIRONMENT                       │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  📂 Project Structure:                                              │
│  /Users/smamidala/Desktop/q-force/                                  │
│  ├─ test-platform-frontend/                                         │
│  │  ├─ src/                                                         │
│  │  ├─ public/                                                      │
│  │  ├─ package.json                                                 │
│  │  └─ npm start (Port 3000)                                        │
│  │                                                                  │
│  └─ test-platform-backend-java/                                     │
│     ├─ src/main/java/                                               │
│     ├─ src/main/resources/                                          │
│     │  └─ application.yml                                           │
│     ├─ pom.xml                                                      │
│     └─ ./start.sh (Port 8080)                                       │
│                                                                     │
│  📦 External Repositories:                                          │
│  /Users/smamidala/Desktop/                                          │
│  ├─ kepler-app/                                                     │
│  │  ├─ project-service/                                             │
│  │  ├─ contributor-service/                                         │
│  │  ├─ work-service/                                                │
│  │  └─ public-api-service/                                          │
│  │                                                                  │
│  └─ shared-services/                                                │
│     └─ (various service modules)                                    │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 10. Security Considerations

```
┌─────────────────────────────────────────────────────────────────────┐
│  🔒 Security Features                                               │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ✓ GitHub Token Authentication                                      │
│    • Stored in environment variable: GITHUB_TOKEN                   │
│    • Required for private repository access                         │
│    • Used for PR API calls                                          │
│                                                                     │
│  ✓ CORS Configuration                                               │
│    • Restricted to localhost:3000 in development                    │
│    • Configurable for production                                    │
│                                                                     │
│  ✓ Input Validation                                                 │
│    • Repository ID validation                                       │
│    • Path traversal prevention                                      │
│    • SQL injection prevention (JPA)                                 │
│                                                                     │
│  ✓ Read-Only File Access                                            │
│    • Scans test files only                                          │
│    • No write operations to repositories                            │
│    • Sandboxed file system access                                   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 📊 Quick Reference

**Frontend URL:** http://localhost:3000
**Backend URL:** http://localhost:8080
**API Docs:** http://localhost:8080/swagger-ui.html (if enabled)

**Key Directories:**
- Frontend: `/Users/smamidala/Desktop/q-force/test-platform-frontend`
- Backend: `/Users/smamidala/Desktop/q-force/test-platform-backend-java`
- Test Repos: `/Users/smamidala/Desktop/{kepler-app,shared-services}`

**Main Branches:**
- `main` - Production-ready code
- `prod-1` - Production deployment branch

---

*Generated: October 17, 2025*
*Version: 1.0*

