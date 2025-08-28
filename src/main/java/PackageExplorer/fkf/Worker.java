package PackageExplorer.fkf;

import com.rabbitmq.client.*;
import java.util.Random;

public class Worker {
    private final static String TASK_QUEUE = "mc_tasks";
    private final static String RESULT_QUEUE = "mc_results";

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");   // Docker maps port 5672 to localhost
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(TASK_QUEUE, true, false, false, null);
        channel.queueDeclare(RESULT_QUEUE, true, false, false, null);
        channel.basicQos(1);

        System.out.println("Worker waiting for tasks...");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            String[] parts = message.split(",");
            String taskId = parts[0];
            int nPaths = Integer.parseInt(parts[1]);
            double S0 = Double.parseDouble(parts[2]);
            double K = Double.parseDouble(parts[3]);
            double r = Double.parseDouble(parts[4]);
            double sigma = Double.parseDouble(parts[5]);
            double T = Double.parseDouble(parts[6]);

            // Monte Carlo simulation
            Random rand = new Random();
            double sumPayoffs = 0.0;
            for (int i = 0; i < nPaths; i++) {
                double u1 = rand.nextDouble();
                double u2 = rand.nextDouble();
                double z = Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2 * Math.PI * u2);
                double ST = S0 * Math.exp((r - 0.5 * sigma * sigma) * T + sigma * Math.sqrt(T) * z);
                double payoff = Math.max(ST - K, 0.0);
                sumPayoffs += Math.exp(-r * T) * payoff;
            }

            // Send results back
            String result = taskId + "," + sumPayoffs + "," + nPaths;
            channel.basicPublish("", RESULT_QUEUE, null, result.getBytes("UTF-8"));
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            System.out.println("Worker finished task " + taskId);
        };

        channel.basicConsume(TASK_QUEUE, false, deliverCallback, consumerTag -> {});
    }
}
