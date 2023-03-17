package web.pUnits;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;


public class ServiceSigmaPU {
    
    @PersistenceUnit(unitName = "sigmaPU")
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }    
}
