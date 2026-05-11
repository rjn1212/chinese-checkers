#!/bin/bash
set -e

echo "Starting Chinese Checkers..."

# Build backend
echo "Building backend..."
cd chinesecheckers-backend
mvn package -q -DskipTests
cd ..

# Build frontend
echo "Building frontend..."
cd chinesecheckers-frontend
npm install --silent
npm run build
cd ..

# Copy frontend dist to backend static
mkdir -p chinesecheckers-backend/target/classes/static
cp -r chinesecheckers-frontend/dist/* chinesecheckers-backend/target/classes/static/

# Start backend (serves both API and static frontend)
echo "Starting server on port 8083..."
java -jar chinesecheckers-backend/target/chinesecheckers-backend-1.0.0-SNAPSHOT.jar
