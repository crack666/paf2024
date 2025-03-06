# Task List Application

A Domain-Driven Design (DDD) implementation of a task management application for the PAF2024 module.

## Project Overview

This application demonstrates the implementation of Domain-Driven Design patterns and concurrency concepts:

- **Task Management:** Create, update, complete, and track tasks
- **Dependency Management:** Add dependencies between tasks with deadlock detection
- **Task Queueing and Processing:** Queue and process tasks in parallel using separate threads
- **Concurrent Task Execution:** Execute multiple tasks simultaneously with configurable thread pool
- **Asynchronous Processing:** Support for both synchronous and asynchronous task execution
- **Progress Tracking:** Monitor real-time progress of long-running tasks
- **User Management:** Create, update, and manage users with their assigned tasks
- **Real-time Notifications:** Receive and manage real-time notifications for task events via WebSocket

## Design Patterns Used

- **Repository Pattern:** For data access abstraction (TaskRepository)
- **Factory Pattern:** For creating task instances dynamically
- **Strategy Pattern:** For different task implementations (CalculatePiTask, GenerateReportTask)
- **Command Pattern:** For encapsulating task execution and queueing
- **Observer Pattern:** For real-time notifications about task events via WebSocket
- **Dependency Injection:** Spring-based constructor injection throughout the application
- **Singleton Pattern:** Spring services as singletons (TaskExecutor, TaskRegistry)
- **Facade Pattern:** In TaskManagerService to coordinate different services
- **DTO Pattern:** For data transfer between layers (TaskDTO, TaskResultDTO)
- **State Pattern:** Task status transitions (CREATED → QUEUED → RUNNING → DONE)
- **Thread Pool Pattern:** For managing concurrent task execution with configurable threads

## Technical Stack

- Java 21
- Spring Boot 3.1.5
- PostgreSQL Database 
- Swagger UI for API documentation
- Maven for dependency management
- WebSocket with STOMP for real-time communication
- Java Concurrency Utilities
  - ThreadPoolExecutor for managed thread pool
  - CompletableFuture for asynchronous task processing
  - BlockingQueue for thread coordination
  - Atomic variables for thread-safe operations

## Running the Application

### Prerequisites

1. **Java Development Kit (JDK) 21**
2. **PostgreSQL Database**
3. **IntelliJ IDEA** (recommended) or another Java IDE
4. **Maven**

### PostgreSQL Setup

Before running the application, you need to set up the PostgreSQL database:

1. **Install PostgreSQL** if not already installed

2. **Create the database**:
   ```sql
   CREATE DATABASE tasklist;
   ```

3. **Create a user with appropriate permissions**:
   ```sql
   CREATE USER paf2024 WITH PASSWORD 'paf2024';
   GRANT ALL PRIVILEGES ON DATABASE tasklist TO paf2024;
   ```

### IntelliJ IDEA Setup

1. **Import the Project:**
   - Open IntelliJ IDEA
   - Select "Open" or "Import Project"
   - Navigate to the project directory and select it
   - Choose "Import as Maven project" when prompted

2. **Configure Java SDK:**
   - Go to File > Project Structure > Project
   - Set the Project SDK to Java 21
   - Set Project language level to "21 - Records, patterns, etc."

3. **Set up the Run Configuration:**
   - Click on "Add Configuration" button in the top-right toolbar
   - Click the "+" button and select "Spring Boot"
   - Set the following configuration:
     - Name: `TaskListApplication`
     - Main class: `de.vfh.paf.tasklist.TaskListApplication`
     - Working directory: `$MODULE_WORKING_DIR$`
     - Use classpath of module: `paf2024`
     - JRE: Java 21

4. **Maven Configuration:**
   - Open the Maven toolbar (usually on the right side)
   - Right-click on the project and select "Reload Project"
   - Expand "Lifecycle" and run "clean" followed by "install"

5. **Run the Application:**
   - Select the "TaskListApplication" configuration
   - Click the "Run" button

### Running via Command Line

Alternatively, you can run the application using Maven:

```bash
mvn clean install
mvn spring-boot:run
```

## Accessing the Application

- **Swagger UI**: `http://localhost:8080/api/swagger-ui.html`
- **API Documentation**: `http://localhost:8080/api/api-docs`
- **WebSocket Notifications Test**: `http://localhost:8080/api/notifications-test.html`

