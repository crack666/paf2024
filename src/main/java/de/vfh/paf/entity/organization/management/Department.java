package de.vfh.paf.entity.organization.management;

import de.vfh.paf.entity.organization.basic.Designer;
import de.vfh.paf.entity.organization.basic.Developer;
import de.vfh.paf.entity.organization.basic.Employable;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Context -> Strategy Pattern
 * Abnehmer des Algorithmus
 */
public class Department {
    private String name;
    private String location;
    private List<Employable> employees = new ArrayList<>();
    // Strategy Pattern
    @Setter
    private SortEmployable sortEmployable;

  /**
   * Constructor
   * @param name
   * @param location
   * @param manager
   */
    public Department(String name, String location, String manager) {
      this.name = name;
      this.location = location;
    }

  /**
   * Main method
   * @param args
   */
  public static void main(String[] args) {
      Department department = new Department("IT", "Berlin", "John Doe");
      System.out.println(department);
      department.setSortEmployable(new BubbleSort()); // Strategy Pattern: setter DI der concrete Strategy
      department.employees.add(new Designer("Charlie", "Designer"));
      department.employees.add(new Developer("Bob", "Developer"));
      department.employees.add(new Designer("Alice", "Designer"));
      department.employees.add(new Developer("David", "Developer"));

      department.sortEmployable.sort(department.employees);
      department.employees.forEach(l -> System.out.println(l.getName()));
    }
}
