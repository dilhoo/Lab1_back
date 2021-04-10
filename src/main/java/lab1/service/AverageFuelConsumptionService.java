package lab1.service;

import lab1.dao.VehicleDao;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("vehicle/average_fuel_consumption")
public class AverageFuelConsumptionService {

    @EJB
    private VehicleDao vehicleDao;

    @GET
    public double get() {
        return vehicleDao.getAverageFuelConsumption();
    }
}
