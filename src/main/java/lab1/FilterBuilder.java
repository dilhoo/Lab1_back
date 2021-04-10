package lab1;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class FilterBuilder {

    private final CriteriaBuilder criteriaBuilder;
    private final Root<?> root;

    private final List<Predicate> predicates = new ArrayList<>();

    public FilterBuilder(CriteriaBuilder criteriaBuilder,
                         Root<?> root) {
        this.criteriaBuilder = criteriaBuilder;
        this.root = root;

        predicates.add(criteriaBuilder.and());
    }

    public Predicate build() {
        Iterator<Predicate> predicateIterator = predicates.iterator();
        Predicate filter = predicateIterator.next();
        while (predicateIterator.hasNext()) {
            filter = criteriaBuilder.and(filter, predicateIterator.next());
        }
        return filter;
    }

    private void addPredicate(String columnName, Object value) {
        String camelCaseColumn = StringHelper.toCamelCase(columnName);
        predicates.add(criteriaBuilder.equal(root.get(camelCaseColumn), value));
    }

    public FilterBuilder addFilter(String columnName, Object value) {
        if (value != null) {
            addPredicate(columnName, value);
        }
        return this;
    }

    public FilterBuilder addDateFilter(String columnName, Long value) throws InvalidFilterException {
        if (value == null) return this;
        if (value < 0) {
            throw new InvalidFilterException(columnName, value);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(value));
        LocalDate filter = LocalDate.of(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        addPredicate(columnName, LocalDateTime.of(filter, LocalTime.MIDNIGHT));
        return this;
    }

    public FilterBuilder addEnumFilter(Enum<?>[] enumValues,
                                       String columnName,
                                       String value) throws InvalidFilterException {
        if (value == null) return this;
        String uppercaseValue = value.toUpperCase();
        for (Enum<?> enumValue : enumValues) {
            if (uppercaseValue.equals(enumValue.name())) {
                addPredicate(columnName, enumValue);
                return this;
            }
        }
        throw new InvalidFilterException(columnName, value);
    }
}
