package de.vfh.paf.component.geometry;

import org.springframework.stereotype.Component;

@Component
public interface GeometricShape {

  float calcArea();

  void setSides(float ... sideLengths);
}
