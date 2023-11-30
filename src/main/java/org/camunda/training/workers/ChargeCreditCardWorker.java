package org.camunda.training.workers;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import org.camunda.training.exceptions.InvalidCreditCardException;
import org.camunda.training.exceptions.InvalidCreditCardExpiryDate;
import org.camunda.training.services.CreditCardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ChargeCreditCardWorker implements JobHandler {

  Logger LOGGER = LoggerFactory.getLogger(ChargeCreditCardWorker.class);

  @Override
  public void handle(JobClient jobClient, ActivatedJob job) throws Exception {

    Map<String, Object> variablesAsMap = job.getVariablesAsMap();
    String cardNumber = (String) variablesAsMap.get("cardNumber");
    String cvc = (String) variablesAsMap.get("cvc");
    String expiryDate = (String) variablesAsMap.get("expiryDate");

    Double openAmount = Double.valueOf(0);
    try {
      openAmount = Double.valueOf(variablesAsMap.get("openAmount").toString());
    } catch (Exception e) {
      LOGGER.info(
          "ChargeCreditCard handled: " + job.getType() + " " + job.getKey() + " Can't get openOrder " + e.getMessage());
    }
    LOGGER.info(
        "ChargeCreditCard handled: " + job.getType() + " " + job.getKey() + " chargeOnCard[" + cardNumber + "] cvc["
            + cvc + "] expiryDate[" + cvc + "] amount " + openAmount);

    CreditCardService creditCardService = new CreditCardService();

    try {
      if (cardNumber != null)
        creditCardService.chargeAmount(cardNumber, cvc, expiryDate, openAmount);
    }
    // Exercise 8 : handle failure
    catch (InvalidCreditCardExpiryDate e) {
      LOGGER.error("Invalid credit card Expiry Date");
      int retries = job.getRetries()==0? 3 : job.getRetries() -1;

      jobClient.newFailCommand(job).retries(retries).send().join();
      return;
    }
    // exercise 9 : throw an error
    catch (InvalidCreditCardException e) {
      LOGGER.error("Invalid credit card");
      jobClient.newThrowErrorCommand(job)
          .errorCode("creditCardChargeError")
          .variables(Map.of("errorCode", "No more credit"))
          .errorMessage(e.getMessage()).send().join();
      return;
    }
    // nothing to return
    Map<String, Object> variablesToUpdate = new HashMap<>();

    // Complete the Job
    jobClient.newCompleteCommand(job.getKey()).variables(variablesToUpdate).send().join();
  }
}