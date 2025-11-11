import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import.util.*;
import.util.concurrent.Executors;
import.util.concurrent.ScheduledExecutorService;
import.util.concurrent.TimeUnit;

public class TextEditorWithAutoCorrect extends JFrame {
    
    private JTextArea textArea;
    private JScrollPane scrollPane;
    private JMenuBar menuBar;
    private JToolBar toolBar;
    private JLabel statusBar;
    
    // Меню
    private JMenu fileMenu, editMenu, viewMenu, helpMenu;
    private JMenuItem newItem, openItem, saveItem, saveAsItem, exitItem;
    private JMenuItem cutItem, copyItem, pasteItem, selectAllItem, findItem;
    private JMenuItem wordWrapItem, fontSizeItem;
    private JMenuItem aboutItem;
    
    // Файловые операции
    private JFileChooser fileChooser;
    private File currentFile;
    
    // Настройки автозамены
    private JCheckBoxMenuItem autoCorrectItem;
    private boolean autoCorrectEnabled = true;
    private ScheduledExecutorService scheduler;
    private Timer spaceTimer;
    private boolean spacePressed = false;
    
    // Словарь автозамены
    private Map<String, String> autoCorrectMap;
    
    public TextEditorWithAutoCorrect() {
        initializeAutoCorrect();
        initializeUI();
        createMenuBar();
        createToolBar();
        createStatusBar();
        setupEventHandlers();
        startAutoCorrectScheduler();
    }
    
    private void initializeAutoCorrect() {
        // Инициализация словаря автозамены
        autoCorrectMap = new HashMap<>();
        
        // Примеры распространенных опечаток и их исправлений
        autoCorrectMap.put("првиет", "привет");
        autoCorrectMap.put("здраствуйте", "здравствуйте");
        autoCorrectMap.put("извеняюсь", "извиняюсь");
        autoCorrectMap.put("симпотичный", "симпатичный");
        autoCorrectMap.put("агенство", "агентство");
        autoCorrectMap.put("достоен", "достоин");
        autoCorrectMap.put("зделать", "сделать");
        autoCorrectMap.put("здесь", "здесь");
        autoCorrectMap.put("интиресный", "интересный");
        autoCorrectMap.put("координально", "кардинально");
        autoCorrectMap.put("магазин", "магазин");
        autoCorrectMap.put("непрвычно", "непривычно");
        autoCorrectMap.put("обеденый", "обеденный");
        autoCorrectMap.put("похожу", "похоже");
        autoCorrectMap.put("превет", "привет");
        autoCorrectMap.put("скучно", "скучно");
        autoCorrectMap.put("сдесь", "здесь");
        autoCorrectMap.put("через", "через");
        autoCorrectMap.put("щяс", "сейчас");
        autoCorrectMap.put("язык", "язык");
        
        // Таймер для отслеживания нажатия пробела
        spaceTimer = new Timer(500, e -> {
            if (spacePressed) {
                spacePressed = false;
                scheduleAutoCorrect();
            }
        });
        spaceTimer.setRepeats(false);
    }
    
