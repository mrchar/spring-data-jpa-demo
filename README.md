# SpringDataJPA Demo

本项目用于演示SpringDataJPA的使用，通过渐进式的演示用例，逐步介绍JPA的使用方式。

## JPA

JPA并不是具体的ORM实现，而是一条持久化规范，SpringDataJPA是基于Hibernate的JPA实现。

## 演示用例

### 定义对象

根据面向对象的思想，我们会在程序中将各种概念抽象为对象，并通过将持久化对象的方式保存系统中的数据。

为了使用JPA持久化对象，首先要定义实体类，使用@Entity注解将类标注为可持久化的类。SpringDataJPA在启动时会扫描到这些类，并根据注解进行处理。

```java

@Getter
@Entity
@NoArgsConstructor
public class Student {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String name;
    private Integer age;

    public Student(String name, Integer age) {
        this.name = name;
        this.age = age;
    }
}
```

在上面的例子中，我们使用@Entity注解将Student类标注为实体类，实体类必须定义主键，所以我们需要使用@Id注解将Student的id属性定义为主键。

另外，Hibernate还提供了多种主键赋值方式，可以使用@GeneratedValue注解来指定,常用的类型包括自增、序列、UUID或者表格，当然也可以交由Hibernate根据数据库管理系统类型自行选择，这里我们使用最简单的自增。

### 定义仓库

在JPA规范中，将数据访问层抽象为仓库，也就是是忽略数据持久层的具体实现，把他理解成一个存放实体类的大房子，需要什么实体类就去里面获取，通常是根据主键去获取，也可以根据条件查找。

使用@Repository注解标注仓库接口，Spring启动后会扫描到这些仓库定义，并通过动态代理的方式创建仓库。

```java

@Repository
public interface StudentRepository extends CrudRepository<Student, Long> {
}
```

上文中，我们使用@Repository注解标注实体类仓库，并继承CrudRepository接口，其中传入的泛型参数分别是实体类类型和主键类型。

现在我们就已经可以使用这个仓库进行持久化操作了，我们并不需要实现这个接口，因为SpringDataJPA会在启动时使用动态代理实现持久化方法，而我们定义的StudentRepository已经从CrudRepository继承了基本的Crud方法。

下面我们就可以编写一个测试用例来验证我们的程序。

```java

@DataJpaTest
class StudentRepositoryTest {
    @Autowired
    StudentRepository studentRepository;

    @Test
    void save() {
        Student student = new Student("小明", 8);
        Student saved = studentRepository.save(student);
        Assertions.assertNotNull(saved);
        Assertions.assertEquals(1L, saved.getId());
        Assertions.assertEquals("小明", saved.getName());
        Assertions.assertEquals(8, saved.getAge());
    }
}
```

在上面的代码中，我们使用@DataJpaTest注解将StudentRepositoryTest定义为一个持久层测试类，Spring会在测试启动时组装持久化层。

不用为我们还没有创建数据库和表而担心，因为我们在依赖中引入了H2数据库，SpringDataJPA会在测试中自动创建基于内存的数据库，并根据实体类定义创建表。

运行上面的测试，测试通过，说明我们的代码已经可以正确工作了。

### 关联表

SpringDataJPA会根据实体类的名字，按照默认的策略自动创建表名，如果你想要显式指定实体类关联的表名，可以使用@Table注解标注实体类关联的表名。

```java

@Getter
@Entity
@Table(name = "student")
@NoArgsConstructor
public class Student {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "age")
    private Integer age;

    public Student(String name, Integer age) {
        this.name = name;
        this.age = age;
    }
}
```

这对于使用JPA编程是非常有利的，因为在程序迭代的过程中，我们很有可能会对实体类进行重命名，如果没有绑定表名，当实体类名称发生变化时，有可能就和以前的表失去了关联。

另外，还可以使用@Column关联实体类属性和表的列，确保所有的属性都显式绑定列是一个非常好的习惯，这样可以确保修改属性名时依然关联原来的列。

### 自定义持久化方法

在上面的例子中，我们定义了实体类仓库，可以使用save方法保存数据，查看CrudRepository方法可以发现，虽然它实现了大多数的CRUD操作，但是对于不同的实体类，我们往往会有不同的查询条件，这些就需要我们写代码去定义了。

JPA实现了基于名称推断操作的功能，我们只需要按照规范定义方法名，就可以实现持久化操作。

```java

@Repository
public interface StudentRepository extends CrudRepository<Student, Long> {
    Student findOneByName(String name);
}
```

比如，我们为StudentRepository实现了findOneByName的方法，SpringDataJPA会自动推断，我们需要根据Student的name属性查找一个单例的Student。

运行下面的测试：

```java

@DataJpaTest
class StudentRepositoryTest {
    @Autowired
    StudentRepository studentRepository;

    @Test
    void findOneByName() {
        Student student = new Student("小明", 8);
        Student saved = studentRepository.save(student);

        Student found = studentRepository.findOneByName("小明");
        Assertions.assertNotNull(found);
        Assertions.assertEquals(1L, found.getId());
        Assertions.assertEquals("小明", found.getName());
        Assertions.assertEquals(8, found.getAge());
    }
}
```

测试成功，就说明我们编写的findOneByName的方法可以使用了。

