package com.fpo.vendas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "tb_non_perishable_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NonPerishableProduct extends Product {

    private Integer warrantyMonths;

    @Override
    public boolean hasSalesRestriction() {
        return false;
    }
}