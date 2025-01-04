package de.vfh.paf.entity.sensor;

import java.util.Random;

/**
 * Class for the  using the concrete Observer = ParkDistanceControl
 * The ParkDistanceControl is notified when the distance sensor detects a distance below a certain threshold
 */
public class CarDrive {

  /**
   * Main method to demonstrate the Observer Pattern
   * @param args command line arguments
   */
  public static void main(String[] args) {

    // 1. setup the car
    DistanceSensor sensor = new DistanceSensor(); // Create the subject = observable
    ParkDistanceControl pdc = new ParkDistanceControl(15.0); // Threshold = 15.0
    sensor.registerObserver(pdc);  // Register the observer

    // 2. "Drive" Thread: Simulates the distance sensor (while driving)
    Thread sensorThread = new Thread(() -> {
      Random random = new Random();
      while (true) {
        double randomDistance = random.nextDouble() * 100; // Random distance between 0 and 100
        sensor.setDistance(randomDistance);
        try {
          Thread.sleep(1000); // Wait for 1 second
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });

    // 3. "User" Pseudo Thread: Observer (just observes, no additional thread logic needed here)
    // The notifier will run on the main thread when notified.

    // Start the sensor thread
    sensorThread.start();
  }

}
