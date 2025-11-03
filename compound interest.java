import java.util.Scanner;
import java.util.InputMismatchException;

public class CompoundInterestCalculator {
    
    /**
     * Расчет будущей стоимости по сложному проценту
     * @param principal начальная сумма
     * @param rate процентная ставка (в десятичной форме, например 0.05 для 5%)
     * @param periods количество периодов
     * @return будущая стоимость
     */
    public static double calculateFutureValue(double principal, double rate, int periods) {
        return principal * Math.pow(1 + rate, periods);
    }
    
    /**
     * Расчет необходимой процентной ставки для достижения целевой суммы
     * @param principal начальная сумма
     * @param futureValue целевая сумма
     * @param periods количество периодов
     * @return необходимая процентная ставка (в десятичной форме)
     */
    public static double calculateRequiredRate(double principal, double futureValue, int periods) {
        if (principal <= 0 || futureValue <= 0 || periods <= 0) {
            throw new IllegalArgumentException("Все значения должны быть положительными");
        }
        return Math.pow(futureValue / principal, 1.0 / periods) - 1;
    }
    
    /**
     * Форматирование процента для красивого вывода
     */
    public static String formatPercentage(double rate) {
        return String.format("%.2f%%", rate * 100);
    }
    
    /**
     * Вывод таблицы роста по периодам
     */
    public static void printGrowthTable(double principal, double rate, int periods) {
        System.out.println("\n--- Таблица роста ---");
        System.out.println("Период | Сумма");
        System.out.println("----------------");
        
        double currentAmount = principal;
        for (int i = 1; i <= periods; i++) {
            currentAmount = calculateFutureValue(principal, rate, i);
            System.out.printf("%6d | %,.2f%n", i, currentAmount);
        }
    }
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== КАЛЬКУЛЯТОР СЛОЖНОГО ПРОЦЕНТА ===");
        System.out.println("1. Расчет будущей стоимости");
        System.out.println("2. Расчет необходимой процентной ставки");
        System.out.println("3. Выход");
        
        while (true) {
            System.out.print("\nВыберите операцию (1-3): ");
            
            try {
                int choice = scanner.nextInt();
                
                switch (choice) {
                    case 1:
                        calculateFutureValueMenu(scanner);
                        break;
                    case 2:
                        calculateRequiredRateMenu(scanner);
                        break;
                    case 3:
                        System.out.println("До свидания!");
                        scanner.close();
                        return;
                    default:
                        System.out.println("Неверный выбор. Попробуйте снова.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Ошибка: введите число от 1 до 3");
                scanner.nextLine(); // очистка буфера
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
                scanner.nextLine();
            }
        }
    }
    
    private static void calculateFutureValueMenu(Scanner scanner) {
        System.out.println("\n--- РАСЧЕТ БУДУЩЕЙ СТОИМОСТИ ---");
        
        try {
            System.out.print("Введите начальную сумму: ");
            double principal = scanner.nextDouble();
            
            System.out.print("Введите годовую процентную ставку (%): ");
            double annualRate = scanner.nextDouble() / 100.0;
            
            System.out.print("Введите количество лет: ");
            int years = scanner.nextInt();
            
            System.out.print("Капитализация в год (раз): ");
            int compoundsPerYear = scanner.nextInt();
            
            // Расчет общей ставки и периодов
            double ratePerPeriod = annualRate / compoundsPerYear;
            int totalPeriods = years * compoundsPerYear;
            
            // Расчет будущей стоимости
            double futureValue = calculateFutureValue(principal, ratePerPeriod, totalPeriods);
            double totalInterest = futureValue - principal;
            
            // Вывод результатов
            System.out.println("\n--- РЕЗУЛЬТАТЫ ---");
            System.out.printf("Начальная сумма: %,.2f%n", principal);
            System.out.printf("Годовая ставка: %.2f%%%n", annualRate * 100);
            System.out.printf("Период: %d лет%n", years);
            System.out.printf("Капитализация: %d раз в год%n", compoundsPerYear);
            System.out.printf("Будущая стоимость: %,.2f%n", futureValue);
            System.out.printf("Общий доход: %,.2f%n", totalInterest);
            System.out.printf("Рост: %.2f раз%n", futureValue / principal);
            
            // Дополнительные расчеты
            System.out.println("\n--- ДОПОЛНИТЕЛЬНО ---");
            System.out.printf("Эффективная годовая ставка: %.2f%%%n", 
                (Math.pow(1 + ratePerPeriod, compoundsPerYear) - 1) * 100);
            
            // Таблица роста для первых 5 лет (если общий период больше)
            if (years > 1) {
                printGrowthTable(principal, ratePerPeriod, Math.min(5 * compoundsPerYear, totalPeriods));
            }
            
        } catch (InputMismatchException e) {
            System.out.println("Ошибка: введите корректные числовые значения");
            scanner.nextLine();
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
            scanner.nextLine();
        }
    }
    
