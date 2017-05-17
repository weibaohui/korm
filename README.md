[![Maven Central](https://img.shields.io/maven-central/v/com.sdibt/korm.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.sdibt%22%20AND%20a%3A%22korm%22)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)

# korm
kotlin orm




#Doc
[gitbook](https://weibaohui.gitbooks.io/korm/)



#简介

当前处于测试阶段。欢迎您提供建议意见。

本项目受PDF.NET项目启发，并参考了其OQL的实现原理，将其移植到kotlin下。并在此基础上进行了功能扩展。

##Maven坐标
```
<dependency>
    <groupId>com.sdibt</groupId>
    <artifactId>korm</artifactId>
    <version>X.X.X</version>
</dependency>
```
##Gradle
```
compile 'com.sdibt:korm:X.X.X'
```
##便利点：
1、编译阶段提供字段检查。避免修改字段而没修改sql语句造成的错误。

2、OQL语句接近于SQL，降低学习成本。

3、提供丰富的SQL执行日志，方便排查问题。

4、支持Entity、OQL两种操作方式。

5、集成Springboot 后，可以使用@Repository继承BaseRepository<EntityBase>获取CRUD基本操作。无需编写实现逻辑。并且支持spring data jpa 风格的查询语句。
```
@Repository
interface TestBookRepository : BaseRepository<TestBook>{
    fun get10ByTestNameOrderByTestIdDesc(name:String):List<TestBook>
}
```

无需写具体的实现逻辑，执行后get10ByTestNameOrderByTestIdDesc("abc")
转换为select * from testbook where test_name='abc' order by test_id desc limit 10
结果集映射为List<TestBook>

6、支持多数据源，以及读写分离（一主多从）

7、支持自动填充createdAt、createdBy、updatedAt、updatedBy

8、支持软删除，删除操作改为填充deletedAt字段

9、支持version乐观锁

10、数据库交互以callback链方式执行，可以进行按需扩展

##支持数据库
1.mysql
2、oracle ：未测试
3、postgres ：未测试
4、sqlite ：未测试

##交流

QQ群号：637927287
