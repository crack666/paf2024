package de.vfh.paf.plain.di;

import de.vfh.paf.plain.factory.IProduct;

/**
 * Implementation for the persistence of products via file serialization/deserialization.
 * This is just a dummy implementation to show the dependency injection.
*  Only one product is saved in-memory. It is not used in the application.
 */
public class ProductFilePersistence implements IProductPersistence {

  private IProduct product;
  @Override
  public void save(IProduct product) {
    this.product = product;
    System.out.println("ProductFilePersistence.save " + product.toString());
  }

  @Override
  public void delete(IProduct product) {
    this.product = null;
    System.out.println("ProductFilePersistence.delete " + product.toString());
  }

  @Override
  public void update(IProduct product) {
    this.product = product;
    System.out.println("ProductFilePersistence.update " + product.toString());
  }

  @Override
  public IProduct findByName(String name) {
    return this.product;
  }
}
