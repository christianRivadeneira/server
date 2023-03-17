package controller.marketing;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import model.marketing.City;

public class CityController {

    public static City[] getCitiesByEnterprise(int idEnterprise, EntityManager em) {
        Query q = em.createQuery("SELECT c FROM City as c, Agency as a WHERE a.enterpriseId = ?1 AND a.cityId = c.id ORDER BY c.name ASC");
        q.setParameter(1, idEnterprise);
        List<City> lst = q.getResultList();
        City[] cities = new City[lst.size()];
        return lst.toArray(cities);
    }

    public static City[] getCitiesByZone(int idZone, EntityManager em) {
        Query q = em.createQuery("SELECT c FROM City as c WHERE c.zoneId = ?1 ORDER BY c.name ASC");
        q.setParameter(1, idZone);
        List<City> lst = q.getResultList();
        City[] cities = new City[lst.size()];
        return lst.toArray(cities);
    }

    public static City[] getBillingCities(EntityManager em) {
        Query q = em.createQuery("SELECT c FROM City as c WHERE c.dbName IS NOT NULL ORDER by c.name ASC");
        List<City> lst = q.getResultList();
        City[] cities = new City[lst.size()];
        return lst.toArray(cities);
    }

    public static City getCity(Integer id, EntityManager em) {
        return em.find(City.class, id);
    }
}
