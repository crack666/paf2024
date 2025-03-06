# Changelog

## Version 0.4.0 - 2025-03-06

### Added
1. **Real-time WebSocket Notifications**
   - Implemented WebSocket support using STOMP protocol
   - Created dedicated `NotificationController` for REST endpoints
   - Added `NotificationWebSocketController` for WebSocket communication
   - Created HTML test page for notification system demonstration
   - Added message brokers for topic-based and user-specific notifications

2. **Enhanced Notification System**
   - Added notification types (TASK_STARTED, TASK_COMPLETED, TASK_ERROR, etc.)
   - Integrated task lifecycle events with automatic notifications
   - Implemented user-specific notification channels
   - Added broadcast capability for system-wide announcements
   - Created dedicated REST endpoints for notification management

3. **WebSocket Testing Interface**
   - Added interactive WebSocket testing page (`notifications-test.html`)
   - Implemented real-time notification display with styling by urgency
   - Added controls for connecting, sending, and marking notifications as read
   - Created visual indicators for notification status and type

### Changed
1. **Notification Model**
   - Enhanced `Notification` with type field for event categorization
   - Updated notification creation methods to support new fields
   - Improved notification filtering capabilities

2. **Task Processing Service**
   - Integrated notification events at key task lifecycle points:
     - Task start notification
     - Task completion notification
     - Task error notification

### Technical Improvements
1. **WebSocket Configuration**
   - Added STOMP message broker configuration
   - Implemented SockJS fallback for browser compatibility
   - Created secure communication channels for user-specific data

2. **New Dependencies**
   - Added Spring WebSocket starter
   - Included SockJS client for browser fallback support
   - Added STOMP WebSocket messaging protocol

## Version 0.3.0 - 2025-03-06

### Added
1. **Enhanced Task Progress Tracking**
   - Added real-time progress monitoring for long-running tasks
   - Implemented `TaskProgressDTO` for progress information
   - Added progress percentage and estimated completion time
   - Created endpoint to monitor task progress (`GET /api/tasks/{id}/progress`)

2. **User Management System**
   - Added `User` entity with associated tasks and notifications
   - Implemented `UserRepository` for user data storage
   - Created `UserService` for user operations
   - Added `UserDTO` for API communication
   - Created `UserController` with REST endpoints for user management

3. **Improved Notification Management**
   - Enhanced `Notification` model with urgency levels and related task references
   - Added `NotificationRepository` for notification storage
   - Extended `NotificationService` with filtering capabilities
   - Created endpoints for notification management in `UserController`
   - Added support for marking notifications as read

4. **Task Queue Management**
   - Created `TaskQueueController` for queue operations
   - Added endpoints for queue creation and management
   - Implemented filtering for queue tasks by status
   - Added support for processing tasks individually or in batches
   - Implemented queue reordering capabilities

5. **Enhanced CalculatePiTask**
   - Added progress tracking with percentage complete
   - Implemented `ProgressData` class to track execution state
   - Added ability to monitor intermediate calculation results
   - Improved thread management for interruptible execution

### Changed
1. **Renamed TaskExecutor to TaskProcessorService**
   - Resolved conflicts with Spring's default task executor
   - Updated dependencies and references throughout the code

2. **Updated TaskResult Class**
   - Added support for both legacy and new properties
   - Improved property naming for better understanding of purpose
   - Enhanced initialization logic with atomic ID generation

3. **Enhanced Error Handling**
   - Added detailed error responses for common failure scenarios
   - Improved validation of input parameters

### Technical Improvements
1. **Thread Management**
   - Updated thread creation to use virtual threads (Java 21 feature)
   - Improved thread pool configuration and management
   - Enhanced thread termination for better resource cleanup

2. **API Organization**
   - Grouped endpoints by functional area (tasks, users, queues)
   - Improved API documentation with detailed operation descriptions
   - Added comprehensive parameter descriptions

## Version 0.2.0 - 2025-02-15

### Added
1. **Task Dependency Management**
   - Added support for task dependencies
   - Implemented deadlock detection algorithm
   - Created endpoints for adding and removing dependencies

2. **Task Status Tracking**
   - Implemented state transitions for tasks (CREATED → QUEUED → RUNNING → DONE)
   - Added status filtering for task queries

3. **Initial Task Queue Implementation**
   - Created basic queue structure for organizing tasks
   - Added FIFO processing capabilities

## Version 0.1.0 - 2025-01-20

### Added
1. **Core Task Management**
   - Implemented basic task operations (create, read, update, complete)
   - Added due date and assignee tracking

2. **Concurrent Execution Framework**
   - Created thread pool for parallel task execution
   - Implemented asynchronous task processing

3. **Initial API**
   - Set up REST endpoints for core operations
   - Added Swagger documentation