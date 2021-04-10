package lab1.service;

import lab1.dao.EnginePowerToCountDao;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("vehicle/engine_power_to_count")
public class EnginePowerToCountService {

    @EJB
    private EnginePowerToCountDao dao;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Object get() {
        return dao.get();
    }
}