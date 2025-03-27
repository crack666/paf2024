# Frontend Integration

This directory contains the Vue.js frontend for the PAF2024 project, which was originally maintained in a separate Git repository. It has been integrated into the main repository to simplify development, deployment, and GitHub presentation.

## Integration Notes

- The frontend code is maintained directly within this directory as part of the main repository
- Docker builds are configured to work with this directory structure via the docker-compose.yml file
- All frontend dependencies are managed within this directory's package.json

## Development Setup

To set up the frontend for development:

```bash
# Navigate to the frontend directory
cd PatternsAndFrameworks_Frontend

# Install dependencies
npm install

# Start the development server
npm run dev
```

## Building for Production

The frontend is automatically built as part of the Docker setup. To manually build:

```bash
# Navigate to the frontend directory
cd PatternsAndFrameworks_Frontend

# Build for production
npm run build
```

## Original Repository

The frontend was originally maintained in a separate repository. This integration preserves all code history while simplifying the overall project structure.