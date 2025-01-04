package de.vfh.paf.plain.di;


public class App {


  public static void main(String[] args) {
    ProductMan productMan = new ProductMan(new ProductFilePersistence());

  }
}
