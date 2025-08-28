package PackageExplorer.fkf;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class Master {
    private final static String TASK_QUEUE = "mc_tasks";
    private final static String RESULT_QUEUE = "mc_results";

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();

        // Connect to RabbitMQ running in Docker
        factory.setHost("localhost"); // if Docker maps port 5672 to localhost
        factory.setPort(5672);        // default RabbitMQ port
        factory.setUsername("guest"); // default Docker RabbitMQ username
        factory.setPassword("guest"); // default Docker RabbitMQ password

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(TASK_QUEUE, true, false, false, null);
        channel.queueDeclare(RESULT_QUEUE, true, false, false, null);

        double S0 = 100, K = 100, r = 0.01, sigma = 0.2, T = 1.0;
        int totalPaths = 10;
        int batchSize = 2;
        int nBatches = totalPaths / batchSize;

        // Send tasks
        for (int i = 0; i < nBatches; i++) {
            String taskId = UUID.randomUUID().toString();
            String message = taskId + "," + batchSize + "," + S0 + "," + K + "," + r + "," + sigma + "," + T;
            channel.basicPublish("", TASK_QUEUE, null, message.getBytes("UTF-8"));
            System.out.println("Sent task " + taskId);
        }

        // Collect results
        final double[] totalSum = {0.0};
        final int[] totalN = {0};

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String result = new String(delivery.getBody(), "UTF-8");
            String[] parts = result.split(",");
            String taskId = parts[0];
            double sum = Double.parseDouble(parts[1]);
            int n = Integer.parseInt(parts[2]);

            totalSum[0] += sum;
            totalN[0] += n;
            System.out.println("Got result from task " + taskId);

            if (totalN[0] >= totalPaths) {
                double price = totalSum[0] / totalN[0];
                System.out.printf("Final Monte Carlo Price: %.4f%n", price);
                System.exit(0);
            }
        };

        channel.basicConsume(RESULT_QUEUE, true, deliverCallback, consumerTag -> {});
    }
}
