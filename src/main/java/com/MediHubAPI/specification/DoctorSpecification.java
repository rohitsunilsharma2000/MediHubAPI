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
        Join<User, Role> roleJoin = root.join("roles");
        predicates.add(cb.equal(roleJoin.get("name"), ERole.DOCTOR));

        if (criteria.getName() != null) {
            predicates.add(cb.like(cb.lower(root.get("firstName")), "%" + criteria.getName().toLowerCase() + "%"));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
