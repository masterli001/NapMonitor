# MiMo Orbit 百万亿 Token 申请材料

## 申请网址

https://100t.xiaomimimo.com

## 准备工作

1. 先到 https://platform.xiaomimimo.com 注册 MiMo 开放平台（手机号/小米账号）
2. 到 https://account.xiaomi.com 确认绑定邮箱
3. 三个平台的邮箱必须一致！

---

## 表单字段填写内容

### 邮箱
```
填你小米账号绑定的邮箱（必须和 platform.xiaomimimo.com 注册邮箱一致）
```

### 姓名/昵称
```
填你的真实姓名或常用昵称
```

### 使用的 AI 编程工具
```
Claude Code（主要）、GitHub Copilot（辅助）
```

### 使用的底层模型
```
Claude Opus 4.7 / Claude Sonnet 4.6（当前主力）；
计划切换到 MiMo-V2.5-Pro 以降低开发成本、提升中文场景表现
```

### 项目名称
```
NapMonitor（智能小睡守护仪）
```

### 项目链接（GitHub）
```
如果你有 GitHub 仓库就填仓库地址，如果还没有，先创建并上传代码再填
```

---

### 项目描述（核心字段 — 直接复制以下内容）

> NapMonitor 是一款基于 Android 平台开发的智能小睡守护应用，通过蓝牙低功耗（BLE）协议实时连接智能手表/手环的心率传感器，对用户午睡/小睡过程进行全程心率监测与安全守护。
>
> 核心技术栈：Kotlin + Android Jetpack（ViewPager2、ViewModel、Coroutines/Flow）+ BLE GATT 协议栈。
>
> 主要功能：
> - BLE 实时心率监测：自动扫描并连接支持标准心率服务（0x180D）的设备，解析心率测量特征值（0x2A37），支持 UINT8/UINT16 双格式、RR 间期、能量消耗等完整字段
> - 睡眠状态机引擎：内置7状态状态机，基于心率数据实时驱动状态转换
> - 智能安全守护：当心率持续超过用户设定的阈值达到确认窗口时长，自动触发紧急唤醒闹钟
> - 倒计时闹钟系统：用户可配置守护时长（1-240分钟），精确到秒的倒计时显示
> - 前台服务保活：使用 Android Foreground Service + WakeLock + 通知栏常驻，确保持续监测不中断
> - 自动重连机制：BLE 断连后采用指数退避策略自动重连
> - 持久化偏好配置：基于 SharedPreferences + Flow callbackFlow 实现响应式配置系统
>
> 已实现完整可运行的原型，包含 17 个 Kotlin 源文件，覆盖 BLE 通信层、睡眠检测算法、闹钟调度、UI 交互、通知管理、偏好设置等模块。当前适配 vivo WATCH 3，计划扩展支持更多标准心率设备。
>
> AI 使用场景：本项目从零开始完全由 AI 辅助编码完成（Claude Code），包含架构设计、BLE 协议实现、状态机设计、异常处理等全部环节。申请 MiMo Token 额度后，计划将底层模型从 Claude 切换到 MiMo-V2.5-Pro，在 Claude Code 中继续迭代开发以下功能：云端数据同步、睡眠质量评分 AI 分析、历史趋势图表、多设备适配、以及 Wear OS 手表端独立应用。

### English version (if required by form)

> NapMonitor is an Android-based smart nap guardian app that monitors heart rate in real-time via BLE connection to smartwatches/fitness bands, providing safety protection during naps.
>
> Built with Kotlin + Android Jetpack (ViewPager2, ViewModel, Coroutines/Flow) + BLE GATT protocol. Features include: real-time BLE HR monitoring (standard HRS 0x180D), 7-state sleep detection engine, configurable HR safety alarm, countdown alarm system, foreground service with WakeLock for background persistence, exponential backoff BLE auto-reconnect, and reactive preference management.
>
> Currently 17 Kotlin source files covering BLE, sleep detection, alarm scheduling, UI, notifications, and preferences. Adapting vivo WATCH 3 with plans for broader device support. The entire project was built from scratch using AI-assisted coding (Claude Code). After receiving MiMo tokens, we plan to migrate to MiMo-V2.5-Pro for continued iteration including cloud sync, AI sleep quality analysis, historical trends, multi-device support, and Wear OS companion app.

### 证明材料
```
- GitHub 仓库链接（重要！建议先创建仓库上传代码）
- 可以附上应用截图、代码结构截图
- 如果有 Claude Code 使用记录截图也可以附上
```

---

## GitHub 仓库设置

### 仓库名
```
NapMonitor
```

### Description
```
Smart nap guardian for Android — BLE heart rate monitoring with sleep detection and safety alarm
```

### About（右侧栏）
```
智能小睡守护仪 - Android + BLE 心率监测 + 睡眠检测 + 安全闹钟。通过蓝牙连接智能手表，实时监测午睡心率，异常自动唤醒。
```

### Topics
```
android kotlin ble bluetooth-low-energy heart-rate sleep-monitoring nap health wearable jetpack
```

---

## 申请流程

1. 打开 https://platform.xiaomimimo.com → 注册/登录
2. 打开 https://account.xiaomi.com → 确认邮箱已绑定
3. 打开 https://100t.xiaomimimo.com → 点击「立即申请」→ 填写表单（复制上面的内容）
4. 提交后等待审核（通常数小时到3个工作日）
5. 审核通过后邮件通知 → 登录 platform.xiaomimimo.com → 权益24小时内到账
6. 在 Claude Code 中配置 MiMo API 即可使用

## 重要提示

- 截止日期：2026年5月28日 00:00（北京时间），尽快提交！
- 额度已发出约80万亿/100万亿，剩余有限
- 表单填写越详细、项目越具体，拿到 Max 档（16亿Credits）的概率越高
- 三个平台邮箱必须一致
- 如果3天没收到邮件，可以重新提交
