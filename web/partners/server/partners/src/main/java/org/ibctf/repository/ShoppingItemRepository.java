package org.ibctf.repository;

import org.ibctf.model.Partner;
import org.ibctf.model.ShoppingItem;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShoppingItemRepository extends CrudRepository<ShoppingItem, Long> {

    Iterable<ShoppingItem> findByPartner(Partner partner);
    Optional<ShoppingItem> findByIdAndPartner(Long id, Partner partner);
}
