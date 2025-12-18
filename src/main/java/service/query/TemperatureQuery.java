package service.query;

import model.QueryResult;

// 基础查询接口：所有查询/装饰器都实现这个接口
public interface TemperatureQuery {
    // 执行查询，返回结果
    QueryResult execute();
}