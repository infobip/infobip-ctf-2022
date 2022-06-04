package org.ibctf.repository;

import org.ibctf.model.Partner;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PartnerRepository extends CrudRepository<Partner, Long> {

    Optional<Partner> findByUsername(String username);
}
