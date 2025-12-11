# VertebralCare 脊柱病例管理与风险评估系统

基于 Vertebral Column 数据集的脊柱病例管理系统，用于数据库课程设计。

## 技术栈

- **数据库**: MySQL 8.0
- **后端**: Java 8+ / JDBC
- **前端**: Swing GUI
- **构建工具**: Maven
- **图表库**: JFreeChart

## 项目结构

```
vertebralcare/
├── pom.xml                          # Maven配置
├── src/main/
│   ├── java/com/vertebralcare/
│   │   ├── Main.java                # 程序入口
│   │   ├── model/                   # 实体类 (3个)
│   │   │   ├── Patient.java
│   │   │   ├── Exam.java
│   │   │   └── DiagnosisClass.java
│   │   ├── dao/                     # 数据访问层 (4个)
│   │   │   ├── DBUtil.java
│   │   │   ├── PatientDao.java
│   │   │   ├── ExamDao.java
│   │   │   └── DiagnosisClassDao.java
│   │   ├── service/                 # 业务逻辑层 (4个)
│   │   │   ├── PatientService.java
│   │   │   ├── ExamService.java
│   │   │   ├── StatisticsService.java
│   │   │   └── ImportService.java
│   │   └── ui/                      # Swing界面 (5个)
│   │       ├── MainFrame.java
│   │       ├── PatientPanel.java
│   │       ├── ExamPanel.java
│   │       ├── StatisticsPanel.java
│   │       └── ImportPanel.java
│   └── resources/
│       ├── db.properties            # 数据库配置
│       └── init.sql                 # 初始化SQL脚本
└── README.md
```

## 快速开始

### 1. 配置数据库

1. 确保 MySQL 服务已启动
2. 修改 `src/main/resources/db.properties` 中的数据库密码
3. 执行初始化SQL：
   ```bash
   mysql -u root -p < src/main/resources/init.sql
   ```

### 2. 编译运行

```bash
# 进入项目目录
cd vertebralcare

# 编译
mvn clean compile

# 运行
mvn exec:java -Dexec.mainClass="com.vertebralcare.Main"

# 或打包后运行
mvn clean package
java -jar target/vertebralcare-1.0.0-jar-with-dependencies.jar
```

## 功能模块

### 1. 病人管理
- 新增/编辑/删除病人
- 按姓名搜索病人
- 查看病人列表

### 2. 检查记录
- 为病人添加检查记录（6个生物力学指标）
- 按病人/诊断类别筛选记录
- 查看检查详情

### 3. 统计分析
- 病例数量统计
- Normal/Abnormal 占比饼图
- 各类别指标平均值对比

### 4. 数据导入
- 导入CSV格式的数据集
- 自动生成虚拟病人信息
- 批量导入检查记录

## 数据库设计

### E-R关系
- Patient (1) → (N) Exam
- DiagnosisClass (1) → (N) Exam

### 表结构

| 表名 | 说明 | 主键 |
|------|------|------|
| Patient | 病人信息 | patient_id |
| DiagnosisClass | 诊断类别 | class_id |
| Exam | 检查记录 | exam_id |

### BCNF分析
三张表均满足BCNF范式，详见课程报告。

## 数据集

使用 Kaggle Vertebral Column 数据集，包含310条脊柱检查记录：
- 6个生物力学指标
- 二分类标签（Normal/Abnormal）

## 许可证

仅供学习使用
