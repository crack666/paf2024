package de.vfh.paf.entity.sensor;

/**
 * Interface for the Observer in the Observer Pattern
 */
public interface SensorObserver {

  /**
   * Update the observer with a new value
   * @param value new value
   */
  void update(double value);
}
