package service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import model.User;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TemperatureService {
    // ！！！替换为你的JSON根目录绝对路径（比如E:/TemperatureRecord/json/）
    private static final String JSON_ROOT_PATH = "O:/JavaProgram/TemperatureRecord/json/";
    private ObjectMapper objectMapper = new ObjectMapper();

    // 获取指定年月的所有用户体温记录
    public List<User> getMonthlyTemperature(String year, String month) {
        List<User> userList = new ArrayList<>();
        // 拼接当月JSON文件路径：JSON_ROOT_PATH + 2025-12.json
        String jsonPath = JSON_ROOT_PATH + year + "-" + month + ".json";
        File jsonFile = new File(jsonPath);

        if (jsonFile.exists()) {
            try {
                // 读取JSON根节点
                JsonNode root = objectMapper.readTree(jsonFile);
                JsonNode usersNode = root.get("users");
                
                // 解析users数组为List<User>
                CollectionType listType = objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, User.class);
                userList = objectMapper.readValue(usersNode.toString(), listType);

                // 兜底：若某用户temperature数组长度不足31，补-1
                for (User user : userList) {
                    double[] tempArr = user.getTemperature();
                    if (tempArr == null) {
                        tempArr = new double[31];
                        // 初始化全为-1
                        for (int i = 0; i < 31; i++) tempArr[i] = -1;
                        user.setTemperature(tempArr);
                    } else if (tempArr.length < 31) {
                        double[] newTempArr = new double[31];
                        // 复制原有数据
                        System.arraycopy(tempArr, 0, newTempArr, 0, tempArr.length);
                        // 剩余位置补-1
                        for (int i = tempArr.length; i < 31; i++) newTempArr[i] = -1;
                        user.setTemperature(newTempArr);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return userList;
    }
}