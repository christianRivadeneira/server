package controller.ordering;

import javax.persistence.EntityManager;

public class OrdOfficeController {

    public static String getOfficeName(int id, EntityManager em) {
        return em.createNativeQuery("SELECT description FROM ord_office WHERE id = " + id).getSingleResult().toString();
    }
}
