package de.vfh.paf.plain.factory;

import lombok.ToString;

@ToString
public class Phone implements IProduct {

  private String UoM = "g";
  private Float weight;
  private String name;
  public Phone(Float weight, String name) {
    this.name = name;
    this.weight = weight;
  }
  @Override
  public String getName() {
    return name;
  }

  @Override
  public Float getWeight() {
    return weight;
  }

  @Override
  public String getUoM() {
    return UoM;
  }
}
