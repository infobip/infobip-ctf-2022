package org.ibctf.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.ibctf.util.WebConst;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.Set;

@Entity
@Table(name = "partner")
public class Partner implements UserDetails {

    @JsonIgnore
    @Id
    @GeneratedValue
    @Column(name = "partner_id")
    private Long id;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "password")
    private String password;

    @JsonIgnore
    @Lob
    @Column(name = "process_template")
    private String template;

    @JsonIgnore
    @OneToMany(mappedBy = "partner")
    private Set<ShoppingItem> shoppingItems;

    @JsonIgnore
    @OneToOne(mappedBy = "partner", cascade = CascadeType.MERGE)
    @PrimaryKeyJoinColumn
    private Avatar avatar;

    public Partner() {
    }

    public Partner(
            Long id,
            String username,
            String password,
            String template,
            Set<ShoppingItem> shoppingItems,
            Avatar avatar) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.template = template;
        this.shoppingItems = shoppingItems;
        this.avatar = avatar;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.createAuthorityList(
                WebConst.AUTHENTICATION_LEVEL_LOW,
                WebConst.AUTHENTICATION_LEVEL_HIGH
        );
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public Set<ShoppingItem> getShoppingItems() {
        return shoppingItems;
    }

    public void setShoppingItems(Set<ShoppingItem> shoppingItems) {
        this.shoppingItems = shoppingItems;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public void setAvatar(Avatar avatar) {
        this.avatar = avatar;
    }
}