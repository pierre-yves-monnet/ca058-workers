package org.camunda.training;

import java.time.Duration;
import java.util.Scanner;

import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.camunda.training.workers.ChargeCreditCardWorker;
import org.camunda.training.workers.CreditDeductionWorker;

import org.camunda.training.workers.PaymentCompletionWorker;
import org.camunda.training.workers.PaymentInvocationWorker;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Deployment(resources = "classpath:*.bpmn")

public class PaymentApplication {

	static Logger LOGGER = LoggerFactory.getLogger(PaymentApplication.class);

	// Zeebe Client Credentials
	private static final String ZEEBE_ADDRESS="25fdd1e6-e4a1-4362-b49c-5eced08cb893.ont-1.zeebe.camunda.io:443";
	private static final String ZEEBE_CLIENT_ID="OuRHzG9aeC1uRVn.nqKkBVuYuHRNtdk-";
	private static final String ZEEBE_CLIENT_SECRET="xA4fV-8vkm9~nL3Nph3qIUHujeYYzY_n8ntLynheQa-cYUP620j2y.t.qq-PIO8s";
	private static final String ZEEBE_AUTHORIZATION_SERVER_URL="https://login.cloud.camunda.io/oauth/token";
	private static final String ZEEBE_TOKEN_AUDIENCE="zeebe.camunda.io";

	private static final String ZEEBE_BROKER_GATEWAY_ADDRESS="127.0.0.1:26500";

	private static final boolean ZEEBE_CLOUD= false;

	public static void main(String[] args) {

		final OAuthCredentialsProvider credentialsProvider = new OAuthCredentialsProviderBuilder()
			.authorizationServerUrl(ZEEBE_AUTHORIZATION_SERVER_URL)
			.audience(ZEEBE_TOKEN_AUDIENCE)
			.clientId(ZEEBE_CLIENT_ID)
			.clientSecret(ZEEBE_CLIENT_SECRET)
			.build();

		ZeebeClient client =null;

		if (ZEEBE_CLOUD) {
			client = ZeebeClient.newClientBuilder().gatewayAddress(ZEEBE_ADDRESS).credentialsProvider(credentialsProvider).build();
			LOGGER.info("Cloud connection");

		}
		else {
			client= ZeebeClient.newClientBuilder().gatewayAddress(ZEEBE_BROKER_GATEWAY_ADDRESS).usePlaintext()
			.defaultJobWorkerMaxJobsActive(10)
					.defaultJobTimeout(Duration.ofMillis(5*60*1000))
					.numJobWorkerExecutionThreads(10)
					.build();

			LOGGER.info("Local connection");
		}



		try {

			// Request the Cluster Topology
			LOGGER.info("Connected to: " + client.newTopologyRequest().send().join());

			// Start a Job Worker
			final JobWorker creditCardWorker = client.newWorker()
				.jobType("credit-deduction")
				.handler(new CreditDeductionWorker())
				.open();

			// Start a Job Worker
			final JobWorker creditCardChargingWorker = client.newWorker()
					.jobType("credit-card-charging")
					.handler(new ChargeCreditCardWorker())
					.open();

			// Start a Job Worker
			final JobWorker paymentInvocationWorker = client.newWorker()
					.jobType("payment-invocation")
					.handler(new PaymentInvocationWorker(client))
					.open();

			final JobWorker paymentCompletionWorker = client.newWorker()
					.jobType("payment-completion")
					.handler(new PaymentCompletionWorker(client))
					.open();


			// SpringApplication.run(PaymentApplication.class, args);


			// Terminate the worker with an Integer input
			Scanner sc = new Scanner(System.in);			
			sc.nextInt();
			sc.close();
			creditCardWorker.close();
			creditCardChargingWorker.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