## API Endpoints

The application provides the following main endpoints:

### Task Management
- `GET /api/tasks` - List all tasks (with optional filtering by user ID and status)
- `GET /api/tasks/{id}` - Get a specific task
- `GET /api/tasks/status/{status}` - Get all tasks with a specific status
- `GET /api/tasks/{id}/progress` - Get real-time progress information for a running task
- `POST /api/tasks` - Create a new task (regular or runnable)
- `PUT /api/tasks/{id}` - Update a task
- `POST /api/tasks/{id}/complete` - Mark a task as complete
- `POST /api/tasks/{id}/dependencies/{dependencyId}` - Add a dependency to a task
- `POST /api/tasks/{id}/execute` - Execute a task (synchronously or asynchronously)
- `GET /api/tasks/types` - List all available task types
- `GET /api/tasks/thread-pool-stats` - Get statistics about the task executor thread pool

### User Management
- `GET /api/users` - List all users
- `GET /api/users/{id}` - Get a specific user
- `GET /api/users/{id}/tasks` - Get all tasks assigned to a user
- `GET /api/users/{id}/notifications` - Get all notifications for a user (with optional filtering by read status)
- `POST /api/users` - Create a new user
- `PUT /api/users/{id}` - Update a user
- `POST /api/users/{id}/notifications/{notificationId}/read` - Mark a notification as read

### Notification Management
- `GET /api/notifications` - List all notifications
- `GET /api/notifications/{id}` - Get a specific notification
- `GET /api/notifications/user/{userId}` - Get notifications for a user (with optional read status filter)
- `POST /api/notifications/{id}/read` - Mark a notification as read
- `POST /api/notifications/send` - Send a notification to a specific user
- `POST /api/notifications/broadcast` - Broadcast a notification to all users

### Task Queue Management
- `GET /api/task-queues` - List all task queues
- `GET /api/task-queues/{id}` - Get a specific task queue
- `GET /api/task-queues/{id}/tasks` - Get all tasks in a queue (with optional filtering by status)
- `POST /api/task-queues` - Create a new task queue
- `POST /api/task-queues/{queueId}/tasks/{taskId}` - Add a task to a queue
- `POST /api/task-queues/{queueId}/process-next` - Process the next task in a queue
- `POST /api/task-queues/{queueId}/process-all` - Process all tasks in a queue
- `POST /api/task-queues/{queueId}/reorder` - Reorder tasks in a queue based on criteria

## WebSocket Notifications

The application provides real-time notifications via WebSocket:

1. **WebSocket Connection:**
   - Endpoint: `/api/ws`
   - Protocol: STOMP over SockJS
   - Topic subscriptions for receiving notifications:
     - `/topic/notifications` - Broadcast notifications
     - `/topic/system` - System-wide notifications
     - `/user/queue/notifications` - User-specific notifications

2. **Notification Types:**
   - `TASK_CREATED` - When a new task is created
   - `TASK_STARTED` - When a task begins execution
   - `TASK_COMPLETED` - When a task finishes successfully
   - `TASK_ERROR` - When a task fails during execution
   - `TASK_OVERDUE` - When a task passes its due date

3. **Interactive Testing:**
   - Test page: `/api/notifications-test.html`
   - Features:
     - Connect to WebSocket with user ID
     - Send notifications to specific users
     - Broadcast system-wide notifications
     - Mark notifications as read
     - View notifications by urgency level

## Concurrent Task Execution

The application features a robust system for executing tasks concurrently:

1. **Configurable Thread Pool:**
   - The thread pool size can be configured in `application.yml` via `tasklist.concurrent.thread-pool-size`
   - Task queue size is configurable via `tasklist.concurrent.max-queue-size`

2. **Task Execution Models:**
   - **Synchronous Execution:** Wait for task completion and get results (`/tasks/{id}/execute?wait=true`)
   - **Asynchronous Execution:** Start task in background and return immediately (`/tasks/{id}/execute?wait=false`)

3. **Custom Task Implementations:**
   - Implement the `RunnableTask` interface to create new task types
   - Each task runs in its own thread from the thread pool
   - Task results are stored with the task entity
   - Some tasks support real-time progress monitoring (e.g., `CalculatePiTask`)

