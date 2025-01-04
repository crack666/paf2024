package de.vfh.paf.plain.factory;

import lombok.ToString;

@ToString
public class Gold implements IProduct {

  private String UoM = "g";
  private Float volume;
  private String name;

  /**
   * Constructor
   * @param volume in cubic centimetre
   * @param name
   */

  public Gold(Float volume, String name) {
    this.name = name;
    this.volume = volume;
  }
  @Override
  public String getName() {
    return name;
  }

  @Override
  public Float getWeight() {
    return 19.3f * volume;
  }

  @Override
  public String getUoM() {
    return UoM;
  }
}
