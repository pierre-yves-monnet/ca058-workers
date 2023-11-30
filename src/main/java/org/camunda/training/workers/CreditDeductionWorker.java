package org.camunda.training.workers;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import org.camunda.training.services.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class CreditDeductionWorker implements JobHandler {

  Logger LOGGER = LoggerFactory.getLogger(CreditDeductionWorker.class);

  @Override
  public void handle(JobClient client, ActivatedJob job) throws Exception {

    Map<String, Object> variablesAsMap = job.getVariablesAsMap();
    String customerId = (String) variablesAsMap.get("customerId");

    Double orderTotal = getDoubleValue(variablesAsMap.get("orderTotal"), Double.valueOf(0));
    Double customerCredit = getDoubleValue(variablesAsMap.get("customerCredit"), Double.valueOf(0));

    LOGGER.info("Job handled: " + job.getType() + " " + job.getKey() + " customerid:" + customerId + " OrderTotal: "
        + orderTotal);

    CustomerService customerService = new CustomerService();

    Double openAmount = customerService.deductCredit(customerId, customerCredit, orderTotal);

    // we collect the new credit, after the deduction
    Double newCustomerCredit = customerService.getCustomerCredit(customerId);

    // openAmount if the value the customer has to pay
    Map<String, Object> variablesToUpdate = new HashMap<>();
    variablesToUpdate.put("openAmount", openAmount);
    variablesToUpdate.put("customerCredit", newCustomerCredit);

    // Complete the Job
    client.newCompleteCommand(job.getKey()).variables(variablesToUpdate).send().join();
  }

  private Double getDoubleValue(Object value, Double defaultValue) {
    try {
      if (value instanceof Double)
        return (Double) value;
      if (value == null)
        return defaultValue;

      return Double.valueOf(value.toString());
    } catch (Exception e) {
      return defaultValue;
    }
  }
}