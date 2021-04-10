package lab1.dao;

import lab1.FilterBuilder;
import lab1.InvalidFilterException;
import lab1.StringHelper;
import lab1.model.FuelType;
import lab1.model.Vehicle;
import lab1.model.VehicleType;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.ws.rs.BadRequestException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@LocalBean
@Stateless
public class VehicleDao {

    private static final Set<String> COLUMN_SET = new HashSet<>();

    static {
        COLUMN_SET.add("id");
        COLUMN_SET.add("name");
        COLUMN_SET.add("creation_date");
        COLUMN_SET.add("engine_power");
        COLUMN_SET.add("fuel_consumption");
        COLUMN_SET.add("type");
        COLUMN_SET.add("fuel_type");
    }

    @PersistenceContext
    private EntityManager entityManager;

    private void throwBadRequest(String message) {
        throw new BadRequestException(message);
    }

    public List<Vehicle> filter(Long id,
                                String name,
                                Long creationDate,
                                String type,
                                Float enginePower,
                                String fuelType,
                                Integer fuelConsumption,
                                List<String> ascColumns,
                                List<String> descColumns,
                                int pageSize,
                                int page) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Vehicle> query = criteriaBuilder.createQuery(Vehicle.class);
        Root<Vehicle> root = query.from(Vehicle.class);

        Predicate filter;
        try {
            filter = new FilterBuilder(criteriaBuilder, root)
                    .addFilter("id", id)
                    .addFilter("name", name)
                    .addDateFilter("creation_date", creationDate)
                    .addFilter("engine_power", enginePower)
                    .addFilter("fuel_consumption", fuelConsumption)
                    .addEnumFilter(VehicleType.values(), "type", type)
                    .addEnumFilter(FuelType.values(), "fuel_type", fuelType)
                    .build();
        } catch (InvalidFilterException e) {
            throw new BadRequestException("Invalid '" + e.getFilteredColumn()
                    + "' filter value : " + e.getInvalidFilterValue());
        }
        query.where(filter);

        final List<Order> orders = new ArrayList<>();
        if (ascColumns != null) {
            for (String column : ascColumns) {
                if (!COLUMN_SET.contains(column)) {
                    throwBadRequest("Unknown column: '" + column + "'");
                }
                String camelCaseColumn = StringHelper.toCamelCase(column);
                orders.add(criteriaBuilder.asc(root.get(camelCaseColumn)));
            }
        }
        if (descColumns != null) {
            for (String column : descColumns) {
                if (!COLUMN_SET.contains(column)) {
                    throwBadRequest("Unknown column: '" + column + "'");
                }
                String camelCaseColumn = StringHelper.toCamelCase(column);
                orders.add(criteriaBuilder.desc(root.get(camelCaseColumn)));
            }
        }
        if (!orders.isEmpty()) {
            query.orderBy(orders);
        }

        if (pageSize < 0) throwBadRequest("Invalid page size: " + pageSize);
        if (page < 1) throwBadRequest("Invalid page: " + page);

        TypedQuery<Vehicle> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((page - 1) * pageSize).setMaxResults(pageSize);
        return typedQuery.getResultList();
    }

    public Vehicle getById(long id) throws VehicleNotFoundException {
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        Vehicle vehicle = entityManager.find(Vehicle.class, id);
        if (vehicle == null) {
            transaction.rollback();
            throw new VehicleNotFoundException(id);
        }
        return vehicle;
    }

    public List<Vehicle> getByName(String nameSubstring) {
        return entityManager
                .createNamedQuery("nameLike", Vehicle.class)
                .setParameter("value", nameSubstring)
                .getResultList();
    }

    public double getAverageFuelConsumption() {
        return entityManager
                .createNamedQuery("avgFuelConsumption", Double.class)
                .getResultList()
                .get(0);
    }

    public void create(Vehicle vehicle) {
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        entityManager.persist(vehicle);
        transaction.commit();
    }

    public void update(Vehicle vehicle) throws VehicleNotFoundException {
        long id = vehicle.getId();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        Vehicle storedVehicle = entityManager.find(Vehicle.class, id);
        if (storedVehicle == null) {
            transaction.rollback();
            throw new VehicleNotFoundException(id);
        }
        vehicle.setCreationDate(storedVehicle.getCreationDate());
        entityManager.merge(vehicle);
        transaction.commit();
    }

    public void delete(long id) throws VehicleNotFoundException {
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        Vehicle vehicle = entityManager.find(Vehicle.class, id);
        if (vehicle == null) {
            transaction.rollback();
            throw new VehicleNotFoundException(id);
        }
        entityManager.remove(vehicle);
        transaction.commit();
    }
}