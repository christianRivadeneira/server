/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api.rpt.model.cubeImport;

public class HtmlBuilder implements Builder {

    private final StringBuilder sb = new StringBuilder("<html><body><font face=\"Consolas\" size=\"5\" >");

    @Override
    public HtmlBuilder sp() {
        sb.append(" ");
        return this;
    }

    @Override
    public HtmlBuilder br() {
        sb.append("<br/>");
        sb.append(System.lineSeparator());
        return this;
    }

    @Override
    public HtmlBuilder black(String text) {
        sb.append("<FONT COLOR = \"000000\"><b>").append(text).append("</b></font>");
        return this;
    }

    @Override
    public HtmlBuilder gray(String text) {
        sb.append("<FONT COLOR = \"808080\">").append(text).append("</font>");
        return this;
    }

    @Override
    public HtmlBuilder blue(String text) {
        sb.append("<FONT COLOR = \"0000FF\"><b>").append(text).append("</b></font>");
        return this;
    }

    @Override
    public HtmlBuilder blueReg(String text) {
        sb.append("<FONT COLOR = \"0000FF\">").append(text).append("</font>");
        return this;
    }

    @Override
    public HtmlBuilder fucsia(String text) {
        sb.append("<FONT COLOR = \"FF00FF\">").append(text).append("</font>");
        return this;
    }

    @Override
    public HtmlBuilder olive(String text) {
        sb.append("<FONT COLOR = \"808000\">").append(text).append("</font>");
        return this;
    }

    @Override
    public String toString() {
        sb.append("</font></body></html>");
        return sb.toString();
    }
}
