package de.vfh.paf.component.hello;

public interface IHelloComponent {
  String getHello();

  Integer getInstanceId();

  Integer getInstanceValue();

  void setInstanceId(Integer instanceId);

  void setInstanceValue(Integer instanceValue);

  String toString();
}
