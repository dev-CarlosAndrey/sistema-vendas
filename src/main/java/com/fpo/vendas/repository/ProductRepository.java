package com.fpo.vendas.repository;

import com.fpo.vendas.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
