package de.vfh.paf.plain.di;

import de.vfh.paf.plain.factory.IProduct;

/**
 * Implementation for the persistence of products using a database.
 * This is just a dummy implementation to show the dependency injection.
 *  Only one product is saved in-memory. It is not used in the application.
 */
public class ProductDBPersistence implements IProductPersistence {
  @Override
  public void save(IProduct product) {
    System.out.println("ProductDBPersistence.save " + product.toString());
  }

  @Override
  public void delete(IProduct product) {
    System.out.println("ProductDBPersistence.delete " + product.toString());
  }

  @Override
  public void update(IProduct product) {
    System.out.println("ProductDBPersistence.update " + product.toString());
  }

  @Override
  public IProduct findByName(String name) {
    return null;
  }
}
