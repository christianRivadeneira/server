package web.quality.project;

import java.util.Date;
import java.util.List;

class Section {

    private String name;
    private String outLineNumber;
    private Date begin;
    private Date end;
    private List<SubSection> subs;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBegin() {
        return begin;
    }

    public void setBegin(Date begin) {
        this.begin = begin;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public List<SubSection> getSubs() {
        return subs;
    }

    public void setSubs(List<SubSection> subs) {
        this.subs = subs;
    }

    public String getOutLineNumber() {
        return outLineNumber;
    }

    public void setOutLineNumber(String outLineNumber) {
        this.outLineNumber = outLineNumber;
    }

}
