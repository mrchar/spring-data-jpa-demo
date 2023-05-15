package net.mrchar.demo.springdatajpa.repository;

import net.mrchar.demo.springdatajpa.entity.ClassRoom;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassRoomRepository extends CrudRepository<ClassRoom, Long> {
    @Query("select s.classRoom from Student s where s.name = :studentName")
    ClassRoom findOneByStudentName(@Param("studentName") String studentName);

    @Query("select c from ClassRoom c inner join c.students s where s.name = :studentName")
    ClassRoom findOneByStudentNameImpl2(@Param("studentName") String studentName);
}
