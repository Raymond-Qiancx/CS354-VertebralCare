# VertebralCare 脊柱病例管理系统 - 技术报告

## Technical Report: VertebralCare - Spine Case Management & Risk Assessment System

**课程**: 数据库与数据科学
**项目名称**: VertebralCare - 脊柱病例管理与风险评估系统
**技术栈**: MySQL 8.0 + Java 8 + JDBC + Maven + Swing GUI + JFreeChart

---

## 一、项目背景与动机 (Background & Motivation)

### 1.1 研究背景

脊柱疾病是现代社会常见的健康问题，尤其是下背痛（Lower Back Pain）影响着全球数亿人口。脊柱滑脱（Spondylolisthesis）和椎间盘突出（Disc Hernia）等脊柱异常可通过生物力学指标进行早期识别和诊断。

本项目基于 **UCI Machine Learning Repository** 提供的 **Vertebral Column Data Set**（脊柱数据集），该数据集包含310条患者脊柱生物力学特征记录，每条记录包含6个关键指标和对应的诊断分类。

### 1.2 项目动机

1. **临床需求**: 医疗机构需要系统化管理脊柱检查病例，追踪患者诊疗历史
2. **数据分析需求**: 通过统计分析正常/异常病例的指标差异，辅助临床决策
3. **教学目的**: 展示数据库设计规范化（BCNF）、三层架构、GUI开发等核心技术

### 1.3 数据集概述

| 特征 | 说明 |
|------|------|
| 数据来源 | UCI Machine Learning Repository |
| 记录数量 | 310 条 |
| 特征数量 | 6 个生物力学指标 + 1 个分类标签 |
| 分类任务 | 二分类（Normal / Abnormal） |

**六个生物力学指标**:

| 指标名称 | 英文全称 | 缩写 | 说明 |
|----------|----------|------|------|
| 骨盆入射角 | Pelvic Incidence | PI | 骨盆与脊柱的相对位置角度 |
| 骨盆倾斜度 | Pelvic Tilt | PT | 骨盆相对于垂直轴的倾斜程度 |
| 腰椎前凸角 | Lumbar Lordosis Angle | LL | 腰椎弯曲的曲率角度 |
| 骶骨倾斜度 | Sacral Slope | SS | 骶骨平面与水平面的夹角 |
| 骨盆半径 | Pelvic Radius | PR | 骨盆的几何测量参数 |
| 滑脱程度 | Degree of Spondylolisthesis | GS | 椎体滑移的程度量化 |

---

## 二、数据库设计 (Database Design)

### 2.1 设计原则

本项目严格遵循**BCNF（Boyce-Codd范式）**进行数据库设计，确保：
- 消除数据冗余
- 避免更新异常
- 保证数据完整性

### 2.2 E-R 关系图

```
┌─────────────┐         ┌─────────────┐         ┌─────────────────┐
│   Patient   │ 1     N │    Exam     │ N     1 │ DiagnosisClass  │
│─────────────│─────────│─────────────│─────────│─────────────────│
│ patient_id  │◄────────│ patient_id  │────────►│ class_id        │
│ name        │         │ exam_id     │         │ code            │
│ gender      │         │ exam_date   │         │ description     │
│ birth_date  │         │ 6 indicators│         └─────────────────┘
│ phone       │         │ class_id    │
│ created_at  │         │ notes       │
└─────────────┘         └─────────────┘
```

**关系说明**:
- **Patient 1:N Exam**: 一个患者可以有多次检查记录
- **DiagnosisClass 1:N Exam**: 一个诊断类别对应多条检查记录

### 2.3 表结构设计

#### 表1: Patient（患者表）

