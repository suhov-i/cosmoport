package com.space.service;

import com.space.model.Ship;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.util.List;

public interface ShipService {
    List<Ship> getShipList(Specification<Ship> specification, Pageable pageable);
    Ship createShip(Ship newShip);
    Ship getShip(Long id);
    Ship updateShip(Long id, Ship newShip);
    void deleteShip(Long id);
    boolean isExist(Long id);
}
