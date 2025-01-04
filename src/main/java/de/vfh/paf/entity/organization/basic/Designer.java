package de.vfh.paf.entity.organization.basic;

public class Designer extends Employee {

  public Designer(String name, String position) {
    super(name, position);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void print() {
    System.out.println("Designer [name=" + name + "]");
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getPosition() {
    return position;
  }

  @Override
  public void setPosition(String position) {
    this.position = position;
  }

  @Override
  public String toString() {
    return "Designer [name=" + name + "]";
  }
}