    private static void calculateRequiredRateMenu(Scanner scanner) {
        System.out.println("\n--- РАСЧЕТ НЕОБХОДИМОЙ СТАВКИ ---");
        
        try {
            System.out.print("Введите начальную сумму: ");
            double principal = scanner.nextDouble();
            
            System.out.print("Введите целевую сумму: ");
            double futureValue = scanner.nextDouble();
            
            System.out.print("Введите количество лет: ");
            int years = scanner.nextInt();
            
            System.out.print("Капитализация в год (раз): ");
            int compoundsPerYear = scanner.nextInt();
            
            if (principal <= 0 || futureValue <= 0 || years <= 0 || compoundsPerYear <= 0) {
                throw new IllegalArgumentException("Все значения должны быть положительными");
            }
            
            if (futureValue <= principal) {
                throw new IllegalArgumentException("Целевая сумма должна быть больше начальной");
            }
            
            int totalPeriods = years * compoundsPerYear;
            
            // Расчет необходимой ставки
            double requiredRatePerPeriod = calculateRequiredRate(principal, futureValue, totalPeriods);
            double requiredAnnualRate = requiredRatePerPeriod * compoundsPerYear;
            
            // Вывод результатов
            System.out.println("\n--- РЕЗУЛЬТАТЫ ---");
            System.out.printf("Начальная сумма: %,.2f%n", principal);
            System.out.printf("Целевая сумма: %,.2f%n", futureValue);
            System.out.printf("Период: %d лет%n", years);
            System.out.printf("Капитализация: %d раз в год%n", compoundsPerYear);
            System.out.printf("Необходимая годовая ставка: %s%n", formatPercentage(requiredAnnualRate));
            System.out.printf("Ставка за период: %s%n", formatPercentage(requiredRatePerPeriod));
            System.out.printf("Общий рост: %.2f раз%n", futureValue / principal);
            
            // Проверка расчета
            double verification = calculateFutureValue(principal, requiredRatePerPeriod, totalPeriods);
            System.out.printf("Проверка: %,.2f (погрешность: %,.4f)%n", 
                verification, Math.abs(verification - futureValue));
            
            // Сравнение с разными периодами
            System.out.println("\n--- СРАВНЕНИЕ С ДРУГИМИ ПЕРИОДАМИ ---");
            int[] compareYears = {years - 1, years, years + 1};
            for (int y : compareYears) {
                if (y > 0) {
                    double rateForPeriod = calculateRequiredRate(principal, futureValue, y * compoundsPerYear);
                    double annualRateForPeriod = rateForPeriod * compoundsPerYear;
                    System.out.printf("За %d лет: %s%n", y, formatPercentage(annualRateForPeriod));
                }
            }
            
        } catch (InputMismatchException e) {
            System.out.println("Ошибка: введите корректные числовые значения");
            scanner.nextLine();
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
            scanner.nextLine();
        }
    }
}
