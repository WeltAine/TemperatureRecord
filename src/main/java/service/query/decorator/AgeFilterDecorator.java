package service.query.decorator;

import service.query.TemperatureQuery;
import model.QueryResult;
import model.User;
import java.util.List;
import java.util.stream.Collectors;

// 年龄筛选装饰器：内存层筛选年龄范围（仅链1用）
public class AgeFilterDecorator extends QueryDecorator {

    public AgeFilterDecorator(TemperatureQuery wrappedQuery) {
        super(wrappedQuery);
    }

    @Override
    public QueryResult execute() {
        // 1. 执行被装饰对象，获取基础数据
        QueryResult result = wrappedQuery.execute();
        List<User> userList = result.getUserList();
        Integer minAge = context.getMinAge();
        Integer maxAge = context.getMaxAge();

        // 2. 未设置年龄范围，返回原结果
        if (minAge == null && maxAge == null) {
            return result;
        }

        // 3. 内存层筛选年龄（minAge ≤ age ≤ maxAge）
        List<User> filteredList = userList.stream()
                .filter(user -> {
                    int age = user.getAge();
                    boolean minMatch = (minAge == null) || (age >= minAge);
                    boolean maxMatch = (maxAge == null) || (age <= maxAge);
                    return minMatch && maxMatch;
                })
                .collect(Collectors.toList());

        result.setUserList(filteredList);
        return result;
    }
}