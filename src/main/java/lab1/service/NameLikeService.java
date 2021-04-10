package lab1.service;

import lab1.dao.VehicleDao;
import lab1.model.Vehicle;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("vehicle/name_like")
public class NameLikeService {

    @EJB
    private VehicleDao vehicleDao;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Vehicle> get(@QueryParam("value") String nameSubstring) {
        if (nameSubstring == null) throw new BadRequestException("No name substring specified");
        return vehicleDao.getByName(nameSubstring);
    }
}