package model.indicator.grid;

import java.math.BigDecimal;
import java.util.ArrayList;

public class GridResultGroup {
    private ArrayList<Double> proyections;
    private ArrayList<Double> results;    
    private ArrayList<Integer> colors;
    private BigDecimal score;
    
    public GridResultGroup(){
        results = new ArrayList<Double>();
        proyections = new ArrayList<Double>();
        colors = new ArrayList<Integer>();
        score = BigDecimal.ZERO;
    }

    public ArrayList<Double> getResults() {
        return results;
    }

    public void setResults(ArrayList<Double> results) {
        this.results = results;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }
    
    public ArrayList<Integer> getColors() {
        return colors;
    }
    
    public void setColors(ArrayList<Integer> colors) {
        this.colors = colors;
    }
    
    public ArrayList<Double> getProyections() {
        return proyections;
    }
    
    public void setProyections(ArrayList<Double> proyections) {
        this.proyections = proyections;
    }
}
