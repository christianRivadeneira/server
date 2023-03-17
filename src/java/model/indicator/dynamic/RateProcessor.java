package model.indicator.dynamic;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import javax.persistence.Query;
import javax.persistence.EntityManager;
import model.indicator.IndScale;

public class RateProcessor extends LogicProcessor {

    public RateProcessor() {
    }

    @Override
    public List<Query> createQueriesAccumulated(EntityManager em) {
        ArrayList<Query> res = new ArrayList<Query>();
        res.add(em.createNativeQuery("SELECT sum(v1)/ sum(v2) FROM ind_value WHERE ?1 <= when_value AND ?2 >= when_value AND scale_id = ?3 AND agency_id = ?4"));
        return res;
    }

    @Override
    public List<Query> createQueriesProjected(EntityManager em) {
        return createQueriesAccumulated(em);
    }

    @Override
    public BigDecimal processAccumulated(int agencyId, GregorianCalendar begin, GregorianCalendar end, int maxDay, IndScale scale, EntityManager em, List<Query> queries) {

        Query numQ = queries.get(0);
        numQ.setParameter(1, begin);
        numQ.setParameter(2, end);
        numQ.setParameter(3, scale.getId());
        numQ.setParameter(4, agencyId);
        Object o = numQ.getSingleResult();
        return o != null ? new BigDecimal(o.toString()).setScale(1, RoundingMode.HALF_EVEN) : null;
    }

    @Override
    public BigDecimal processProyected(int agencyId, GregorianCalendar begin, GregorianCalendar end, int maxDay, IndScale scale, EntityManager em, List<Query> queries) {
        return processAccumulated(agencyId, begin, end, maxDay, scale, em, queries);
    }
}
