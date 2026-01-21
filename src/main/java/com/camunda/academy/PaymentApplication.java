package com.camunda.academy;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.camunda.academy.handler.CreditCardServiceHandler;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.worker.JobWorker;
import io.camunda.client.impl.oauth.OAuthCredentialsProvider;
import io.camunda.client.impl.oauth.OAuthCredentialsProviderBuilder;

public class PaymentApplication {

    // Zeebe Client Credentials
    private static String CAMUNDA_AUTHORIZATION_SERVER_URL = "https://login.cloud.camunda.io/oauth/token";
    private static String CAMUNDA_TOKEN_AUDIENCE = "zeebe.camunda.io";
    private static String CAMUNDA_REST_ADDRESS = "https://[CLUSTER_REGION].zeebe.camunda.io/[CLUSTER_ID]";
    private static String CAMUNDA_GRPC_ADDRESS = "https://[CLUSTER_ID].[CLUSTER_REGION].zeebe.camunda.io:443";
    private static String CAMUNDA_CLIENT_ID = "[CAMUNDA_CLIENT_ID]";
    private static String CAMUNDA_CLIENT_SECRET = "[CAMUNDA_CLIENT_SECRET]";

    public static void main(String[] args) {
        
        final OAuthCredentialsProvider credentialsProvider = new OAuthCredentialsProviderBuilder()
            .authorizationServerUrl(CAMUNDA_AUTHORIZATION_SERVER_URL)
            .audience(CAMUNDA_TOKEN_AUDIENCE)
            .clientId(CAMUNDA_CLIENT_ID)
            .clientSecret(CAMUNDA_CLIENT_SECRET)
            .build();
        
        try (final CamundaClient  client = CamundaClient.newClientBuilder()
            .grpcAddress(URI.create(CAMUNDA_GRPC_ADDRESS))
            .restAddress(URI.create(CAMUNDA_REST_ADDRESS))
            .credentialsProvider(credentialsProvider)
            .build()) {

                final Map<String, Object> variables = new HashMap<String, Object>();
                variables.put("reference", "C8_12345");
                variables.put("amount", Double.valueOf(100.00));
                variables.put("cardNumber", "1234567812345678");
                variables.put("cardExpiry", "12/2027");
                variables.put("cardCVC", "123");

                client.newCreateInstanceCommand()
                    .bpmnProcessId("paymentProcess")
                    .latestVersion()
                    .variables(variables)
                    .send()
                    .join();

                final JobWorker creditCardWorker =
                    client.newWorker()
                        .jobType("chargeCreditCard")
                        .handler(new CreditCardServiceHandler())
                        .timeout(Duration.ofSeconds(10).toMillis())
                        .open();

                Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}