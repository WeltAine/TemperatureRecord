package model;

import java.util.List;

/**
 * 多月份体温数据 JSON 实体（顶层）
 */
public class MultiTempExportVO {
    // 多个年月的体温记录列表
    private List<SingleMonthTempVO> tempRecords;

    // 空参/全参构造 + getter/setter
    public MultiTempExportVO() {}
    public MultiTempExportVO(List<SingleMonthTempVO> tempRecords) {
        this.tempRecords = tempRecords;
    }

    public List<SingleMonthTempVO> getTempRecords() { return tempRecords; }
    public void setTempRecords(List<SingleMonthTempVO> tempRecords) { this.tempRecords = tempRecords; }
}

