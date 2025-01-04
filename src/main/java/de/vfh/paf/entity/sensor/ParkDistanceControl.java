package de.vfh.paf.entity.sensor;

/**
 * Concrete Observer: The PDC is basically a threshold notifier in this simplified example
 */
class ParkDistanceControl implements SensorObserver {
  private double threshold; // Threshold value = distance limit

  /**
   * Constructor
   * @param threshold threshold value = lower limit for distance
   */
  public ParkDistanceControl(double threshold) {
    this.threshold = threshold;
  }

  /**
   * Update the observer with a new value
   * @param distance new value for the distance
   */
  @Override
  public void update(double distance) {
    if (distance < threshold) { // Check if distance exceeds threshold
      // notify the user ("Alert" is printed in red)
      System.out.println("\n\033[31mALERT\033[0m: Distance exceeded threshold! Current distance = " + distance);
    } else {
      // no need to notify the user, but for demonstration purposes we print the distance
      System.out.print("/\rDriving: distance = " + distance);
    }
  }
}
