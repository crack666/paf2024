# Stage 1: Build the Vue.js application
FROM node:18 as build-stage
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .

# Create a public directory if it doesn't exist
RUN mkdir -p /app/public

# Copy env.js template
RUN echo 'window.VUE_APP_API_URL = "${API_URL}";' > /app/public/env.js.template

RUN npm run build

# Stage 2: Setup Nginx to serve the application
FROM nginx:stable-alpine
COPY --from=build-stage /app/dist /usr/share/nginx/html
COPY --from=build-stage /app/public/env.js.template /usr/share/nginx/html/env.js.template

# Copy nginx configuration if it exists
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

# Install envsubst utility for environment variable substitution
RUN apk add --no-cache gettext

# Run envsubst to generate env.js from env.js.template and start Nginx in the foreground
CMD ["/bin/sh", "-c", "envsubst '$API_URL' < /usr/share/nginx/html/env.js.template > /usr/share/nginx/html/env.js && nginx -g 'daemon off;'"]

# Add startup script to substitute environment variables
#RUN echo '#!/bin/sh \n\ envsubst < /usr/share/nginx/html/env.js.template > /usr/share/nginx/html/env.js \n\ nginx -g "daemon off;"' > /docker-entrypoint.sh && chmod +x /docker-entrypoint.sh

#ENTRYPOINT ["/docker-entrypoint.sh"]