package service.query.decorator;

import service.query.TemperatureQuery;
import model.QueryResult;
import model.User;
import java.util.List;
import java.util.stream.Collectors;

// 性别筛选装饰器：内存层精确匹配性别（仅链1用）
public class GenderFilterDecorator extends QueryDecorator {

    public GenderFilterDecorator(TemperatureQuery wrappedQuery) {
        super(wrappedQuery);
    }

    @Override
    public QueryResult execute() {
        // 1. 先执行被装饰对象，获取基础数据
        QueryResult result = wrappedQuery.execute();
        List<User> userList = result.getUserList();
        String gender = context.getGender();

        // 2. 未设置性别，直接返回原结果
        if (gender == null || gender.isEmpty()) {
            return result;
        }

        // 3. 内存层精确筛选性别
        List<User> filteredList = userList.stream()
                .filter(user -> gender.equals(user.getGender()))
                .collect(Collectors.toList());

        // 4. 返回筛选后的结果
        result.setUserList(filteredList);
        return result;
    }
}