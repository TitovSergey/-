import java.util.InputMismatchException;
import java.util.Scanner;

public class LeapYearChecker {

    /**
     * Проверяет, является ли год високосным
     * @param year год для проверки
     * @return true если год високосный, false если нет
     * @throws IllegalArgumentException если год отрицательный или нулевой
     */
    public static boolean isLeapYear(int year) {
        if (year <= 0) {
            throw new IllegalArgumentException("Год должен быть положительным числом. Введено: " + year);
        }
        
        // Правило високосного года:
        // 1. Год делится на 4
        // 2. Но если год делится на 100, он должен делиться и на 400
        return (year % 4 == 0) && (year % 100 != 0 || year % 400 == 0);
    }

    /**
     * Проверяет корректность введенного года
     */
    private static void validateYear(int year) {
        if (year <= 0) {
            throw new IllegalArgumentException("Год должен быть положительным числом!");
        }
        if (year > 100000) {
            throw new IllegalArgumentException("Год не может быть больше 100000!");
        }
    }

    /**
     * Получает год от пользователя с обработкой ошибок ввода
     */
    public static int getYearFromUser(Scanner scanner) {
        while (true) {
            try {
                System.out.print("Введите год: ");
                int year = scanner.nextInt();
                validateYear(year);
                return year;
                
            } catch (InputMismatchException e) {
                System.out.println("Ошибка: Введите целое число!");
                scanner.nextLine(); // Очищаем буфер сканера
                
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== ПРОВЕРКА ВИСОКОСНОГО ГОДА ===");
        System.out.println("Программа определяет, является ли год високосным");
        System.out.println("Для выхода введите '0'");
        
        while (true) {
            try {
                int year = getYearFromUser(scanner);
                
                // Проверка на выход
                if (year == 0) {
                    System.out.println("Программа завершена.");
                    break;
                }
                
                // Проверка високосности
                boolean isLeap = isLeapYear(year);
                
                // Вывод результата
                System.out.println("\n" + "=".repeat(40));
                System.out.println("Год: " + year);
                System.out.println("Результат: " + (isLeap ? "ВИСОКОСНЫЙ" : "НЕ ВИСОКОСНЫЙ"));
                
                // Дополнительная информация
                printYearInfo(year, isLeap);
                System.out.println("=".repeat(40) + "\n");
                
            } catch (Exception e) {
                System.out.println("Неожиданная ошибка: " + e.getMessage());
                scanner.nextLine(); // Очищаем буфер
            }
        }
        
        scanner.close();
    }

    /**
     * Выводит дополнительную информацию о годе
     */
    private static void printYearInfo(int year, boolean isLeap) {
        if (isLeap) {
            System.out.println("✓ В феврале 29 дней");
            System.out.println("✓ Год длится 366 дней");
        } else {
            System.out.println("✓ В феврале 28 дней");
            System.out.println("✓ Год длится 365 дней");
        }
        
        // Проверка на особые годы
        if (year % 100 == 0) {
            System.out.println("✓ Столетний год");
        }
        if (year % 400 == 0) {
            System.out.println("✓ Особый столетний год (делится на 400)");
        }
    }
}
