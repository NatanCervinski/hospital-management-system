# Simple Angular development server
FROM node:20-alpine

# Install curl for health check
RUN apk add --no-cache curl

# Set working directory
WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies (remove --silent to see errors)
RUN npm install

# Copy source code
COPY . .

# Expose port 4200
EXPOSE 4200

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:4200/ || exit 1

# Start Angular development server
CMD ["npm", "start", "--", "--host", "0.0.0.0", "--port", "4200"]
