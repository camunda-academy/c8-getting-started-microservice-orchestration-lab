package com.camunda.academy.service;

public class CreditCardService {

    public String chargeCreditCard(final String reference,
                final Double amount,
                final String cardNumber,
                final String cardExpiryDate,
                final String cardCVC) {

        System.out.println("Starting Transaction: " + reference);
        System.out.println("Card Number: " + cardNumber);
        System.out.println("Card Expiry Date: " + cardExpiryDate);
        System.out.println("Card CVC: " + cardCVC);
        System.out.println("Amount: " + amount);

        final String confirmation = String.valueOf(System.currentTimeMillis());
        System.out.println("Successful Transaction: " + confirmation);
        return confirmation;
    }
}
