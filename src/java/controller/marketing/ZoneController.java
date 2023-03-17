package controller.marketing;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import model.marketing.Zone;

public class ZoneController {

    public static Zone[] getZones(EntityManager em) {
        Query q = em.createQuery("select object(o) from Zone as o ORDER BY o.name ASC");
        List<Zone> res = q.getResultList();
        Zone[] lst = new Zone[res.size()];
        return res.toArray(lst);
    }

    public static Zone getZone(Integer id, EntityManager em) {
        return em.find(Zone.class, id);
    }
}
