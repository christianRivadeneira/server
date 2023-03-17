package controller.marketing;

import controller.Utils;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import model.marketing.CylinderType;

public class CylinderTypeController {

    public static CylinderType[] findCylinderTypeEntities(EntityManager em) {
        Query q = em.createQuery("select object(o) from CylinderType as o ORDER BY o.capacity ASC");
        List<CylinderType> lst = q.getResultList();
        CylinderType[] cylinders = new CylinderType[lst.size()];
        return lst.toArray(cylinders);
    }

    public static CylinderType findCylinderType(Integer id, EntityManager em) {
        return em.find(CylinderType.class, id);
    }
}
