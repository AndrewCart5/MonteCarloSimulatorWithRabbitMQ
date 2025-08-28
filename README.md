# Distributed Monte Carlo Option Pricer

A simple **distributed Monte Carlo option pricer** implemented in Java using **RabbitMQ** for task distribution. The project splits Monte Carlo simulations across multiple workers and aggregates the results to estimate the price of a European call option.

---

## Project Structure

```
distributed-monte-carlo/
├── master/
│    └── Master.java          # Sends tasks and collects results
├── worker/
│    └── Worker.java          # Processes Monte Carlo tasks
└── README.md
```

---

## Features

* Distributes Monte Carlo simulation tasks across multiple workers using RabbitMQ.
* Each worker computes a batch of Monte Carlo paths and sends results back.
* Master aggregates results and computes the final option price.
* Supports **Dockerized RabbitMQ** for easy setup.

---

## Prerequisites

* **Java 8+**
* **RabbitMQ** (can run in Docker)
* **Maven** (optional, if you want to manage dependencies)
* **Internet connection** (for downloading RabbitMQ client library if using Maven)

---

## Setting Up RabbitMQ (Docker)

1. Pull and run RabbitMQ Docker image:

```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

2. Access RabbitMQ management console at [http://localhost:15672](http://localhost:15672)
   Default credentials:

   ```
   username: guest
   password: guest
   ```

---

## Running the Project

1. **Start the RabbitMQ server** (if using Docker, run the command above).
2. **Compile and run the Master**:

```bash
javac -cp .:amqp-client-5.16.0.jar PackageExplorer/fkf/Master.java
java -cp .:amqp-client-5.16.0.jar PackageExplorer.fkf.Master
```

3. **Compile and run one or more Workers**:

```bash
javac -cp .:amqp-client-5.16.0.jar PackageExplorer/fkf/Worker.java
java -cp .:amqp-client-5.16.0.jar PackageExplorer.fkf.Worker
```

* The **Master** sends Monte Carlo tasks to the queue.
* Workers pick up tasks, compute the option price, and send results back.
* The Master collects results and prints the **final Monte Carlo price**.

---

## How It Works

1. **Master.java**

   * Splits total Monte Carlo paths into batches.
   * Sends each batch as a task to the `mc_tasks` queue.
   * Waits for results from `mc_results` queue.
   * Aggregates results and computes the average option price.

2. **Worker.java**

   * Listens to the `mc_tasks` queue.
   * For each task:

     * Runs Monte Carlo simulations for `nPaths`.
     * Computes discounted payoff for a European call option.
     * Sends the sum and count back to `mc_results` queue.

3. **RabbitMQ Queues**

   * `mc_tasks` – Holds tasks sent by Master.
   * `mc_results` – Holds computed results sent by Workers.

---

## Example Output

**Master:**

```
Sent task 123e4567-e89b-12d3-a456-426614174000
Sent task 987f6543-e
```