4. **Thread Pool Monitoring:**
   - Monitor thread pool usage via `/tasks/thread-pool-stats` endpoint
   - View active threads, queue size, and completed task count

5. **Scheduled Task Processing:**
   - Background scheduler checks for ready tasks every minute
   - Tasks with completed dependencies are automatically executed

6. **Thread Safety:**
   - Thread-safe task status updates
   - Safe shutdown with proper thread termination

## Troubleshooting

If you encounter any issues:

1. **PostgreSQL Connection Issues:**
   - Verify PostgreSQL is running
   - Check that the database `tasklist` exists
   - Ensure user `paf2024` has appropriate privileges
   - Try connecting with a PostgreSQL client to verify credentials

2. **Dependency Problems:**
   - Open the Maven toolbar
   - Run "clean" and "install" again

3. **Run Configuration Issues:**
   - Verify main class is `de.vfh.paf.tasklist.TaskListApplication`
   - Check console logs for any startup errors

4. **Thread Pool Issues:**
   - Check thread pool statistics via the API
   - Adjust thread pool size in configuration if tasks are queuing too much
   - Look for thread-related errors in the logs

5. **Application Context Path:**
   - Remember all endpoints are prefixed with `/api`
   - Swagger UI is at `/api/swagger-ui.html`

## Project Structure

The application follows a clean DDD architecture:

- **Domain Layer**: Core business logic (models, repositories, services)
  - `/tasklist/domain/model/` - Domain entities and interfaces
    - `Task.java` - Core entity with dependencies and execution information
    - `User.java` - User entity with associated tasks and notifications
    - `Notification.java` - Notification entity with urgency and read status
    - `RunnableTask.java` - Interface for executable tasks
    - `TaskResult.java` - Result of task execution
    - `TaskQueue.java` - Queue for organizing and processing tasks
  - `/tasklist/domain/repository/` - Repository interfaces
    - `TaskRepository.java` - Repository for tasks
    - `UserRepository.java` - Repository for users
    - `NotificationRepository.java` - Repository for notifications
  - `/tasklist/domain/service/` - Domain services
    - `TaskService.java` - Task management operations
    - `UserService.java` - User management operations
    - `TaskProcessorService.java` - Concurrent task execution service with thread pool
    - `TaskQueueService.java` - Management of task queues and execution
    - `NotificationService.java` - Notification management and delivery
    - `TaskRegistry.java` - Registry of available task implementations
  - `/tasklist/domain/tasks/` - Concrete task implementations
    - `CalculatePiTask.java` - Task that calculates Pi with progress tracking
    - `GenerateReportTask.java` - Task that generates reports

- **Application Layer**: Coordination (services, DTOs)
  - `/tasklist/application/dto/` - Data transfer objects
    - `TaskDTO.java` - Task data with execution details
    - `UserDTO.java` - User data with associated tasks and notifications
    - `NotificationDTO.java` - Notification data with urgency and status
    - `NotificationPayload.java` - Simplified notification data for WebSocket transmission
    - `TaskResultDTO.java` - Task result information
    - `TaskProgressDTO.java` - Real-time progress information for running tasks
    - `TaskQueueDTO.java` - Queue information with contained tasks
    - `TaskTypeDTO.java` - Information about available task types
  - `/tasklist/application/service/` - Application services
    - `TaskManagerService.java` - Coordination facade for task operations

- **Infrastructure Layer**: Technical capabilities
  - `/tasklist/infrastructure/persistence/` - Repository implementations
    - `TaskRepositoryInMemory.java` - In-memory implementation of task repository
  - `/tasklist/infrastructure/config/` - Configuration classes
    - `TaskListConfig.java` - Service and repository bean configurations
    - `OpenApiConfig.java` - Swagger configuration for API documentation
    - `WebSocketConfig.java` - WebSocket and STOMP configuration

- **Presentation Layer**: User interface
  - `/tasklist/presentation/rest/` - REST controllers
    - `TaskController.java` - REST endpoints for task management
    - `UserController.java` - REST endpoints for user management
    - `TaskQueueController.java` - REST endpoints for queue management
    - `NotificationController.java` - REST endpoints for notification management
  - `/tasklist/presentation/websocket/` - WebSocket controllers
    - `NotificationWebSocketController.java` - Real-time notification handling

