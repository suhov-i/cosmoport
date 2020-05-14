package com.space.service;

import com.space.model.Ship;
import com.space.repository.ShipRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.List;

@Service
public class ShipServiceImpl implements ShipService {
    private ShipRepository shipRepository;
    private static final Logger logger = LoggerFactory.getLogger(ShipServiceImpl.class);

    @Autowired
    public void setShipRepository(ShipRepository shipRepository) {
        this.shipRepository = shipRepository;
    }

    @Override
    public List<Ship> getShipList(Specification<Ship> specification, Pageable pageable) {
        //if (pageable == null) the method was called from getAllShipsCount, therefore we don't need paging
        if (pageable == null)
            return shipRepository.findAll(specification);
        return shipRepository.findAll(specification, pageable).getContent();
    }

    @Override
    public Ship createShip(Ship newShip) {
        newShip.setRating(calculateNewRating(newShip));
        return shipRepository.saveAndFlush(newShip);
    }

    @Override
    public Ship getShip(Long id) {
        return shipRepository.findById(id).get();
    }

    @Override
    public Ship updateShip(Long id, Ship newShip) {
        newShip.setRating(calculateNewRating(newShip));
        return shipRepository.save(newShip);
    }

    @Override
    public void deleteShip(Long id) {
        shipRepository.deleteById(id);
    }

    @Override
    public boolean isExist(Long id) {
        return shipRepository.existsById(id);
    }

    //calculating new rating using test's business logic
    private Double calculateNewRating(Ship ship) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ship.getProdDate());
        int year = calendar.get(Calendar.YEAR);

        BigDecimal result = BigDecimal.valueOf(
                (80 * ship.getSpeed() * (ship.getUsed() ? 0.5 : 1)) / (3019 - year + 1));
        result = result.setScale(2, RoundingMode.HALF_UP);
        return result.doubleValue();
    }
}
