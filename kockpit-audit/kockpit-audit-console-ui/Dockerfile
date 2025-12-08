# Build stage
FROM node:22-alpine AS build

WORKDIR /app

ARG NODE_ENV=production

ENV NODE_ENV=$NODE_ENV

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy source code
COPY . .

# Build the application
RUN if [ "$NODE_ENV" = "development" ]; then \
      npm run build -- --mode development; \
    else \
      npm run build; \
    fi

# Production stage
FROM nginx:alpine

# Copy built assets from build stage
COPY --from=build /app/dist /usr/share/nginx/html

COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

# Copy custom nginx configuration if needed
# COPY nginx.conf /etc/nginx/nginx.conf

# Expose port 80
EXPOSE 80

ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["nginx", "-g", "daemon off;"]
# Start nginx
#CMD ["nginx", "-g", "daemon off;"]
