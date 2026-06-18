package com.fpo.vendas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "tb_perishable_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerishableProduct extends Product {

    private LocalDate expirationDate;

    @Override
    public boolean hasSalesRestriction() {
        //LSP: altera o comportamento de forma segura com base no contrato
        return expirationDate != null && expirationDate.isBefore(LocalDate.now());
    }
}