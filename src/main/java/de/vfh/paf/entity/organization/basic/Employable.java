package de.vfh.paf.entity.organization.basic;

// Employee interface
// Not good: EmployeeI oder I_Employee
// Not good: EmployeeInterface
// Not good: EmployeeImpl for implentation
public interface Employable extends Comparable<Employable> {
  abstract String getName();
  abstract void print();
  abstract void setName(String name);
  abstract String getPosition();
  abstract void setPosition(String position);

}
