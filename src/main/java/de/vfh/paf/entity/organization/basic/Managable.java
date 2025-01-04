package de.vfh.paf.entity.organization.basic;

public interface Managable extends Employable {
  @Override
  String getName();

  @Override
  void print();

  @Override
  void setName(String name);

  @Override
  String getPosition();

  @Override
  void setPosition(String position);

  @Override
  String toString();

  void addSubordinate(Employable employee);
}
