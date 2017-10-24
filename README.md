# spring-ddal简介

  Spring DDAL是基于spring AOP和AbstractRoutingDataSource实现了读写分离和分库分表功能，是一款轻量级的插件，简单易用、轻耦合，使用注解即可完成读写分离、分库分表。

  Spring-DDAL的拆分方式与其他中间件不一样，是基于方法注解的方式实现，是一种轻量级的读写分离、分库分表实现。而其他中间件如Amoeba、Cobar、TDDL、Sharding-JDBC以及MyCat等等功能更完备、也更强大。

# Spring-DDAL十分钟快速上手

### Step 01：配置Spring-DDAL.xml文件
  将Spring-DDAL.xml配置文件中dateSourcePointcut部分修改为自己项目的包名。如使用的是MyBatis，也请修改相应的包名。
  
```xml
    <context:component-scan base-package="io.isharing.example"/>
    <import resource="spring-ddal.xml" />
```
    
### Step 02：配置datanodes.xml文件
  主要是datanodes.xml文件中的dataSource和dataNode部分。dataSources部分主要是配置数据源，dataNode部分主要是配置根据数据源配置读写库。看XML文件节点就知道怎么配置。
  数据源配置：
  
```xml
    <dataSources>
        <dataSource name="ds0">
          <url>jdbc:mysql://localhost:3306/example?useUnicode=true&amp;characterEncoding=UTF-8</url>
          <userName>test</userName>
          <password>111111</password>
        </dataSource>
        <dataSource name="ds0_repl1">
          <url>jdbc:mysql://localhost:3306/example?useUnicode=true&amp;characterEncoding=UTF-8</url>
          <userName>test</userName>
          <password>111111</password>
        </dataSource>
        <dataSource name="ds0_repl2">
          <url>jdbc:mysql://localhost:3306/example?useUnicode=true&amp;characterEncoding=UTF-8</url>
          <userName>test</userName>
          <password>111111</password>
        </dataSource>

        <dataSource name="ds1">
          <url>jdbc:mysql://192.168.2.3:3306/example?useUnicode=true&amp;characterEncoding=UTF-8</url>
          <userName>test</userName>
          <password>111111</password>
        </dataSource>
        <dataSource name="ds1_repl1">
          <url>jdbc:mysql://192.168.2.3:3306/example?useUnicode=true&amp;characterEncoding=UTF-8</url>
          <userName>test</userName>
          <password>111111</password>
        </dataSource>
        <dataSource name="ds1_repl2">
          <url>jdbc:mysql://192.168.2.3:3306/example?useUnicode=true&amp;characterEncoding=UTF-8</url>
          <userName>test</userName>
          <password>111111</password>
        </dataSource>
    <dataSources>
```

    数据节点配置：
```xml
    <dataNodes>
        <dataNode name="global">
          <writeNodes>ds0</writeNodes>
          <readNodes>ds0_repl1, ds0_repl2</readNodes>
        </dataNode>
        <dataNode name="dn0">
          <writeNodes>ds0</writeNodes>
          <readNodes>ds0_repl1, ds0_repl2</readNodes>
        </dataNode>
        <dataNode name="dn1">
          <writeNodes>ds1</writeNodes>
          <readNodes>ds1_repl1, ds1_repl2</readNodes>
        </dataNode>
        <dataNode name="dn2">
          <writeNodes>ds2</writeNodes>
          <readNodes>ds2_repl1, ds2_repl2</readNodes>
        </dataNode>
	  </dataNodes>
```

Step03：配置rule.xml文件
  配置拆分规则rule.xml文件，这里可以使用规则模板直接复制修改相应的规则即可。
