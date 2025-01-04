package de.vfh.paf.plain;

/**
 * Singleton vs static utility class
 * Runtime Polymorphism
 * - Singleton = class (can be extended), Singletons can implement interfaces
 * - Static methods are resolved at compile-time -> can’t be overridden at runtime.Static class can’t benefit from runtime polymorphism
 * - Method Parameters: Singleton = object (pass it to other methods as an argument)
 * - Object State, Serialization, and Cloneability
 * - Loading Mechanism: S. can be lazily loaded
 * - Testing: S. can be mocked
 * - Classloaders: S. can be loaded by different classloaders
 * - Dependency Injection: S. can be injected
 * - Thread Safety: S. can be made thread-safe
 * - Sngleton can be refactored to a multiton easily
 */
public class Singleton {
}
