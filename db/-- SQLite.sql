-- 修正版：单主键（id）+ 唯一索引（name+year_month），避免主键冲突
CREATE TABLE IF NOT EXISTS user_temperature (
    id INTEGER PRIMARY KEY AUTOINCREMENT, -- 唯一主键（自增）
    name TEXT NOT NULL, -- 姓名
    gender TEXT, -- 性别
    age INTEGER, -- 年龄
    address TEXT, -- 住址
    year_month TEXT NOT NULL, -- 年月（格式：yyyy-mm）
    temperature TEXT -- 体温数组（逗号分隔）
);

-- -- 核心：给name+year_month加唯一索引，保证同一个用户当月只有一条记录
-- CREATE UNIQUE INDEX idx_name_yearmonth ON user_temperature (name, year_month);


-- -- 插入测试数据（对应2025-11的张三）
-- INSERT INTO user_temperature (name, gender, age, address, year_month, temperature) 
-- VALUES (
--     '张三', 
--     '男', 
--     28, 
--     '北京市/朝阳区/望京街道', 
--     '2025-12', 
--     '36.4,36.5,-1,36.3,36.6,-1,36.4,36.5,36.7,-1,36.4,36.5,36.6,-1,36.7,36.4,36.5,36.6,-1,36.7,36.4,-1,36.5,36.6,36.7,36.4,36.5,-1,36.6,36.7,36.4'
-- );


-- -- 给year_month字段加索引，加速按月查询
-- CREATE INDEX idx_temp_year_month ON user_temperature (year_month);


-- 插入2025-12月测试数据（李四）
-- INSERT INTO user_temperature (name, gender, age, address, year_month, temperature) 
-- VALUES (
--     '李四', 
--     '女', 
--     30, 
--     '上海市/浦东新区/张江街道', 
--     '2025-12', 
--     '-1,36.8,36.7,36.9,36.8,-1,36.7,36.9,-1,36.8,36.7,-1,36.9,36.8,36.7,-1,36.9,36.8,-1,36.7,36.9,36.8,-1,36.7,36.9,-1,36.8,36.7,36.9,-1,36.8'
-- );

-- -- 可选：插入2025-11月数据（保证月份切换功能正常）
-- INSERT INTO user_temperature (name, gender, age, address, year_month, temperature) 
-- VALUES (
--     '赵六', 
--     '女', 
--     32, 
--     '深圳市/南山区/科技园街道', 
--     '2025-11', 
--     '-1,36.8,36.7,36.9,-1,36.8,36.7,-1,36.9,36.8,36.7,36.9,-1,36.8,36.7,36.9,36.8,-1,36.7,36.9,36.8,36.7,-1,36.9,36.8,36.7,36.9,-1,36.8,36.7,-1'
-- );

-- SELECT DISTINCT year_month FROM user_temperature;


CREATE TABLE IF NOT EXISTS admin (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE, -- 用户名唯一
    password TEXT NOT NULL        -- 密码（你手动维护，明文/加密均可）
);

-- 手动插入初始管理员（示例：用户名admin，密码123456，你可自行修改）
INSERT OR IGNORE INTO admin (username, password) VALUES ('admin', '123456');