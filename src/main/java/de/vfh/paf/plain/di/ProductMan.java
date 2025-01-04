package de.vfh.paf.plain.di;

import de.vfh.paf.plain.factory.IProduct;

/**
 * ProductMananeger for handling product CRUD operations (Create, Read, Update, Delete).
 * Similar to a service layer that uses a persistence layer (repo, DAO).
 */
public class ProductMan {

  // inject the product persistence
  private IProductPersistence productPersistence;

  // Singleton ?
  // public final ProductMan INSTANCE;
  public ProductMan(IProductPersistence productPersistence) {
    this.productPersistence = productPersistence;
  }

  public void addProduct(IProduct product) {
    productPersistence.save(product);
  }

  public void removeProduct(IProduct product) {
    productPersistence.delete(product);
  }

  public void updateProduct(IProduct product) {
    productPersistence.update(product);
  }

  public IProduct findProduct(String name) {
    return productPersistence.findByName(name);
  }
}
