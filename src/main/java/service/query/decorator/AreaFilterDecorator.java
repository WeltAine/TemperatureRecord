package service.query.decorator;

import service.query.TemperatureQuery;
import model.QueryResult;
import model.User;
import java.util.List;
import java.util.stream.Collectors;

// 地区模糊筛选装饰器：内存层模糊匹配地址（仅链1用）
public class AreaFilterDecorator extends QueryDecorator {

    public AreaFilterDecorator(TemperatureQuery wrappedQuery) {
        super(wrappedQuery);
    }

    @Override
    public QueryResult execute() {
        // 1. 执行被装饰对象，获取基础数据
        QueryResult result = wrappedQuery.execute();
        List<User> userList = result.getUserList();
        String areaKeyword = context.getAreaKeyword();

        // 2. 未设置地区关键词，返回原结果
        if (areaKeyword == null || areaKeyword.isEmpty()) {
            return result;
        }

        // 3. 内存层模糊筛选（忽略大小写）
        List<User> filteredList = userList.stream()
                .filter(user -> {
                    String address = user.getAddress();
                    return address != null && address.toLowerCase().contains(areaKeyword.toLowerCase());
                })
                .collect(Collectors.toList());

        result.setUserList(filteredList);
        return result;
    }
}