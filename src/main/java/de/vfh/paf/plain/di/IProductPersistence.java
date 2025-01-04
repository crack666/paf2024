package de.vfh.paf.plain.di;

import de.vfh.paf.plain.factory.IProduct;

/**
 * Interface for the persistence of products.
 */
public interface IProductPersistence {

  void save(IProduct product);
  void delete(IProduct product);
  void update(IProduct product);
  IProduct findByName(String name);

}
