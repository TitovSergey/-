//animals:
package zoo.animals.interfaces;

public interface Animal {
    String getName();
    int getAge();
    void makeSound();
    void eat();
    void sleep();
}
//Интерфейсы и абстрактные классы:
Animal
package zoo.animals.interfaces;

public interface Animal {
    String getName();
    int getAge();
    void makeSound();
    void eat();
    void sleep();
}
Feedable
package zoo.animals.interfaces;

public interface Feedable {
    void feed(String food);
    String getFavoriteFood();
    boolean isHungry();
}
Movable
package zoo.animals.interfaces;

public interface Movable {
    void move();
    double getSpeed();
    String getMovementType();
}
//Абстрактные классы для категорий животных
Mammal
package zoo.animals.abstract;

import zoo.animals.interfaces.Movable;

public abstract class Mammal extends AbstractAnimal implements Movable {
    protected int gestationPeriod; // период беременности в днях
    protected boolean hasFur;
    
    public Mammal(String name, int age, double weight, String habitat, 
                  int gestationPeriod, boolean hasFur) {
        super(name, age, weight, habitat);
        this.gestationPeriod = gestationPeriod;
        this.hasFur = hasFur;
    }
    
    @Override
    public String getMovementType() {
        return "ходит";
    }
    
    public abstract void giveBirth();
    
    public int getGestationPeriod() {
        return gestationPeriod;
    }
    
    public boolean hasFur() {
        return hasFur;
    }
}
Bird
package zoo.animals.abstract;

import zoo.animals.interfaces.Movable;

public abstract class Bird extends AbstractAnimal implements Movable {
    protected double wingspan;
    protected boolean canFly;
    
    public Bird(String name, int age, double weight, String habitat, 
                double wingspan, boolean canFly) {
        super(name, age, weight, habitat);
        this.wingspan = wingspan;
        this.canFly = canFly;
    }
    
    @Override
    public String getMovementType() {
        return canFly ? "летает" : "ходит";
    }
    
    public abstract void layEggs();
    
    public double getWingspan() {
        return wingspan;
    }
    
    public boolean canFly() {
        return canFly;
    }
}
Reptile 
package zoo.animals.abstract;

import zoo.animals.interfaces.Movable;

public abstract class Reptile extends AbstractAnimal implements Movable {
    protected boolean isColdBlooded;
    protected String scaleType;
    
    public Reptile(String name, int age, double weight, String habitat, 
                   boolean isColdBlooded, String scaleType) {
        super(name, age, weight, habitat);
        this.isColdBlooded = isColdBlooded;
        this.scaleType = scaleType;
    }
    
    @Override
    public String getMovementType() {
        return "ползает";
    }
    
    public abstract void shedSkin();
    
    public boolean isColdBlooded() {
        return isColdBlooded;
    }
    
    public String getScaleType() {
        return scaleType;
    }
}
Lion
package zoo.animals.mammals;

import zoo.animals.abstract.Mammal;

public class Lion extends Mammal {
    private String maneColor;
    
    public Lion(String name, int age, double weight, String maneColor) {
        super(name, age, weight, "саванна", 110, true);
        this.maneColor = maneColor;
    }
    
    @Override
    public void makeSound() {
        System.out.println(name + " рычит: Р-р-р-р!");
    }
    
    @Override
    public void eat() {
        System.out.println(name + " ест мясо");
    }
    
    @Override
    public String getFavoriteFood() {
        return "мясо";
    }
    
    @Override
    public void move() {
        System.out.println(name + " грациозно бежит по саванне");
    }
    
    @Override
    public double getSpeed() {
        return 80.0; // км/ч
    }
    
    @Override
    public void giveBirth() {
        System.out.println(name + " родила львенка после " + gestationPeriod + " дней беременности");
    }
    
    public void hunt() {
        System.out.println(name + " охотится на антилопу");
    }
    
    public String getManeColor() {
        return maneColor;
    }
}
Monkey
package zoo.animals.mammals;

import zoo.animals.abstract.Mammal;

public class Monkey extends Mammal {
    private int intelligenceLevel; // от 1 до 10
    
    public Monkey(String name, int age, double weight, int intelligenceLevel) {
        super(name, age, weight, "джунгли", 165, true);
        this.intelligenceLevel = intelligenceLevel;
    }
    
    @Override
    public void makeSound() {
        System.out.println(name + " кричит: У-у-у-а-а-а!");
    }
    
    @Override
    public void eat() {
        System.out.println(name + " ест бананы");
    }
    
    @Override
    public String getFavoriteFood() {
        return "бананы";
    }
    
    @Override
    public void move() {
        System.out.println(name + " ловко лазает по деревьям");
    }
    
    @Override
    public double getSpeed() {
        return 25.0;
    }
    
    @Override
    public void giveBirth() {
        System.out.println(name + " родила детеныша обезьяны");
    }
    
    public void climbTree() {
        System.out.println(name + " быстро забирается на дерево");
    }
    
    public void useTool() {
        if (intelligenceLevel > 5) {
            System.out.println(name + " использует палку чтобы достать еду");
        }
    }
    
    public int getIntelligenceLevel() {
        return intelligenceLevel;
    }
}

