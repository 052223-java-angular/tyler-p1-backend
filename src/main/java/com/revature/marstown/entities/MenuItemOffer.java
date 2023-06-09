package com.revature.marstown.entities;

import java.math.BigDecimal;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "menu_item_offers")
public class MenuItemOffer {
    @Id
    private String id;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "price_currency", nullable = false)
    private String priceCurrency;

    @ManyToOne
    @JoinColumn(name = "menu_item_id")
    @JsonBackReference
    private MenuItem menuItem;

    @Column(name = "min_quantity")
    private Integer minimumQuantity;

    @Column(name = "max_quantity")
    private Integer maximumQuantity;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "menuItemOffer")
    @JsonManagedReference
    private Set<CartMenuItemOffer> cartMenuItemOffers;

    @Column(name = "stripe_price_id")
    private String stripePriceId;
}
