# Test Platform Backend - Java Spring Boot

This is the Java Spring Boot conversion of the Node.js Test Platform Backend, providing 100% API compatibility with the existing React frontend.

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Running the Application

1. **Clone and navigate to the project:**
   ```bash
   cd test-platform-backend-java
   ```

2. **Build the project:**
   ```bash
   mvn clean compile
   ```

3. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

4. **Alternative - Run the JAR:**
   ```bash
   mvn clean package
   java -jar target/test-platform-backend-1.0.0.jar
   ```

The server will start on `http://localhost:8080`

## 📋 API Endpoints

### Health & Info
- `GET /health` - Health check
- `GET /` - API information

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration

### Pull Requests
- `GET /api/pull-requests` - Get all pull requests
- `GET /api/pull-requests/{id}` - Get specific pull request
- `POST /api/pull-requests` - Create new pull request
- `PUT /api/pull-requests/{id}` - Update pull request
- `DELETE /api/pull-requests/{id}` - Delete pull request
- `POST /api/pull-requests/{id}/tests/generate` - Generate tests for PR
- `POST /api/pull-requests/{id}/tests/execute` - Execute tests for PR

### Tests
- `GET /api/tests/suites` - Get all test suites
- `POST /api/tests/generate` - Generate test cases
- `POST /api/tests/execute` - Execute tests
- `GET /api/tests/executions/{id}` - Get execution status
- `GET /api/tests/stats` - Get test statistics

### Dashboard
- `GET /api/dashboard/stats` - Get dashboard statistics
- `GET /api/dashboard/metrics` - Get detailed metrics

### Webhooks
- `POST /api/webhook/pr-created` - Handle PR creation webhook

## 🔐 Test Credentials

- **Email:** admin@testplatform.com
- **Password:** admin123

## 🏗️ Project Structure

```
src/main/java/com/testplatform/backend/
├── config/           # Configuration classes
├── controller/       # REST controllers
├── dto/             # Data Transfer Objects
├── enums/           # Enum definitions
├── exception/       # Exception handling
├── model/           # Domain models
├── service/         # Business logic services
├── util/            # Utility classes
└── TestPlatformBackendApplication.java
```

## 🔄 Frontend Compatibility

This Java backend maintains 100% API compatibility with the existing React frontend:

- **Same API Response Format:** All endpoints return the exact same JSON structure
- **Same HTTP Status Codes:** Identical status codes for all scenarios
- **Same Error Handling:** Error responses match the Node.js format exactly
- **Same CORS Configuration:** Supports the same frontend origins
- **Same Authentication:** Compatible token format and user structure

## 🛠️ Configuration

Configuration is managed through `application.yml`:

```yaml
server:
  port: 8080

app:
  api-prefix: /api
  allowed-origins: 
    - http://localhost:3000
    - http://localhost:3001
  jwt:
    secret: your-jwt-secret
    expires-in: 24h
```

## 🧪 Testing with Frontend

1. Start the Java backend: `mvn spring-boot:run`
2. Start the React frontend: `npm start` (in the frontend directory)
3. The frontend will work seamlessly with the Java backend

## 📊 Features

- **Test Generation:** Automated test case generation for different file types
- **Test Execution:** Simulated test execution with realistic results
- **Pull Request Management:** Full CRUD operations for pull requests
- **Dashboard Analytics:** Real-time statistics and metrics
- **Mock Data:** Pre-populated data for immediate testing
- **Async Processing:** Non-blocking test generation and execution
- **Error Handling:** Comprehensive error handling with proper HTTP status codes

## 🔧 Development

### Building
```bash
mvn clean compile
```

### Running Tests
```bash
mvn test
```

### Packaging
```bash
mvn clean package
```

### Running with Different Profiles
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=production
```

## 📝 API Response Format

All API responses follow this consistent format:

```json
{
  "success": true,
  "data": { ... },
  "count": 10,
  "message": "Operation successful"
}
```

Error responses:
```json
{
  "success": false,
  "error": {
    "message": "Error description"
  }
}
```

## 🚀 Deployment

### Docker (Optional)
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/test-platform-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Build and Run with Docker
```bash
mvn clean package
docker build -t test-platform-backend .
docker run -p 8080:8080 test-platform-backend
```

## 🤝 Contributing

1. Make changes to the Java codebase
2. Ensure all tests pass: `mvn test`
3. Test with the React frontend to ensure compatibility
4. Submit pull request

## 📄 License

MIT License - Same as the original Node.js version
