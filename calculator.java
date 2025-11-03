import java.util.Scanner;



public class SimpleCalculator {

    

    public static double calculate(String expression) {

        // Удаляем пробелы и разбиваем строку

        expression = expression.replaceAll("\\s+", "");

        String[] parts;

        

        // Проверяем какая операция используется

        if (expression.contains("+")) {

            parts = expression.split("\\+");

            return Double.parseDouble(parts[0]) + Double.parseDouble(parts[1]);

        } else if (expression.contains("-")) {

            parts = expression.split("-");

            return Double.parseDouble(parts[0]) - Double.parseDouble(parts[1]);

        } else if (expression.contains("*")) {

            parts = expression.split("\\*");

            return Double.parseDouble(parts[0]) * Double.parseDouble(parts[1]);

        } else if (expression.contains("/")) {

            parts = expression.split("/");

            double divisor = Double.parseDouble(parts[1]);

            if (divisor == 0) {

                throw new ArithmeticException("Деление на ноль!");

            }

            return Double.parseDouble(parts[0]) / divisor;

        } else {

            throw new IllegalArgumentException("Неизвестная операция: " + expression);

        }

    }

    

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        

        System.out.println("Калькулятор (поддерживает +, -, *, /)");

        System.out.println("Введите выражение (например: 2 * 6.5):");

        System.out.print("> ");

        

        String input = scanner.nextLine();

        

        try {

            double result = calculate(input);

            System.out.println("Результат: " + result);

        } catch (Exception e) {

            System.out.println("Ошибка: " + e.getMessage());

        }

        

        scanner.close();

    }

}
