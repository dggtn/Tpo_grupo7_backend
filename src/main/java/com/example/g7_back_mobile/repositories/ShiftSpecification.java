package com.example.g7_back_mobile.repositories;

import com.example.g7_back_mobile.repositories.entities.Course;
import com.example.g7_back_mobile.repositories.entities.Headquarter;
import com.example.g7_back_mobile.repositories.entities.Shift;
import com.example.g7_back_mobile.repositories.entities.Sport;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class ShiftSpecification implements Specification<Shift> {

    public enum Campo {
        SEDE,
        HORA_INICIO,
        TIPO_DEPORTE
    }

    private Campo campo;
    private Object valor;

    public ShiftSpecification(Campo campo, Object valor) {
        this.campo = campo;
        this.valor = valor;
    }

    @Override
    public Predicate toPredicate(Root<Shift> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

        if (this.campo.equals(Campo.SEDE)) {
            Join<Shift, Headquarter> headquarterJoin = root.join("sede");
            return criteriaBuilder.equal(headquarterJoin.get("id"), this.valor);
        } else if (this.campo.equals(Campo.TIPO_DEPORTE)) {
            Join<Shift, Course> courseJoin = root.join("clase");
            Join<Course, Sport> sportJoin = courseJoin.join("sportName");
            return criteriaBuilder.equal(sportJoin.get("id"), this.valor);
        } else if (this.campo.equals(Campo.HORA_INICIO)) {
            Path<String> pathHoraInicio = root.get("horaInicio");
            return criteriaBuilder.equal(pathHoraInicio, this.valor);
        }
        return null;
    }
}
