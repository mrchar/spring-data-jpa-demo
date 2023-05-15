package net.mrchar.demo.springdatajpa.repository;

import net.mrchar.demo.springdatajpa.entity.ClassRoom;
import net.mrchar.demo.springdatajpa.entity.Student;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class StudentRepositoryTest {
    @Autowired
    StudentRepository studentRepository;
    @Autowired
    ClassRoomRepository classRoomRepository;

    @Test
    void save() {
        Student student = new Student("小明", 8);
        Student saved = studentRepository.save(student);
        Assertions.assertNotNull(saved);
        Assertions.assertEquals(1L, saved.getId());
        Assertions.assertEquals("小明", saved.getName());
        Assertions.assertEquals(8, saved.getAge());
    }

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