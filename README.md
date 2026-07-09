[README.md](https://github.com/user-attachments/files/29849448/README.md)
# HITSZ-AircraftWar-APP
AircraftWar 是一个基于 Android 原生 Java 开发的飞机大战游戏。项目包含单人闯关、双人在线对战/合作、登录注册、排行榜、音效控制、多种敌机与道具系统，适合作为 Android 游戏开发、面向对象设计模式和 Socket 通信实践项目。

## 功能特性

- 单人模式：支持简单、普通、困难三种难度。
- 在线模式：支持账号登录/注册后进入在线匹配。
- 双人玩法：包含经典对战模式和双人实时合作模式。
- 排行榜：本地保存游戏分数，并按分数排序展示。
- 敌机系统：普通敌机、精英敌机、强化精英敌机、Boss 等多种敌机。
- 道具系统：加血、炸弹、火力增强等补给道具。
- 子弹策略：直线、散射、环形、追踪、发散等射击策略。
- 音频控制：支持背景音乐和游戏音效开关。

## 技术栈

- 开发语言：Java
- 平台：Android
- 构建工具：Gradle / Android Gradle Plugin 7.2.0
- Android SDK：`compileSdk 32`，`minSdk 21`，`targetSdk 32`
- 主要依赖：
  - AndroidX AppCompat
  - Material Components
  - JUnit / Espresso
  - `app/libs/json.jar`

## 目录结构

```text
.
├── app/
│   ├── build.gradle
│   ├── libs/
│   │   └── json.jar
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/edu/hitsz/
│       │   ├── activity/      # 页面入口、登录注册、排行榜、游戏界面
│       │   ├── aircraft/      # 英雄机、友机、敌机、Boss
│       │   ├── application/   # 图片、音乐、控制器、全局配置
│       │   ├── basic/         # 飞行物基类、观察者接口
│       │   ├── bullet/        # 英雄/敌机/追踪子弹
│       │   ├── factory/       # 飞机与道具工厂
│       │   ├── game/          # 游戏主循环与不同难度实现
│       │   ├── prop/          # 道具实现
│       │   ├── score/         # 本地排行榜 DAO
│       │   ├── strategy/      # 射击策略
│       │   └── DAO/           # 在线用户相关 DAO
│       └── res/
│           ├── drawable-nodpi/ # 游戏贴图资源
│           ├── layout/         # Activity 布局
│           └── raw/            # 背景音乐和音效
├── gradle/
├── pics/                       # 项目展示截图
├── build.gradle
└── settings.gradle
```

## 快速开始

### 环境要求

- Android Studio
- JDK 8 或以上
- Android SDK Platform 32
- 一台 Android 设备或模拟器

### 构建项目

在项目根目录执行：

```bash
./gradlew assembleDebug
```

Windows 环境可执行：

```bash
gradlew.bat assembleDebug
```

构建成功后，Debug APK 会生成在：

```text
app/build/outputs/apk/debug/app-debug.apk
```

### 在 Android Studio 中运行

1. 使用 Android Studio 打开项目根目录。
2. 等待 Gradle 同步完成。
3. 选择 `app` 运行配置。
4. 连接真机或启动模拟器。
5. 点击 Run 启动游戏。

## 在线模式配置

在线模式需要配套服务器支持。客户端服务器地址和端口配置在 `app/build.gradle`：

```gradle
buildConfigField "String", "SERVER_HOST", "\"10.253.206.219\""
buildConfigField "int", "AUTH_SERVER_PORT", "9999"
buildConfigField "int", "GAME_SERVER_PORT", "8888"
```

其中：

- `SERVER_HOST`：认证服务器和游戏服务器所在主机地址。
- `AUTH_SERVER_PORT`：登录/注册服务端口。
- `GAME_SERVER_PORT`：在线游戏匹配与通信端口。

如果使用 Android 模拟器访问宿主机服务，通常可以将地址配置为 `10.0.2.2`。代码中也提供了连接回退逻辑，会依次尝试 `SERVER_HOST`、`10.0.2.2`、`127.0.0.1` 和 `localhost`。

## 玩法说明

1. 进入游戏后可选择简单、普通、困难模式开始单人游戏。
2. 玩家通过触控移动英雄机，自动发射子弹攻击敌机。
3. 击败敌机会获得分数，不同敌机分值不同。
4. 拾取道具可恢复生命、清屏或增强火力。
5. 游戏结束后可保存分数并在排行榜中查看。
6. 在线模式需要先登录或注册账号，再选择经典对战或双人实时模式。

## 设计亮点

- 模板方法模式：`Game` 定义游戏主流程，`EasyGame`、`MediumGame`、`HardGame`、`CoopGame` 扩展不同难度和玩法。
- 工厂模式：通过 `AircraftFactory`、`PropFactory` 等工厂类创建敌机和道具。
- 策略模式：通过 `ShootStrategy` 及其实现类切换不同射击方式。
- 观察者模式：炸弹道具通过观察者机制通知敌机和敌方子弹销毁。
- DAO 分层：排行榜和用户数据通过 DAO 类封装读写逻辑。

## 常用开发命令

```bash
# 构建 Debug APK
./gradlew assembleDebug

# 运行单元测试
./gradlew test

# 运行 Android Instrumentation 测试
./gradlew connectedAndroidTest

# 清理构建产物
./gradlew clean
```

## 注意事项

- 在线功能依赖外部服务器，未启动服务器时登录、注册和在线匹配会连接失败。
- 本地排行榜使用应用私有文件 `scores.txt` 保存，卸载应用后数据会被清除。
- 游戏画面基准尺寸配置在 `GameConfig` 中，当前设计尺寸为 `1080 x 2160`。
- 项目包名在 Manifest 中为 `edu.hitsz`，应用 ID 为 `com.example.aircraft_war`。

