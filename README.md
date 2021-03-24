# multiDownloader
多线程多任务下载框架

效果如下：
https://github.com/shuiming11/multiDownloader/blob/main/app.gif

框架依赖了okhttp,gson和eventbus

使用方法：
工程的build.gradle添加
repositories {
        maven { url 'https://jitpack.io' }
    }


allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}


app module或者子module添加：
implementation 'com.github.shuiming11:multiDownloader:1.0'
如果是在子module,需要将implementation改为api,否则会依赖不到


更多具体用法请查看demo
