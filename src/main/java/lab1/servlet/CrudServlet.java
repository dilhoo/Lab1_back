package lab1.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lab1.*;
import lab1.model.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class CrudServlet extends HttpServlet {

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

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, String[]> paramMap = req.getParameterMap();

        EntityManager entityManager = EntityManagerProvider.provide();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Vehicle> query = criteriaBuilder.createQuery(Vehicle.class);
        Root<Vehicle> root = query.from(Vehicle.class);

        Predicate filter;
        try {
            filter = new FilterBuilder(paramMap, criteriaBuilder, root)
                    .addLongFilter("id")
                    .addStringFilter("name")
                    .addDateFilter("creation_date")
                    .addFloatFilter("engine_power")
                    .addIntFilter("fuel_consumption")
                    .addEnumFilter(VehicleType.values(), "type")
                    .addEnumFilter(FuelType.values(), "fuel_type")
                    .build();
        } catch (InvalidFilterException e) {
            ServletHelper.setBadRequest(resp, e.getMessage());
            return;
        }
        query.where(filter);

        List<Order> orders = null;
        String[] ascColumns = paramMap.get("asc");
        if (ascColumns != null) {
            orders = new ArrayList<>();
            for (String column : ascColumns) {
                if (!COLUMN_SET.contains(column)) {
                    ServletHelper.setBadRequest(resp, "Unknown column: '" + column + "'");
                    return;
                }
                String camelCaseColumn = StringHelper.toCamelCase(column);
                orders.add(criteriaBuilder.asc(root.get(camelCaseColumn)));
            }
        }
        String[] descColumns = paramMap.get("desc");
        if (descColumns != null) {
            if (orders == null) {
                orders = new ArrayList<>();
            }
            for (String column : descColumns) {
                if (!COLUMN_SET.contains(column)) {
                    ServletHelper.setBadRequest(resp, "Unknown column: '" + column + "'");
                    return;
                }
                String camelCaseColumn = StringHelper.toCamelCase(column);
                orders.add(criteriaBuilder.desc(root.get(camelCaseColumn)));
            }
        }
        if (orders != null) {
            query.orderBy(orders);
        }

        Integer pageSize = null;
        int page = 0;
        if (paramMap.containsKey("page_size")) {
            String value = paramMap.get("page_size")[0];
            try {
                pageSize = Integer.parseInt(value);
                if (pageSize < 0) {
                    ServletHelper.setBadRequest(resp, "Invalid page size: " + pageSize);
                    return;
                }
            } catch (NumberFormatException e) {
                ServletHelper.setBadRequest(resp, "Invalid page size: " + value);
                return;
            }
        }
        if (paramMap.containsKey("page")) {
            if (pageSize == null) {
                ServletHelper.setBadRequest(resp, "Page size is not specified");
                return;
            }
            String value = paramMap.get("page")[0];
            try {
                page = Integer.parseInt(value);
                if (page < 0) {
                    ServletHelper.setBadRequest(resp, "Invalid page: " + page);
                    return;
                }
            } catch (NumberFormatException e) {
                ServletHelper.setBadRequest(resp, "Invalid page: " + value);
                return;
            }
        }

        TypedQuery<Vehicle> typedQuery = entityManager.createQuery(query);
        if (pageSize != null) {
            typedQuery.setFirstResult(page * pageSize).setMaxResults(pageSize);
        }
        List<Vehicle> result = typedQuery.getResultList();
        resp.getWriter().write(gson.toJson(result));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Vehicle vehicle;
        try {
            vehicle = gson.fromJson(ServletHelper.getBody(req), Vehicle.class);
        } catch (JsonSyntaxException e) {
            ServletHelper.setBadRequest(resp, "Request body syntax error");
            return;
        }

        try {
            vehicle = new ValidatedVehicle(vehicle).getVehicle();
        } catch (ValidationException e) {
            ServletHelper.setBadRequest(resp, e.getMessage());
            return;
        }

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        vehicle.setCreationDate(now);

        EntityManager entityManager = EntityManagerProvider.provide();
        entityManager.getTransaction().begin();
        entityManager.persist(vehicle);
        entityManager.getTransaction().commit();
    }

    private void copy(Vehicle from, Vehicle to) {
        to.setName(from.getName());
        to.setCoordinates(from.getCoordinates());
        to.setEnginePower(from.getEnginePower());
        to.setFuelConsumption(from.getFuelConsumption());
        to.setType(from.getType());
        to.setFuelType(from.getFuelType());
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Vehicle vehicle;
        try {
            vehicle = gson.fromJson(ServletHelper.getBody(req), Vehicle.class);
        } catch (JsonSyntaxException e) {
            ServletHelper.setBadRequest(resp, "Request body syntax error");
            return;
        }

        try {
            vehicle = new ValidatedVehicle(vehicle).getVehicle();
        } catch (ValidationException e) {
            ServletHelper.setBadRequest(resp, e.getMessage());
        }

        EntityManager entityManager = EntityManagerProvider.provide();
        entityManager.getTransaction().begin();
        Vehicle storedVehicle = entityManager.find(Vehicle.class, vehicle.getId());
        if (storedVehicle == null) {
            ServletHelper.setBadRequest(resp, "Vehicle with id " + vehicle.getId() + " not found");
            entityManager.getTransaction().rollback();
            return;
        }
        copy(vehicle, storedVehicle);
        entityManager.getTransaction().commit();
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idValue = req.getParameter("id");
        long id;
        try {
            id = Long.parseLong(idValue);
        } catch (NumberFormatException e) {
            ServletHelper.setBadRequest(resp, "Request body syntax error");
            return;
        }

        EntityManager entityManager = EntityManagerProvider.provide();
        entityManager.getTransaction().begin();
        Vehicle storedVehicle = entityManager.find(Vehicle.class, id);
        if (storedVehicle == null) {
            ServletHelper.setBadRequest(resp, "Vehicle with id " + id + " not found");
            entityManager.getTransaction().rollback();
            return;
        }
        entityManager.remove(storedVehicle);
        entityManager.getTransaction().commit();
    }
}