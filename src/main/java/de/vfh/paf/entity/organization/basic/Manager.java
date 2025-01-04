package de.vfh.paf.entity.organization.basic;

import java.util.ArrayList;
import java.util.List;

public class Manager extends Employee implements Managable {

//  protected String name; // DRY violation: The field name is duplicated in Manager and Developer
//  protected String postion;
  protected List<Employable> subordinates;

  /**
   * Constructor
   * @param name
   * @param position
   */
  public Manager(String name, String position) {
    super(name, position);
    subordinates = new ArrayList<>();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void print() {
    System.out.println("Manager [name=" + name + "]");
    for (Employable employee : subordinates) {
        employee.print();
    }
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getPosition() {
    return this.position;
  }

  @Override
  public void setPosition(String position) {
  }

  @Override
  public String toString() {
    return "Manager [name=" + name + "]";
  }

  @Override
  public void addSubordinate(Employable employee) {
    subordinates.add(employee);
  }
}
