package com.camunda.academy.handler;

import java.util.HashMap;
import java.util.Map;

import com.camunda.academy.service.CreditCardService;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;

public class CreditCardServiceHandler implements JobHandler {
	
	//Process Variables
	private static final String VARIABLE_CARD_CVC = "cardCVC";
	private static final String VARIABLE_CARD_EXPIRY = "cardExpiry";
	private static final String VARIABLE_CARD_NUMBER = "cardNumber";
	private static final String VARIABLE_AMOUNT = "amount";
	private static final String VARIABLE_REFERENCE = "reference";
	private static final String VARIABLE_CONFIRMATION = "confirmation";

	//Create a Credit Card Service for Testing
    CreditCardService creditCardService = new CreditCardService();

    @Override
    public void handle(JobClient client, ActivatedJob job) throws Exception {
    	
    	//Obtain the Process Variables
    	final Map<String, Object> inputVariables = job.getVariablesAsMap();
    	final String reference = (String) inputVariables.get(VARIABLE_REFERENCE);
    	final Double amount = (Double) inputVariables.get(VARIABLE_AMOUNT);
    	final String cardNumber = (String) inputVariables.get(VARIABLE_CARD_NUMBER);
    	final String cardExpiry = (String) inputVariables.get(VARIABLE_CARD_EXPIRY);
    	final String cardCVC =  (String) inputVariables.get(VARIABLE_CARD_CVC);
    	
    	//Charge the Credit Card
    	final String confirmation = creditCardService.chargeCreditCard(reference, amount, cardNumber, cardExpiry, cardCVC);
        
    	//Build the Output Process Variables
    	final Map<String, Object> outputVariables = new HashMap<String, Object>();
    	outputVariables.put(VARIABLE_CONFIRMATION, confirmation);
    	
    	//Complete the Job
    	client.newCompleteCommand(job.getKey()).variables(outputVariables).send().join();
    }
}
