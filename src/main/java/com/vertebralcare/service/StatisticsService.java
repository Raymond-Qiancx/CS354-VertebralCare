package com.vertebralcare.service;

import com.vertebralcare.dao.ExamDao;
import com.vertebralcare.dao.PatientDao;

import java.sql.SQLException;
import java.util.Map;

/**
 * 统计分析业务逻辑服务
 */
public class StatisticsService {

    private final ExamDao examDao;
    private final PatientDao patientDao;

    public StatisticsService() {
        this.examDao = new ExamDao();
        this.patientDao = new PatientDao();
    }

    /**
     * 获取病人总数
     */
    public int getPatientCount() throws SQLException {
        return patientDao.count();
    }

    /**
     * 获取检查记录总数
     */
    public int getExamCount() throws SQLException {
        return examDao.count();
    }

    /**
     * 按诊断类别统计检查数量
     * @return Map<类别描述, 数量>
     */
    public Map<String, Integer> getCountByClass() throws SQLException {
        return examDao.countByClass();
    }

    /**
     * 计算异常占比
     * @return 异常占总数的百分比
     */
    public double getAbnormalPercentage() throws SQLException {
        Map<String, Integer> counts = getCountByClass();
        int total = 0;
        int abnormal = 0;

        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            total += entry.getValue();
            if (entry.getKey().contains("Abnormal")) {
                abnormal = entry.getValue();
            }
        }

        if (total == 0) return 0.0;
        return (abnormal * 100.0) / total;
    }

    /**
     * 计算正常占比
     * @return 正常占总数的百分比
     */
    public double getNormalPercentage() throws SQLException {
        return 100.0 - getAbnormalPercentage();
    }

    /**
     * 按类别获取各指标平均值
     * @return Map<类别描述, double[6]> 6个指标的平均值
     */
    public Map<String, double[]> getAveragesByClass() throws SQLException {
        return examDao.getAveragesByClass();
    }

    /**
     * Indicator names array (corresponding to average values array)
     */
    public static String[] getIndicatorNames() {
        return new String[]{
                "Pelvic Incidence (PI)",
                "Pelvic Tilt (PT)",
                "Lumbar Lordosis Angle (LL)",
                "Sacral Slope (SS)",
                "Pelvic Radius (PR)",
                "Degree of Spondylolisthesis (GS)"
        };
    }

    /**
     * 指标英文名称数组
     */
    public static String[] getIndicatorEnglishNames() {
        return new String[]{
                "Pelvic Incidence",
                "Pelvic Tilt",
                "Lumbar Lordosis Angle",
                "Sacral Slope",
                "Pelvic Radius",
                "Grade of Spondylolisthesis"
        };
    }
}
