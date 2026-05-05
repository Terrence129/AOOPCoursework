# CHC6186 AOOP Sudoku Coursework 写作与实现计划

## 1. 作业目标与评分机制

本 coursework 占模块总成绩 50%。任务是用 Java 实现两个版本的 Sudoku：

- GUI version：使用 Swing，并严格按照 MVC 架构实现。
- CLI version：命令行版本，必须复用 GUI 版本同一个 Model。
- 两个版本都以同一个 Sudoku Model 为核心，所有游戏规则、完成检测、验证逻辑、文件读取、flags 都应在 Model 中。

提交物必须是三份文件：

- `{YourStudentNumber}-coursework.pdf`
- `{YourStudentNumber}-coursework.zip`
- `{YourStudentNumber}-coursework.mp4`

PDF 是主要评分依据，必须包含 UML class diagram、JUnit 测试截图、Java 代码打印、GitHub 私有仓库链接与 commit 记录截图。如使用 AI 工具，还应按 handbook 要求在附录放入 AI 使用声明。

## 2. 评分项拆解

| 评分项 | 分值 | A 档要求 | 实现策略 |
|---|---:|---|---|
| Model | 15% | 方便 Controller/View/JUnit 使用；无多余 public 方法；无 View/Controller 引用；无 GUI 代码；继承 Observable；调用 `setChanged()` 和 `notifyObservers()` | 建立 `SudokuModel extends Observable` 作为唯一 Model 门面；文件读取和规则检测全部在 Model 层 |
| Controller | 5% | 只转发有效请求；调用 Model 前先查询合法性；可启用/禁用按钮；无 GUI 绘制代码 | `SudokuController` 只处理输入事件与按钮状态，不保存棋盘规则 |
| GUI View | 10% | Swing；实现 Observer；`update()` 覆盖全部 FR；有 Model 和 Controller 属性；调用 `addObserver` | `SudokuView implements Observer`；负责显示 9x9 网格、按钮、flags、虚拟键盘 |
| CLI | 10% | 覆盖全部 FR；复用同一 Model；没有单独 Controller/View；视频中演示 | `SudokuCLI` 直接读取用户命令并调用 `SudokuModel` |
| Model asserts/specification | 10% | 所有 public 方法都有 formal pre/postconditions；包含 class invariants | Java assertions + Javadoc specification；报告中列出 formal logic 版本 |
| JUnit | 10% | 三个显著不同的 Model 测试；场景描述清楚；输入满足 preconditions | 测试 validation、undo/reset、completion 三类不同场景 |
| Code quality | 10% | 命名、格式、轻量注释、无 long method/duplicated code/low cohesion | 用 Week 1 code quality 标准审查；短方法、清晰职责 |
| Class diagram | 10% | 所有 attributes/methods/visibility/relationships 正确；展示 Observable/Observer 关系 | UML 中显示 Model/View/Controller/CLI/library relationships 和关键 calls |
| Video | 20% | 5 分钟内；演示 FR/NFR；展示代码和程序运行；表达清楚 | 按脚本录制 GUI、CLI、JUnit、代码结构、asserts |

## 3. 课程内容如何服务 coursework

Week 1：OOP、UML、代码质量

- 报告中的 class diagram 应使用正确的 UML 可见性、属性、方法、继承、实现、关联、依赖。
- 代码必须体现 encapsulation：字段私有，Model 通过受控 public API 暴露状态。
- 避免 `switch/type field` 处理变体行为；优先使用清晰方法和对象职责。
- 按 lecture 要求避免 duplicated code、long method、large class、poor naming。

Week 2：MVC 与 Swing

- GUI 版本必须严格采用 MVC。
- Model 存储状态并修改状态，变化后通知 View。
- View 观察 Model，在 `update(Observable, Object)` 中重新渲染。
- Controller 把鼠标、键盘、按钮操作转换为 Model 方法调用，并负责按钮启用/禁用。

Week 3-5：设计模式

