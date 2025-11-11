import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LayoutManagerDemo extends JFrame implements ActionListener {
    
    // Компоненты для демонстрации
    private JPanel mainPanel;
    private JPanel controlPanel;
    private JPanel demoPanel;
    private JButton addButton;
    private JButton removeButton;
    private JButton changeLayoutButton;
    private JLabel infoLabel;
    private JComboBox<String> colorComboBox;
    
    // Счетчик для добавления кнопок
    private int buttonCounter = 1;
    
    // Цвета для кнопок
    private Color[] colors = {
        Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, 
        Color.MAGENTA, Color.CYAN, Color.PINK, Color.YELLOW
    };
    
    public LayoutManagerDemo() {
        initializeUI();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeUI() {
        setTitle("Демонстрация менеджеров компоновки");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // Создаем главную панель с BorderLayout
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Панель управления (будет вверху)
        controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Панель управления"));
        controlPanel.setBackground(new Color(240, 240, 240));
        
        // Демонстрационная панель (будет в центре)
        demoPanel = new JPanel(new FlowLayout());
        demoPanel.setBorder(BorderFactory.createTitledBorder("Демонстрационная панель - FlowLayout"));
        demoPanel.setBackground(Color.WHITE);
        
        // Создаем компоненты управления
        addButton = new JButton("Добавить кнопку");
        removeButton = new JButton("Удалить кнопку");
        changeLayoutButton = new JButton("Сменить Layout");
        infoLabel = new JLabel("Кнопок: 0");
        
        // ComboBox для выбора цвета
        colorComboBox = new JComboBox<>(new String[]{
            "Случайный цвет", "Красный", "Синий", "Зеленый", 
            "Оранжевый", "Фиолетовый", "Голубой"
        });
        
        // Добавляем несколько начальных кнопок для демонстрации
        addDemoButton();
        addDemoButton();
        addDemoButton();
    }
    
    private void setupLayout() {
        // Добавляем компоненты на панель управления
        controlPanel.add(addButton);
        controlPanel.add(removeButton);
        controlPanel.add(changeLayoutButton);
        controlPanel.add(new JSeparator(SwingConstants.VERTICAL));
        controlPanel.add(infoLabel);
        controlPanel.add(new JSeparator(SwingConstants.VERTICAL));
        controlPanel.add(new JLabel("Цвет:"));
        controlPanel.add(colorComboBox);
        
        // Добавляем панели на главную панель
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(demoPanel, BorderLayout.CENTER);
        
        // Добавляем информационную панель внизу
        JPanel infoPanel = createInfoPanel();
        mainPanel.add(infoPanel, BorderLayout.SOUTH);
        
        // Устанавливаем главную панель
        setContentPane(mainPanel);
    }
    
    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Информация"));
        infoPanel.setBackground(new Color(220, 220, 220));
        
        JLabel layoutInfo = new JLabel(
            "<html><b>BorderLayout</b> - главная панель | <b>FlowLayout</b> - демонстрационная панель<br>" +
            "• BorderLayout размещает компоненты по сторонам (NORTH, SOUTH, EAST, WEST, CENTER)<br>" +
            "• FlowLayout размещает компоненты в строку, перенося на следующую при нехватке места</html>"
        );
        
        infoPanel.add(layoutInfo);
        return infoPanel;
    }
    
    private void setupEventHandlers() {
        addButton.addActionListener(this);
        removeButton.addActionListener(this);
        changeLayoutButton.addActionListener(this);
        colorComboBox.addActionListener(this);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {
            addDemoButton();
        } else if (e.getSource() == removeButton) {
            removeDemoButton();
        } else if (e.getSource() == changeLayoutButton) {
            changeDemoPanelLayout();
        } else if (e.getSource() == colorComboBox) {
            // Обработка выбора цвета
        }
    }
    
    private void addDemoButton() {
        JButton newButton = new JButton("Кнопка " + buttonCounter++);
        
        // Устанавливаем цвет в зависимости от выбора
        int colorIndex = colorComboBox.getSelectedIndex();
        Color buttonColor;
        
        if (colorIndex == 0) {
            // Случайный цвет
            buttonColor = colors[(int)(Math.random() * colors.length)];
        } else {
            buttonColor = colors[colorIndex - 1];
        }
        
        newButton.setBackground(buttonColor);
        newButton.setForeground(Color.BLACK);
        newButton.setOpaque(true);
        newButton.setBorderPainted(false);
        
        // Добавляем действие для кнопки
        newButton.addActionListener(e -> {
            JButton source = (JButton) e.getSource();
            JOptionPane.showMessageDialog(this, 
                "Нажата: " + source.getText() + "\n" +
                "Цвет: " + getColorName(buttonColor),
                "Информация о кнопке", 
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        demoPanel.add(newButton);
        updateButtonCount();
        demoPanel.revalidate();
        demoPanel.repaint();
    }
    
    private void removeDemoButton() {
        int componentCount = demoPanel.getComponentCount();
        if (componentCount > 0) {
            demoPanel.remove(componentCount - 1);
            buttonCounter--;
            updateButtonCount();
            demoPanel.revalidate();
            demoPanel.repaint();
        }
    }
    
    private void changeDemoPanelLayout() {
        // Чередуем разные менеджеры компоновки
        LayoutManager currentLayout = demoPanel.getLayout();
        
        if (currentLayout instanceof FlowLayout) {
            // Меняем на GridLayout
            demoPanel.setLayout(new GridLayout(0, 3, 5, 5));
            demoPanel.setBorder(BorderFactory.createTitledBorder("Демонстрационная панель - GridLayout (3 колонки)"));
        } else if (currentLayout instanceof GridLayout) {
            // Меняем на BoxLayout
            demoPanel.setLayout(new BoxLayout(demoPanel, BoxLayout.Y_AXIS));
            demoPanel.setBorder(BorderFactory.createTitledBorder("Демонстрационная панель - BoxLayout (вертикальный)"));
        } else {
            // Возвращаем к FlowLayout
            demoPanel.setLayout(new FlowLayout());
            demoPanel.setBorder(BorderFactory.createTitledBorder("Демонстрационная панель - FlowLayout"));
        }
        
        demoPanel.revalidate();
        demoPanel.repaint();
    }
    
    private void updateButtonCount() {
        infoLabel.setText("Кнопок: " + demoPanel.getComponentCount());
    }
    
    private String getColorName(Color color) {
        if (color.equals(Color.RED)) return "Красный";
        if (color.equals(Color.BLUE)) return "Синий";
        if (color.equals(Color.GREEN)) return "Зеленый";
        if (color.equals(Color.ORANGE)) return "Оранжевый";
        if (color.equals(Color.MAGENTA)) return "Фиолетовый";
        if (color.equals(Color.CYAN)) return "Голубой";
        if (color.equals(Color.PINK)) return "Розовый";
        if (color.equals(Color.YELLOW)) return "Желтый";
        return "Неизвестный";
    }
    
    public static void main(String[] args) {
        // Устанавливаем системный Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Запускаем приложение
        SwingUtilities.invokeLater(() -> {
            new LayoutManagerDemo().setVisible(true);
        });
    }
}
