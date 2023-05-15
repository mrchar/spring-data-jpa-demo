package net.mrchar.demo.springdatajpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

import static jakarta.persistence.GenerationType.IDENTITY;

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
