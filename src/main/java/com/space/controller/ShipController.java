package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import com.space.specification.ShipSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Calendar;
import java.util.List;

@RestController
@RequestMapping("/rest")
public class ShipController {
    private ShipService shipService;

    @Autowired
    public void setShipService(ShipService shipService) {
        this.shipService = shipService;
    }

    @GetMapping("/ships")
    @ResponseStatus(HttpStatus.OK)
    public List<Ship> getShipsList(@RequestParam(value = "name", required = false) String name,
                                   @RequestParam(value = "planet", required = false) String planet,
                                   @RequestParam(value = "shipType", required = false) ShipType shipType,
                                   @RequestParam(value = "after", required = false) Long after,
                                   @RequestParam(value = "before", required = false) Long before,
                                   @RequestParam(value = "isUsed", required = false) Boolean isUsed,
                                   @RequestParam(value = "minSpeed", required = false) Double minSpeed,
                                   @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
                                   @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
                                   @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
                                   @RequestParam(value = "minRating", required = false) Double minRating,
                                   @RequestParam(value = "maxRating", required = false) Double maxRating,
                                   @RequestParam(value = "order", required = false, defaultValue = "ID") ShipOrder order,
                                   @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
                                   @RequestParam(value = "pageSize", required = false, defaultValue = "3") Integer pageSize) {

        //constructing pagination and sorting
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(order.getFieldName()));
        //constructing specification to filter
        Specification<Ship> specification = ShipSpecification.getSpecification(
                name, planet, shipType, after, before, isUsed, minSpeed, maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating);

        return shipService.getShipList(specification, pageable);
    }

    @GetMapping("/ships/count")
    @ResponseStatus(HttpStatus.OK)
    public Integer getAllShipsCount(@RequestParam(value = "name", required = false) String name,
                                    @RequestParam(value = "planet", required = false) String planet,
                                    @RequestParam(value = "shipType", required = false) ShipType shipType,
                                    @RequestParam(value = "after", required = false) Long after,
                                    @RequestParam(value = "before", required = false) Long before,
                                    @RequestParam(value = "isUsed", required = false) Boolean isUsed,
                                    @RequestParam(value = "minSpeed", required = false) Double minSpeed,
                                    @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
                                    @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
                                    @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
                                    @RequestParam(value = "minRating", required = false) Double minRating,
                                    @RequestParam(value = "maxRating", required = false) Double maxRating,
                                    @RequestParam(value = "order", required = false, defaultValue = "ID") ShipOrder order,
                                    @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
                                    @RequestParam(value = "pageSize", required = false, defaultValue = "3") Integer pageSize) {

        //constructing specification to filter
        Specification<Ship> specification = ShipSpecification.getSpecification(
                name, planet, shipType, after, before, isUsed, minSpeed, maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating);

        //pageable == null because we need only to count entities
        return shipService.getShipList(specification, null).size();
    }

    @PostMapping("/ships")
    @ResponseBody
    public ResponseEntity<Ship> createShip(@RequestBody Ship newShip) {
        //if any of the necessary fields in request is not set or valid, returns 400
        if (newShip.getName() == null ||
            newShip.getPlanet() == null ||
            newShip.getShipType() == null ||
            newShip.getProdDate() == null ||
            newShip.getSpeed() == null ||
            newShip.getCrewSize() == null ||
            !isNewShipValid(newShip))
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        //if there's no isUsed param in request, sets false by default
        if (newShip.getUsed() == null)
            newShip.setUsed(false);
        return new ResponseEntity<>(shipService.createShip(newShip), HttpStatus.OK);
    }

    @PostMapping("/ships/{id}")
    @ResponseBody
    public ResponseEntity<Ship> updateShip(@PathVariable String id, @RequestBody Ship ship) {
        Long validId = getValidId(id);
        if (isShipRequestIsEmpty(ship))
            //no changes in DB if request is empty, just returns existing ship
            return new ResponseEntity<>(shipService.getShip(validId), HttpStatus.OK);
        //requests with null fields should be in 200 status so checking only empty name
        if (validId == 0 || (ship.getName() != null && ship.getName().isEmpty()))
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        if (!shipService.isExist(validId))
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);

        //building ship to update using existing one in DB
        Ship shipToUpdate = shipService.getShip(validId);
        if (ship.getName() != null)
            shipToUpdate.setName(ship.getName());
        if (ship.getPlanet() != null)
            shipToUpdate.setPlanet(ship.getPlanet());
        if (ship.getShipType() != null)
            shipToUpdate.setShipType(ship.getShipType());
        if (ship.getProdDate() != null)
            shipToUpdate.setProdDate(ship.getProdDate());
        if (ship.getSpeed() != null)
            shipToUpdate.setSpeed(ship.getSpeed());
        if (ship.getUsed() != null)
            shipToUpdate.setUsed(ship.getUsed());
        if (ship.getCrewSize() != null)
            shipToUpdate.setCrewSize(ship.getCrewSize());
        //checking if this ship we built from request is valid
        if (!isNewShipValid(shipToUpdate))
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(shipService.updateShip(validId, shipToUpdate), HttpStatus.OK);
    }

    @GetMapping("/ships/{id}")
    @ResponseBody
    public ResponseEntity<Ship> getShip(@PathVariable String id) {
        Long validId = getValidId(id);
        if (validId == 0)
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        if (!shipService.isExist(validId))
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(shipService.getShip(validId), HttpStatus.OK);
    }

    @DeleteMapping("/ships/{id}")
    @ResponseBody
    public ResponseEntity<Ship> deleteShip(@PathVariable String id) {
        Long validId = getValidId(id);
        if (validId == 0)
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        if (!shipService.isExist(validId))
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        shipService.deleteShip(validId);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    //gets valid Long ID from request
    //returns 0L if it's not valid
    private Long getValidId(String id) {
        if (id == null) return 0L;
        long l;
        try {
            l = Long.parseLong(id);
            if (l < 0 || (int) l != l) return 0L; //if id is not integer or (< 0)
        } catch (NumberFormatException e) {
            return 0L;
        }
        return l;
    }

     // checks if the Ship to create or update fields are valid
     private boolean isNewShipValid(Ship newShip) {
        if (newShip.getName().length() < 1 ||
                newShip.getName().length() > 50 ||
                newShip.getPlanet().length() < 1 ||
                newShip.getPlanet().length() > 50 ||
                newShip.getSpeed() < 0.01 ||
                newShip.getSpeed() > 0.99 ||
                newShip.getCrewSize() < 1 ||
                newShip.getCrewSize() > 9999
        ) return false;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(newShip.getProdDate());
        return calendar.get(Calendar.YEAR) >= 2800 && calendar.get(Calendar.YEAR) <= 3019;
    }

    private boolean isShipRequestIsEmpty(Ship ship) {
        return (
                ship.getId() == null &&
                ship.getName() == null &&
                ship.getUsed() == null &&
                ship.getSpeed() == null &&
                ship.getProdDate() == null &&
                ship.getCrewSize() == null &&
                ship.getPlanet() == null &&
                ship.getShipType() == null &&
                ship.getRating() == null
                );
    }
}
