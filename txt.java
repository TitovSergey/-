import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;

public class TextEditor extends JFrame implements ActionListener {
    
    // Компоненты интерфейса
    private JTextArea textArea;
    private JScrollPane scrollPane;
    private JMenuBar menuBar;
    private JMenu fileMenu, editMenu, helpMenu;
    private JMenuItem newItem, openItem, saveItem, saveAsItem, exitItem;
    private JMenuItem cutItem, copyItem, pasteItem, selectAllItem;
    private JMenuItem aboutItem;
    
    // Файловые операции
    private JFileChooser fileChooser;
    private File currentFile;
    
    // Константы
    private final String APP_TITLE = "Текстовый редактор";
    private final Font TEXT_FONT = new Font("Arial", Font.PLAIN, 14);
    
    public TextEditor() {
        initializeUI();
        setupMenuBar();
        setupEventHandlers();
    }
    
    private void initializeUI() {
        setTitle(APP_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null); // Центрирование окна
        
        // Создание текстовой области
        textArea = new JTextArea();
        textArea.setFont(TEXT_FONT);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(10, 10, 10, 10));
        
        // Добавление скроллпанели
        scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // Настройка файлового диалога
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Текстовые файлы (*.txt)", "txt"));
        
        // Добавление компонентов на форму
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void setupMenuBar() {
        menuBar = new JMenuBar();
        
        // Меню "Файл"
        fileMenu = new JMenu("Файл");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        
        newItem = new JMenuItem("Новый");
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
        
        openItem = new JMenuItem("Открыть...");
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        
        saveItem = new JMenuItem("Сохранить");
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        
        saveAsItem = new JMenuItem("Сохранить как...");
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 
            KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
        
        exitItem = new JMenuItem("Выход");
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));
        
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Меню "Правка"
        editMenu = new JMenu("Правка");
        editMenu.setMnemonic(KeyEvent.VK_E);
        
        cutItem = new JMenuItem("Вырезать");
        cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK));
        
        copyItem = new JMenuItem("Копировать");
        copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK));
        
        pasteItem = new JMenuItem("Вставить");
        pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK));
        
        selectAllItem = new JMenuItem("Выделить все");
        selectAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK));
        
        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);
        editMenu.addSeparator();
        editMenu.add(selectAllItem);
        
        // Меню "Справка"
        helpMenu = new JMenu("Справка");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        
        aboutItem = new JMenuItem("О программе");
        aboutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        
        helpMenu.add(aboutItem);
        
        // Добавление меню в менюбар
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void setupEventHandlers() {
        // Обработчики меню "Файл"
        newItem.addActionListener(this);
        openItem.addActionListener(this);
        saveItem.addActionListener(this);
        saveAsItem.addActionListener(this);
        exitItem.addActionListener(this);
        
        // Обработчики меню "Правка"
        cutItem.addActionListener(this);
        copyItem.addActionListener(this);
        pasteItem.addActionListener(this);
        selectAllItem.addActionListener(this);
        
        // Обработчики меню "Справка"
        aboutItem.addActionListener(this);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        // Меню "Файл"
        if (source == newItem) {
            newFile();
        } else if (source == openItem) {
            openFile();
        } else if (source == saveItem) {
            saveFile();
        } else if (source == saveAsItem) {
            saveFileAs();
        } else if (source == exitItem) {
            exitApplication();
        }
        
        // Меню "Правка"
        else if (source == cutItem) {
            textArea.cut();
        } else if (source == copyItem) {
            textArea.copy();
        } else if (source == pasteItem) {
            textArea.paste();
        } else if (source == selectAllItem) {
            textArea.selectAll();
        }
        
        // Меню "Справка"
        else if (source == aboutItem) {
            showAboutDialog();
        }
    }
    
    private void newFile() {
        if (checkUnsavedChanges()) {
            textArea.setText("");
            currentFile = null;
            updateTitle();
        }
    }
    
    private void openFile() {
        if (checkUnsavedChanges()) {
            int returnValue = fileChooser.showOpenDialog(this);
            
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                loadFile(selectedFile);
            }
        }
    }
    
    private void loadFile(File file) {
        try (FileReader reader = new FileReader(file);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            
            StringBuilder content = new StringBuilder();
            String line;
            
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line).append("\n");
            }
            
            textArea.setText(content.toString());
            currentFile = file;
            updateTitle();
            
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Ошибка при открытии файла: " + ex.getMessage(),
                "Ошибка",
                JOptionPane.ERROR_MESSAGE);
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
        int returnValue = fileChooser.showSaveDialog(this);
        
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // Добавляем расширение .txt если его нет
            if (!selectedFile.getName().toLowerCase().endsWith(".txt")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".txt");
            }
            
            // Проверяем, существует ли файл
            if (selectedFile.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(this,
                    "Файл уже существует. Перезаписать?",
                    "Подтверждение",
                    JOptionPane.YES_NO_OPTION);
                
                if (overwrite != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            saveToFile(selectedFile);
        }
    }
    
    private void saveToFile(File file) {
        try (FileWriter writer = new FileWriter(file);
             BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
            
            bufferedWriter.write(textArea.getText());
            currentFile = file;
            updateTitle();
            
            JOptionPane.showMessageDialog(this,
                "Файл успешно сохранен!",
                "Сохранение",
                JOptionPane.INFORMATION_MESSAGE);
            
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Ошибка при сохранении файла: " + ex.getMessage(),
                "Ошибка",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void exitApplication() {
        if (checkUnsavedChanges()) {
            System.exit(0);
        }
    }
    
    private boolean checkUnsavedChanges() {
        if (textArea.getText().trim().isEmpty()) {
            return true;
        }
        
        if (currentFile == null && !textArea.getText().isEmpty()) {
            int result = JOptionPane.showConfirmDialog(this,
                "Сохранить изменения в новом файле?",
                "Несохраненные изменения",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (result == JOptionPane.YES_OPTION) {
                saveFileAs();
                return true;
            } else if (result == JOptionPane.NO_OPTION) {
                return true;
            } else {
                return false;
            }
        }
        
        return true;
    }
    
    private void updateTitle() {
        String title = APP_TITLE;
        if (currentFile != null) {
            title += " - " + currentFile.getName();
        }
        if (!textArea.getText().isEmpty()) {
            title += " *";
        }
        setTitle(title);
    }
    
    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
            "Текстовый редактор\n\n" +
            "Версия 1.0\n" +
            "Простой текстовый редактор с базовыми функциями\n\n" +
            "Реализованные возможности:\n" +
            "• Создание, открытие и сохранение текстовых файлов\n" +
            "• Стандартные операции редактирования\n" +
            "• Поддержка горячих клавиш",
            "О программе",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void main(String[] args) {
        // Установка системного Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Запуск приложения в Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            TextEditor editor = new TextEditor();
            editor.setVisible(true);
        });
    }
}
