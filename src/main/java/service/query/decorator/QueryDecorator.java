package service.query.decorator;

import service.query.TemperatureQuery;
import model.QueryResult;
import model.QueryContext;

// 装饰器抽象类：持有被装饰的查询对象，提供模板方法
public abstract class QueryDecorator implements TemperatureQuery {
    // 被装饰的查询对象（核心：装饰器模式的组合特性）
    protected TemperatureQuery wrappedQuery;
    // 筛选上下文（从被装饰对象中获取，共享参数）
    protected QueryContext context;

    // 构造器：传入被装饰的查询对象
    public QueryDecorator(TemperatureQuery wrappedQuery) {
        this.wrappedQuery = wrappedQuery;
        // 从被装饰对象的结果中获取上下文
        this.context = wrappedQuery.execute().getContext();
    }

    // 子类必须实现execute方法，叠加筛选逻辑
    @Override
    public abstract QueryResult execute();
}