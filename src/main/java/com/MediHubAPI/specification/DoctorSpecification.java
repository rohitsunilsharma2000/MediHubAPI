package com.MediHubAPI.specification;

import com.MediHubAPI.dto.DoctorSearchCriteria;
import com.MediHubAPI.model.*;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class DoctorSpecification implements Specification<User> {

    private final DoctorSearchCriteria criteria;

    public DoctorSpecification(DoctorSearchCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        // Ensure user has DOCTOR role
        Join<User, Role> roleJoin = root.join("roles");
        predicates.add(cb.equal(roleJoin.get("name"), ERole.DOCTOR));

        // Name filter (full name)
        if (criteria.getName() != null && !criteria.getName().isBlank()) {
            Expression<String> fullName = cb.concat(cb.lower(root.get("firstName")), " ");
            fullName = cb.concat(fullName, cb.lower(root.get("lastName")));
            predicates.add(cb.like(fullName, "%" + criteria.getName().toLowerCase() + "%"));
        }

        // Email filter (optional)
        if (criteria.getEmail() != null && !criteria.getEmail().isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("email")), "%" + criteria.getEmail().toLowerCase() + "%"));
        }

        // Active flag filter (optional)
//        if (criteria.getActive() != null) {
//            predicates.add(cb.equal(root.get("active"), criteria.getActive()));
//        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
