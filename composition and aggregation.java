// Композиция: Дом и комната - комнаты неотъемлемая часть дома, сильная связь часть-целое
// Класс Комната - часть Дома
class Room {
    private String type;
    private double area;
    
    public Room(String type, double area) {
        this.type = type;
        this.area = area;
    }
    
    public String getType() { return type; }
    public double getArea() { return area; }
}

// Класс Дом - содержит Комнаты
class House {
    private String address;
    private List<Room> rooms; // КОМПОЗИЦИЯ
  
Агрегация: Университет и студент - студент и университет существуют независимо друг от друга, слабая связь часть-целое
// Класс Студент - существует независимо от Университета
class Student {
    private String name;
    private int studentId;
    
    public Student(String name, int studentId) {
        this.name = name;
        this.studentId = studentId;
    }
    
    public String getName() { return name; }
    public int getStudentId() { return studentId; }
    
    public void displayInfo() {
        System.out.println("Студент: " + name + " (ID: " + studentId + ")");
    }
}
