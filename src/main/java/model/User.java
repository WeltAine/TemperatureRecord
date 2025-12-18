package model;

public class User {
    private String name;
    private String gender;
    private int age;
    private String address;
    private double[] temperature; // 31长度数组，对应1-31日体温
    private String yearMonth; // 新增：年月（如2025-12

    // 无参构造（Jackson解析必需）
    public User() {}

    public User(String name, String gender, int age, String address, double[] temperature, String yearMonth) {
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.address = address;
        this.temperature = temperature;
        this.yearMonth = yearMonth;
    }


    // Getter & Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public double[] getTemperature() { return temperature; }
    public void setTemperature(double[] temperature) { this.temperature = temperature; }

    // getter/setter （补充yearMonth的get/set）
    public String getYearMonth() { return yearMonth; }
    public void setYearMonth(String yearMonth) { this.yearMonth = yearMonth; }
}