package de.vfh.paf.entity.organization.management;

import de.vfh.paf.entity.organization.basic.Employable;

import java.util.List;

/**
 * Strategy Pattern: Strategy Interface
 * Sort employable objects
 */
public interface SortEmployable {
  /**
   * Sorts a list of employees
   * @param employees
   */
   public void sort(List<Employable> employees);
}
