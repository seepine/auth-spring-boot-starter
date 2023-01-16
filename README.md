# 本仓库已停止维护，请使用用法一致的https://github.com/seepine/secret



# ~~auth-spring-boot-starter~~

~~easy auth for springboot~~

## ~~一、集成~~

### ~~1.引入依赖~~

- Latest
  Version: [![Maven Central](https://img.shields.io/maven-central/v/com.seepine/auth-spring-boot-starter.svg)](https://search.maven.org/search?q=g:com.seepine%20a:auth-spring-boot-starter)
- Maven:

```xml

<dependency>
  <groupId>com.seepine</groupId>
  <artifactId>auth-spring-boot-starter</artifactId>
  <version>${latest-version}</version>
</dependency>
```

### ~~2.注解简介~~

- @Expose/@NotExpose 暴露/不暴露接口
- @Secret/@NotSecret 接口请求头加密，可大大避免通过F12得知接口地址和传参恶意调用
- @Permission/@PermissionPrefix 接口鉴权，快速实现用户、角色、权限功能
- @RateLimit 接口速率限制，支持秒/分/时/天，可用于例如短信/邮箱发送、注册、下单、支付等，被恶意刷量
- @Log 快速实现日志记录

## ~~二、基本使用~~

> ~~@Expose/@NotExpose~~

### ~~1.获取token~~

登录接口使用注解`@Expose`暴露，获取到用户信息后调用AuthUtil.loginSuccess，该方法将会返回用户token 并且可传入不同用户信息，比如User，比如UserVo

```java
public class Controller {
  @Expose
  @GetMapping("/login/{username}/{password}")
  public R login(@PathVariable String username, @PathVariable String password) {
    User user = userService.getByUsername(username, password);
    return R.ok(AuthUtil.loginSuccess(user));
  }

  @Expose
  @GetMapping("/login/{code}")
  public R login(@PathVariable String code) {
    UserVO user = userService.getByCode(code);
    return R.ok(AuthUtil.loginSuccess(user));
  }
}
```

### 2.请求接口

请求接口时，请求头中加上{'token':'xxxxxxxxxxxxxxxxxx'}`其中token后的字符串由登录接口获得`，即可在方法中通过AuthUtil.getUser()
获取到当前登录者的用户信息

```java
public class Controller {
  @GetMapping("/info")
  public R info() {
    Object obj = AuthUtil.getUser();
    User user = AuthUtil.<User>getUser();
    // UserVO userVO=AuthUtil.<UserVO>getUser();
    return R.ok(user);
  }

  // 暴露接口使用注解
  @Expose
  @GetMapping("/info")
  public void info() {
    // ...
  }
}
```

### 3.自定义配置

常用自定义的配置

```yml
auth:
  header: custom_token #请求头参数，如{"custom_token":"asfoav5h35v43692"}
  cache-prefix: xxx #缓存redis的key
  timeout: 3600 #登录有效期,单位秒
  reset-timeout: true #是否自动续租token过期时间
```

## 三、接口加密

> @Secret/@NotSecret

### 1.获取RSA公私钥

若自己有rsa公私钥可跳过此步骤

```java
class Main {
  public static void main(String[] args) {
    RSA rsa = new RSA();
    rsa.getPublicKey();
    rsa.getPrivateKey();
  }
}
```

### 2.配置解密私钥

```yml
auth:
  secret:
    rsa-private-key: xxx #rsa私钥，配置该项则开启
    timeout: 60 #允许超时时间，单位秒，默认为3分钟
```

### 3.指定接口需要密文鉴权

一般用于高安全性接口，例如验证码发送/支付接口等。通过此方式无法完全避免安全性问题， 建议勤更换公私钥或加入行为验证码

```java

@RestController
public class Controller {
  @Secret
  @GetMapping("/pay")
  public void pay() {
    // ...
  }
}
```

### 4.前端附带加密请求头

其中secret的值为公钥加密时间戳的值，并且时间戳与后端时间默认相差不超过4小时

```json
{
  "secret": "xxxxxxxxxxxxxxxxxx"
}
```

### 5.重写规则

若想自定义规则，可实现该接口并注入bean即可

```java
public interface AuthSecretService {
  void verify(String secretValue) throws AuthException;
}
```

## 四、接口鉴权

> @Permission/@PrePermission

使用接口鉴权注解时，需要在登陆时传入用户所拥有的权限list，例如`AuthUtil.loginSuccess(user,permissionList)`

### 1.单独使用Permission

```java

@RestController
public class Controller {
  // 必须拥有'add'权限才可访问
  @Permission("add")
  @GetMapping("/add")
  public void add() {
  }

  // 必须拥有'edit'权限才可访问
  @Permission("edit")
  @GetMapping("/edit")
  public void edit() {
  }

  // 必须拥有'edit'和'del'权限才可访问
  @Permission({"edit", "del"})
  @GetMapping("/edit/and/del")
  public void editAndDel() {
  }

  // 拥有'del_all'或者'administrator'权限即可访问
  @Permission(or = {"del_all", "administrator"})
  @GetMapping("/del/all")
  public void delAll() {
  }


  @Resource
  Service service;

  @GetMapping("/del/all")
  public void func() {
    //也会需要鉴权，可得知@Permission不仅仅可加在接口上，只要是spring容器接管的都可以（原理使用aop实现）
    service.func();
  }

}

@Service
class Service {
  @Permission("service_permission_a")
  public void func() {
  }
}
```

### 2.使用PermissionPrefix为所有权限加上前缀

正常使用场景中，一般的权限会如同`xxx_add`,`yyy_add`,`zzz_add`,`xxx_edit`这般，前面带有模块或业务的标识，当然使用`@Permission`
直接指定具体权限也是可以的例如`@Permission("xxx_add")`，但是一般业务按Controller划分，同一个Controller中所有接口的权限前缀基本是相同的

```java

@PermissionPrefix("sys_user_")
@RestController
public class Controller {
  // 必须拥有'sys_user_add'权限才可访问
  @Permission("add")
  @GetMapping("/add")
  public void add() {
  }

  // 必须拥有'sys_user_edit'权限才可访问
  @Permission("edit")
  @GetMapping("/edit")
  public void edit() {
  }

  // 必须拥有'sys_role_edit'权限才可访问
  // prefix为false时，不会拼接类上@PermissionPrefix的前缀
  @Permission(value = "sys_role_edit", prefix = false)
  @GetMapping("/role/edit")
  public void roleEdit() {
  }
}
```

### 3.实现带鉴权功能的BaseController

一般业务都会有crud接口，所以我们可以抽离出BaseController结合PermissionPrefix快速实现crud接口并且拥有接口鉴权功能

- BaseController

```java
public class BaseController<S, T> {
  @Resource
  S service;

  @Permission("add")
  @GetMapping("/add")
  public void add(@RequestBody T entity) {
    service.add(entity);
  }

  @Permission("edit")
  @GetMapping("/edit")
  public void edit() {
    service.edit();
  }
}
```

- UserController

```java

@RestController
@PermissionPrefix("user_")
@RequestMapping("user")
public class UserController extends BaseController<UserService, User> {
}
```

此时实现了用户新增和编辑功能，并且新增和编辑需要拥有权限分别是`user_add`和`user_edit`，并且当重写父类方法时，权限注解仍然有效

## 五、接口限速

> @RateLimit

```java

@RestController
public class Controller {
  // 每秒最多请求10次
  @RateLimit(10)
  @GetMapping("/rate1")
  public void rate1() {
    // ...
  }

  // 每小时最多请求50次
  @RateLimit(hour = 50)
  @GetMapping("/rate2")
  public void rate2() {
    // ...
  }

  // 每秒最多10次且每分钟最多20次
  @RateLimit(second = 10, minute = 20)
  @GetMapping("/rate3")
  public void rate3() {
    // ...
  }
}
```

## 六、日志记录

> @Log

### 1.注解使用

```java

@RestController
public class UserController {
  @Log("新增用户")
  @GetMapping("/add")
  public void add() {
  }

  // 可详细描述日志行为
  @Log(title = "编辑用户", content = "管理员编辑用户")
  @GetMapping("/edit")
  public void edit() {
  }

  // 错误时也会触发日志记录
  @Log("删除用户")
  @GetMapping("/del")
  public void rate3() {
    throw new Exception("用户不存在，删除失败");
  }
}
```

### 2.日志存库

```java

@Component
public class MyAuthLogService implements AuthLogService {
  @Override
  public void save(LogEvent logEvent) {
    // 此处可以将日志自行保存到mysql等
  }
}
```
