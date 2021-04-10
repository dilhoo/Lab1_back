package lab1.dao;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@LocalBean
@Stateless
public class EnginePowerToCountDao {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Object> get() {
        return entityManager
                .createNamedQuery("enginePowerToCount", Object.class)
                .getResultList();
    }
}
