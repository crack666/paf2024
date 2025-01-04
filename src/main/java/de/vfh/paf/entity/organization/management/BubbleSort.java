package de.vfh.paf.entity.organization.management;

import de.vfh.paf.entity.organization.basic.Employable;

import java.util.List;

/**
 * Concrete Strategy
 * Bubble Sort
 */
public class BubbleSort implements SortEmployable {
  public void sort(List<Employable> employees) {
    int n = employees.size();
    Employable temp;

    for (int i = 0; i < n; i++) {
      for (int j = 1; j < (n - i); j++) {
        if (employees.get(j - 1).compareTo(employees.get(j)) > 0) {
          // swap elements
          temp = employees.get(j - 1);
          employees.set(j - 1, employees.get(j));
          employees.set(j, temp);
        }
      }
    }
  }
}
