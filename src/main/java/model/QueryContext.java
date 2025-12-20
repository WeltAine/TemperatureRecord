package model;

// 筛选上下文：存储所有筛选条件，强制校验月份格式
public class QueryContext {
    // 必选：前端显示的月份（格式：yyyy-mm）
    private String yearMonth;
    // 可选筛选参数
    private String name;        // 姓名（仅链2用）
    private String gender;      // 性别（仅链1用）
    private Integer minAge;     // 最小年龄（仅链1用）
    private Integer maxAge;     // 最大年龄（仅链1用）
    private String areaKeyword; // 地区模糊关键词（仅链1用）
    private int startDay = 1;   // 日期段开始日（默认1，链1/链2都用）
    private int endDay = 31;    // 日期段结束日（默认31，链1/链2都用）

    // 强制校验：月份不能为空且格式正确
    public void validate() {
        if (yearMonth == null || !yearMonth.matches("\\d{4}-\\d{1,2}")) {
            throw new IllegalArgumentException("查询月份格式错误，必须为yyyy-m或yyyy-mm（如2025-9、2025-12）");       
        }

        // 额外校验：月份在1-12之间（避免2025-13这种非法值）
        String[] parts = yearMonth.split("-");
        int month = Integer.parseInt(parts[1]);
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("月份必须在1-12之间");
        }

        // 修正日期段范围（确保1-31）
        this.startDay = Math.max(1, Math.min(31, this.startDay));
        this.endDay = Math.max(1, Math.min(31, this.endDay));
        if (this.startDay > this.endDay) {
            throw new IllegalArgumentException("开始日不能大于结束日");
        }
    }

    // getter/setter 全量生成
    public String getYearMonth() { return yearMonth; }
    public void setYearMonth(String yearMonth) { this.yearMonth = yearMonth; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Integer getMinAge() { return minAge; }
    public void setMinAge(Integer minAge) { this.minAge = minAge; }
    public Integer getMaxAge() { return maxAge; }
    public void setMaxAge(Integer maxAge) { this.maxAge = maxAge; }
    public String getAreaKeyword() { return areaKeyword; }
    public void setAreaKeyword(String areaKeyword) { this.areaKeyword = areaKeyword; }
    public int getStartDay() { return startDay; }
    public void setStartDay(int startDay) { this.startDay = startDay; }
    public int getEndDay() { return endDay; }
    public void setEndDay(int endDay) { this.endDay = endDay; }
}