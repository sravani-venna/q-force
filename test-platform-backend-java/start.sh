#!/bin/bash

echo "🚀 Starting Test Platform Backend (Java Spring Boot)"
echo "=================================================="

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or higher."
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven 3.6 or higher."
    exit 1
fi

# Build and run the application
echo "📦 Building the application..."
mvn clean compile

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo "🚀 Starting the server..."
    mvn spring-boot:run
else
    echo "❌ Build failed. Please check the logs above."
    exit 1
fi
