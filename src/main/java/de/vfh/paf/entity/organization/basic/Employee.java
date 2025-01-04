package de.vfh.paf.entity.organization.basic;

// Component (Composite Pattern
public abstract class Employee implements Employable {

  protected String name;
  protected String position;

  public Employee(String name, String position) {
    this.name = name;
    this.position = position;
  }

  public abstract String getName();
  public abstract void print();
  public abstract void setName(String name);
  public abstract String getPosition();
  public abstract void setPosition(String position);

  // Vollständigkeit, aber Leaf-Klassen haben keine Subordinates
  // abstract void addSubordinate(Employee employee);
  // oder die Composite-Klasse Manager implementiert die Methode alleine + Type Safety (Polymorphismus--)
  @Override
  public int compareTo(Employable o) {

    return this.name.compareTo(o.getName());
  }

}