- Observer 是本作业核心模式：`SudokuModel extends Observable`，`SudokuView implements Observer`。
- Command 思想可用于 Undo：用 `Move` 或 `CellChange` 对象记录一次用户操作，以便撤销。
- State/Strategy/Decorator 等不一定强行使用，但报告可说明没有滥用模式，保持设计简单。
- OCP 思想用于让 GUI/CLI 共享 Model：新增界面不改规则代码。

Week 6：Design by Contract、assertions、JUnit

- Model public methods 需要 precondition、postcondition、invariant。
- Java `assert` 用于运行时检查，例如坐标范围、值范围、棋盘尺寸、不变量。
- JUnit 测试应对应 postconditions，重点测试 Model，而不是 GUI。

Week 7-9：Java/C#/.NET

- Java 5/7/8 内容可用于提升实现质量：generics、enhanced for、enum、try-with-resources、lambda listener。
- coursework 本身必须使用 Java；不能提交 JavaScript，也不能改写参考网站代码。
- C#/.NET 主要服务考试，不应写进 Java Sudoku 实现，除非 PDF 附录中用于学习反思，但非必需。

## 4. 需求核对清单

### Functional Requirements

- FR1：完成检测在 Model 中；GUI 弹窗确认；CLI 文本确认。
- FR2：Model 可随时检测行、列、3x3 宫重复；validation feedback 开启时 GUI 高亮、CLI 提示；关闭时允许临时无效状态，但完成检测仍必须严格正确。
- FR3：三个 boolean flags 存在 Model 中：validation feedback、hint enabled、random/fixed puzzle selection。GUI 可运行时修改；CLI 可不支持运行时修改。
- FR4：只有初始空格可编辑；预填充格不能修改、清除、撤销；输入只接受 1-9。
- FR5：GUI 按钮包含 Erase、Undo、Hint、Reset、New Game，并遵守 flags 和预填充限制。
- FR6：GUI 显示 9x9 网格、3x3 粗分隔；区分预填充/可编辑；支持鼠标选择、键盘导航、物理键盘输入、虚拟 1-9 键盘。
- FR7：CLI 显示可读网格；支持 set、clear、undo、hint、reset、new game；复用同一个 Model。

### Non-Functional Requirements

- NFR1：GUI 和 CLI 是两个独立程序，各自有 `main` 方法。
- NFR2：GUI 严格 MVC：Model 无 UI，View 无游戏逻辑，Controller 协调输入和 Model。
- NFR3：CLI 直接使用 Model，不单独定义 View/Controller。
- NFR4：Model 统一执行 Sudoku 规则；非法输入不导致崩溃；assertions 捕获 invariants。
- NFR5：JUnit 测试 Model，覆盖 valid 和 invalid scenarios。
- NFR6：包含 class diagram，并有足够注释/文档解释关键设计。
- NFR7：代码可读、结构清楚、命名合适、无重复代码。

## 5. 推荐项目结构

```text
src/
  sudoku/
    model/
      SudokuModel.java
      Cell.java
      Move.java
      PuzzleLoader.java
      SudokuSolver.java
    gui/
      SudokuGuiMain.java
      SudokuView.java
      SudokuController.java
      SudokuCellButton.java
    cli/
      SudokuCliMain.java
test/
  sudoku/
    SudokuModelTest.java
resources/
  puzzles.txt
docs/
  uml-class-diagram.png
```

如果课程要求使用普通 Java project 而不是 Maven/Gradle，也可以保持类似 package 结构；关键是 ZIP 中项目可运行，PDF 中代码完整。

## 6. Model Public API 草案

Model 的 public 方法应少而够用，避免为了 View 暴露内部实现。

