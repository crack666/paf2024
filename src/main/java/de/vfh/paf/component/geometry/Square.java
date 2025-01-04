package de.vfh.paf.component.geometry;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component("square")
@NoArgsConstructor
@Getter
@Setter
public class Square implements GeometricShape {

  private float sideLength = 0;

  public Square(float sideLength) {
    this.sideLength = sideLength;
  }
  @Override
  public float calcArea() {
    return sideLength * sideLength;
  }

  @Override
  public void setSides(float... sideLengths) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
