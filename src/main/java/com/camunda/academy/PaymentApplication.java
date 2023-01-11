package com.camunda.academy;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.camunda.academy.handler.CreditCardServiceHandler;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;

public class PaymentApplication {
	
	//Zeebe Client Credentials
	private static final String ZEEBE_ADDRESS = "[ZEEBE_ADDRESS]";
	private static final String ZEEBE_CLIENT_ID = "[ZEEBE_CLIENT_ID]";
	private static final String ZEEBE_CLIENT_SECRET = "[ZEEBE_CLIENT_SECRET]";
	private static final String ZEEBE_AUTHORIZATION_SERVER_URL = "[ZEEBE_AUTHORIZATION_SERVER_URL]";
	private static final String ZEEBE_TOKEN_AUDIENCE = "[ZEEBE_TOKEN_AUDIENCE]";
	
	//Payment Application Details
	private static final int WORKER_TIMEOUT = 10;
	private static final int WORKER_TIME_TO_LIVE = 10000;

	//Process Definition Details
	private static final String CREDIT_CARD_JOB_TYPE = "chargeCreditCard";
	private static final String BPMN_PROCESS_ID = "paymentProcess";
	
	//Process Variables
	private static final String VARIABLE_CARD_CVC = "cardCVC";
	private static final String VARIABLE_CARD_EXPIRY = "cardExpiry";
	private static final String VARIABLE_CARD_NUMBER = "cardNumber";
	private static final String VARIABLE_AMOUNT = "amount";
	private static final String VARIABLE_REFERENCE = "reference";
	
    public static void main(String[] args){
    	
    	final OAuthCredentialsProvider credentialsProvider =
    			new OAuthCredentialsProviderBuilder()
			    	.authorizationServerUrl(ZEEBE_AUTHORIZATION_SERVER_URL)
			        .audience(ZEEBE_TOKEN_AUDIENCE)
			        .clientId(ZEEBE_CLIENT_ID)
			        .clientSecret(ZEEBE_CLIENT_SECRET)
			        .build();
	    	
		try (final ZeebeClient client =
		        ZeebeClient.newClientBuilder()
		            .gatewayAddress(ZEEBE_ADDRESS)
		            .credentialsProvider(credentialsProvider)
		            .build()) {
			
			//Request the Cluster Topology
			System.out.println("Connected to: " + client.newTopologyRequest().send().join());
			
			//Build the Start Process Variables
			final Map<String, Object> variables = new HashMap<String, Object>();
			variables.put(VARIABLE_REFERENCE, "C8_12345");
			variables.put(VARIABLE_AMOUNT, Double.valueOf(100.00));
			variables.put(VARIABLE_CARD_NUMBER, "1234567812345678");
			variables.put(VARIABLE_CARD_EXPIRY, "12/2023");
			variables.put(VARIABLE_CARD_CVC, "123");
			
			//Launch the Process Instance
			client.newCreateInstanceCommand()
			    .bpmnProcessId(BPMN_PROCESS_ID)
			    .latestVersion()
			    .variables(variables)
			    .send()
			    .join();
			
			//Start a Job Worker
			final JobWorker creditCardWorker =
				    client.newWorker()
				        .jobType(CREDIT_CARD_JOB_TYPE)
				        .handler(new CreditCardServiceHandler())
				        .timeout(Duration.ofSeconds(WORKER_TIMEOUT).toMillis())
				        .open();
			
			//Wait for the Workers
			Thread.sleep(WORKER_TIME_TO_LIVE);
			
		} catch (Exception e) {
		    e.printStackTrace();
		}
    }
}
