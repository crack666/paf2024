package de.vfh.paf.entity.sensor;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to simulate a distance sensor to demonstrate the Observer Pattern: Subject
 */
class DistanceSensor implements SensorObservable {
  private List<SensorObserver> observers = new ArrayList<>(); // List of observers
  private double distance; // Sensor measurement = value

  /**
   * Register an observer
   * @param observer observer to be registered
   */
  @Override
  public void registerObserver(SensorObserver observer) {
    observers.add(observer);
  }

  /**
   * Remove an observer
   * @param observer observer to be removed
   */
  @Override
  public void removeObserver(SensorObserver observer) {
    observers.remove(observer);
  }

  /**
   * Notify all observers
   */
  @Override
  public void notifyObservers() {
    for (SensorObserver observer : observers) {
      observer.update(distance);
    }
  }

  /**
   * Method to simulate sensor measurement
   */
  public void setDistance(double distance) {
    this.distance = distance;
    notifyObservers();
  }
}