```sql
CREATE TABLE Patient (
    patient_id    INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    gender        CHAR(1) CHECK (gender IN ('M', 'F')),
    birth_date    DATE,
    phone         VARCHAR(20),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**BCNF分析**: `patient_id → name, gender, birth_date, phone, created_at`（所有非主属性完全函数依赖于主键）

#### 表2: DiagnosisClass（诊断类别表）

```sql
CREATE TABLE DiagnosisClass (
    class_id      INT AUTO_INCREMENT PRIMARY KEY,
    code          VARCHAR(10) NOT NULL UNIQUE,
    description   VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始数据
INSERT INTO DiagnosisClass (code, description) VALUES
('NO', 'Normal'),
('AB', 'Abnormal');
```

**BCNF分析**: `class_id → code, description`，`code` 也是候选键

#### 表3: Exam（检查记录表）

```sql
CREATE TABLE Exam (
    exam_id                   INT AUTO_INCREMENT PRIMARY KEY,
    patient_id                INT NOT NULL,
    exam_date                 DATE NOT NULL,
    pelvic_incidence          DOUBLE NOT NULL,
    pelvic_tilt               DOUBLE NOT NULL,
    lumbar_lordosis_angle     DOUBLE NOT NULL,
    sacral_slope              DOUBLE NOT NULL,
    pelvic_radius             DOUBLE NOT NULL,
    degree_spondylolisthesis  DOUBLE NOT NULL,
    class_id                  INT NOT NULL,
    notes                     VARCHAR(500),
    created_at                TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (patient_id) REFERENCES Patient(patient_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (class_id) REFERENCES DiagnosisClass(class_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    INDEX idx_patient_id (patient_id),
    INDEX idx_exam_date (exam_date),
    INDEX idx_class_id (class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**BCNF分析**: `exam_id → patient_id, exam_date, 6 indicators, class_id, notes, created_at`

### 2.4 索引优化

为提高查询性能，在Exam表上建立了三个索引：
- `idx_patient_id`: 按患者筛选检查记录
- `idx_exam_date`: 按日期排序和范围查询
- `idx_class_id`: 按诊断类别统计分析

### 2.5 外键约束策略

| 外键 | ON DELETE | ON UPDATE | 原因 |
|------|-----------|-----------|------|
| patient_id | CASCADE | CASCADE | 删除患者时自动删除其检查记录 |
| class_id | RESTRICT | CASCADE | 防止删除仍在使用的诊断类别 |

---

## 三、系统架构设计 (System Architecture)

### 3.1 三层架构模式

本系统采用经典的**三层架构（3-Tier Architecture）**设计：

```
┌─────────────────────────────────────────────────────────────┐
│                    UI Layer (Swing GUI)                     │
│  MainFrame / PatientPanel / ExamPanel / StatisticsPanel     │
├─────────────────────────────────────────────────────────────┤
│                    Service Layer (Business Logic)           │
│  PatientService / ExamService / StatisticsService           │
├─────────────────────────────────────────────────────────────┤
│                    DAO Layer (Data Access)                  │
│  PatientDao / ExamDao / DiagnosisClassDao / DBUtil          │
├─────────────────────────────────────────────────────────────┤
│                    Database (MySQL)                         │
│  Patient / Exam / DiagnosisClass                            │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 项目文件结构

```
vertebralcare/
├── pom.xml                           # Maven配置
├── run.sh                            # 一键启动脚本
├── src/main/
│   ├── java/com/vertebralcare/
│   │   ├── Main.java                 # 程序入口
│   │   ├── model/                    # 实体类层
│   │   │   ├── Patient.java          # 患者实体
│   │   │   ├── Exam.java             # 检查记录实体
│   │   │   └── DiagnosisClass.java   # 诊断类别实体
│   │   ├── dao/                      # 数据访问层
│   │   │   ├── DBUtil.java           # 数据库连接工具
│   │   │   ├── PatientDao.java       # 患者CRUD
│   │   │   ├── ExamDao.java          # 检查记录CRUD+统计
│   │   │   └── DiagnosisClassDao.java
│   │   ├── service/                  # 业务逻辑层
│   │   │   ├── PatientService.java   # 患者业务逻辑
│   │   │   ├── ExamService.java      # 检查记录业务逻辑
│   │   │   ├── StatisticsService.java# 统计分析服务
│   │   │   └── ImportService.java    # CSV导入服务
│   │   └── ui/                       # 界面层
│   │       ├── MainFrame.java        # 主窗口
│   │       ├── PatientPanel.java     # 患者管理面板
│   │       ├── ExamPanel.java        # 检查记录面板
│   │       ├── StatisticsPanel.java  # 统计分析面板
│   │       └── ImportPanel.java      # 数据导入面板
│   └── resources/
│       ├── db.properties             # 数据库配置
│       └── init.sql                  # 数据库初始化脚本
└── Dataset_spine.csv                 # 原始数据集
```

### 3.3 技术依赖

```xml
<dependencies>
    <!-- MySQL JDBC Driver 8.0.33 -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>8.0.33</version>
    </dependency>

    <!-- JFreeChart 1.5.3 for statistical charts -->
    <dependency>
        <groupId>org.jfree</groupId>
        <artifactId>jfreechart</artifactId>
        <version>1.5.3</version>
    </dependency>
</dependencies>
```

---

## 四、功能模块实现 (Feature Implementation)

### 4.1 患者管理模块 (Patient Management)

**功能描述**: 提供患者信息的完整CRUD操作

**界面布局**:
```
┌────────────────────────────────────────────────────┐
│ Search: [__________] [Search] [Add Patient]        │
├────────────────────────────────────────────────────┤
│ ID │ Name │ Gender │ Birth Date │ Phone │ Actions │
├────────────────────────────────────────────────────┤
│  1 │ John │ Male   │ 1985-03-15 │ (555)..│ [Edit]  │
│  2 │ Mary │ Female │ 1990-07-22 │ (555)..│ [Edit]  │
└────────────────────────────────────────────────────┘
```

**核心功能**:
- **新增患者**: 表单输入姓名、性别、出生日期、电话
- **搜索患者**: 按姓名模糊搜索（LIKE查询）
- **编辑/删除**: 双击行编辑，支持级联删除关联检查记录

**数据验证**:
```java
// PatientService.java
private void validatePatient(Patient patient) {
    if (patient.getName() == null || patient.getName().trim().isEmpty()) {
        throw new IllegalArgumentException("Patient name is required");
    }
    if (patient.getGender() != null &&
        !patient.getGender().equals("M") && !patient.getGender().equals("F")) {
        throw new IllegalArgumentException("Gender must be 'M' or 'F'");
    }
}
```

### 4.2 检查记录模块 (Exam Records)

**功能描述**: 管理患者的脊柱检查记录，记录6个生物力学指标和诊断结果

**界面布局**:
```
┌─────────────────────────────────────────────────────────────────────┐
│ Patient: [All ▼]    Diagnosis: [All ▼]    [Add] [View] [Edit] [Del] │
├─────────────────────────────────────────────────────────────────────┤
│ ID │ Patient │ Date │  PI  │  PT  │  LL  │  SS  │  PR  │  GS │Diag │
├─────────────────────────────────────────────────────────────────────┤
│  1 │ John S. │ 2024 │63.03 │22.36 │54.12 │40.67 │98.67 │-0.25│Normal│
└─────────────────────────────────────────────────────────────────────┘
```

**核心功能**:
- **新增检查**: 选择患者 → 输入6个指标 → 选择诊断结果
- **多维筛选**: 按患者、按诊断类别筛选
- **详情查看**: 完整显示所有指标和备注信息

**SQL关联查询**:
```sql
SELECT e.*, p.name as patient_name,
       d.code as diagnosis_code, d.description as diagnosis_desc
FROM Exam e
JOIN Patient p ON e.patient_id = p.patient_id
JOIN DiagnosisClass d ON e.class_id = d.class_id
ORDER BY e.exam_date DESC;
```

### 4.3 统计分析模块 (Statistics & Analytics)

**功能描述**: 可视化展示病例统计数据和指标对比分析

**界面布局**:
```
┌────────────────────────────────────────────────────────────────┐
│ ┌──────────┐ ┌──────────┐ ┌───────────┐ ┌────────────┐        │
│ │Patients: │ │ Exams:   │ │ Normal:   │ │ Abnormal:  │        │
│ │   310    │ │   310    │ │  32.3%    │ │   67.7%    │        │
│ └──────────┘ └──────────┘ └───────────┘ └────────────┘        │
├────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────┐    ┌─────────────────────┐           │
│  │    [Pie Chart]      │    │    [Bar Chart]      │           │
│  │  Diagnosis Class    │    │   Cases by Class    │           │
│  │   Distribution      │    │                     │           │
│  └─────────────────────┘    └─────────────────────┘           │
├────────────────────────────────────────────────────────────────┤
│ Indicator           │ Normal   │ Abnormal                     │
│ Pelvic Incidence    │ 51.5467  │ 68.6828                      │
│ Pelvic Tilt         │ 13.0689  │ 21.4629                      │
│ ...                 │ ...      │ ...                          │
└────────────────────────────────────────────────────────────────┘
```

**核心功能**:
1. **概览统计卡片**: 患者总数、检查总数、正常/异常占比
2. **饼图**: 诊断类别分布可视化
3. **柱状图**: 各类别病例数量对比
4. **指标平均值表**: Normal vs Abnormal 六项指标均值对比

**统计SQL实现**:
```sql
-- 按类别统计数量
SELECT d.description, COUNT(*) as cnt
FROM Exam e JOIN DiagnosisClass d ON e.class_id = d.class_id
GROUP BY d.class_id, d.description;

-- 按类别计算各指标平均值
SELECT d.description,
       AVG(e.pelvic_incidence) as avg_pi,
       AVG(e.pelvic_tilt) as avg_pt,
       AVG(e.lumbar_lordosis_angle) as avg_lla,
       AVG(e.sacral_slope) as avg_ss,
       AVG(e.pelvic_radius) as avg_pr,
       AVG(e.degree_spondylolisthesis) as avg_ds
FROM Exam e JOIN DiagnosisClass d ON e.class_id = d.class_id
GROUP BY d.class_id, d.description;
```

**JFreeChart图表生成**:
```java
// 饼图 - 诊断分布
DefaultPieDataset pieDataset = new DefaultPieDataset();
for (Map.Entry<String, Integer> entry : countByClass.entrySet()) {
    pieDataset.setValue(entry.getKey(), entry.getValue());
}
JFreeChart pieChart = ChartFactory.createPieChart(
    "Diagnosis Class Distribution", pieDataset, true, true, false);

// 设置颜色：Normal=绿色, Abnormal=红色
PiePlot piePlot = (PiePlot) pieChart.getPlot();
piePlot.setSectionPaint("Normal", new Color(46, 139, 87));
piePlot.setSectionPaint("Abnormal", new Color(205, 92, 92));
```

### 4.4 数据导入模块 (CSV Import)

**功能描述**: 批量导入Vertebral Column数据集，自动生成虚拟患者信息

**导入流程**:
```
CSV文件 → 解析数据行 → 生成虚拟患者 → 插入Patient表 →
        → 解析6个指标 → 映射诊断类别 → 插入Exam表
```

**虚拟患者生成策略**:
```java
private Patient generateVirtualPatient() {
    Patient patient = new Patient();

    // 随机英文姓名 (40个名 × 30个姓 = 1200种组合)
    String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
    String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
    patient.setName(firstName + " " + lastName);

    // 随机性别
    patient.setGender(random.nextBoolean() ? "M" : "F");

    // 随机出生日期（20-80岁）
    long minDay = Date.valueOf("1945-01-01").getTime();
    long maxDay = Date.valueOf("2005-01-01").getTime();
    patient.setBirthDate(new Date(ThreadLocalRandom.current().nextLong(minDay, maxDay)));

    // 随机美式电话号码
    patient.setPhone(String.format("(%03d) %03d-%04d",
        random.nextInt(900) + 100,
        random.nextInt(900) + 100,
        random.nextInt(10000)));

    return patient;
}
```

**CSV解析逻辑**:
```java
// 支持的CSV格式: PI, PT, LL, SS, PR, GS, Class
// 示例: 63.03,22.36,54.12,40.67,98.67,-0.25,Normal

private Exam parseExamFromRow(String[] row, int patientId) {
    Exam exam = new Exam();
    exam.setPatientId(patientId);

    // 解析6个指标
    exam.setPelvicIncidence(parseDouble(row[0]));
    exam.setPelvicTilt(parseDouble(row[1]));
    exam.setLumbarLordosisAngle(parseDouble(row[2]));
    exam.setSacralSlope(parseDouble(row[3]));
    exam.setPelvicRadius(parseDouble(row[4]));
    exam.setDegreeSpondylolisthesis(parseDouble(row[5]));

    // 映射诊断类别: "Normal" → class_id=1, "Abnormal" → class_id=2
    String classLabel = row[6].trim();
    int classId = diagnosisClassDao.getClassIdByLabel(classLabel);
    exam.setClassId(classId);

    return exam;
}
```

**异步导入与进度反馈**:
```java
SwingWorker<Integer, Integer> worker = new SwingWorker<>() {
    @Override
    protected Integer doInBackground() {
        return importService.importCSV(file, (current, total) -> {
            int percent = (int) ((current * 100.0) / total);
            publish(percent);  // 更新进度条
        });
    }

    @Override
    protected void done() {
        int count = get();
        showMessage("Import successful! " + count + " records imported");
        refreshCallback.run();  // 刷新其他面板
    }
};
```

---

## 五、技术亮点 (Technical Highlights)

### 5.1 SQL注入防护

所有数据库操作均使用 **PreparedStatement** 参数化查询：

```java
// 安全的参数化查询
String sql = "SELECT * FROM Patient WHERE name LIKE ?";
PreparedStatement ps = conn.prepareStatement(sql);
ps.setString(1, "%" + name + "%");

// 而非拼接字符串（危险！）
// String sql = "SELECT * FROM Patient WHERE name LIKE '%" + name + "%'";
```

### 5.2 资源管理

使用 **try-with-resources** 确保数据库连接自动关闭：

```java
try (Connection conn = DBUtil.getConnection();
     PreparedStatement ps = conn.prepareStatement(sql);
     ResultSet rs = ps.executeQuery()) {
    // 处理结果集
}  // 自动关闭 rs, ps, conn
```

### 5.3 事务处理

批量导入使用事务确保数据一致性：

```java
conn.setAutoCommit(false);
try {
    for (Exam exam : exams) {
        ps.addBatch();
        if (count % 100 == 0) ps.executeBatch();
    }
    ps.executeBatch();
    conn.commit();
} catch (Exception e) {
    conn.rollback();
    throw e;
}
```

### 5.4 GUI线程安全

长时间操作使用 **SwingWorker** 在后台执行，避免UI卡顿：

```java
// 在Event Dispatch Thread启动GUI
SwingUtilities.invokeLater(() -> {
    MainFrame frame = new MainFrame();
    frame.setVisible(true);
});

// 使用SwingWorker执行耗时操作
SwingWorker<Integer, Integer> worker = new SwingWorker<>() {
    @Override
    protected Integer doInBackground() {
        return importService.importCSV(file, callback);
    }
};
worker.execute();
```

---

## 六、数据分析结论 (Data Analysis Insights)

### 6.1 数据集分布

基于导入的310条Vertebral Column数据集记录：

| 分类 | 数量 | 占比 |
|------|------|------|
| Normal | 100 | 32.3% |
| Abnormal | 210 | 67.7% |

### 6.2 指标差异分析

通过统计面板的平均值对比表可以观察到：

| 指标 | Normal均值 | Abnormal均值 | 差异分析 |
|------|------------|--------------|----------|
| Pelvic Incidence | ~51.5 | ~68.7 | 异常组显著更高 |
| Pelvic Tilt | ~13.1 | ~21.5 | 异常组倾斜更大 |
| Lumbar Lordosis | ~42.0 | ~53.0 | 异常组曲率更大 |
| Sacral Slope | ~38.4 | ~47.2 | 异常组斜率更高 |
| Pelvic Radius | ~118.8 | ~119.2 | 差异不显著 |
| Spondylolisthesis | ~0.5 | ~38.4 | 差异最显著（滑脱程度）|

**关键发现**: 滑脱程度（Degree of Spondylolisthesis）是区分Normal和Abnormal最显著的指标，异常组均值高出正常组约38倍。

---

## 七、运行与部署 (Deployment)

### 7.1 一键启动脚本

项目提供 `run.sh` 脚本实现一键配置和启动：

```bash
./run.sh
```

**脚本功能**:
1. 检查Java、Maven、MySQL环境
2. 启动MySQL服务（如未运行）
3. 交互式配置数据库密码
4. 执行init.sql初始化数据库
5. Maven编译项目
6. 运行应用程序

### 7.2 手动运行

```bash
# 1. 初始化数据库
mysql -u root -p < src/main/resources/init.sql

# 2. 配置db.properties
db.url=jdbc:mysql://localhost:3306/vertebral_db
db.user=root
db.password=your_password

# 3. 编译运行
mvn clean compile
mvn exec:java -Dexec.mainClass="com.vertebralcare.Main"
```

---

## 八、总结与展望 (Conclusion)

### 8.1 项目成果

本项目成功实现了一个完整的脊柱病例管理系统，具备：

1. **规范化数据库设计**: 3张表满足BCNF范式，消除数据冗余
2. **完整CRUD功能**: 患者管理、检查记录管理
3. **数据可视化**: JFreeChart饼图、柱状图展示诊断分布
4. **统计分析能力**: 按类别统计、指标均值对比
5. **批量数据导入**: 支持CSV文件导入，自动生成虚拟患者
6. **安全性保障**: PreparedStatement防SQL注入，事务保证数据一致性

### 8.2 技术价值

- 展示了数据库规范化设计的实践应用
- 演示了三层架构的分层解耦思想
- 实现了GUI多线程编程的最佳实践
- 提供了完整的数据导入和统计分析流程

### 8.3 未来扩展

- 集成机器学习模型进行自动诊断预测
- 添加数据导出功能（PDF报告）
- 支持多用户登录和权限管理
- 开发Web版本提供远程访问

---

## 附录：核心文件清单

| 文件路径 | 说明 | 行数 |
|----------|------|------|
| `Main.java` | 程序入口 | ~90 |
| `model/Patient.java` | 患者实体类 | ~92 |
| `model/Exam.java` | 检查记录实体类 | ~200 |
| `model/DiagnosisClass.java` | 诊断类别实体类 | ~60 |
| `dao/DBUtil.java` | 数据库工具类 | ~100 |
| `dao/PatientDao.java` | 患者数据访问 | ~150 |
| `dao/ExamDao.java` | 检查记录数据访问+统计 | ~330 |
| `dao/DiagnosisClassDao.java` | 诊断类别数据访问 | ~100 |
| `service/PatientService.java` | 患者业务逻辑 | ~80 |
| `service/ExamService.java` | 检查记录业务逻辑 | ~90 |
| `service/StatisticsService.java` | 统计分析服务 | ~110 |
| `service/ImportService.java` | CSV导入服务 | ~210 |
| `ui/MainFrame.java` | 主窗口 | ~200 |
| `ui/PatientPanel.java` | 患者管理面板 | ~350 |
| `ui/ExamPanel.java` | 检查记录面板 | ~500 |
| `ui/StatisticsPanel.java` | 统计分析面板 | ~230 |
| `ui/ImportPanel.java` | 数据导入面板 | ~220 |

**总计约 17 个 Java 文件，~3100 行代码**

---

*报告完成日期: 2025年12月*
