# Use Eclipse Temurin 21 as the base image (replacement for deprecated openjdk images)
FROM eclipse-temurin:21-jdk

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven wrapper and pom.xml to leverage Docker layer caching
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Download dependencies (this layer will be cached if pom.xml hasn't changed)
RUN ./mvnw dependency:go-offline -B

# Copy the source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose the port the app runs on (Heroku will override this)
EXPOSE 8080

# Set environment variables for production
ENV SPRING_PROFILES_ACTIVE=production
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run the application (Heroku will set PORT environment variable)
CMD java $JAVA_OPTS -jar target/assignP-0.0.1-SNAPSHOT.jar --server.port=${PORT:-8080}