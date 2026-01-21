package com.camunda.academy.handler;

import java.util.HashMap;
import java.util.Map;

import com.camunda.academy.service.CreditCardService;

import io.camunda.client.api.worker.JobClient;
import io.camunda.client.api.worker.JobHandler;
import io.camunda.client.api.response.ActivatedJob;

public class CreditCardServiceHandler implements JobHandler {

      CreditCardService creditCardService = new CreditCardService();

      @Override
      public void handle(JobClient client, ActivatedJob job) throws Exception {

            final Map<String, Object> inputVariables = job.getVariablesAsMap();
            final String reference = (String) inputVariables.get("reference");
            final Double amount = (Double) inputVariables.get("amount");
            final String cardNumber = (String) inputVariables.get("cardNumber");
            final String cardExpiry = (String) inputVariables.get("cardExpiry");
            final String cardCVC =  (String) inputVariables.get("cardCVC");

            final String confirmation = creditCardService.chargeCreditCard(reference, amount, cardNumber, cardExpiry, cardCVC);

            final Map<String, Object> outputVariables = new HashMap<String, Object>();
            outputVariables.put("confirmation", confirmation);

            client.newCompleteCommand(job.getKey()).variables(outputVariables).send().join();
      }
}