    private void initializeUI() {
        setTitle("Текстовый редактор с автозаменой");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        
        // Текстовая область
        textArea = new JTextArea();
        textArea.setFont(new Font("Arial", Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(10, 10, 10, 10));
        
        // Добавление слушателя клавиш для отслеживания пробела
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE && autoCorrectEnabled) {
                    spacePressed = true;
                    spaceTimer.restart();
                }
            }
        });
        
        // Добавление слушателя для обновления статусбара
        textArea.addCaretListener(e -> updateStatusBar());
        
        // Скроллпанель
        scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // Файловый диалог
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Текстовые файлы (*.txt)", "txt"));
        
        // Главная панель
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        setContentPane(mainPanel);
    }
    
    private void createMenuBar() {
        menuBar = new JMenuBar();
        
        // Меню Файл
        fileMenu = new JMenu("Файл");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        
        newItem = createMenuItem("Новый", "NEW", KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK);
        openItem = createMenuItem("Открыть...", "OPEN", KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK);
        saveItem = createMenuItem("Сохранить", "SAVE", KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);
        saveAsItem = createMenuItem("Сохранить как...", "SAVE_AS", KeyEvent.VK_S, 
            KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        exitItem = createMenuItem("Выход", "EXIT", KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK);
        
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Меню Правка
        editMenu = new JMenu("Правка");
        editMenu.setMnemonic(KeyEvent.VK_E);
        
        cutItem = createMenuItem("Вырезать", "CUT", KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK);
        copyItem = createMenuItem("Копировать", "COPY", KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK);
        pasteItem = createMenuItem("Вставить", "PASTE", KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK);
        selectAllItem = createMenuItem("Выделить все", "SELECT_ALL", KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK);
        findItem = createMenuItem("Найти...", "FIND", KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK);
        
        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);
        editMenu.addSeparator();
        editMenu.add(selectAllItem);
        editMenu.addSeparator();
        editMenu.add(findItem);
        
        // Меню Вид
        viewMenu = new JMenu("Вид");
        viewMenu.setMnemonic(KeyEvent.VK_V);
        
        wordWrapItem = createCheckBoxMenuItem("Перенос слов", "WORD_WRAP");
        wordWrapItem.setSelected(true);
        
        autoCorrectItem = createCheckBoxMenuItem("Автозамена", "AUTO_CORRECT");
        autoCorrectItem.setSelected(autoCorrectEnabled);
        
        fontSizeItem = createMenuItem("Размер шрифта...", "FONT_SIZE", 0, 0);
        
        viewMenu.add(wordWrapItem);
        viewMenu.add(autoCorrectItem);
        viewMenu.addSeparator();
        viewMenu.add(fontSizeItem);
        
        // Меню Справка
        helpMenu = new JMenu("Справка");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        
        aboutItem = createMenuItem("О программе", "ABOUT", KeyEvent.VK_F1, 0);
        
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void createToolBar() {
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        JButton newButton = createToolBarButton("Новый", "NEW", "Создать новый файл (Ctrl+N)");
        JButton openButton = createToolBarButton("Открыть", "OPEN", "Открыть файл (Ctrl+O)");
        JButton saveButton = createToolBarButton("Сохранить", "SAVE", "Сохранить файл (Ctrl+S)");
        
        toolBar.add(newButton);
        toolBar.add(openButton);
        toolBar.add(saveButton);
        toolBar.addSeparator();
        
        JButton cutButton = createToolBarButton("Вырезать", "CUT", "Вырезать (Ctrl+X)");
        JButton copyButton = createToolBarButton("Копировать", "COPY", "Копировать (Ctrl+C)");
        JButton pasteButton = createToolBarButton("Вставить", "PASTE", "Вставить (Ctrl+V)");
        
        toolBar.add(cutButton);
        toolBar.add(copyButton);
        toolBar.add(pasteButton);
        toolBar.addSeparator();
        
        // Кнопка для принудительной проверки автозамены
        JButton autoCorrectButton = createToolBarButton("Проверить", "CHECK_SPELLING", "Проверить орфографию");
        toolBar.add(autoCorrectButton);
        
        add(toolBar, BorderLayout.NORTH);
    }
    
    private void createStatusBar() {
        statusBar = new JLabel(" Готов | Автозамена: ВКЛ ");
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        
        add(statusBar, BorderLayout.SOUTH);
    }
    
    private JMenuItem createMenuItem(String text, String actionCommand, int key, int modifiers) {
        JMenuItem item = new JMenuItem(text);
        item.setActionCommand(actionCommand);
        if (key != 0) {
            item.setAccelerator(KeyStroke.getKeyStroke(key, modifiers));
        }
        return item;
    }
    
    private JCheckBoxMenuItem createCheckBoxMenuItem(String text, String actionCommand) {
        JCheckBoxMenuItem item = new JCheckBoxMenuItem(text);
        item.setActionCommand(actionCommand);
        return item;
    }
    
    private JButton createToolBarButton(String text, String actionCommand, String toolTip) {
        JButton button = new JButton(text);
        button.setActionCommand(actionCommand);
        button.setToolTipText(toolTip);
        button.addActionListener(this::handleAction);
        return button;
    }
    
    private void setupEventHandlers() {
        ActionListener menuHandler = this::handleAction;
        
        // Файл
        newItem.addActionListener(menuHandler);
        openItem.addActionListener(menuHandler);
        saveItem.addActionListener(menuHandler);
        saveAsItem.addActionListener(menuHandler);
        exitItem.addActionListener(menuHandler);
        
        // Правка
        cutItem.addActionListener(menuHandler);
        copyItem.addActionListener(menuHandler);
        pasteItem.addActionListener(menuHandler);
        selectAllItem.addActionListener(menuHandler);
        findItem.addActionListener(menuHandler);
        
        // Вид
        wordWrapItem.addActionListener(menuHandler);
        autoCorrectItem.addActionListener(menuHandler);
        fontSizeItem.addActionListener(menuHandler);
        
        // Справка
        aboutItem.addActionListener(menuHandler);
    }
    
    private void handleAction(ActionEvent e) {
        String command = e.getActionCommand();
        
        switch (command) {
            case "NEW": newFile(); break;
            case "OPEN": openFile(); break;
            case "SAVE": saveFile(); break;
            case "SAVE_AS": saveFileAs(); break;
            case "EXIT": exitApplication(); break;
            case "CUT": textArea.cut(); break;
            case "COPY": textArea.copy(); break;
            case "PASTE": textArea.paste(); break;
            case "SELECT_ALL": textArea.selectAll(); break;
            case "FIND": findText(); break;
            case "WORD_WRAP": toggleWordWrap(); break;
            case "AUTO_CORRECT": toggleAutoCorrect(); break;
            case "FONT_SIZE": changeFontSize(); break;
            case "CHECK_SPELLING": performAutoCorrect(); break;
            case "ABOUT": showAbout(); break;
        }
    }
    
    private void startAutoCorrectScheduler() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        // Периодическая проверка каждые 5 секунд
        scheduler.scheduleAtFixedRate(this::performAutoCorrect, 5, 5, TimeUnit.SECONDS);
    }
    
    private void scheduleAutoCorrect() {
        if (autoCorrectEnabled) {
            // Запускаем проверку в отдельном потоке с небольшой задержкой
            new Thread(() -> {
                try {
                    Thread.sleep(100); // Небольшая задержка для завершения ввода
                    SwingUtilities.invokeLater(this::performAutoCorrect);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }
    
    private void performAutoCorrect() {
        if (!autoCorrectEnabled || textArea.getText().isEmpty()) {
            return;
        }
        
        String text = textArea.getText();
        String originalText = text;
        StringBuilder correctedText = new StringBuilder(text);
        int corrections = 0;
        
        // Разбиваем текст на слова
        String[] words = text.split("\\s+");
        int currentPos = 0;
        
        for (String word : words) {
            // Очищаем слово от знаков препинания
            String cleanWord = word.replaceAll("[^\\p{L}]", "").toLowerCase();
            
            if (autoCorrectMap.containsKey(cleanWord)) {
                String correctWord = autoCorrectMap.get(cleanWord);
                int wordStart = text.indexOf(word, currentPos);
                
                if (wordStart != -1) {
                    correctedText.replace(wordStart, wordStart + word.length(), correctWord);
                    corrections++;
                    currentPos = wordStart + correctWord.length();
                }
            } else {
                currentPos += word.length() + 1; // +1 для пробела
            }
        }
        
        // Применяем исправления, если они есть
        if (corrections > 0 && !correctedText.toString().equals(originalText)) {
            int caretPosition = textArea.getCaretPosition();
            textArea.setText(correctedText.toString());
            textArea.setCaretPosition(Math.min(caretPosition, correctedText.length()));
            
            // Показываем уведомление
            if (corrections == 1) {
                statusBar.setText(" Исправлено 1 слово ");
            } else {
                statusBar.setText(" Исправлено " + corrections + " слов ");
            }
            
            // Восстанавливаем стандартный статус через 2 секунды
            new Timer(2000, e -> {
                updateStatusBar();
                ((Timer)e.getSource()).stop();
            }).start();
        }
    }
    
    private void toggleAutoCorrect() {
        autoCorrectEnabled = autoCorrectItem.isSelected();
        if (autoCorrectEnabled) {
            statusBar.setText(" Автозамена: ВКЛ ");
        } else {
            statusBar.setText(" Автозамена: ВЫКЛ ");
        }
    }
    
    private void newFile() {
        if (confirmUnsavedChanges()) {
            textArea.setText("");
            currentFile = null;
            updateTitle();
            updateStatusBar();
        }
    }
    
    private void openFile() {
        if (confirmUnsavedChanges()) {
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                loadFile(fileChooser.getSelectedFile());
            }
        }
    }
    
    private void loadFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder content = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            
            textArea.setText(content.toString());
            currentFile = file;
            updateTitle();
            updateStatusBar();
            
        } catch (IOException ex) {
            showError("Ошибка открытия файла", ex.getMessage());
        }
    }
    
    private void saveFile() {
        if (currentFile == null) {
            saveFileAs();
        } else {
            saveToFile(currentFile);
        }
    }
    
    private void saveFileAs() {
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".txt")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }
            
            if (file.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(this,
                    "Файл существует. Перезаписать?", "Подтверждение", 
                    JOptionPane.YES_NO_OPTION);
                if (overwrite != JOptionPane.YES_OPTION) return;
            }
            
            saveToFile(file);
        }
    }
    
    private void saveToFile(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(textArea.getText());
            currentFile = file;
            updateTitle();
            
            JOptionPane.showMessageDialog(this, "Файл сохранен успешно!", 
                "Сохранение", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (IOException ex) {
            showError("Ошибка сохранения файла", ex.getMessage());
        }
    }
    
    private void exitApplication() {
        if (confirmUnsavedChanges()) {
            scheduler.shutdown();
            System.exit(0);
        }
    }
    
    private boolean confirmUnsavedChanges() {
        if (textArea.getText().isEmpty()) return true;
        
        if (currentFile == null || hasUnsavedChanges()) {
            int result = JOptionPane.showConfirmDialog(this,
                "Сохранить изменения?", "Несохраненные изменения",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (result == JOptionPane.YES_OPTION) {
                saveFile();
                return true;
            } else if (result == JOptionPane.NO_OPTION) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }
    
    private boolean hasUnsavedChanges() {
        if (currentFile == null) return !textArea.getText().isEmpty();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(currentFile))) {
            StringBuilder fileContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line).append("\n");
            }
            return !fileContent.toString().equals(textArea.getText());
        } catch (IOException e) {
            return true;
        }
    }
    
    private void findText() {
        String search = JOptionPane.showInputDialog(this, "Введите текст для поиска:", "Поиск", 
            JOptionPane.QUESTION_MESSAGE);
        
        if (search != null && !search.isEmpty()) {
            String text = textArea.getText();
            int index = text.indexOf(search);
            
            if (index >= 0) {
                textArea.setCaretPosition(index);
                textArea.select(index, index + search.length());
                textArea.grabFocus();
            } else {
                JOptionPane.showMessageDialog(this, "Текст не найден", "Поиск", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    private void toggleWordWrap() {
        boolean wrap = wordWrapItem.isSelected();
        textArea.setLineWrap(wrap);
        textArea.setWrapStyleWord(wrap);
    }
    
    private void changeFontSize() {
        String size = JOptionPane.showInputDialog(this, "Введите размер шрифта:", "Размер шрифта", 
            JOptionPane.QUESTION_MESSAGE);
        
        if (size != null) {
            try {
                int fontSize = Integer.parseInt(size);
                textArea.setFont(new Font("Arial", Font.PLAIN, fontSize));
            } catch (NumberFormatException e) {
                showError("Ошибка", "Неверный размер шрифта");
            }
        }
    }
    
    private void updateTitle() {
        String title = "Текстовый редактор с автозаменой";
        if (currentFile != null) {
            title += " - " + currentFile.getName();
        }
        if (hasUnsavedChanges()) {
            title += " *";
        }
        setTitle(title);
    }
    
    private void updateStatusBar() {
        int caretPos = textArea.getCaretPosition();
        int line = 1;
        int column = 1;
        
        try {
            line = textArea.getLineOfOffset(caretPos) + 1;
            int lineStart = textArea.getLineStartOffset(line - 1);
            column = caretPos - lineStart + 1;
        } catch (Exception e) {
            // Игнорируем ошибки позиционирования
        }
        
        String text = textArea.getText();
        int chars = text.length();
        int words = text.isEmpty() ? 0 : text.trim().split("\\s+").length;
        
        String autoCorrectStatus = autoCorrectEnabled ? "Автозамена: ВКЛ" : "Автозамена: ВЫКЛ";
        
        statusBar.setText(String.format(" Строка: %d, Колонка: %d | Символов: %d | Слов: %d | %s ", 
            line, column, chars, words, autoCorrectStatus));
    }
    
    private void showAbout() {
        JOptionPane.showMessageDialog(this,
            "<html><center><b>Текстовый редактор с автозаменой</b><br>" +
            "Версия 2.0<br><br>" +
            "Функции:<br>" +
            "• Редактирование текста<br>" +
            "• Работа с файлами<br>" +
            "• Автоматическая коррекция орфографии<br>" +
            "• Проверка по нажатию пробела и по таймеру<br>" +
            "• Отключение автозамены</center></html>",
            "О программе",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }
    
    @Override
    public void dispose() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
        if (spaceTimer != null) {
            spaceTimer.stop();
        }
        super.dispose();
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new TextEditorWithAutoCorrect().setVisible(true);
        });
    }
}