处理使用方法名创建持久化操作，我们也可以直接编写查询，只不过使用的不是SQL语句，而是JPQL语句，SpringDataJPA会在运行时会根据连接的数据库管理系统类型将JPQL语句组装为对应的SQL语法。

比如上面的findOneByName可以使用@Query注解定义为：

```java

@Repository
public interface StudentRepository extends CrudRepository<Student, Long> {
    @Query("select s from Student s where s.name = :name")
    Student findOneByName(@Param("name") String name);
}
```

其中`:name`对应的是@Param注解指定的参数名，当然你也可以省略@Param注解，直接使用方法参数名。除了`:`前缀可以定义参数外，`?`
前缀也可以定义参数，区别在于前者会对参数进行字符转义，避免发生SQL注入。

当前，如果JPQL不能实现你的需求，也可以是使用原生SQL定义查询，只不过要自己去控制兼容的数据库类型。

```java

@Repository
public interface StudentRepository extends CrudRepository<Student, Long> {
    @Query("select * from student where name = :name")
    Student findOneByName(@Param("name") String name);
}
```

### 对象关系

在使用关系型数据库中，最重要的当然是对象之间的关系，我们可以使用关系注解标注各种实体类之间的关系，可以使用的关系包括:
@OneToOne @OneToMany @ManyToOne @ManyToMany。

比如当我们定义学生班级之前的关系时，可以使用@ManyToOne注解。

```java

@Getter
@Entity
@Table(name = "student")
@NoArgsConstructor
public class Student {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "age")
    private Integer age;
    @ManyToOne
    @JoinColumn(name = "class_room_id")
    private ClassRoom classRoom;

    public Student(String name, Integer age) {
        this.name = name;
        this.age = age;
    }
}
```

其中，@ManyToOne注解说明Student实体类和ClassRoom实体类之间的关系是多对一，@JoinColumn表示student表的列class_room_id关联class_room表的id。当使用关系查询时，会使用student表的class_room_id查询class_room.

使用关系查询有两种方式，第一种是直接使用查询出来的实体类的get方法获取关系对象，比如：

```java

@DataJpaTest
class StudentRepositoryTest {
    @Autowired
    StudentRepository studentRepository;
    @Autowired
    ClassRoomRepository classRoomRepository;

    @Test
    void findClassRoom() {
        Student student = new Student("小明", 8);
        studentRepository.save(student);

        ClassRoom classRoom = new ClassRoom(3, 2);
        classRoomRepository.save(classRoom);

        student.setClassRoom(classRoom);
        studentRepository.save(student);

        Student found = studentRepository.findOneByName("小明");
        Assertions.assertNotNull(found);
        Assertions.assertEquals(1L, found.getClassRoom().getId());
        Assertions.assertEquals(3, found.getClassRoom().getLevel());
        Assertions.assertEquals(2, found.getClassRoom().getIndex());
    }
}
```

在上面的测试中，我们首先创建了Student并进行持久化，然后创建了ClassRoom并进行持久化，随后使用setClassRoom关联Student和ClassRoom。

在随后的查询中，我们使用getClassRoom获取小明的classRoom，可以想象SpringDataJPA在查询student后会在合适的时候查询student的classRoom，并使用getClassRoom方法返回结果。

另外一种方式是使用JPQL定义关联查询，JPQL语法支持使用面向对象的查询方式，可以直接在语句中定义关系查询。

```java

@Repository
public interface ClassRoomRepository extends CrudRepository<ClassRoom, Long> {
    @Query("select s.classRoom from Student s where s.name = :studentName")
    ClassRoom findOneByStudentName(@Param("studentName") String studentName);
}
```

我们可以看到，在定义查询条件的时候，可以直接使用`s.classRoom`语句定义关系查询，JPA会自动帮我们组装联表查询。

在上面的示例中，我们定义了Student关联的ClassRoom，对应的ClassRoom也对应多个Student，应该使用@OneToMany注解定义在ClassRoom中，与Stuent中的@ManyToOne定义不同，@OneToMany定义不需要增加任何外键，而是由student表中的class_room_id列映射。所以我们需要在注解中添加mappedBy属性，表明这个关系是由Student实体类中的classRoom属性上的@ManyToOne注解定义的。

```java

@Getter
@Entity
@Table(name = "class_room")
@NoArgsConstructor
public class ClassRoom {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "level")
    private Integer level;

    @Column(name = "index")
    private Integer index;

    @OneToMany(mappedBy = "classRoom")
    Set<Student> students;

    public ClassRoom(Integer level, Integer index) {
        this.level = level;
        this.index = index;
    }
}
```

现在我们换一种方式重新实现上面的关系查询：

```java

@Repository
public interface ClassRoomRepository extends CrudRepository<ClassRoom, Long> {
    @Query("select c from ClassRoom c inner join c.students s where s.name = :studentName")
    ClassRoom findOneByStudentNameImpl2(@Param("studentName") String studentName);
}
```

在上面的实现中，我们使用了`inner join`
关键字，其中s是使用c.students声明的，这表明了s和c的关系，所以我们不需要使用`on`
关键字指定联表条件，JPA会自动判断根据student表的外键class_room_id关联class_room表。

### 对象嵌入

### 查询结果

### 分页排序


