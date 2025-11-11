import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Stack;

public class XMLParser extends JFrame implements ActionListener {
    
    // Компоненты интерфейса
    private JTree xmlTree;
    private DefaultTreeModel treeModel;
    private JScrollPane treeScrollPane;
    private JTextArea contentArea;
    private JScrollPane contentScrollPane;
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenuItem openItem, exitItem;
    private JFileChooser fileChooser;
    
    public XMLParser() {
        initializeUI();
        setupMenu();
        setupLayout();
    }
    
    private void initializeUI() {
        setTitle("XML Parser with JTree");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        
        // Создаем корневой узел для дерева
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("XML Structure");
        treeModel = new DefaultTreeModel(root);
        xmlTree = new JTree(treeModel);
        
        // Настраиваем дерево
        xmlTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        xmlTree.setShowsRootHandles(true);
        xmlTree.setExpandsSelectedPaths(true);
        
        // Добавляем слушатель для отображения содержимого
        xmlTree.addTreeSelectionListener(e -> displayNodeContent());
        
        treeScrollPane = new JScrollPane(xmlTree);
        treeScrollPane.setPreferredSize(new Dimension(300, 0));
        
        // Область для отображения содержимого
        contentArea = new JTextArea();
        contentArea.setEditable(false);
        contentArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        contentArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentScrollPane = new JScrollPane(contentArea);
        
        // Файловый диалог
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("XML Files", "xml"));
    }
    
    private void setupMenu() {
        menuBar = new JMenuBar();
        
        fileMenu = new JMenu("File");
        openItem = new JMenuItem("Open XML File");
        openItem.addActionListener(this);
        openItem.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
        
        exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(this);
        exitItem.setAccelerator(KeyStroke.getKeyStroke("ctrl Q"));
        
        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }
    
    private void setupLayout() {
        // Используем BorderLayout для основного расположения
        setLayout(new BorderLayout());
        
        // Создаем split pane для разделения дерева и содержимого
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
                                            treeScrollPane, contentScrollPane);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.3);
        
        add(splitPane, BorderLayout.CENTER);
        
        // Добавляем статус бар
        JLabel statusBar = new JLabel(" Ready to open XML file ");
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        add(statusBar, BorderLayout.SOUTH);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == openItem) {
            openXMLFile();
        } else if (e.getSource() == exitItem) {
            System.exit(0);
        }
    }
    
    private void openXMLFile() {
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            parseXMLFile(selectedFile);
        }
    }
    
    private void parseXMLFile(File file) {
        try {
            String xmlContent = readFile(file);
            DefaultMutableTreeNode rootNode = parseXML(xmlContent);
            
            // Обновляем модель дерева
            treeModel.setRoot(rootNode);
            
            // Разворачиваем все узлы
            expandAllNodes(xmlTree, 0, xmlTree.getRowCount());
            
            setTitle("XML Parser - " + file.getName());
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Error reading file: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error parsing XML: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String readFile(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
    
    private DefaultMutableTreeNode parseXML(String xmlContent) {
        // Удаляем комментарии и лишние пробелы
        xmlContent = xmlContent.replaceAll("<!--.*?-->", "").trim();
        
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("XML Document");
        Stack<DefaultMutableTreeNode> nodeStack = new Stack<>();
        nodeStack.push(root);
        
        int index = 0;
        while (index < xmlContent.length()) {
            // Ищем открывающий тег
            int openTagStart = xmlContent.indexOf('<', index);
            if (openTagStart == -1) break;
            
            int openTagEnd = xmlContent.indexOf('>', openTagStart);
            if (openTagEnd == -1) break;
            
            String tagContent = xmlContent.substring(openTagStart + 1, openTagEnd).trim();
            
            // Пропускаем XML declaration и processing instructions
            if (tagContent.startsWith("?") || tagContent.startsWith("!")) {
                index = openTagEnd + 1;
                continue;
            }
            
            // Проверяем, является ли тег закрывающим
            if (tagContent.startsWith("/")) {
                // Закрывающий тег
                nodeStack.pop();
            } else {
                // Открывающий тег
                String tagName = extractTagName(tagContent);
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(tagName);
                nodeStack.peek().add(node);
                
                // Проверяем, самозакрывающийся ли тег
                if (tagContent.endsWith("/")) {
                    // Самозакрывающийся тег - не добавляем в стек
                } else {
                    nodeStack.push(node);
                }
                
                // Ищем текстовое содержимое между тегами
                int contentStart = openTagEnd + 1;
                int nextTagStart = xmlContent.indexOf('<', contentStart);
                
                if (nextTagStart > contentStart) {
                    String textContent = xmlContent.substring(contentStart, nextTagStart).trim();
                    if (!textContent.isEmpty()) {
                        DefaultMutableTreeNode textNode = new DefaultMutableTreeNode("[TEXT]: " + truncateText(textContent));
                        node.add(textNode);
                    }
                }
            }
            
            index = openTagEnd + 1;
        }
        
        return root;
    }
    
    private String extractTagName(String tagContent) {
        // Извлекаем имя тега (без атрибутов и закрывающего слэша)
        int spaceIndex = tagContent.indexOf(' ');
        int slashIndex = tagContent.indexOf('/');
        
        int endIndex = tagContent.length();
        if (spaceIndex != -1) endIndex = Math.min(endIndex, spaceIndex);
        if (slashIndex != -1) endIndex = Math.min(endIndex, slashIndex);
        
        return tagContent.substring(0, endIndex);
    }
    
    private String truncateText(String text) {
        if (text.length() > 50) {
            return text.substring(0, 47) + "...";
        }
        return text;
    }
    
    private void displayNodeContent() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) xmlTree.getLastSelectedPathComponent();
        
        if (selectedNode == null) return;
        
        StringBuilder content = new StringBuilder();
        content.append("Selected Node: ").append(selectedNode.getUserObject()).append("\n\n");
        content.append("Path: ").append(getNodePath(selectedNode)).append("\n\n");
        
        // Показываем детей узла
        if (selectedNode.getChildCount() > 0) {
            content.append("Child Nodes:\n");
            for (int i = 0; i < selectedNode.getChildCount(); i++) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) selectedNode.getChildAt(i);
                content.append("  • ").append(child.getUserObject()).append("\n");
            }
        } else {
            content.append("No child nodes\n");
        }
        
        contentArea.setText(content.toString());
    }
    
    private String getNodePath(DefaultMutableTreeNode node) {
        Object[] path = node.getUserObjectPath();
        StringBuilder pathBuilder = new StringBuilder();
        
        for (int i = 0; i < path.length; i++) {
            if (i > 0) pathBuilder.append(" → ");
            pathBuilder.append(path[i]);
        }
        
        return pathBuilder.toString();
    }
    
    private void expandAllNodes(JTree tree, int startingIndex, int rowCount) {
        for (int i = startingIndex; i < rowCount; ++i) {
            tree.expandRow(i);
        }
        
        if (tree.getRowCount() != rowCount) {
            expandAllNodes(tree, rowCount, tree.getRowCount());
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new XMLParser().setVisible(true);
        });
    }
}
