public class BankAccount {

    public double balance; // Прямой доступ к полю - здесь прямой доступ к полю, это не надёжно.

}

//В этом случае любой другой код может изменить баланс произвольным образом, что может привести к ошибкам:

BankAccount account = new BankAccount();

account.balance = 1000; // Нормально

account.balance = -500; // Отрицательный баланс

account.balance = 999999; // Слишком большой баланс


