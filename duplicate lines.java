import java.io.*;
import java.util.*;
import java.nio.file.*;

public class FileDeduplicator {
    
    /**
     * Удаляет дублирующиеся строки из файла и сохраняет результат
     * @param inputFile исходный файл
     * @param outputFile файл для сохранения результата
     * @return количество удаленных дублирующихся строк
     */
    public static int removeDuplicates(Path inputFile, Path outputFile) throws IOException {
        if (!Files.exists(inputFile)) {
            throw new FileNotFoundException("Исходный файл не найден: " + inputFile);
        }
        
        // Читаем все строки из файла
        List<String> lines = Files.readAllLines(inputFile);
        System.out.println("Прочитано строк из исходного файла: " + lines.size());
        
        // Используем LinkedHashSet для сохранения порядка и удаления дубликатов
        Set<String> uniqueLines = new LinkedHashSet<>(lines);
        int duplicatesRemoved = lines.size() - uniqueLines.size();
        
        // Записываем уникальные строки в выходной файл
        Files.write(outputFile, uniqueLines);
        
        // Добавляем информацию об удаленных дубликатах в конец файла
        String duplicatesInfo = "\n\n=== ИНФОРМАЦИЯ О ДУБЛИКАТАХ ===\n" +
                               "Удалено дублирующихся строк: " + duplicatesRemoved + "\n" +
                               "Осталось уникальных строк: " + uniqueLines.size() + "\n" +
                               "Исходное количество строк: " + lines.size();
        
        Files.write(outputFile, duplicatesInfo.getBytes(), StandardOpenOption.APPEND);
        
        System.out.println("Удалено дублирующихся строк: " + duplicatesRemoved);
        System.out.println("Сохранено уникальных строк: " + uniqueLines.size());
        
        return duplicatesRemoved;
    }
    
    /**
     * Удаляет дублирующиеся строки с дополнительной информацией о каждом дубликате
     */
    public static int removeDuplicatesWithDetails(Path inputFile, Path outputFile) throws IOException {
        if (!Files.exists(inputFile)) {
            throw new FileNotFoundException("Исходный файл не найден: " + inputFile);
        }
        
        List<String> lines = Files.readAllLines(inputFile);
        System.out.println("Прочитано строк из исходного файла: " + lines.size());
        
        // Собираем статистику по дубликатам
        Map<String, Integer> lineCount = new HashMap<>();
        for (String line : lines) {
            lineCount.put(line, lineCount.getOrDefault(line, 0) + 1);
        }
        
        // Фильтруем уникальные строки
        List<String> uniqueLines = new ArrayList<>();
        List<String> duplicateInfo = new ArrayList<>();
        int duplicatesRemoved = 0;
        
        for (String line : lines) {
            if (lineCount.get(line) == 1) {
                uniqueLines.add(line);
            } else {
                duplicatesRemoved++;
                if (lineCount.get(line) > 1 && !duplicateInfo.contains(line)) {
                    duplicateInfo.add("Дубликат: '" + line + "' встречается " + 
                                    lineCount.get(line) + " раз");
                }
            }
        }
        
        // Записываем уникальные строки
        Files.write(outputFile, uniqueLines);
        
        // Добавляем детальную информацию о дубликатах
        List<String> footer = new ArrayList<>();
        footer.add("\n=== ДЕТАЛЬНАЯ ИНФОРМАЦИЯ О ДУБЛИКАТАХ ===");
        footer.add("Удалено дублирующихся строк: " + duplicatesRemoved);
        footer.add("Осталось уникальных строк: " + uniqueLines.size());
        footer.add("Исходное количество строк: " + lines.size());
        footer.add("\nПовторяющиеся строки:");
        footer.addAll(duplicateInfo);
        
        Files.write(outputFile, footer, StandardOpenOption.APPEND);
        
        System.out.println("Удалено дублирующихся строк: " + duplicatesRemoved);
        System.out.println("Найдено " + duplicateInfo.size() + " различных дублирующихся строк");
        
        return duplicatesRemoved;
    }
}
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

public class FileCompressionManager {
    
