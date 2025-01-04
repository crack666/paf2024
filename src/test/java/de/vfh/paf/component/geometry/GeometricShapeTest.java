package de.vfh.paf.component.geometry;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
class GeometricShapeTest {

  @Autowired
  @Qualifier("square")
  GeometricShape geometricShape;

  @Autowired
  Rectangle rectangle;


  @Test
  void calcAreaOfShape() {
    // given
    // when
    float area = geometricShape.calcArea();
    // then
    assertEquals(0, area);
  }

  @Test
  void calcAreaOfRectangle() {
    rectangle.setSideALength(10);
    rectangle.setSideBLength(20);
    float area = rectangle.calcArea();
    assertEquals(200, area);
  }

}
