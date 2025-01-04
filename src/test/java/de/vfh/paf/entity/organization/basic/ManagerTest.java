package de.vfh.paf.entity.organization.basic;

import org.junit.jupiter.api.Test;

class ManagerTest {

  @Test
  void print() {
    Managable manager = new Manager("John", "Manager");

    Developer developer = new Developer("Tom", "Developer");
    manager.addSubordinate(developer);
    Developer developer2 = new Developer("Jerry", "Developer");
    manager.addSubordinate(developer2);

    Designer designer = new Designer("Alice", "Designer");
    manager.addSubordinate(designer);

    manager.print();
  }
}
