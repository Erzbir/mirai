# Mirai Console - Run

Mirai Console 可以独立启动，也可以被嵌入到某个应用中。

## 使用工具自动独立启动

官方: https://github.com/iTXTech/mirai-console-loader  
第三方: https://github.com/LXY1226/MiraiOK

## 手动配置独立启动

强烈建议使用自动启动工具，若无法使用，可以参考如下手动启动方式。

### 环境
- JRE 8+ / JDK 8+

### 准备文件

要启动 Mirai Console，你需要：
- mirai-core-qqandroid 
- mirai-console 后端
- mirai-console 任一前端
- 相关依赖

只有 mirai-console 前端才有入口点 `main` 方法。目前只有一个 terminal 前端可用。

#### 从 JCenter 下载模块

mirai 在版本发布时会将发布的构建存放与 [mirai-bintray-repo]。

- mirai-core 会提供 [mirai-core-all]
- mirai-console 与其各个模块都会提供 `-all` 的 Shadowed 构建

```shell script
# 注: 自行更换对应版本号

# Download mirai-core-all

curl -L https://maven.aliyun.com/repository/public/net/mamoe/mirai-core-all/1.3.3/mirai-core-all-1.3.3-all.jar -o mirai-core-all-1.3.3.jar

# Download mirai-console

curl -L https://maven.aliyun.com/repository/public/net/mamoe/mirai-console/1.0.0/mirai-console-1.0.0-all.jar -o mirai-console-1.0.0.jar

# Download mirai-console-terminal

curl -L https://maven.aliyun.com/repository/public/net/mamoe/mirai-console-terminal/1.0.0/mirai-console-terminal-1.0.0-all.jar -o mirai-console-terminal-1.0.0.jar

```

### 启动 mirai-console-terminal 前端

1. 下载如下三个模块的最新版本文件并放到一个文件夹内 (如 `libs`)(详见 [下载模块](#从-jcenter-下载模块))：
   - mirai-core-all
   - mirai-console
   - mirai-console-terminal

2. 创建一个新的文件, 名为 `start-mirai-console.bat`/`start-mirai-console.ps1`/`start-mirai-console.sh`

Windows CMD:
```shell script
@echo off
title Mirai Console
java -cp "./libs/*" net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader %*
pause
```

Windows PowerShell:
```shell script
$Host.UI.RawUI.WindowTitle = "Mirai Console"
java -cp "./libs/*" net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader $args
pause
```

Linux:
```shell script
#!/usr/bin/env bash
echo -e '\033]2;Mirai Console\007'
java -cp "./libs/*" net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader $*
```

然后就可以开始使用 mirai-console 了。

#### mirai-console-terminal 前端参数
使用 `./start-mirai-console --help` 查看 mirai-console-terminal 支持的启动参数。

[mirai-repo]: https://github.com/project-mirai/mirai-repo/tree/master/shadow
[mirai-bintray-repo]: https://bintray.com/him188moe/mirai
[mirai-core-all]: https://bintray.com/him188moe/mirai/mirai-core-all


## 嵌入应用启动（实验性）

Mirai Console 可以嵌入一个 JVM 应用启动。

### 环境

- JDK 1.8+ / Android SDK 26+ (Android 8+)
- Kotlin 1.4+

### 添加依赖

[选择版本](ConfiguringProjects.md#选择版本)

`build.gradle.kts`:
```kotlin
repositories {
    jcenter()
}
dependencies {
    implementation("net.mamoe:mirai-console:1.0.1")
    implementation("net.mamoe:mirai-console-terminal:1.0.1")
    implementation("net.mamoe:mirai-core:1.3.3")
}
```

### 启动 Terminal 前端

一行启动：
```kotlin
MiraiConsoleTerminalLoader.startAsDaemon()
```

注意, Mirai Console 将会以 '守护进程' 形式启动，不会阻止主线程退出。

### 从内存加载 JVM 插件（实验性）

在嵌入使用时，插件可以直接加载：

```kotlin
```kotlin
MiraiConsoleTerminalLoader.startAsDaemon()
// 先启动 Mirai Console

// Kotlin
Plugin.load() // 扩展函数
Plugin.enable() // 扩展函数 

// Java
PluginManager.INSTANCE.loadPlugin(Plugin)
PluginManager.INSTANCE.enablePlugin(Plugin)
```

但注意：这种方法目前是实验性的——一些特定的功能如注册扩展可能不会正常工作。