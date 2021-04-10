package lab1.service;

import lab1.ValidationException;
import lab1.dao.VehicleDao;
import lab1.dao.VehicleNotFoundException;
import lab1.model.Vehicle;
import lab1.model.VehicleValidator;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Path("vehicle")
public class CrudService {

    @EJB
    private VehicleDao vehicleDao;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Vehicle> getAll(@QueryParam("id") Long id,
                                @QueryParam("name") String name,
                                @QueryParam("creation_date") Long creationDate,
                                @QueryParam("type") String type,
                                @QueryParam("enginePower") Float enginePower,
                                @QueryParam("fuelType") String fuelType,
                                @QueryParam("fuelConsumption") Integer fuelConsumption,
                                @QueryParam("asc") List<String> ascColumns,
                                @QueryParam("desc") List<String> descColumns,
                                @QueryParam("page_size") @DefaultValue("10") int pageSize,
                                @QueryParam("page") @DefaultValue("1") int page) {
        return vehicleDao.filter(id, name, creationDate, type, enginePower, fuelType,
                fuelConsumption, ascColumns, descColumns, pageSize, page);
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("id") long id) {
        try {
            Vehicle vehicle =  vehicleDao.getById(id);
            return Response.ok(vehicle).build();
        } catch (VehicleNotFoundException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    private Vehicle validate(Vehicle vehicle) throws ValidationException {
        return new VehicleValidator(vehicle).getValidatedVehicle();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(Vehicle vehicle) {
        Vehicle validatedVehicle;
        try {
            validatedVehicle = validate(vehicle);
        } catch (ValidationException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
        validatedVehicle.setCreationDate(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS));
        vehicleDao.create(vehicle);
        return Response.ok().build();
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") long id, Vehicle vehicle) {
        Vehicle validatedVehicle;
        try {
            validatedVehicle = validate(vehicle);
        } catch (ValidationException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
        validatedVehicle.setId(id);
        try {
            vehicleDao.update(validatedVehicle);
        } catch (VehicleNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
        return Response.ok().build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") long id) {
        try {
            vehicleDao.delete(id);
        } catch (VehicleNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
        return Response.ok().build();
    }
}