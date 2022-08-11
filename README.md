```text
      _____ _     _
  __ /__   \ |__ (_)_ __   __ _
 / _` |/ /\/ '_ \| | '_ \ / _` |
| (_| / /  | | | | | | | | (_| |
 \__,_\/   |_| |_|_|_| |_|\__, |
                          |___/

Just a Thing
```

# 设备隧道

## 框架使用

### 添加仓库

```xml
<!-- pom.xml增加仓库 -->
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/athingx/athing-tunnel</url>
    </repository>
</repositories>
```

### 构建客户端

```xml
<!-- pom.xml增加引用 -->
<dependency>
    <groupId>io.github.athingx.athing</groupId>
    <artifactId>athing-tunnel-thing</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

```java
// 构建设备隧道
final var thingTunnel = new ThingTunnelBuilder()
        .secret(THING_SECRET)
        .provider("ssh://127.0.0.1:22?name=LOCAL_SSH&connectTimeout=30000")
        .build(thing)
        .get();
```
