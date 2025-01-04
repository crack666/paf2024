package de.vfh.paf.component.geometry;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component("rectangle")
@NoArgsConstructor
@Getter
@Setter
public class Rectangle implements GeometricShape {

  private float sideALength = 0;
  private float sideBLength = 0;

  public Rectangle(float sideALength, float sideBLength) {
    this.sideALength = sideALength;
    this.sideBLength = sideBLength;
  }
  @Override
  public float calcArea() {
    return sideALength * sideBLength;
  }

  @Override
  public void setSides(float... sideLengths) {
    this.sideALength = sideLengths[0];
    this.sideBLength = sideLengths[1];
  }
}
