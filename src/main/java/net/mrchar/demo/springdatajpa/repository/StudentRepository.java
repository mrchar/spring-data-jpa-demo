package net.mrchar.demo.springdatajpa.repository;

import net.mrchar.demo.springdatajpa.entity.Student;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends CrudRepository<Student, Long> {
    @Query("select s from Student s where s.name = :name")
    Student findOneByName(@Param("name") String name);
}