    /**
     * Создает сжатую версию файла с информацией для восстановления
     */
    public static void createCompressedVersion(Path inputFile, Path compressedFile) throws IOException {
        if (!Files.exists(inputFile)) {
            throw new FileNotFoundException("Исходный файл не найден: " + inputFile);
        }
        
        List<String> lines = Files.readAllLines(inputFile);
        
        // Создаем карту для замены дубликатов на идентификаторы
        Map<String, Integer> lineToId = new HashMap<>();
        List<String> uniqueLines = new ArrayList<>();
        List<Integer> compressedLines = new ArrayList<>();
        int nextId = 1;
        
        // Сжимаем данные
        for (String line : lines) {
            if (!lineToId.containsKey(line)) {
                lineToId.put(line, nextId);
                uniqueLines.add(line);
                nextId++;
            }
            compressedLines.add(lineToId.get(line));
        }
        
        // Сохраняем сжатую версию
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(compressedFile))) {
            // Заголовок с метаданными
            writer.println("=== СЖАТАЯ ВЕРСИЯ ФАЙЛА ===");
            writer.println("Исходный файл: " + inputFile.getFileName());
            writer.println("Общее количество строк: " + lines.size());
            writer.println("Уникальных строк: " + uniqueLines.size());
            writer.println("Коэффициент сжатия: " + 
                String.format("%.2f", (double) lines.size() / uniqueLines.size()));
            writer.println("=== ДАННЫЕ ===");
            
            // Сохраняем уникальные строки с идентификаторами
            writer.println("[УНИКАЛЬНЫЕ_СТРОКИ]");
            for (int i = 0; i < uniqueLines.size(); i++) {
                writer.println((i + 1) + ":" + uniqueLines.get(i));
            }
            
            // Сохраняем сжатые данные
            writer.println("[СЖАТЫЕ_ДАННЫЕ]");
            for (int i = 0; i < compressedLines.size(); i++) {
                writer.print(compressedLines.get(i));
                if (i < compressedLines.size() - 1) {
                    writer.print(",");
                }
            }
            writer.println();
        }
        
        System.out.println("Создана сжатая версия файла:");
        System.out.println("  Исходных строк: " + lines.size());
        System.out.println("  Уникальных строк: " + uniqueLines.size());
        System.out.println("  Коэффициент сжатия: " + 
            String.format("%.2f", (double) lines.size() / uniqueLines.size()));
    }
    
    /**
     * Восстанавливает полную версию файла из сжатой
     */
    public static void restoreFromCompressed(Path compressedFile, Path outputFile) throws IOException {
        if (!Files.exists(compressedFile)) {
            throw new FileNotFoundException("Сжатый файл не найден: " + compressedFile);
        }
        
        List<String> compressedLines = Files.readAllLines(compressedFile);
        Map<Integer, String> idToLine = new HashMap<>();
        List<Integer> lineIds = new ArrayList<>();
        boolean readingUniqueLines = false;
        boolean readingCompressedData = false;
        
        // Парсим сжатый файл
        for (String line : compressedLines) {
            if (line.equals("[УНИКАЛЬНЫЕ_СТРОКИ]")) {
                readingUniqueLines = true;
                readingCompressedData = false;
                continue;
            } else if (line.equals("[СЖАТЫЕ_ДАННЫЕ]")) {
                readingUniqueLines = false;
                readingCompressedData = true;
                continue;
            } else if (line.startsWith("===") || line.isEmpty()) {
                continue;
            }
            
            if (readingUniqueLines) {
                // Парсим строки вида "1:текст строки"
                int separatorIndex = line.indexOf(':');
                if (separatorIndex > 0) {
                    try {
                        int id = Integer.parseInt(line.substring(0, separatorIndex));
                        String text = line.substring(separatorIndex + 1);
                        idToLine.put(id, text);
                    } catch (NumberFormatException e) {
                        System.err.println("Ошибка парсинга ID: " + line);
                    }
                }
            } else if (readingCompressedData) {
                // Парсим сжатые данные
                String[] ids = line.split(",");
                for (String idStr : ids) {
                    try {
                        lineIds.add(Integer.parseInt(idStr.trim()));
                    } catch (NumberFormatException e) {
                        System.err.println("Ошибка парсинга ID данных: " + idStr);
                    }
                }
            }
        }
        
        // Восстанавливаем оригинальные строки
        List<String> restoredLines = new ArrayList<>();
        for (int id : lineIds) {
            String originalLine = idToLine.get(id);
            if (originalLine != null) {
                restoredLines.add(originalLine);
            } else {
                System.err.println("Предупреждение: не найден ID " + id);
                restoredLines.add("??? ВОССТАНОВЛЕНИЕ_ОШИБКИ ???");
            }
        }
        
        // Сохраняем восстановленный файл
        Files.write(outputFile, restoredLines);
        
        System.out.println("Восстановлен файл из сжатой версии:");
        System.out.println("  Восстановлено строк: " + restoredLines.size());
        System.out.println("  Уникальных строк в словаре: " + idToLine.size());
    }
}
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileProcessor {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== ОБРАБОТКА ФАЙЛОВ: УДАЛЕНИЕ ДУБЛИКАТОВ И СЖАТИЕ ===\n");
        
        // Создаем тестовый файл с дубликатами
        createTestFile("test_input.txt");
        
        while (true) {
            System.out.println("\nВыберите операцию:");
            System.out.println("1. Удалить дублирующиеся строки из файла");
            System.out.println("2. Удалить дубликаты с детальной информацией");
            System.out.println("3. Создать сжатую версию файла");
            System.out.println("4. Восстановить файл из сжатой версии");
            System.out.println("5. Сравнить файлы");
            System.out.println("6. Выход");
            System.out.print("Ваш выбор: ");
            
            try {
                int choice = scanner.nextInt();
                scanner.nextLine(); // consume newline
                
                switch (choice) {
                    case 1:
                        processRemoveDuplicates(scanner, false);
                        break;
                    case 2:
                        processRemoveDuplicates(scanner, true);
                        break;
                    case 3:
                        processCompression(scanner);
                        break;
                    case 4:
                        processRestoration(scanner);
                        break;
                    case 5:
                        processComparison(scanner);
                        break;
                    case 6:
                        System.out.println("Программа завершена.");
                        return;
                    default:
                        System.out.println("Неверный выбор. Попробуйте снова.");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
                scanner.nextLine(); // очистка буфера
            }
        }
    }
    
    private static void processRemoveDuplicates(Scanner scanner, boolean detailed) {
        System.out.print("Введите путь к исходному файлу: ");
        String inputPath = scanner.nextLine();
        System.out.print("Введите путь для сохранения результата: ");
        String outputPath = scanner.nextLine();
        
        try {
            int duplicatesRemoved;
            if (detailed) {
                duplicatesRemoved = FileDeduplicator.removeDuplicatesWithDetails(
                    Paths.get(inputPath), Paths.get(outputPath));
            } else {
                duplicatesRemoved = FileDeduplicator.removeDuplicates(
                    Paths.get(inputPath), Paths.get(outputPath));
            }
            
            System.out.println("✅ Операция завершена успешно!");
            System.out.println("Удалено дубликатов: " + duplicatesRemoved);
            
        } catch (IOException e) {
            System.out.println("❌ Ошибка при обработке файла: " + e.getMessage());
        }
    }
    
    private static void processCompression(Scanner scanner) {
        System.out.print("Введите путь к исходному файлу: ");
        String inputPath = scanner.nextLine();
        System.out.print("Введите путь для сжатой версии: ");
        String compressedPath = scanner.nextLine();
        
        try {
            FileCompressionManager.createCompressedVersion(
                Paths.get(inputPath), Paths.get(compressedPath));
            System.out.println("✅ Сжатая версия создана успешно!");
            
        } catch (IOException e) {
            System.out.println("❌ Ошибка при сжатии файла: " + e.getMessage());
        }
    }
    
    private static void processRestoration(Scanner scanner) {
        System.out.print("Введите путь к сжатому файлу: ");
        String compressedPath = scanner.nextLine();
        System.out.print("Введите путь для восстановленного файла: ");
        String outputPath = scanner.nextLine();
        
        try {
            FileCompressionManager.restoreFromCompressed(
                Paths.get(compressedPath), Paths.get(outputPath));
            System.out.println("✅ Файл восстановлен успешно!");
            
        } catch (IOException e) {
            System.out.println("❌ Ошибка при восстановлении файла: " + e.getMessage());
        }
    }
    
    private static void processComparison(Scanner scanner) {
        System.out.print("Введите путь к первому файлу: ");
        String file1Path = scanner.nextLine();
        System.out.print("Введите путь ко второму файлу: ");
        String file2Path = scanner.nextLine();
        
        try {
            compareFiles(Paths.get(file1Path), Paths.get(file2Path));
        } catch (IOException e) {
            System.out.println("❌ Ошибка при сравнении файлов: " + e.getMessage());
        }
    }
    
    private static void compareFiles(Path file1, Path file2) throws IOException {
        if (!Files.exists(file1) || !Files.exists(file2)) {
            throw new FileNotFoundException("Один из файлов не найден");
        }
        
        List<String> lines1 = Files.readAllLines(file1);
        List<String> lines2 = Files.readAllLines(file2);
        
        System.out.println("\n=== СРАВНЕНИЕ ФАЙЛОВ ===");
        System.out.println("Файл 1: " + file1.getFileName() + " (" + lines1.size() + " строк)");
        System.out.println("Файл 2: " + file2.getFileName() + " (" + lines2.size() + " строк)");
        
        if (lines1.equals(lines2)) {
            System.out.println("✅ Файлы идентичны!");
        } else {
            System.out.println("❌ Файлы различаются!");
            
            // Находим различия
            int minSize = Math.min(lines1.size(), lines2.size());
            for (int i = 0; i < minSize; i++) {
                if (!lines1.get(i).equals(lines2.get(i))) {
                    System.out.println("Различие в строке " + (i + 1) + ":");
                    System.out.println("  Файл 1: " + lines1.get(i));
                    System.out.println("  Файл 2: " + lines2.get(i));
                    break;
                }
            }
            
            if (lines1.size() != lines2.size()) {
                System.out.println("Разное количество строк:");
                System.out.println("  Файл 1: " + lines1.size() + " строк");
                System.out.println("  Файл 2: " + lines2.size() + " строк");
            }
        }
    }
    
    /**
     * Создает тестовый файл с дублирующимися строками
     */
    private static void createTestFile(String filename) {
        List<String> testLines = Arrays.asList(
            "Первая строка",
            "Вторая строка",
            "Третья строка",
            "Первая строка", // дубликат
            "Четвертая строка",
            "Вторая строка", // дубликат
            "Пятая строка",
            "Первая строка", // дубликат
            "Шестая строка",
            "Третья строка"  // дубликат
        );
        
        try {
            Files.write(Paths.get(filename), testLines);
            System.out.println("Создан тестовый файл: " + filename);
            System.out.println("Содержит " + testLines.size() + " строк, из них 4 дубликата");
        } catch (IOException e) {
            System.out.println("Ошибка при создании тестового файла: " + e.getMessage());
        }
    }
}
