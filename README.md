# react-native-douyin-lib

## Getting started

`$ npm install react-native-douyin-lib --save`

### Mostly automatic installation

RN > 0.59 you need do nothing

else

`$ react-native link react-native-douyin`

### Manual installation


#### iOS

1. pod 'react-native-douyin-lib', :path => '../node_modules/react-native-douyin-lib/react-native-douyin-lib.podspec'

2. 为了保证可以正常唤起抖音短视频，在 info 标签栏的Custom iOS Target Properties中找到 LSApplicationQueriesSchemes 如果没有点击“+”添加一个并设置 Key 为LSApplicationQueriesSchemes, Value 类型为数组，将如下配置粘贴到数组中：

 ```
<key>LSApplicationQueriesSchemes</key>
<array> 
<string>douyinliteopensdk</string>
<string>snssdk1128</string>
<string>douyinsharesdk</string>
<string>douyinopensdk</string>
</array>
  ```

3. 配置你的App与抖音短视频通讯的 URLScheme，即申请到的appKey

4. 分享的图片通过相册进行跨进程共享手段，如需使用分享功能，还需要填相册访问权限，在 info 标签栏中添加 Privacy - Photo Library Usage Description,具体请参考抖音开平台。

5.初始化以及UIApplicationDelegate部分#
在AppDelegate中引入DouyinOpenSDKApplicationDelegate.h头文件 并在 App 启动、收到 Open URL 打开 App 时调用 SDK 进行处理
```
#import<DouyinOpenSDK/DouyinOpenSDKApplicationDelegate.h>

@implementation AppDelegate



- (BOOL)application:(UIApplication *)application openURL:(nonnull NSURL *)url options:(nonnull NSDictionary<UIApplicationOpenURLOptionsKey,id> *)options {
 
 if ([[DouyinOpenSDKApplicationDelegate sharedInstance] application:application openURL:url sourceApplication:options[UIApplicationOpenURLOptionsSourceApplicationKey] annotation:options[UIApplicationOpenURLOptionsAnnotationKey]]
        ) {
 return YES;
    }
 
 return NO;
}

- (BOOL)application:(UIApplication *)application openURL:(NSURL *)url sourceApplication:(NSString *)sourceApplication annotation:(id)annotation
{
 
 if ([[DouyinOpenSDKApplicationDelegate sharedInstance] application:application openURL:url sourceApplication:sourceApplication annotation:annotation]) {
 return YES;
    }
 
 return NO;
}
 
- (BOOL)application:(UIApplication *)application handleOpenURL:(NSURL *)url
{
 if ([[DouyinOpenSDKApplicationDelegate sharedInstance] application:application openURL:url sourceApplication:nil annotation:nil]) {
 return YES;
    }
 return NO;
}
@end
```



#### Android

1. React Native 0.60以上会自动链接，其他请手动集成
2. 如遇sdk下载失败,在 project 级别 build.gradle文件中添加：

  	```gradle
   allprojects {
		 repositories {
			   ...
            maven { url 'https://dl.bintray.com/aweme-open-sdk-team/public' }
		 }
   }
  	```
3. 若您的应用的代码存在混淆，若在混淆的情况下存在不能吊起分享的情况，请在您的proguard文件中添加 -keep class com.bytedance.sdk.open.aweme.**

4.请确申请信息的准确性，如包名、md5等
  


## Usage
```javascript
import Douyin from 'react-native-douyin-lib';
```

#### init(appKey) 注册

App 启动后合理的地方初始化。

- `appKey` {String} 

#### auth(scope,state) 注册

- `scope` {String} 请参考抖音开放平台，如'user_info'

- `state` {String} 目前无用

成功的返回:

```ts
interface  IResponse {
   msg: string;
   code: number;
   data: {
      authCode: string;
      state: string;
   }
}
```

#### shareVideo(config) 注册
该方法目前不完善，后续完成

```
interface IShareVideoConfig {
   videos: string[]; // 本地路径
   isPublish: boolean; // 是否跳转到发布页
   title: string; // 标题
   shortTitle: string; // 短标题
}
```
|

