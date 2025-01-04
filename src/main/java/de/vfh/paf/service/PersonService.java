package de.vfh.paf.service;

import de.vfh.paf.entity.Person;
import de.vfh.paf.repo.PersonRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PersonService {


  private final PersonRepo personRepo;

  @Autowired // Constructor injection
  public PersonService(PersonRepo personRepo) {
    this.personRepo = personRepo;
  }

  public List<Person> findPersonById(Long idFilter){
    List<Person> personList = new ArrayList<>();
    if (idFilter == null) {
      personRepo.findAll().forEach(personList::add);
    } else {
      personRepo.findById(idFilter).ifPresent(personList::add);
    }
    return personList;
  }

  public Person upsertPerson(final Person person) {
    Person savedPerson = personRepo.save(person);
    if (savedPerson == null) {
      throw new RuntimeException("Person could not be saved");
    }
    return savedPerson;
  }

  public String analyzePerson(Long id) {
    StringBuilder analysis = new StringBuilder();
    personRepo.findById(id).ifPresent(
      p -> checkForNull(p, analysis));
    return analysis.toString();
  }

  private void checkForNull(Person person, StringBuilder analysis) {
    analysis.append("Person with id: ")
      .append(person.getName()).append(" has no name\n");
  }
}
