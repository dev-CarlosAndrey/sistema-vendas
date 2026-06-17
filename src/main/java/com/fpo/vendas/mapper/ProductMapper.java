package com.fpo.vendas.mapper;

import com.fpo.vendas.dto.request.ProductRequest;
import com.fpo.vendas.dto.response.ProductResponse;
import com.fpo.vendas.model.NonPerishableProduct;
import com.fpo.vendas.model.PerishableProduct;
import com.fpo.vendas.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequest request) {
        if (request == null) return null;

        if ("PERISHABLE".equalsIgnoreCase(request.type())) {
            PerishableProduct perishable = new PerishableProduct();
            mapBaseFields(request, perishable);
            perishable.setExpirationDate(request.expirationDate());
            return perishable;
        } else {
            NonPerishableProduct nonPerishable = new NonPerishableProduct();
            mapBaseFields(request, nonPerishable);
            nonPerishable.setWarrantyMonths(request.warrantyMonths());
            return nonPerishable;
        }
    }

    private void mapBaseFields(ProductRequest request, Product product) {
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCategory(request.category());
    }

    public ProductResponse toResponse(Product entity) {
        if (entity == null) return null;

        String type = (entity instanceof PerishableProduct) ? "PERISHABLE" : "NON_PERISHABLE";
        java.time.LocalDate expDate = (entity instanceof PerishableProduct p) ? p.getExpirationDate() : null;
        Integer warranty = (entity instanceof NonPerishableProduct np) ? np.getWarrantyMonths() : null;

        return new ProductResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getCategory(),
                type,
                expDate,
                warranty
        );
    }


}
