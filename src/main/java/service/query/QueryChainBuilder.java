package service.query;

import service.query.decorator.NameFilterDecorator;
import service.query.decorator.GenderFilterDecorator;
import service.query.decorator.AgeFilterDecorator;
import service.query.decorator.AreaFilterDecorator;
import service.query.decorator.DateRangeFilterDecorator;
import model.QueryResult;
import model.QueryContext;

// 查询链构建工具：封装两条链的组合逻辑，直接返回最终结果
public class QueryChainBuilder {

    // 链1：月份（数据库）→ 性别 → 年龄 → 地区 → 日期段（无姓名筛选）
    public static QueryResult buildChain1(QueryContext context) {
        // 步骤1：数据库层查询当前月份所有数据
        TemperatureQuery baseQuery = new BaseTemperatureQuery(context);
        // 步骤2：内存层叠加性别筛选
        TemperatureQuery genderFilter = new GenderFilterDecorator(baseQuery);
        // 步骤3：内存层叠加年龄筛选
        TemperatureQuery ageFilter = new AgeFilterDecorator(genderFilter);
        // 步骤4：内存层叠加地区筛选
        TemperatureQuery areaFilter = new AreaFilterDecorator(ageFilter);
        // 步骤5：内存层叠加日期段筛选
        TemperatureQuery dateFilter = new DateRangeFilterDecorator(areaFilter);
        // 执行最终筛选并返回结果
        return dateFilter.execute();
    }

    // 链2：月份（数据库）→ 姓名 → 日期段（无性别/年龄/地区筛选）
    public static QueryResult buildChain2(QueryContext context) {
        // 步骤1：数据库层查询当前月份所有数据
        TemperatureQuery baseQuery = new BaseTemperatureQuery(context);
        // 步骤2：内存层叠加姓名筛选
        TemperatureQuery nameFilter = new NameFilterDecorator(baseQuery);
        // 步骤3：内存层叠加日期段筛选
        TemperatureQuery dateFilter = new DateRangeFilterDecorator(nameFilter);
        // 执行最终筛选并返回结果
        return dateFilter.execute();
    }
}