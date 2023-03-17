package utilities;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import javax.persistence.NoResultException;
import javax.persistence.Query;

public class Results {

    public static Date getDate(Query q) {
        try {
            Object o = q.getSingleResult();
            if (o != null) {
                return (Date) o;
            }
            return null;
        } catch (NoResultException ex) {
            return null;
        }
    }

    public static BigInteger getBigInteger(Query q) {
        Object o = q.getSingleResult();
        if (o != null) {
            if (o instanceof BigInteger) {
                return (BigInteger) o;
            } else if (o instanceof BigDecimal) {
                return BigInteger.valueOf(((BigDecimal) o).longValue());
            } else {
                throw new RuntimeException(o.getClass().getName() + " No se puede convertir a BigInteger.");
            }
        } else {
            return BigInteger.ZERO;
        }
    }

    /**
     * Retorna el resultado de un query como BigDecinmal
     *
     * @param Query creado en un EntityManager
     * @return El valor del resultado del query como BigDecimal o
     * BigDecimal.ZERO si no hay resultados
     */
    public static BigDecimal getBigDecimal(Query q) {
        Object o;
        try {
            o = q.getSingleResult();
            if (o != null) {
                return (BigDecimal) o;
            }
        } catch (NoResultException ex) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.ZERO;
    }

    public static Long getLong(Query q) {
        return MySQLQuery.getAsLong(q.getSingleResult());
    }

    public static int getInteger(Query q) {
        Object o = q.getSingleResult();
        if (o != null) {
            if (o instanceof Integer) {
                return (Integer) o;
            } else if (o instanceof Long) {
                return ((Long) o).intValue();
            } else {
                throw new RuntimeException(o.getClass().getName() + " no se puede convertir a int.");
            }
        }
        return 0;
    }
}
