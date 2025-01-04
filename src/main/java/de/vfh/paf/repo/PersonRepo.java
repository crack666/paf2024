package de.vfh.paf.repo;

import de.vfh.paf.entity.Person;
import org.springframework.data.repository.CrudRepository;

public interface PersonRepo extends CrudRepository<Person, Long> {
}
