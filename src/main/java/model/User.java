package model;

public class User {
    private String name;
    private String gender;
    private int age;
    private String address;
    private double[] temperature; // 31长度数组，对应1-31日体温

    // 无参构造（Jackson解析必需）
    public User() {}

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
}