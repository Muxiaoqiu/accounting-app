# 考拉记账 🐨

一款简洁好用的 Android 记账应用，使用 Kotlin + Jetpack Compose 构建。

## 功能

- **多账本管理** — 创建多个账本，分别管理不同场景的收支（日常、旅行、项目等）
- **收支记录** — 快速记录支出和收入，支持备注和时间选择
- **分类管理** — 内置 30+ 分类，支持自定义分类、图标和排序
- **数据统计** — 支出/收入趋势折线图 + 分类占比饼图，支持周/月/年切换
- **预算管理** — 年度/月度/分类三级预算，独立设置提醒开关和阈值
- **超支提醒** — 首页醒目提示即将超支或已超支的预算

## 技术栈

| 技术 | 用途 |
|------|------|
| Kotlin | 开发语言 |
| Jetpack Compose | UI 框架 |
| Material 3 | 设计系统 |
| Room | 本地数据库 |
| Navigation Compose | 页面导航 |
| Coroutines + Flow | 异步数据流 |

## 项目结构

```
app/src/main/java/com/muxiaoqiu/accounting/
├── AccountingApp.kt          # Application，初始化数据库和默认分类
├── MainActivity.kt           # 入口 Activity
├── data/
│   ├── dao/                  # Room DAO
│   │   ├── BookDao.kt
│   │   ├── BudgetDao.kt
│   │   ├── CategoryDao.kt
│   │   └── TransactionDao.kt
│   ├── database/
│   │   └── AppDatabase.kt    # 数据库定义和迁移
│   └── entity/               # 数据实体
│       ├── Book.kt
│       ├── Budget.kt
│       ├── CategoryEntity.kt
│       └── Transaction.kt
└── ui/
    ├── navigation/
    │   └── AppNavigation.kt  # 导航路由
    ├── screens/              # 页面和 ViewModel
    │   ├── BookListScreen.kt
    │   ├── BookViewModel.kt
    │   ├── HomeScreen.kt
    │   ├── AccountingViewModel.kt
    │   ├── AddTransactionScreen.kt
    │   ├── StatisticsScreen.kt
    │   ├── StatisticsViewModel.kt
    │   ├── BudgetScreen.kt
    │   ├── BudgetViewModel.kt
    │   ├── BudgetAlertCard.kt
    │   ├── CategoryManageScreen.kt
    │   └── CategoryViewModel.kt
    └── theme/
        ├── Theme.kt
        └── CategoryIcons.kt
```

## 构建

```bash
# 要求：Android Studio Hedgehog+ / JDK 17

./gradlew assembleDebug
```

## 截图

| 账本列表 | 记账首页 | 统计页面 |
|---------|---------|---------|
| （待添加） | （待添加） | （待添加） |

## 许可

MIT
