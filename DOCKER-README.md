# Docker Setup for Task List Application

This repository contains Docker configuration for running the Task List Application with both frontend and backend components.

## Components

1. **Backend**: Spring Boot Java application
2. **Frontend**: Vue.js web application
3. **Database**: PostgreSQL database

## Prerequisites

- Docker and Docker Compose installed on your system
- Git for cloning the repository

## Running the Application

### 1. Clone the Repository

```bash
git clone <repository-url>
cd paf2024
```

### 2. Start the Docker Containers

```bash
docker-compose up -d
```

This command:
- Builds the Docker images for both frontend and backend
- Creates and starts the PostgreSQL database
- Connects all components together

### 3. Access the Application

- Frontend: http://localhost
- Backend API Documentation: http://localhost:8080/api/swagger-ui.html
- WebSocket Test Page: http://localhost:8080/api/notifications-test.html

### 4. Stop the Application

```bash
docker-compose down
```

To completely remove all data including the database volume:

```bash
docker-compose down -v
```

## Environment Configuration

The application is configured using environment variables in the docker-compose.yml file:

- Backend connects to PostgreSQL database using the following configuration:
  - Database: tasklist
  - Username: paf2024
  - Password: paf2024

- Frontend connects to the backend using the API_URL environment variable
  - In the Docker environment, this is set to http://backend:8080/api

## Customization

You can customize the configuration by editing the docker-compose.yml file:

- To change the PostgreSQL credentials, update the environment variables in both postgres and backend services
- To expose the services on different ports, modify the port mappings

## Troubleshooting

- **Database Connection Issues**: Check that the postgres container is running and healthy
- **Frontend can't connect to Backend**: Ensure the API_URL is correctly set
- **Container Startup Failures**: View logs using `docker-compose logs [service-name]`