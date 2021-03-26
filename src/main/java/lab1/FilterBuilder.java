package lab1;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class FilterBuilder {

    private final Map<String, String[]> paramMap;
    private final CriteriaBuilder criteriaBuilder;
    private final Root<?> root;

    private final List<Predicate> predicates = new ArrayList<>();

    public FilterBuilder(Map<String, String[]> paramMap,
                         CriteriaBuilder criteriaBuilder,
                         Root<?> root) {
        this.paramMap = paramMap;
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

    public FilterBuilder addStringFilter(String columnName, String paramName) throws InvalidFilterException {
        if (paramMap.containsKey(paramName)) {
            String value = paramMap.get(paramName)[0];
            if (value.isEmpty()) {
                throw new InvalidFilterException(columnName, value);
            }
            addPredicate(columnName, value);
        }
        return this;
    }

    public FilterBuilder addStringFilter(String columnName) throws InvalidFilterException {
        return addStringFilter(columnName, columnName);
    }

    public FilterBuilder addIntFilter(String columnName) throws InvalidFilterException {
        if (paramMap.containsKey(columnName)) {
            String value = paramMap.get(columnName)[0];
            try {
                int intValue = Integer.parseInt(value);
                addPredicate(columnName, intValue);
            } catch (NumberFormatException e) {
                throw new InvalidFilterException(columnName, value);
            }
        }
        return this;
    }

    public FilterBuilder addLongFilter(String columnName) throws InvalidFilterException {
        if (paramMap.containsKey(columnName)) {
            String value = paramMap.get(columnName)[0];
            try {
                long longValue = Long.parseLong(value);
                addPredicate(columnName, longValue);
            } catch (NumberFormatException e) {
                throw new InvalidFilterException(columnName, value);
            }
        }
        return this;
    }

    public FilterBuilder addFloatFilter(String columnName) throws InvalidFilterException {
        if (paramMap.containsKey(columnName)) {
            String value = paramMap.get(columnName)[0];
            try {
                float floatValue = Float.parseFloat(value);
                addPredicate(columnName, floatValue);
            } catch (NumberFormatException e) {
                throw new InvalidFilterException(columnName, value);
            }
        }
        return this;
    }

    public FilterBuilder addDateFilter(String columnName) throws InvalidFilterException {
        if (paramMap.containsKey(columnName)) {
            String value = paramMap.get(columnName)[0];
            try {
                long dateValue = Long.parseLong(value);
                if (dateValue < 0) {
                    throw new InvalidFilterException(columnName, value);
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date(dateValue));
                LocalDate filter = LocalDate.of(calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
                addPredicate(columnName, LocalDateTime.of(filter, LocalTime.MIDNIGHT));
            } catch (NumberFormatException e) {
                throw new InvalidFilterException(columnName, value);
            }
        }
        return this;
    }

    public FilterBuilder addEnumFilter(Enum<?>[] enumValues, String columnName) throws InvalidFilterException {
        if (paramMap.containsKey(columnName)) {
            String value = paramMap.get(columnName)[0].toUpperCase();
            for (Enum<?> enumValue : enumValues) {
                if (value.equals(enumValue.name())) {
                    addPredicate(columnName, enumValue);
                    return this;
                }
            }
            throw new InvalidFilterException(columnName, value);
        }
        return this;
    }
}
