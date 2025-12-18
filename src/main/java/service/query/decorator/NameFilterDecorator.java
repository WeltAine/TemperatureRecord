package service.query.decorator;

import service.query.TemperatureQuery;
import model.QueryResult;
import model.User;
import java.util.List;
import java.util.stream.Collectors;

// 姓名筛选装饰器：内存层精确匹配姓名（仅链2用）
public class NameFilterDecorator extends QueryDecorator {

    public NameFilterDecorator(TemperatureQuery wrappedQuery) {
        super(wrappedQuery);
    }

    @Override
    public QueryResult execute() {
        // 1. 执行被装饰对象，获取基础数据
        QueryResult result = wrappedQuery.execute();
        List<User> userList = result.getUserList();
        String name = context.getName();

        // 2. 未设置姓名，返回原结果
        if (name == null || name.isEmpty()) {
            return result;
        }

        // 3. 内存层精确筛选姓名
        List<User> filteredList = userList.stream()
                .filter(user -> name.equals(user.getName()))
                .collect(Collectors.toList());

        result.setUserList(filteredList);
        return result;
    }
}