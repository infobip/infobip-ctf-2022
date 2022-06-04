package org.ibctf.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.math.BigDecimal;

@Entity
@Table(name = "shopping_item")
@XmlRootElement(name = "shoppingItem")
@XmlAccessorType(XmlAccessType.FIELD)
public class ShoppingItem {

    @XmlTransient
    @JsonIgnore
    @Id
    @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    @XmlElement(name = "name")
    @Column(name = "name")
    private String name;

    @XmlElement(name = "description")
    @Column(name = "description")
    private String description;

    @XmlElement(name = "price")
    @Column(name = "price")
    private BigDecimal price;

    @XmlTransient
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "partner_id")
    private Partner partner;

    public ShoppingItem() {
    }

    public ShoppingItem(Long id, String name, String description, BigDecimal price, Partner partner) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.partner = partner;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Partner getPartner() {
        return partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
    }
}
