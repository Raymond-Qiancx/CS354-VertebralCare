package com.vertebralcare.service;

import com.vertebralcare.dao.DiagnosisClassDao;
import com.vertebralcare.dao.ExamDao;
import com.vertebralcare.model.DiagnosisClass;
import com.vertebralcare.model.Exam;

import java.sql.SQLException;
import java.util.List;

/**
 * 检查记录业务逻辑服务
 */
public class ExamService {

    private final ExamDao examDao;
    private final DiagnosisClassDao diagnosisClassDao;

    public ExamService() {
        this.examDao = new ExamDao();
        this.diagnosisClassDao = new DiagnosisClassDao();
    }

    /**
     * 添加检查记录
     */
    public int addExam(Exam exam) throws SQLException {
        validateExam(exam);
        return examDao.insert(exam);
    }

    /**
     * 更新检查记录
     */
    public boolean updateExam(Exam exam) throws SQLException {
        validateExam(exam);
        return examDao.update(exam);
    }

    /**
     * 删除检查记录
     */
    public boolean deleteExam(int examId) throws SQLException {
        return examDao.delete(examId);
    }

    /**
     * 根据ID获取检查记录
     */
    public Exam getExamById(int examId) throws SQLException {
        return examDao.findById(examId);
    }

    /**
     * 获取某病人的所有检查记录
     */
    public List<Exam> getExamsByPatientId(int patientId) throws SQLException {
        return examDao.findByPatientId(patientId);
    }

    /**
     * 获取所有检查记录
     */
    public List<Exam> getAllExams() throws SQLException {
        return examDao.findAll();
    }

    /**
     * 按诊断类别查询检查记录
     */
    public List<Exam> getExamsByClassId(int classId) throws SQLException {
        return examDao.findByClassId(classId);
    }

    /**
     * 获取检查记录总数
     */
    public int getExamCount() throws SQLException {
        return examDao.count();
    }

    /**
     * 获取所有诊断类别
     */
    public List<DiagnosisClass> getAllDiagnosisClasses() throws SQLException {
        return diagnosisClassDao.findAll();
    }

    /**
     * 根据类别代码获取诊断类别
     */
    public DiagnosisClass getDiagnosisClassByCode(String code) throws SQLException {
        return diagnosisClassDao.findByCode(code);
    }

    /**
     * 验证检查记录数据
     */
    private void validateExam(Exam exam) {
        if (exam == null) {
            throw new IllegalArgumentException("检查记录不能为空");
        }
        if (exam.getPatientId() == null || exam.getPatientId() <= 0) {
            throw new IllegalArgumentException("必须选择病人");
        }
        if (exam.getExamDate() == null) {
            throw new IllegalArgumentException("检查日期不能为空");
        }
        if (exam.getClassId() == null || exam.getClassId() <= 0) {
            throw new IllegalArgumentException("必须选择诊断类别");
        }
    }
}
