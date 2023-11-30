package org.camunda.training.services;

import org.camunda.training.workers.CreditDeductionWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomerService {
	Logger LOGGER = LoggerFactory.getLogger(CustomerService.class);

	/**
	 * The customer credit are the last digits of the customer id
	 */
	private Pattern pattern = Pattern.compile("(.*?)(\\d*)");

	/**
	 * Deduct the credit for the given customer and the given amount
	 * 
	 * @param customerId
	 * @param amount
	 * @return the open order amount
	 */
	public Double deductCredit(String customerId, Double customerCredit, Double amount) {
		Double credit = getCustomerCredit(customerId);
		if (credit==null)
			credit = customerCredit;

		Double openAmount;
		Double deductedCredit;
		if (credit > amount) {
			deductedCredit = amount;
			openAmount = 0.0;
		} else {
			openAmount = amount - credit;
			deductedCredit = credit;
		}
		LOGGER.info("charged %f from the credit, open amount is %f %s", deductedCredit, openAmount, System.lineSeparator());
		return openAmount;
	}

	/**
	 * Get the current customer credit
	 * 
	 * @param customerId
	 * @return the current credit of the given customer
	 */
	public Double getCustomerCredit(String customerId) {
		if (customerId==null)
			return null;
		Double credit = 0.0;
		Matcher matcher = pattern.matcher(customerId);

		if (matcher.matches() && matcher.group(2) != null && matcher.group(2).length() > 0) {
			credit = Double.valueOf(matcher.group(2));
		}

		System.out.printf("customer %s has credit of %f %s", customerId, credit, System.lineSeparator());

		return credit;
	}

}