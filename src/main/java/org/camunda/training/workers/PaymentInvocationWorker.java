package org.camunda.training.workers;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.Random;


public class PaymentInvocationWorker implements JobHandler  {

  Logger LOGGER = LoggerFactory.getLogger(PaymentInvocationWorker.class);

  ZeebeClient zeebeClient;

  public PaymentInvocationWorker(ZeebeClient zeebeClient) {
    this.zeebeClient = zeebeClient;
  }
  @Override
  public void handle(final JobClient jobClient, final ActivatedJob job) {
    LOGGER.info("Task definition type: " + job.getType());

    Map<String, Object> variables = job.getVariablesAsMap();
    String orderId = generateRandomOrderId(6);
    variables.put("orderId", orderId);

    zeebeClient.newPublishMessageCommand()
        .messageName("paymentRequestMessage")
        .correlationKey(orderId)
        .variables(variables)
        .timeToLive(Duration.ofMillis(1000*50))
        .send().join();

    jobClient.newCompleteCommand(job).variables(variables).send().join();
  }



  // Generates a random order ID with a given length, consisting of letters and/or digits
  private String generateRandomOrderId(int length) {
    var stringBuilder = new StringBuilder();
    var random = new Random();

    for (int i = 0; i < length; ++i) {
      boolean appendChar = random.nextBoolean();

      if (appendChar) {
        stringBuilder.append((char) ('A' + random.nextInt(26)));
      } else {
        stringBuilder.append(random.nextInt(9));
      }
    }

    return stringBuilder.toString();
  }
}
