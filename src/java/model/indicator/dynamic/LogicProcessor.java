package model.indicator.dynamic;

import java.math.BigDecimal;
import java.util.GregorianCalendar;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import model.indicator.IndScale;

public abstract class LogicProcessor {

    public LogicProcessor(){
        
    }

    public abstract BigDecimal processAccumulated(
            int agencyId,
            GregorianCalendar begin,
            GregorianCalendar end,
            int maxDay,
            IndScale scale,            
            EntityManager em,
            List<Query> queries
            );

    public abstract BigDecimal processProyected(
            int agencyId,
            GregorianCalendar begin,
            GregorianCalendar end,
            int maxDay,
            IndScale scale,
            EntityManager em,
            List<Query> queries
            );

    public abstract List<Query> createQueriesAccumulated(EntityManager em);
    public abstract List<Query> createQueriesProjected(EntityManager em);


}
