package model.indicator.dynamic;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import javax.persistence.Query;
import javax.persistence.EntityManager;
import model.indicator.IndScale;

public class DebtProcessor extends LogicProcessor {

    public DebtProcessor() {
    }

    @Override
    public List<Query> createQueriesAccumulated(EntityManager em) {
        return createQueriesProjected(em);
    }

    @Override
    public List<Query> createQueriesProjected(EntityManager em) {
        ArrayList<Query> res = new ArrayList<Query>();
        res.add(em.createNativeQuery("SELECT ((sum(v1)/sum(v2))*30)/?1 FROM ind_value WHERE ?2 <= when_value AND ?3 >= when_value AND scale_id = ?4 AND agency_id = ?5"));
        return res;
    }

    @Override
    public BigDecimal processAccumulated(int agencyId, GregorianCalendar begin, GregorianCalendar end, int maxDay, IndScale scale, EntityManager em, List<Query> queries) {
        return processProyected(agencyId, begin, end, maxDay, scale, em, queries);
    }

    @Override
    public BigDecimal processProyected(int agencyId, GregorianCalendar begin, GregorianCalendar end, int maxDay, IndScale scale, EntityManager em, List<Query> queries) {
        Query numQ = queries.get(0);
        numQ.setParameter(1, end.get(GregorianCalendar.DAY_OF_MONTH));
        numQ.setParameter(2, begin);
        numQ.setParameter(3, end);
        numQ.setParameter(4, scale.getId());
        numQ.setParameter(5, agencyId);
        Object o = numQ.getSingleResult();
        return o != null ? new BigDecimal(o.toString()) : null;
    }
}
