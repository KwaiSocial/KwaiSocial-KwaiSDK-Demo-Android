
# 快手OpenSdk

此Demo是Android端接入快手OpenSdk的完整示例，供开发者参考。

详细接入文档请参考：[快手开放平台](https://open.kuaishou.com/platform/openApi?menu=11)
# Demo 编译配置
## app/build.gradle

- 替换签名文件信息，需要和在快手开放平台上注册的app签名信息一致
- KWAI_APP_ID替换为在快手开放平台上注册的app包名

```
signingConfigs {
    demoConfig {
        storeFile file("")  // todo 请补充签名文件信息
        storePassword ""    // todo 请补充签名文件信息
        keyAlias ""         // todo 请补充签名文件信息
        keyPassword ""      // todo 请补充签名文件信息
        v2SigningEnabled true
    }
}

manifestPlaceholders = [
    "KWAI_APP_ID": "", // todo 请补充申请的appId
    "KWAI_SCOPE" : "user_info" // 需要申请的scope权限，如：user_info(用户基本信息), user_phone(电话号码)，relation(关系链信息)等
]
```
