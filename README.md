# 全链路日志跟踪的简单实现

## demo使用方法

https://gitee.com/xyc0123456789/MDCTtlDemo

操作系统：win10，JVM版本：

```shell
java version "1.8.0_101"
Java(TM) SE Runtime Environment (build 1.8.0_101-b13)
Java HotSpot(TM) 64-Bit Server VM (build 25.101-b13, mixed mode)
```

1、首先部署好nacos，配置上nacos_yamls下的两个配置文件

2、对TtlMDCAdapterAgent打包，将TtlMDCAdapterAgent.jar和transmittable-thread-local的jar包准备好，放置在某一路径，例如

`C:/mvn_repository/com/king/TtlMDCAdapterAgent/1.0-SNAPSHOT/TtlMDCAdapterAgent-1.0-SNAPSHOT.jar`

`C:/mvn_repository/com/alibaba/transmittable-thread-local/2.10.2/transmittable-thread-local-2.10.2.jar`

3、在idea中需要打开启动配置的VM options，启动前在VM options文本框中加入：

```java
-javaagent:C:/mvn_repository/com/alibaba/transmittable-thread-local/2.10.2/transmittable-thread-local-2.10.2.jar
-javaagent:C:/mvn_repository/com/king/TtlMDCAdapterAgent/1.0-SNAPSHOT/TtlMDCAdapterAgent-1.0-SNAPSHOT.jar
```

在命令行需要写成如下形式：

```shell
java -Dfile.encoding=utf-8 -javaagent:C:/mvn_repository/com/alibaba/transmittable-thread-local/2.10.2/transmittable-thread-local-2.10.2.jar -javaagent:C:/mvn_repository/com/king/TtlMDCAdapterAgent/1.0-SNAPSHOT/TtlMDCAdapterAgent-1.0-SNAPSHOT.jar -jar mdc-ttl-server-1.0-SNAPSHOT.jar
```

4、成功启动了mdc-ttl-server和mdc-ttl-customer后，向mdc-ttl-customer发送get请求即可看到demo效果

`127.0.0.1:19889/mdc-ttl-customer/rpc1`

`127.0.0.1:19889/mdc-ttl-customer/rpc2`

`127.0.0.1:19889/mdc-ttl-customer/rpc3`

## 之前的实现思路

由于并发时业务日志杂乱无章，没法排查故障，通过搜索看到了`org.slf4j.MDC`通过`ThreadLocal`可以实现日志上的跟踪，一般需要配合AOP。但是这个`ThreadLocal`在创建新线程后的线程内会失效，于是想到了`InheritableThreadLocal`。但是实际业务中一般都是用线程池，当线程复用时会导致这个值不正确，于是在看到了[transmittable-thread-local](https://github.com/alibaba/transmittable-thread-local)，以及logback对应实现的[logback-mdc-ttl](https://github.com/chiwenheng/logback-mdc-ttl)。

1、[logback-mdc-ttl](https://github.com/chiwenheng/logback-mdc-ttl)在使用过程中并没能生效，又因为我不太看得懂logback配置的加载机制和它的spi，于是我选择用java agent对`MDC.bwCompatibleGetMDCAdapterFromBinder`方法进行了替换。这解决了新的子线程获取不到父线程全局变量的问题。

2、参考[transmittable-thread-local](https://github.com/alibaba/transmittable-thread-local)中的使用方法，在VM options中增加相关启动参数对线程池相关类进行增强。这解决了线程复用时全局变量不正确的问题

3、1和2解决了单个应用内变量跟踪，要实现全链路日志跟踪，还需对所有的rpc调用进行拦截。这个项目对常见的RestTemplate和Feign的方式进行了拦截。具体实现主要分为两部分，一个是对调用方的拦截，一个是对被调用方的拦截。

3.1、对调用方的拦截，RestTemplate和Feign两者实现上有部分不同，RestTemplate需要实现

`public class RestTrackInterceptor implements ClientHttpRequestInterceptor`，并通过`restTemplate.setInterceptors(Collections.singletonList(restTrackInterceptor));`使它生效。

，Feign需要实现`public class FeignTrackInterceptor implements RequestInterceptor `，并通过注解`@FeignClient(name = "mdc-ttl-server", configuration = FeignTrackInterceptor.class)`使它生效

3.2、对被调用方的拦截，主要通过实现`public class HttpIntercepter implements HandlerInterceptor`，并通过

```java
@Configuration
public class IntercepterConfig implements WebMvcConfigurer {
    @Autowired
    private HttpIntercepter httpIntercepter;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(httpIntercepter);
    }
}
```

配置使它生效即可。

## 当下实现思路

因为这样子的配置还是有一定的侵入性，我想更进一步可以通过java agent以及javassist字节码修改来预增强调用端底层的代码。对`feign.SynchronousMethodHandler#executeAndDecode`,`org.springframework.web.client.RestTemplate#doExecute`，`org.springframework.web.servlet.HandlerExecutionChain#applyPreHandle`这三个方法分别对feign方式的rpc调用，restTemplate方式的rpc调用，被调用端拦截的增强。

现在在应用未配置各种拦截器的情况下（不需要上面“之前的实现思路”的3的全部实现），只需要在idea中需要打开启动配置的VM options，启动前在VM options文本框中加入：

```
-javaagent:C:/mvn_repository/com/alibaba/transmittable-thread-local/2.10.2/transmittable-thread-local-2.10.2.jar
-javaagent:C:/mvn_repository/com/king/TtlMDCAdapterAgent/1.0-SNAPSHOT/TtlMDCAdapterAgent-1.0-SNAPSHOT.jar
```

即可实现feign和RestTemplate的链路信息的传递

