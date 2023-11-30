package org.camunda.training.services;

import org.camunda.training.exceptions.InvalidCreditCardException;
import org.camunda.training.exceptions.InvalidCreditCardExpiryDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreditCardService {
  Logger LOGGER = LoggerFactory.getLogger(CreditCardService.class);

  public void chargeAmount(String cardNumber, String cvc, String expiryDate, Double amount)
      throws InvalidCreditCardException, InvalidCreditCardExpiryDate {
    if (expiryDate == null || expiryDate.length() != 5) {
      LOGGER.error("The credit card's expiry date is invalid: " + expiryDate);
      throw new InvalidCreditCardExpiryDate("Invalid credit card expiry date");
    }
    if (cvc == null || cvc.length() != 3) {
      LOGGER.error("The credit card's expiry date is invalid: " + expiryDate);
      throw new InvalidCreditCardException("Invalid cvc");
    }

    LOGGER.info("charging card %s that expires on %s and has cvc %s with amount of %f %s", cardNumber, expiryDate, cvc,
        amount, System.lineSeparator());

  }
}