# Task List Application

A Domain-Driven Design (DDD) implementation of a task management application for the PAF2024 module.

## Project Overview

This application demonstrates the implementation of Domain-Driven Design patterns and concurrency concepts. The project consists of a Java Spring Boot backend and a Vue.js frontend, integrated into a single repository:

- **Task Management:** Create, update, complete, and track tasks
- **Dependency Management:** Add dependencies between tasks with deadlock detection
- **Task Queueing and Processing:** Queue and process tasks in parallel using separate threads
- **Concurrent Task Execution:** Execute multiple tasks simultaneously with configurable thread pool
- **Asynchronous Processing:** Support for both synchronous and asynchronous task execution
- **Progress Tracking:** Monitor real-time progress of long-running tasks with visual indicators
- **User Management:** Create, update, and manage users with their assigned tasks
- **Real-time Notifications:** Receive and manage real-time notifications for task events via WebSocket
- **Task Status Visualization:** View task taskStatus with intuitive icons and real-time updates
- **Task Queue Management UI:** Filter and view tasks by taskStatus with automatic data refreshing

## Design Patterns Used

| Pattern | Implementation | Description | Native/Custom | More Info |
|---------|---------------|-------------|---------------|-----------|
| **Repository Pattern** | [TaskRepository](src/main/java/de/vfh/paf/tasklist/domain/repository/TaskRepository.java), [NotificationRepository](src/main/java/de/vfh/paf/tasklist/domain/repository/NotificationRepository.java) | Data access abstraction for domain objects | Custom implementation, inspired by Spring Data pattern | [Wikipedia](https://en.wikipedia.org/wiki/Repository_pattern) |
| **Factory Pattern** | [TaskFactory](src/main/java/de/vfh/paf/tasklist/domain/service/TaskFactory.java) | Creates task instances dynamically based on class name | Custom implementation | [Wikipedia](https://en.wikipedia.org/wiki/Factory_method_pattern) |
| **Strategy Pattern** | [RunnableTask](src/main/java/de/vfh/paf/tasklist/domain/model/RunnableTask.java), [CalculatePiTask](src/main/java/de/vfh/paf/tasklist/domain/tasks/CalculatePiTask.java), [GenerateReportTask](src/main/java/de/vfh/paf/tasklist/domain/tasks/GenerateReportTask.java) | Different task implementations with common interface | Custom implementation | [Wikipedia](https://en.wikipedia.org/wiki/Strategy_pattern) |
| **Command Pattern** | [TaskQueueService](src/main/java/de/vfh/paf/tasklist/domain/service/TaskQueueService.java) | Encapsulates task execution and queueing | Custom implementation | [Wikipedia](https://en.wikipedia.org/wiki/Command_pattern) |
| **Observer Pattern** | [NotificationWebSocketController](src/main/java/de/vfh/paf/tasklist/presentation/websocket/NotificationWebSocketController.java) | Real-time notifications via WebSocket | Uses Spring's WebSocket support | [Wikipedia](https://en.wikipedia.org/wiki/Observer_pattern) |
| **Dependency Injection** | Throughout application (constructor injection) | Inversion of control for loosely coupled components | Native Spring feature | [Wikipedia](https://en.wikipedia.org/wiki/Dependency_injection) |
| **Singleton Pattern** | All `@Service` classes | Services instantiated once by Spring container | Native Spring feature through `@Service` annotation | [Wikipedia](https://en.wikipedia.org/wiki/Singleton_pattern) |
| **Facade Pattern** | [TaskManagerService](src/main/java/de/vfh/paf/tasklist/application/service/TaskManagerService.java) | Coordinates different services with simplified interface | Custom implementation | [Wikipedia](https://en.wikipedia.org/wiki/Facade_pattern) |
| **DTO Pattern** | [TaskDTO](src/main/java/de/vfh/paf/tasklist/application/dto/TaskDTO.java), [NotificationDTO](src/main/java/de/vfh/paf/tasklist/application/dto/NotificationDTO.java) | Data transfer objects between layers | Custom implementation | [Wikipedia](https://en.wikipedia.org/wiki/Data_transfer_object) |
| **State Pattern** | [Task.java](src/main/java/de/vfh/paf/tasklist/domain/model/Task.java) with [Status enum](src/main/java/de/vfh/paf/tasklist/domain/model/Status.java), [Notification.java](src/main/java/de/vfh/paf/tasklist/domain/model/Notification.java) with [NotificationStatus enum](src/main/java/de/vfh/paf/tasklist/domain/model/NotificationStatus.java) | Task taskStatus transitions (CREATED → QUEUED → RUNNING → DONE) and Notification lifecycle (CREATED → SENT → DELIVERED → READ → ARCHIVED) | Custom implementation | [Wikipedia](https://en.wikipedia.org/wiki/State_pattern) |
| **Thread Pool Pattern** | [TaskProcessorService](src/main/java/de/vfh/paf/tasklist/domain/service/TaskProcessorService.java) | Managing concurrent task execution with configurable threads | Uses Java's ExecutorService | [Wikipedia](https://en.wikipedia.org/wiki/Thread_pool) |

## Technical Stack

### Backend
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

### Frontend
- Vue 3 (Composition API)
- Vite as build tool
- Pinia for state management
- Vue Router for navigation
- SockJS and StompJS for WebSocket communication
- Element Plus for UI components

## Running the Application

You can run the application using either direct local setup or Docker, depending on your preference.

### Option 1: Local Setup

#### Prerequisites

1. **Java Development Kit (JDK) 21**
2. **PostgreSQL Database**
3. **IntelliJ IDEA** (recommended) or another Java IDE
4. **Maven**
5. **Node.js** and **npm** (for frontend development)

#### PostgreSQL Setup

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

#### Backend Setup with IntelliJ IDEA

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

5. **Run the Backend Application:**
   - Select the "TaskListApplication" configuration
   - Click the "Run" button

#### Running the Backend via Command Line

Alternatively, you can run the backend application using Maven:

```bash
mvn clean install
mvn spring-boot:run
```

#### Setting up the Frontend

To set up and run the frontend application:

1. **Navigate to the frontend directory**:
   ```bash
   cd PatternsAndFrameworks_Frontend
   ```

2. **Install dependencies**:
   ```bash
   npm install
   ```

3. **Run the development server**:
   ```bash
   npm run dev
   ```

The frontend will be available at `http://localhost:5173` (or another port if 5173 is in use).

### Option 2: Docker Setup

This project includes Docker configuration for easy deployment of the entire application stack, including the backend, frontend, and PostgreSQL database.

#### Prerequisites

- **Docker** and **Docker Compose** installed on your system

#### Running with Docker Compose

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd paf2024
   ```

2. **Start the Docker containers**:
   ```bash
   docker-compose up -d
   ```

   This command:
   - Builds the Docker images for both frontend and backend
   - Creates and starts the PostgreSQL database
   - Connects all components together

3. **Access the application**:
   - Frontend: http://localhost
   - Backend API Documentation: http://localhost:8080/api/swagger-ui.html
   - WebSocket Test Page: http://localhost:8080/api/notifications-test.html

4. **Stop the application**:
   ```bash
   docker-compose down
   ```

   To completely remove all data including the database volume:
   ```bash
   docker-compose down -v
   ```

#### Docker Configuration Details

The application is configured using environment variables in the docker-compose.yml file:

- Backend connects to PostgreSQL database using the following configuration:
  - Database: tasklist
  - Username: paf2024
  - Password: paf2024

- Frontend connects to the backend using the API_URL environment variable
  - In the Docker environment, this is set to http://localhost:8080/api

#### Customizing Docker Setup

You can customize the configuration by editing the docker-compose.yml file:

- To change the PostgreSQL credentials, update the environment variables in both postgres and backend services
- To expose the services on different ports, modify the port mappings

#### Troubleshooting Docker Setup

- **Database Connection Issues**: Check that the postgres container is running and healthy
- **Frontend can't connect to Backend**: Ensure the API_URL is correctly set
- **Container Startup Failures**: View logs using `docker-compose logs [service-name]`

## Accessing the Application

- **Swagger UI**: `http://localhost:8080/api/swagger-ui.html`
- **API Documentation**: `http://localhost:8080/api/api-docs`
- **WebSocket Notifications Test**: `http://localhost:8080/api/notifications-test.html`

## API Endpoints

The application provides the following main endpoints:

### Task Management
- `GET /api/tasks` - List all tasks (with optional filtering by user ID and taskStatus)
- `GET /api/tasks/{id}` - Get a specific task
- `GET /api/tasks/taskStatus/{taskStatus}` - Get all tasks with a specific taskStatus
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
- `GET /api/users/{id}/notifications` - Get all notifications for a user (with optional filtering by read taskStatus)
- `POST /api/users` - Create a new user
- `PUT /api/users/{id}` - Update a user
- `POST /api/users/{id}/notifications/{notificationId}/read` - Mark a notification as read

### Notification Management
- `GET /api/notifications` - List all notifications
- `GET /api/notifications/{id}` - Get a specific notification
- `GET /api/notifications/user/{userId}` - Get notifications for a user (with optional read taskStatus filter)
- `POST /api/notifications/{id}/read` - Mark a notification as read
- `POST /api/notifications/send` - Send a notification to a specific user
- `POST /api/notifications/broadcast` - Broadcast a notification to all users

### Task Queue Management
- `GET /api/task-queues` - List all task queues
- `GET /api/task-queues/{id}` - Get a specific task queue
- `GET /api/task-queues/{id}/tasks` - Get all tasks in a queue (with optional filtering by taskStatus)
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
   - Thread-safe task taskStatus updates
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

The application is structured with a clean DDD architecture and is organized into two main parts:

### Backend (`/src`)

- **Domain Layer**: Core business logic (models, repositories, services)
  - `/tasklist/domain/model/` - Domain entities and interfaces
    - `Task.java` - Core entity with dependencies and execution information
    - `User.java` - User entity with associated tasks and notifications
    - `Notification.java` - Notification entity with urgency and read taskStatus
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
    - `TaskFactory.java` - Registry of available task implementations
  - `/tasklist/domain/tasks/` - Concrete task implementations
    - `CalculatePiTask.java` - Task that calculates Pi with progress tracking
    - `GenerateReportTask.java` - Task that generates reports

- **Application Layer**: Coordination (services, DTOs)
  - `/tasklist/application/dto/` - Data transfer objects
    - `TaskDTO.java` - Task data with execution details
    - `UserDTO.java` - User data with associated tasks and notifications
    - `NotificationDTO.java` - Notification data with urgency and taskStatus
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

### Frontend (`/PatternsAndFrameworks_Frontend`)

- **Components**: Reusable UI components
  - `Header.vue` - Application header with navigation and connection status
  - `NotificationContainer.vue` - Real-time notification display
  - `QueueList.vue` - Task queue visualization
  - Various UI components for tasks, notifications, and forms

- **Pages**: Application views
  - `Dashboard.vue` - Main dashboard showing tasks and queues
  - `Tasks.vue` - Task management interface
  - `TaskQueues.vue` - Queue management and visualization
  - `Notifications.vue` - Notification management

- **Stores**: State management using Pinia
  - `auth.js` - User authentication and session management
  - `notification.js` - Notification state and operations
  - `queue.js` - Queue state and operations
  - `task.js` - Task state and operations

- **Services**: API and WebSocket communication
  - `api.js` - REST API communication with backend
  - `websocket.js` - Real-time communication via WebSocket

## Frontend Components

The frontend is built using Vue 3 with Pinia for state management, offering a modern and responsive user interface for interacting with the backend services.

### Key Frontend Components

1. **Dashboard View**
   - Central hub showing task summaries, queue taskStatus, and important notifications
   - Quick access to task execution and management functions
   - Real-time task taskStatus and notification updates

2. **Task Management**
   - Create, view, and manage tasks with different types and parameters
   - Visual task board showing tasks grouped by taskStatus
   - Task details view with execution results and dependencies

3. **Task Queue Management**
   - Tab-based filtering of tasks by taskStatus (All, Pending, Running, Completed)
   - Real-time progress indicators for running tasks
   - Direct execution controls for individual tasks
   - Automatic data refreshing for active queues

4. **Notification System**
   - Real-time notification display with urgency-based styling
   - Mark notifications as read directly from the interface
   - Filter notifications by taskStatus and type

5. **Reusable Components**
   - `QueueList`: Flexible component for displaying task queues in various contexts
   - Status indicators with intuitive icons and colors
   - Progress visualization for running tasks

### UI Features

1. **Adaptive Layouts**
   - Responsive design for various screen sizes
   - Compact views for dashboard integration
   - Detailed views for dedicated management pages

2. **Real-time Updates**
   - Automatic refreshing of data for running tasks
   - WebSocket integration for instant notifications
   - Visual taskStatus transitions for task state changes

3. **Contextual Actions**
   - Action buttons that adapt to task taskStatus
   - Tabbed interfaces for focused interaction
   - Direct navigation between related components

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
   - Or listen for WebSocket notifications about task taskStatus changes

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
   - Consider using `GET /api/tasks?userId={userId}&taskStatus=RUNNING` for filtered queries

3. **Optimistic UI Updates**
   - Update UI immediately after operations like task creation or taskStatus change
   - Confirm with backend response or handle errors and revert if needed

## Deadlock Detection and Resolution

The system includes built-in detection for circular dependencies (deadlocks) between tasks. This feature prevents tasks from becoming unprocessable due to circular wait conditions.

### Creating and Detecting a Deadlock through the UI

To demonstrate the deadlock detection feature:

1. **Create Multiple Tasks**:
   - Create at least two tasks (e.g., "Task A" and "Task B")
   - Navigate to the Tasks page and use the "Create Task" button

2. **Set Up Circular Dependencies**:
   - Select Task A and add Task B as a dependency
   - Then try to select Task B and add Task A as a dependency
   - This creates a circular dependency: Task A → Task B → Task A

3. **Expected Result**:
   - The system will detect the deadlock when you try to add the second dependency
   - A notification with HIGH urgency will appear with the message: "Deadlock detected: Adding this dependency would create a circular wait condition"
   - The dependency will not be added, preventing the deadlock

4. **More Complex Scenario**:
   - Create additional tasks (e.g., "Task C", "Task D", "Task E")
   - Create a dependency chain: Task A → Task B → Task C → Task D
   - Attempt to add a dependency from Task D to Task A
   - The system will detect this longer circular chain as a deadlock and prevent it

### Resolving Deadlocks

When attempting to create a circular dependency, the system automatically prevents it. However, if you need to resolve a complex dependency graph with potential deadlocks:

1. **Visualize Dependencies**:
   - Navigate to the task detail view to see both dependencies and dependent tasks

2. **Remove Problem Dependencies**:
   - Identify which dependency is causing the circular reference
   - Click the "Remove" button next to the problematic dependency
   - The system will allow this action since it resolves the deadlock

3. **Reorganize Task Structure**:
   - Consider refactoring tasks into a proper directed acyclic graph (DAG)
   - Create intermediate tasks that can break circular dependencies
   - Use the task queue feature to ensure proper execution order

### Automated Deadlock Detection

The system regularly checks for deadlocks as part of its background processing. If a deadlock is detected:

1. A system notification is sent to administrators
2. The affected tasks are flagged in the UI with a warning indicator
3. The task processing is temporarily suspended for the affected task chain

To resume processing:
1. Navigate to the task detail view of any affected task
2. Review the dependency graph and remove the circular reference
3. Once resolved, the task processing will automatically resume