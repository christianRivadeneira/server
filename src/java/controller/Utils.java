package controller;

import javax.persistence.EntityManager;

public class Utils {

    public static int getCounterJPA(String q, EntityManager em) {
        return ((Long) em.createQuery(q).getSingleResult()).intValue();
    }

    public static int getCounterNative(String q, EntityManager em) {
        return ((Long) em.createNativeQuery(q).getSingleResult()).intValue();
    }

}
