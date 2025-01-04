package de.vfh.paf.component.hello;

import de.vfh.paf.component.geometry.Rectangle;
import org.springframework.beans.factory.annotation.Autowired;

public class PersonPojoExample {

  private String name;
  private Integer age;

  @Autowired // Field DI
  private Rectangle rectangle;

  public PersonPojoExample() {
    // rectangle = new Rectangle();  // ? Concern & Responsibility: Rectangle-Erzeugung, nein: DI
  }

  public PersonPojoExample(String name, Integer age) {
    this.name = name;
    this.age = age;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getAge() {
    return age;
  }

  public void setAge(Integer age) {
    this.age = age;
  }

  @Override
  public String toString() {
    return "PojoExample{" +
      "name='" + name + '\'' +
      ", age=" + age +
      '}';
  }
}
