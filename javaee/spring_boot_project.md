#使用sts_eclipse创建spring boot项目
1. sts下载地址

    [https://spring.io/tools/sts/all](https://spring.io/tools/sts/all)
2. 安装sts

    创建sts创建string boot项目，创建spring stater project项目，如下图

    ![image.png](http://pblog.okgoes.com/upload/images/2018_7_20/blog_okgoes_com1532078392_659.png)

    点击next，如图

    ![image.png](http://pblog.okgoes.com/upload/images/2018_7_20/blog_okgoes_com1532078562_755.png)

    点击next，如图

    ![image.png](http://pblog.okgoes.com/upload/images/2018_7_20/blog_okgoes_com1532078643_78.png)

    勾选需要的组件，DevTools用户热加载，开发时可使用，web用于web项目。

    点击finish完成项目创建，maven自动下载相关依赖。目录结构如下：

    ![image.png](http://pblog.okgoes.com/upload/images/2018_7_20/blog_okgoes_com1532078864_153.png)

    - src/main/java用于存放项目主要代码
    - src/main/resources用于存放资源文件，application.properties为默认配置文件
    - src/test/java存放单元测试代码
    - target目录存放打包后的文件
3. spring boot入口文件代码
    ```java
        package spring.boot;

        import org.springframework.boot.SpringApplication;
        import org.springframework.boot.autoconfigure.SpringBootApplication;
        
        @SpringBootApplication
        public class SpringbootApplication {

            public static void main(String[] args) {
                SpringApplication.run(SpringbootApplication.class, args);
            }
        }
    ```
4. spring boot项目启动方式

    在入口文件右击run as java Application

    ![image.png](http://pblog.okgoes.com/upload/images/2018_7_20/blog_okgoes_com1532079359_573.png)
5. 使用maven打包spring boot项目

    - 命令方式
    ```bash
    #在项目根目录下执行
    mvn package
    ```
    打包生成的jar包在target目录下。
    - sts 打包
    右击项目选择run as maven install，如图

    ![image.png](http://pblog.okgoes.com/upload/images/2018_7_20/blog_okgoes_com1532079620_967.png)
6. spring boot项目部署

    使用java -jar命令执行打包后的jar包
    ```bash
    #linux下运行jar包
    java -jar springboot.jar
    #linux下后台运行jar包项目
    nohup java -jar springboot.jar &
    ```