```java
public class SudokuModel extends Observable {
    public SudokuModel(Path puzzleFile);
    public int getValueAt(int row, int col);
    public boolean isPreFilled(int row, int col);
    public boolean isEditable(int row, int col);
    public boolean setValue(int row, int col, int value);
    public boolean clearValue(int row, int col);
    public boolean undo();
    public boolean revealHint(int row, int col);
    public void reset();
    public void newGame();
    public boolean isComplete();
    public boolean isBoardValid();
    public boolean isCellInvalid(int row, int col);
    public boolean isValidationFeedbackEnabled();
    public void setValidationFeedbackEnabled(boolean enabled);
    public boolean isHintEnabled();
    public void setHintEnabled(boolean enabled);
    public boolean isRandomPuzzleSelectionEnabled();
    public void setRandomPuzzleSelectionEnabled(boolean enabled);
    public boolean invariant();
}
```

坐标统一使用 `0..8`，CLI 可把用户输入的 `1..9` 转换为内部坐标。

`coursework/puzzles.txt` 当前有 10001 行，每行 81 位数字，`0` 表示空格；文件没有提供答案。因此 Hint 应由 Model 层调用 `SudokuSolver` 计算一个当前谜题的解，再把某个空 editable cell 填成正确值。Solver 是 Model 内部 helper，不应被 GUI/CLI 直接调用。

## 7. Model Invariants 与 Contracts

建议在报告和代码注释中明确这些 invariants：

- `board != null && board.length == 9`
- `forall r,c: 0 <= board[r][c].value <= 9`
- `forall r,c: board[r][c] != null`
- `forall r,c: initialBoard[r][c] != 0 -> board[r][c].value == initialBoard[r][c]`
- flags 始终有定义：`validationFeedbackEnabled`、`hintEnabled`、`randomPuzzleSelectionEnabled`
- undo 记录如果存在，只能指向 editable cell。

典型 public method specification：

```text
setValue(row, col, value)
@pre 0 <= row < 9 && 0 <= col < 9
@pre 1 <= value <= 9
@pre isEditable(row, col)
@post 若 validation feedback 开启且该值违反规则，则棋盘不改变并返回 false
@post 否则 board[row][col] == value，并保存一次可 undo 的用户操作
@post invariant()
```

```text
undo()
@pre true
@post 若存在可撤销操作，则对应 editable cell 恢复到 old value
@post 预填充 cell 不改变
@post invariant()
```

```text
isComplete()
@pre true
@post result == 所有 cell 非 0 且所有 row/column/box 无重复 1..9
@post 不修改棋盘状态
```

## 8. JUnit 三个测试设计

测试 1：validation 与临时非法状态

- 场景：validation feedback 开启时，同一行重复值应被拒绝；关闭时可存在临时非法状态，但 `isComplete()` 仍为 false。
- 覆盖 FR2、FR4、NFR4。

测试 2：undo 与 reset

- 场景：设置 editable cell 后 undo 恢复旧值；reset 清空所有用户输入并保留预填充格。
- 覆盖 FR5。

测试 3：completion detection

- 场景：使用辅助方法把 Model 设置成完整正确棋盘，`isComplete()` 为 true；再制造一个完整但重复的棋盘，`isComplete()` 为 false。
- 覆盖 FR1。

每个测试方法必须有注释说明 scenario，所有测试输入应满足方法 preconditions。

## 9. PDF 写作结构

建议 PDF 按以下顺序写，完全贴合 brief：

1. Cover Page
   - Module：CHC6186 Advanced Object-Oriented Programming
   - Coursework：Sudoku GUI and CLI
   - Student number
   - Repository link

2. Design Overview
   - 简述 GUI/CLI 共用 Model。
   - 说明 MVC 分工：Model、View、Controller、CLI 各自职责。
   - 说明 Observer 如何让 View 随 Model 更新。

3. UML Class Diagram
   - 插入高清类图。
   - 必须显示 attributes、methods、visibility、parameters、return types。
   - 显示 `SudokuModel extends Observable`、`SudokuView implements Observer`、View/Controller/Model 关联、CLI 到 Model 的依赖。

4. Functional Requirement Evidence
   - 用表格逐条对应 FR1-FR7，写明实现类/方法和截图位置。

