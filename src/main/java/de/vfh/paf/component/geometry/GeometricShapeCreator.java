package de.vfh.paf.component.geometry;

public class GeometricShapeCreator {

  /**
   * Private constructor to prevent instantiation
   */
  private GeometricShapeCreator() {
  }

  public static GeometricShape createShape(String shapeName) {
    switch (shapeName) {
      case "rectangle":
        return new Rectangle();
      case "triangle":
        return new Square();
      default:
        throw new IllegalArgumentException("Unknown shape: " + shapeName);
    }
  }
}
