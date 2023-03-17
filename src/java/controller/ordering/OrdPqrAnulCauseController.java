package controller.ordering;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import model.ordering.OrdPqrAnulCause;

public class OrdPqrAnulCauseController {

    public static OrdPqrAnulCause[] getPqrAnulCauses(String type, EntityManager em) {
        //ENUM('cyl','tank','other')
        Query q = em.createQuery("select o from OrdPqrAnulCause As o Where o.type= '" + type + "' ORDER BY o.id ASC");
        List<OrdPqrAnulCause> res = q.getResultList();
        OrdPqrAnulCause[] lst = new OrdPqrAnulCause[res.size()];
        return res.toArray(lst);
    }
}
