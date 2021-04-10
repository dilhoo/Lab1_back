package lab1.dao;

public class VehicleNotFoundException extends Exception {

    public VehicleNotFoundException(long id) {
        super("Vehicle with id " + id + " not found");
    }
}