## Frontend Integration Guide

This section describes how a frontend application should interact with the backend API to implement typical workflows.

### Authentication Flow (Simplified)

1. **User Login**
   - When a user logs in, obtain their user ID for subsequent API calls
   - The system doesn't yet implement authentication, so frontend applications should maintain user context

### Task Management Workflow

1. **User Dashboard Initialization**
   - `GET /api/users/{userId}` - Get user details
   - `GET /api/users/{userId}/tasks` - Get tasks assigned to the user
   - `GET /api/users/{userId}/notifications?read=false` - Get unread notifications

2. **Creating and Managing Tasks**
   - `GET /api/tasks/types` - Get available task types for task creation form
   - `POST /api/tasks` - Create a new task with appropriate task type
   - `GET /api/tasks/{taskId}` - Get task details when viewing a specific task
   - `PUT /api/tasks/{taskId}` - Update task properties
   - `POST /api/tasks/{taskId}/dependencies/{dependencyId}` - Add dependencies between tasks

3. **Task Queue Management**
   - `GET /api/task-queues` - List available task queues
   - `POST /api/task-queues` - Create a new task queue if needed
   - `POST /api/task-queues/{queueId}/tasks/{taskId}` - Add task to a queue
   - `POST /api/task-queues/{queueId}/process-all` - Process all tasks in a queue

4. **Task Execution**
   - `POST /api/tasks/{taskId}/execute?wait=false` - Execute task asynchronously
   - For monitoring progress of long-running tasks:
     - `GET /api/tasks/{taskId}/progress` - Poll for task progress (every 1-2 seconds)
   - Once task completes:
     - `GET /api/tasks/{taskId}` - Get final task results

### Real-time Notifications Integration

1. **WebSocket Connection Setup**
   - Connect to WebSocket endpoint `/api/ws` using STOMP over SockJS
   - Subscribe to topics:
     - `/topic/notifications` - For system-wide notifications
     - `/user/queue/notifications` - For user-specific notifications
     - `/topic/system` - For system events

2. **Notification Handling**
   - On receiving a notification via WebSocket:
     - Display notification to user based on urgency level
     - Store in client-side notification list
   - Mark notifications as read:
     - `POST /api/notifications/{notificationId}/read` with userId in request body
     - Or via WebSocket: Send message to `/app/notifications.markRead` with notificationId and userId

3. **Sending Notifications**
   - `POST /api/notifications/send` - Send notification to specific user
   - `POST /api/notifications/broadcast` - Broadcast system-wide notification

### Typical Task Lifecycle

1. **Create Task**
   - `POST /api/tasks` with payload containing:
     ```json
     {
       "title": "Sample Task",
       "description": "Task description",
       "dueDate": "2025-04-01T12:00:00",
       "assignedUserId": 1,
       "taskType": "CalculatePiTask",
       "typeParams": { "iterations": 1000 }
     }
     ```

2. **Add Dependencies (if needed)**
   - `POST /api/tasks/{taskId}/dependencies/{dependencyId}` for each dependency

3. **Execute Task**
   - `POST /api/tasks/{taskId}/execute?wait=false` to run asynchronously

4. **Monitor Progress**
   - Poll `GET /api/tasks/{taskId}/progress` until completion
   - Or listen for WebSocket notifications about task status changes

5. **View Results**
   - `GET /api/tasks/{taskId}` to view final task results

### Error Handling

Frontend applications should handle these common error scenarios:

1. **404 Not Found**: Resource doesn't exist
2. **400 Bad Request**: Invalid parameters or payload
3. **409 Conflict**: Task dependency issues or deadlock detection
4. **500 Internal Server Error**: Unexpected backend errors

### Performance Considerations

1. **Polling Strategy**
   - Use WebSocket for immediate notifications when possible
   - For task progress, poll at reasonable intervals (1-2 seconds)
   - Consider exponential backoff for long-running tasks

2. **Batch Operations**
   - When displaying many tasks, use pagination parameters
   - Consider using `GET /api/tasks?userId={userId}&status=RUNNING` for filtered queries

3. **Optimistic UI Updates**
   - Update UI immediately after operations like task creation or status change
   - Confirm with backend response or handle errors and revert if needed