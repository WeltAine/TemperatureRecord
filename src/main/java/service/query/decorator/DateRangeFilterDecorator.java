package service.query.decorator;

import service.query.TemperatureQuery;
import model.QueryResult;
import model.User;
import java.util.List;
import java.util.stream.Collectors;

// 日期段筛选装饰器：1.筛选时间段内有体温的用户 2.将时间段外的体温设为-1
public class DateRangeFilterDecorator extends QueryDecorator {

    public DateRangeFilterDecorator(TemperatureQuery wrappedQuery) {
        super(wrappedQuery);
    }

    @Override
    public QueryResult execute() {
        // 1. 先执行被装饰对象，获取基础数据
        QueryResult result = wrappedQuery.execute();
        List<User> userList = result.getUserList();
        int startDay = context.getStartDay(); // 前端传的开始日（1-31）
        int endDay = context.getEndDay();     // 前端传的结束日（1-31）

        // 2. 第一步：筛选出「日期段内有体温记录（≠-1）」的用户（保留原有逻辑）
        List<User> filteredUsers = userList.stream()
                .filter(user -> {
                    double[] tempArr = user.getTemperature();
                    if (tempArr == null) {
                        return false;
                    }
                    // 检查startDay到endDay之间是否有有效体温（≠-1）
                    for (int i = startDay - 1; i < endDay; i++) { // 下标=日期-1
                        if (i < tempArr.length && tempArr[i] != -1) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());

        // 3. 第二步：对筛选后的用户，将「时间段外」的体温强制设为-1
        List<User> finalUsers = filteredUsers.stream()
                .map(user -> {
                    double[] tempArr = user.getTemperature();
                    // 兜底：如果体温数组为null/长度不足31，先补全为31位（默认-1）
                    double[] newTempArr = new double[31];
                    for (int i = 0; i < 31; i++) {
                        newTempArr[i] = -1; // 先全部设为-1
                    }
                    // 把原有有效体温复制到新数组（仅保留原数组的有效长度）
                    if (tempArr != null) {
                        int copyLength = Math.min(tempArr.length, 31);
                        System.arraycopy(tempArr, 0, newTempArr, 0, copyLength);
                    }

                    // 核心：只保留[startDay, endDay]范围内的体温，其余强制设为-1
                    for (int i = 0; i < 31; i++) {
                        int currentDay = i + 1; // 下标i对应日期currentDay（1-31）
                        // 如果当前日期不在[startDay, endDay]范围内，设为-1
                        if (currentDay < startDay || currentDay > endDay) {
                            newTempArr[i] = -1;
                        }
                        // 范围内的体温保持原有值（无需修改）
                    }

                    // 替换用户的体温数组
                    user.setTemperature(newTempArr);
                    return user;
                })
                .collect(Collectors.toList());

        // 4. 返回最终处理后的结果
        result.setUserList(finalUsers);
        return result;
    }
}