```xml
    <tableRule name="userRangeRule" class="io.isharing.springddal.route.rule.function.AutoPartitionByLong">
        <property name="defaultNode">userlt2000w</property><!-- 默认写节点，可以定义为最新的库节点 -->
        <property name="routeColumn">id</property><!-- 分片键名称 -->
        <property name="mapFile">autopartition-long.txt</property><!-- 规则配置文件 -->
    </tableRule>
    <tableRule name="orderDateRule" class="io.isharing.springddal.route.rule.function.PartitionByYear">
        <property name="defaultNode">waybilldb2017</property>
        <property name="routeColumn">orderDate</property>
        <property name="dateFormat">yyyy-MM-dd</property>
        <property name="sBeginDate">2016-01-01 00:00:01</property>
        <!-- <property name="sEndDate">2016-08-01 23:59:59</property> -->
    </tableRule>
```
  主要配置tableRule的name属性和property的值即可。具体看文件中的规则说明。

Step 04：其他配置文件
  其他配置与常规的web工程和spring的配置一致，无须改变。

Step05：项目中开发注解配置
  按常规的项目，假设你的项目架构也是有DAO	层的，而DAO层对应的是一个SQL查询操作，如果是这样就简单了，你可以这样操作，在你的DAO层的实现类上使用@Router注解实现读写分离、分库分表，可定义在类名上也可定义在方法名上。如定义在方法名上的：
  
```java
    @Router(dataNode="dn1,dn2",ruleName="part-by-rang-long")
    public List<StudentEntity> getStudentByClassID(int classId){
    ……
    }
```

另外，如果定义有BaseDao的公用父类和方法，则需要在BaseDao的公用方法中定义@Transactional（org.springframework.transaction.annotation.Transactional）表明该方法为写操作，如果是写操作该注解不能定义readOnly为true。

# Router注解说明及示例

  Router注解主要有如下几个属性：isRoute、forceReadOnMaster、readOnly和ruleName。
  其中（红色必须）：
      isRoute：			#是否拆分，默认为拆分
      forceReadOnMaster：	#强制将读操作在写库中读，以避免写的时候从读库读不到	数据（主从同步延迟导致的问题）
      readOnly：			#是否读操作，默认true
      ruleName：			#拆分规则名，对应rule.xml的tableRule的name属性。
      dataNode：			#定义数据节点名称，对应datanode.xml文件部分定义。
      type：				#拆分类型，普通规则拆分默认为空。全局库/表须定义为：global
      
	使用举例：
	示例一：
 ```java
      @Router(dataNode="dn1,dn2",ruleName="part-by-rang-long",forceReadOnMaster=true)
      public List<StudentEntity> getStudentByClassID(int classId){
      ……
      }
```
	说明：示例一为强制从主库读，拆分规则为：part-by-rang-long。

示例二：
```java
      @Router(dataNode="dn1,dn2",ruleName="part-by-rang-long")
      public int saveStudent (Student stu){
      ……
      }
```
	说明：示例二为按拆分规则写数据，拆分规则为：part-by-rang-long

示例三：
```java
      @Router(isRoute=false, dataNode="dn1,dn2,dn3",ruleName="part-by-rang-long")
      public List<StudentEntity> getStudentByClassID(int classId){
      ……
      }
```
	说明：示例三为虽配置了Router，但是这里isRoute=false不配置拆分，默认会走part-by-rang-long规则中定义的defaultNode进行读操作。
	
示例四：
```java
      @Repository
      @Router(isRoute=true, dataNode="dn1,dn2",ruleName="part-by-year")
      public class StudentDaoImpl extends BaseDaoImpl<Waybill, java.lang.Long> implements WaybillDao {
      ……
      }
```
  说明：示例四为基于类的注解配置，但是如果方法上也有注解配置，那么方法中的配置将会覆盖类上的配置信息，不重叠部分不变。比如例三和例四种，最后isRoute的值会是false，并且getStudentByClassID的Router注解值会变成：isRoute=false, dataNode="dn1,dn2,dn3",ruleName="part-by-rang-long"。
  
注意事项：
  a)	同一个表，Router注解建议都定义是类名上；
  b)	如果DAO层是MyBatis生成的，无实现方法，则须在类名上定义Router注解，方法上定义的无效；
  c)	Router定义只针对物理表对应的DAO，如果是一个DAO有多个表操作，则无法支持。


