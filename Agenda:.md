Agenda:
-> UI/UX changes are required, plan for it
-> Confirmation of proposed and existing system behaviour
-> Spike review comments, if reviewed from Chamundeswari and Mukund
-> Analysis of ETA, (one developer: 12-14 weeks if the above requirments are proper.)
-> Plan for execution (like separation of story to chunks and distribution)
-> Permission for any of the integration accounts of the devlopers





















SDA Implementation Plan:
1. Team Level Storage Implementation
Database & Configuration
→ Create new database table kepler_team_storage_config_t for team storage settings
→ Add team storage configuration entity with fields: team_id, storage_provider, bucket_name, region, credentials
→ Implement team storage configuration repository for database operations
→ Create storage provider type enumeration (S3, GCS, Azure, CDS, MINIO)
→ Add database indexes for performance optimization on team_id lookups
→ Create database migration scripts for new table creation
Team Context Management
→ Implement team storage context class to load team-specific configurations
→ Add team storage context factory for creating contexts from team_id
→ Create team storage configuration service for CRUD operations
→ Add team storage context caching for performance
→ Implement team storage context validation and error handling
2. Fallback Storage Implementation
Fallback Service Core
→ Create fallback storage service with search strategy logic
→ Implement data search mechanism: client storage first, then Appen fallback
→ Add fallback storage result tracking (CLIENT/FALLBACK/NONE sources)
→ Create fallback storage error handling and logging
→ Implement fallback storage metrics and monitoring
Search Strategy
→ Implement primary search in client storage using team configuration
→ Add secondary search in Appen fallback storage (existing CDS)
→ Create search result object with source tracking and error messages
→ Add search operation caching to improve performance
→ Implement search timeout mechanisms to prevent hanging requests
Presigned URL Generation
→ Create presigned URL generation for uploads (always client storage)
→ Implement presigned URL generation for downloads (with fallback search)
→ Add presigned URL validation and security checks
→ Create presigned URL caching for frequently accessed files
→ Implement presigned URL expiration and refresh logic
3. Client Storage Provider Implementation
Storage Provider Abstraction
→ Create storage service interface with common operations
→ Implement storage provider factory pattern for provider selection
→ Add storage provider configuration validation
→ Create storage provider error handling and retry logic
→ Implement storage provider health checks and monitoring
S3 Storage Provider
→ Implement S3 storage service with AWS SDK integration
→ Add S3 presigned URL generation for uploads and downloads
→ Create S3 multipart upload support for large files
→ Implement S3 error handling and AWS-specific error mapping
→ Add S3 configuration validation (bucket, region, credentials)
Google Cloud Storage Provider
→ Implement GCS storage service with Google Cloud SDK
→ Add GCS presigned URL generation for uploads and downloads
→ Create GCS multipart upload support for large files
→ Implement GCS error handling and Google-specific error mapping
→ Add GCS configuration validation (project, bucket, service account)
Azure Storage Provider
→ Implement Azure storage service with Azure SDK
→ Add Azure presigned URL generation for uploads and downloads
→ Create Azure multipart upload support for large files
→ Implement Azure error handling and Azure-specific error mapping
→ Add Azure configuration validation (account, container, credentials)
4. Data Search Implementation
Search Flow Logic
→ Implement search flow: team context loading → client storage search → fallback search → result processing
→ Add search operation logging for debugging and monitoring
→ Create search result caching to reduce repeated searches
→ Implement search operation timeout and cancellation
→ Add search operation metrics and performance tracking
Client Storage Search
→ Implement client storage search using team-specific configuration
→ Add client storage search error handling and fallback triggers
→ Create client storage search result validation
→ Implement client storage search performance optimization
→ Add client storage search monitoring and alerting
Appen Fallback Search
→ Implement Appen fallback search using existing CDS service
→ Add fallback search error handling and graceful degradation
→ Create fallback search result validation and processing
→ Implement fallback search performance monitoring
→ Add fallback search usage tracking and analytics
Search Result Processing
→ Create search result object with found status, data, source, and errors
→ Implement search result validation and data integrity checks
→ Add search result logging and audit trail
→ Create search result caching for performance optimization
→ Implement search result error reporting and user communication
5. Required Validations 
Team Configuration Validation
→ Validate team_id format and existence in system
→ Validate storage provider type and supported providers
→ Validate bucket name format and accessibility
→ Validate region settings for each storage provider
→ Validate credentials format and authentication
Storage Provider Validation
→ Validate S3 credentials and bucket access permissions
→ Validate GCS service account and project permissions
→ Validate Azure storage account and container permissions
→ Validate CDS service availability and configuration
→ Validate storage provider connectivity and health
Data Access Validation
→ Validate team access permissions for data operations
→ Validate file path format and security
→ Validate presigned URL generation and expiration
→ Validate multipart upload parameters and limits
→ Validate data integrity and checksum verification
Security Validations
→ Validate team isolation and data access boundaries
→ Validate storage credentials encryption and storage
→ Validate presigned URL security and expiration
→ Validate file upload size limits and type restrictions
→ Validate audit logging and access tracking
Performance Validations
→ Validate search operation timeout limits
→ Validate caching configuration and expiration
→ Validate connection pooling and resource limits
→ Validate error handling and retry mechanisms
→ Validate monitoring and alerting thresholds
Error Handling Validations
→ Validate error message clarity and user communication
→ Validate error logging and debugging information
→ Validate fallback mechanism triggers and behavior
→ Validate error recovery and retry logic
→ Validate error monitoring and alerting
6. Integration Points
File Upload Service Integration
→ Update file upload service to use team storage context
→ Modify upload path generation for team-specific storage
→ Add team storage validation in upload process
→ Update multipart upload handling for new storage providers
→ Add upload monitoring and error handling
File Download Service Integration
→ Update file download service to use fallback search
→ Modify download URL generation with fallback logic
→ Add download source tracking and logging
→ Update download error handling and user communication
→ Add download performance monitoring
Controller Integration
→ Update project file controller for team storage support
→ Add team storage configuration management endpoints
→ Update public API controllers for new storage approach
→ Add team context validation in all endpoints
→ Update authorization checks for team-level storage
CDS Service Integration
→ Modify CDS service to work with fallback mechanism
→ Update CDS method signatures for search integration
→ Add backward compatibility for existing CDS usage
→ Update CDS error handling and logging
→ Add CDS performance monitoring and optimization




email = 'integration+edwardz1@figure-eight.com'; user = Builder::User.find_by(email: email); if user; role = Akon::Role.find_or_create_by(user_id: user.id, name: 'finance_manager'); puts "✅ SUCCESS! #{user.email} can now create teams!"; else; puts "❌ User not found. Checking database..."; puts "Total users: #{Builder::User.count}"; end






By default, we are giving team members DEFAULT_RW (read-write) access to their SDA storage. This allows them to:
✅ Read data from their SDA (download files, access results)
✅ Write data to their SDA (upload files, store results)
✅ Manage their team's data storage
This is the most practical default since teams need both read and write capabilities for their normal operations, while still maintaining security through team-scoped access control.