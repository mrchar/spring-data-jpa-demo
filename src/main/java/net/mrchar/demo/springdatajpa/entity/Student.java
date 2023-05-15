package net.mrchar.demo.springdatajpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static jakarta.persistence.GenerationType.IDENTITY;

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
    @Setter
    @ManyToOne
    @JoinColumn(name = "class_room_id")
    private ClassRoom classRoom;

    public Student(String name, Integer age) {
        this.name = name;
        this.age = age;
    }
}
