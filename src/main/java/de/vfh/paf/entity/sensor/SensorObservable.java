package de.vfh.paf.entity.sensor;

/**
 * Interface for the Subject = Observable in the Observer Pattern
 */
interface SensorObservable {
  /**
   * Register an observer
   * @param observer observer to be registered
   */
  void registerObserver(SensorObserver observer);

  /**
   * Remove an observer
   * @param observer observer to be removed
   */
  void removeObserver(SensorObserver observer);

  /**
   * Notify all observers
   */
  void notifyObservers();
}
