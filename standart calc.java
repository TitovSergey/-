import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

public class CalculatorGUI extends JFrame {
    private JTextField textField;      // Текстовое поле для ввода и вывода
    private double firstOperand;       // Первый операнд для выполнения операций
    private String operator;           // Оператор для определения типа операции
    private DecimalFormat decimalFormat;  // Формат для отображения результатов

    public CalculatorGUI() {
        setTitle("ТИТОВ С.С.");           // Заголовок окна
        setSize(400, 500);                // Размер окна
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // Завершение программы при закрытии окна

        decimalFormat = new DecimalFormat("#.##########");  // Формат с точностью до 10 знаков после запятой

        JPanel panel = new JPanel(new BorderLayout());  // Панель с компоновкой BorderLayout

        textField = new JTextField();       // Создание текстового поля
        textField.setFont(new Font("Arial", Font.PLAIN, 50));  // Установка шрифта для текстового поля
        panel.add(textField, BorderLayout.NORTH);  // Добавление текстового поля в верхнюю часть панели

        JPanel buttonPanel = new JPanel(new GridLayout(0, 4, 5, 5));  // Панель для кнопок с компоновкой GridLayout

        String[] buttonLabels = {
                "sqrt", "C", "CE", "<-",          // Массив меток кнопок для создания
                "x^y", "sin", "cos","/",          // сетки 6x4
                "7", "8", "9","*",
                "4", "5", "6", "-",
                "1", "2", "3", "+",
                ".", "0", "tan", "=",
                "ctg",
        };

        for (String label : buttonLabels) {
            JButton button = new JButton(label);  // Создание кнопки с текущей меткой
            button.setFont(new Font("Arial", Font.PLAIN, 16));  // Установка шрифта для кнопки
            button.addActionListener(new ButtonClickListener());  // Добавление слушателя событий к кнопке
            buttonPanel.add(button);  // Добавление кнопки на панель
        }

        panel.add(buttonPanel, BorderLayout.CENTER);  // Добавление панели с кнопками в центр панели

        add(panel);  // Добавление основной панели в окно
    }

    private class ButtonClickListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JButton source = (JButton) e.getSource();  // Получение источника события (кнопки)
            String command = source.getText();  // Получение текста с кнопки

            switch (command) {
                case "C":
                    clear();  // Очистка всех значений
                    break;
                case "<-":
                    backspace();  // Удаление последнего символа из поля ввода
                    break;
                case "CE":
                    clearEntry();  // Очистка текущего значения в поле ввода
                    break;
                case "=":
                    if (operator.equals("^")) {
                        calculatePower();  // Выполнение операции возведения в степень
                    } else {
                        performCalculation();  // Выполнение основных арифметических операций
                    }
                    break;
                case "sin":
                case "cos":
                case "tan":
                case "cot":
                    calculateTrigonometricFunction(command);  // Вычисление тригонометрических функций
                    break;
                case "sqrt":
                    calculateSquareRoot();  // Вычисление квадратного корня
                    break;
                case "x^y":
                    operator = "^";  // Установка оператора возведения в степень
                    if (textField.getText().isEmpty()) {
                        // Если поле ввода пусто, возводим 0 во вторую степень
                        textField.setText("0");
                    } else {
                        // В противном случае возводим текущее значение в поле ввода во вторую степень
                        firstOperand = Double.parseDouble(textField.getText());
                        textField.setText("");
                        calculatePower();
                    }
                    break;
                default:
                    handleNumericOperator(command);  // Обработка числовых операторов и точки
                    break;
            }
        }

        private void calculatePower() {
            double base = firstOperand;
            double exponent = Double.parseDouble(textField.getText());
            double result = Math.pow(base, exponent);
            textField.setText(decimalFormat.format(result));
        }

        private void calculateSquareRoot() {
            double operand = Double.parseDouble(textField.getText());
            if (operand >= 0) {
                double result = Math.sqrt(operand);
                textField.setText(decimalFormat.format(result));
            } else {
                // Показываем сообщение об ошибке при извлечении корня из отрицательного числа
                JOptionPane.showMessageDialog(null, "Извлечение квадратного корня из отрицательного числа невозможно.",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                clear();  // Очищаем все значения
            }
        }

        private void handleNumericOperator(String command) {
            // Метод обрабатывает нажатие кнопок с числами и точкой.

            if ("0123456789.".contains(command)) {
                // Если нажата кнопка с числом или точкой, добавляем её значение в поле ввода.
                textField.setText(textField.getText() + command);
            } else if ("+-*/".contains(command)) {
                // Если нажата кнопка с арифметическим оператором, сохраняем текущее значение в поле ввода
                // как первый операнд, устанавливаем оператор и очищаем поле ввода для ввода второго операнда.
                firstOperand = Double.parseDouble(textField.getText());
                operator = command;
                textField.setText("");
            }
        }

        private void performCalculation() {
            // Метод выполняет основные арифметические операции (сложение, вычитание, умножение, деление).

            if (operator != null) {
                // Если оператор установлен, получаем второй операнд из поля ввода.
                double secondOperand = Double.parseDouble(textField.getText());

                // Выполняем соответствующую операцию и выводим результат в поле ввода.
                double result = calculate(firstOperand, secondOperand, operator);
                textField.setText(decimalFormat.format(result));
            }
        }

        private void calculateTrigonometricFunction(String function) {
            // Метод вычисляет значения тригонометрических функций (sin, cos, tan, cot).

            double operand = Double.parseDouble(textField.getText());
            double result;

            switch (function) {
                case "sin":
                    result = Math.sin(Math.toRadians(operand));
                    break;
                case "cos":
                    result = Math.cos(Math.toRadians(operand));
                    break;
                case "tan":
                    result = Math.tan(Math.toRadians(operand));
                    break;
                case "cot":
                    result = 1 / Math.tan(Math.toRadians(operand));
                    break;
                default:
                    result = 0;
            }

            // Форматируем результат и выводим в поле ввода.
            DecimalFormat decimalFormat = new DecimalFormat("#.#");
            textField.setText(decimalFormat.format(result));
        }

        private double calculate(double firstOperand, double secondOperand, String operator) {
            // Метод выполняет арифметическую операцию в зависимости от заданного оператора.

            switch (operator) {
                case "+":
                    return firstOperand + secondOperand;
                case "-":
                    return firstOperand - secondOperand;
                case "*":
                    return firstOperand * secondOperand;
                case "/":
                    if (secondOperand == 0) {
                        // Если делитель равен нулю, выводим сообщение об ошибке и очищаем поля.
                        JOptionPane.showMessageDialog(null, "Деление на ноль недопустимо.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        clear();
                        return 0;
                    } else {
                        return firstOperand / secondOperand;
                    }
                default:
                    return 0;
            }
        }

        private void clear() {
            // Метод очищает все значения и сбрасывает состояние калькулятора.

            firstOperand = 0;
            operator = null;
            textField.setText("");
        }

        private void clearEntry() {
            // Метод очищает текущее значение в поле ввода.

            textField.setText("");
        }

        private void backspace() {
            // Метод удаляет последний введенный символ из поля ввода.

            String currentText = textField.getText();
            if (!currentText.isEmpty()) {
                textField.setText(currentText.substring(0, currentText.length() - 1));
            }
        }
    }
        public static void main(String[] args) {
            SwingUtilities.invokeLater(() -> {
                CalculatorGUI calculator = new CalculatorGUI();
                calculator.setVisible(true);
            });
        }
    }
