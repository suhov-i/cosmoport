package com.space.specification;

import com.space.model.Ship;
import com.space.model.ShipType;
import org.springframework.data.jpa.domain.Specification;
import java.util.Date;

//class for building Ship specification to filter entities
public class ShipSpecification {
    public static Specification<Ship> getSpecification(String name,
                                                       String planet,
                                                       ShipType shipType,
                                                       Long after, Long before,
                                                       Boolean isUsed,
                                                       Double minSpeed, Double maxSpeed,
                                                       Integer minCrewSize, Integer maxCrewSize,
                                                       Double minRating, Double maxRating) {
        return Specification.where(
                 ShipSpecification.shipsByName(name)
                .and(ShipSpecification.shipsByPlanet(planet)))
                .and(ShipSpecification.shipsByShipType(shipType))
                .and(ShipSpecification.shipsByDate(after, before))
                .and(ShipSpecification.shipsByUsage(isUsed))
                .and(ShipSpecification.shipsBySpeed(minSpeed, maxSpeed))
                .and(ShipSpecification.shipsByCrewSize(minCrewSize, maxCrewSize))
                .and(ShipSpecification.shipsByRating(minRating, maxRating));
    }

    public static Specification<Ship> shipsByRating(Double min, Double max) {
        return (r, q, cb) -> {
            if (min == null && max == null) return null;
            if (min == null) return cb.lessThanOrEqualTo(r.get("rating"), max);
            if (max == null) return cb.greaterThanOrEqualTo(r.get("rating"), min);
            return cb.between(r.get("rating"), min, max);
        };
    }

    public static Specification<Ship> shipsByName(String name) {
        return (r, q, cb) -> name == null ? null : cb.like(r.get("name"), "%" + name + "%");
    }

    public static Specification<Ship> shipsByPlanet(String planet) {
        return (r, q, cb) -> planet == null ? null : cb.like(r.get("planet"), "%" + planet + "%");
    }

    public static Specification<Ship> shipsByShipType(ShipType shipType) {
        return (r, q, cb) -> shipType == null ? null : cb.equal(r.get("shipType"), shipType);
    }

    public static Specification<Ship> shipsByDate(Long after, Long before) {
        return (r, q, cb) -> {
            if (after == null && before == null) return null;
            if (after == null) {
                Date before1 = new Date(before);
                return cb.lessThanOrEqualTo(r.get("prodDate"), before1);
            }
            if (before == null) {
                Date after1 = new Date(after);
                return cb.greaterThanOrEqualTo(r.get("prodDate"), after1);
            }
            Date before1 = new Date(before);
            Date after1 = new Date(after);
            return cb.between(r.get("prodDate"), after1, before1);
        };
    }

    public static Specification<Ship> shipsByUsage(Boolean isUsed) {
        return (r, q, cb) -> {
            if (isUsed == null) return null;
            if (isUsed) return cb.isTrue(r.get("isUsed"));
            else return cb.isFalse(r.get("isUsed"));
        };
    }

    public static Specification<Ship> shipsBySpeed(Double min, Double max) {
        return (r, q, cb) -> {
            if (min == null && max == null) return null;
            if (min == null) return cb.lessThanOrEqualTo(r.get("speed"), max);
            if (max == null) return cb.greaterThanOrEqualTo(r.get("speed"), min);
            return cb.between(r.get("speed"), min, max);
        };
    }

    public static Specification<Ship> shipsByCrewSize(Integer min, Integer max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min == null) return cb.lessThanOrEqualTo(root.get("crewSize"), max);
            if (max == null) return cb.greaterThanOrEqualTo(root.get("crewSize"), min);
            return cb.between(root.get("crewSize"), min, max);
        };
    }
}
