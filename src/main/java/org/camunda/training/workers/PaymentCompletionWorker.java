package org.camunda.training.workers;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PaymentCompletionWorker  implements JobHandler {

  Logger LOGGER = LoggerFactory.getLogger(PaymentCompletionWorker.class);

  ZeebeClient zeebeClient;
  public PaymentCompletionWorker(ZeebeClient zeebeClient) {
    this.zeebeClient = zeebeClient;
  }

  // @ZeebeWorker(type = "payment-completion")
  @Override
  public void handle(final JobClient jobClient, final ActivatedJob job) {
    LOGGER.info("Task definition type: " + job.getType());

    Map<String, Object> variables = job.getVariablesAsMap();

    // send the message only if an orderId is define
    if (variables.get("orderId") !=null) {
      zeebeClient.newPublishMessageCommand()
          .messageName("paymentCompletedMessage")
          .correlationKey(variables.get("orderId").toString())
          .send()
          .join();
    }

    jobClient.newCompleteCommand(job).send().join();
  }
}
