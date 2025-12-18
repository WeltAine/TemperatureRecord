package model;

/**
 * 管理员实体类（仅用户名+密码）
 */
public class Admin {
    private String username;
    private String password;

    // 空参/全参构造
    public Admin() {}
    public Admin(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // getter/setter
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}