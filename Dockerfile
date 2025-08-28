# Use RabbitMQ management image as base
FROM rabbitmq:3-management

# Install OpenJDK 17
USER root
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk && \
    rm -rf /var/lib/apt/lists/*

# Set working directory for your Java app
WORKDIR /app

# Copy your compiled Java JARs (Master and Worker) into the container
COPY target/master.jar master.jar
COPY target/worker.jar worker.jar

# Expose RabbitMQ ports
EXPOSE 5672 15672

# Default command: start RabbitMQ server
CMD ["rabbitmq-server"]
