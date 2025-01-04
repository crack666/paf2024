package de.vfh.paf.entity.organization.basic;

public class Developer extends Employee {

  public Developer(String name, String position) {
    super(name, position);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void print() {
    System.out.println("Developer [name=" + name + "]");
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

  // @Override
  void addSubordinate(Employable employee) {
    // Leaf-Klassen haben keine Subordinates
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return "Developer [name=" + name + "]";
  }

}
