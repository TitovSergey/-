// Польза полиморфизма в системе обработки платежей:

// Легко добавить новую платежную систему.

class ApplePayProcessor implements PaymentProcessor {

    @Override

    public void processPayment(double amount) {

        System.out.println("Обрабатываю через Apple Pay: " + amount);

    }

    @Override

    public boolean supports(String paymentType) {

        return "apple_pay".equals(paymentType);

    }

}

// PaymentService автоматически будет работать с новой системой.
