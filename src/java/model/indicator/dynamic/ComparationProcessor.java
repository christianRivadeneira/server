package model.indicator.dynamic;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import javax.persistence.Query;
import javax.persistence.EntityManager;
import model.indicator.IndScale;

public class ComparationProcessor extends LogicProcessor {

    public ComparationProcessor() {
    }

    @Override
    public List<Query> createQueriesAccumulated(EntityManager em) {
        ArrayList<Query> res = new ArrayList<Query>();
        res.add(em.createNativeQuery("SELECT sum(v1) FROM ind_value WHERE ?1 <= when_value AND ?2 >= when_value AND scale_id = ?3 AND agency_id = ?4"));
        res.add(em.createNativeQuery("SELECT sum(v2) FROM ind_value WHERE ?1 <= when_value AND ?2 >= when_value AND scale_id = ?3 AND agency_id = ?4"));
        return res;
    }

    @Override
    public List<Query> createQueriesProjected(EntityManager em) {
        return createQueriesAccumulated(em);
    }

    @Override
    public BigDecimal processAccumulated(int agencyId, GregorianCalendar begin,  GregorianCalendar when, int maxDay, IndScale scale, EntityManager em, List<Query> queries) {

        Query val1Q = queries.get(0);
        val1Q.setParameter(1, begin);
        val1Q.setParameter(2, when);
        val1Q.setParameter(3, scale.getId());
        val1Q.setParameter(4, agencyId);

        Query val2Q = queries.get(1);
        val2Q.setParameter(1, begin);
        val2Q.setParameter(2, when);
        val2Q.setParameter(3, scale.getId());
        val2Q.setParameter(4, agencyId);

        Object val1O = val1Q.getSingleResult();
        Object val2O = val2Q.getSingleResult();

        if (val1O == null || val2O == null) {
            return null;
        }

        BigDecimal val1 = new BigDecimal(val1O.toString());
        BigDecimal val2 = new BigDecimal(val2O.toString());
        
        return new BigDecimal(val1.compareTo(val2));
    }

    @Override
    public BigDecimal processProyected(int agencyId,  GregorianCalendar begin, GregorianCalendar end, int maxDay, IndScale scale, EntityManager em, List<Query> queries) {
        return processAccumulated(agencyId, begin, end, maxDay, scale, em, queries);
    }
}