5. Model Specification with Assertions
   - 写 class invariants。
   - 写每个 Model public method 的 pre/postconditions。
   - 插入关键 assert 代码片段。

6. Unit Testing
   - 说明三个 JUnit tests 的 scenario。
   - 插入测试通过截图。
   - 简述每个测试对应的 postcondition。

7. Code Quality
   - 简短说明命名、封装、短方法、消除重复、轻量注释。
   - 可引用 Week 1 的 code quality principles。

8. Git Evidence
   - 私有 repo 链接。
   - commit history 截图。
   - 强调 early and frequent commits。

9. Implementation Code
   - 打印所有 Java 代码。
   - 建议按 package 顺序：model、gui、cli、test。

10. Appendix
   - AI usage declaration，如使用 AI。
   - 任何引用来源，如使用 Stack Overflow/教程/库文档。

## 10. 5 分钟视频脚本

建议控制在 4 分 30 秒左右，留缓冲。

| 时间 | 内容 |
|---:|---|
| 0:00-0:25 | 自我介绍，说明这是 Java Sudoku GUI + CLI，GUI 使用 MVC，CLI 复用 Model |
| 0:25-1:10 | 展示项目结构和 UML：Model extends Observable，View implements Observer，Controller 转发请求 |
| 1:10-2:35 | GUI 演示：选择 cell、物理键盘输入、虚拟键盘输入、invalid highlight、Erase、Undo、Hint、Reset、New Game、完成弹窗 |
| 2:35-3:25 | CLI 演示：打印棋盘、set、clear、undo、hint、reset、new game、完成文本提示 |
| 3:25-4:15 | 展示 Model 代码：rules、flags、completion detection、file loading、assertions |
| 4:15-4:45 | 展示 JUnit 三个测试全部通过 |
| 4:45-5:00 | 总结已满足 FR/NFR，结束 |

## 11. 实施时间表

Day 1：需求拆解、Git 私有仓库、项目结构、`puzzles.txt` 加载、Model 基础结构。

Day 2：Model 完整功能：validation、editable/pre-filled、set/clear、undo、hint、reset、new game、completion。

Day 3：assertions/specification、JUnit 三个测试、修正 Model API。

Day 4：GUI MVC：View 网格、Controller、按钮、flags、键盘和虚拟键盘。

Day 5：CLI、代码质量清理、UML 类图、报告素材截图。

Day 6：PDF 整理、ZIP 打包、视频录制、最终检查。

## 12. 最终提交前检查表

- [ ] GUI 和 CLI 都能独立运行。
- [ ] GUI/CLI 使用同一个 `SudokuModel`。
- [ ] `SudokuModel` 无 GUI 代码，无 View/Controller 引用。
- [ ] `SudokuModel` 继承 `Observable` 并在状态变化后通知 observers。
- [ ] `SudokuView` 实现 `Observer`，并在 `update()` 中刷新显示。
- [ ] Controller 不绘制 GUI，只转发有效请求并控制按钮状态。
- [ ] 三个 flags 存在 Model 中，GUI 可运行时修改。
- [ ] 预填充 cell 不能修改、清除、撤销。
- [ ] 只接受数字 1-9。
- [ ] completion detection 完全在 Model 中。
- [ ] reset/new game 不触发 completion。
- [ ] hints 不覆盖预填充 cell，且受 hint flag 控制。
- [ ] JUnit 三个测试显著不同并全部通过。
- [ ] Model public methods 有 pre/postconditions；class invariants 明确。
- [ ] UML 类图包含所有属性、方法、可见性、关系。
- [ ] PDF 包含 UML、测试截图、代码、repo link、commit 截图。
- [ ] MP4 不超过 5 分钟且小于 1GB，可正常播放。
- [ ] ZIP 包含完整 Java project 和 `puzzles.txt`。
- [ ] 文件名严格使用 `{YourStudentNumber}-coursework.*`。
