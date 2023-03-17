<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="utilities.ClientServerEncrypter"%>
<%@page import="utilities.DesEncrypter"%>
<%@page import="java.sql.Statement"%>
<%@page import="java.sql.Connection"%>
<%@page import="service.MySQL.MySQLCommon"%>
<%@page import="web.polling.Question"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%
    String q = new DesEncrypter("RsH3uT9b").decrypt(request.getParameter("q0"));
    String poolName = "";
    String tz = "";
    String pollId = "";

    String[] parts = q.split("&");
    for (int i = 0; i < parts.length; i++) {
        String[] row = parts[i].split("=");
        if (row[0].equals("p")) {
            poolName = row[1];
        } else if (row[0].equals("t")) {
            tz = (row.length > 1 ? row[1] : "");
        } else if (row[0].equals("i")) {
            pollId = row[1];
        }
    }

    Connection con = MySQLCommon.getConnection(poolName, tz);
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery("SELECT model_id, filled IS NOT NULL FROM cal_poll WHERE id = " + pollId);

    boolean result = rs.next();
    boolean filled = true;
    String name = "";
    Integer modelId = null;
    if (result) {
        modelId = rs.getInt(1);
        filled = rs.getBoolean(2);
        rs.close();
        rs = st.executeQuery("SELECT name FROM cal_node n INNER JOIN cal_version v ON n.id = v.node_id INNER JOIN cal_poll_model m ON v.id = m.version_id AND m.id = " + modelId);
        rs.next();
        name = rs.getString(1);
        rs.close();

    }


%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Vista Previa del Formulario</title>
        <style type="text/css">
            .button {
                -moz-box-shadow:inset 0px 1px 0px 0px #ffffff;
                -webkit-box-shadow:inset 0px 1px 0px 0px #ffffff;
                box-shadow:inset 0px 1px 0px 0px #ffffff;
                background:-webkit-gradient( linear, left top, left bottom, color-stop(0.05, #ededed), color-stop(1, #dfdfdf) );
                background:-moz-linear-gradient( center top, #ededed 5%, #dfdfdf 100% );
                filter:progid:DXImageTransform.Microsoft.gradient(startColorstr='#ededed', endColorstr='#dfdfdf');
                background-color:#ededed;
                -webkit-border-top-left-radius:6px;
                -moz-border-radius-topleft:6px;
                border-top-left-radius:6px;
                -webkit-border-top-right-radius:6px;
                -moz-border-radius-topright:6px;
                border-top-right-radius:6px;
                -webkit-border-bottom-right-radius:6px;
                -moz-border-radius-bottomright:6px;
                border-bottom-right-radius:6px;
                -webkit-border-bottom-left-radius:6px;
                -moz-border-radius-bottomleft:6px;
                border-bottom-left-radius:6px;
                text-indent:0;
                border:1px solid #dcdcdc;
                display:inline-block;
                color:#777777;
                font-family:arial;
                font-size:15px;
                font-weight:bold;
                font-style:normal;
                height:50px;
                line-height:50px;
                width:100px;
                text-decoration:none;
                text-align:center;
                text-shadow:1px 1px 0px #ffffff;
            }
            .button:hover {
                background:-webkit-gradient( linear, left top, left bottom, color-stop(0.05, #dfdfdf), color-stop(1, #ededed) );
                background:-moz-linear-gradient( center top, #dfdfdf 5%, #ededed 100% );
                filter:progid:DXImageTransform.Microsoft.gradient(startColorstr='#dfdfdf', endColorstr='#ededed');
                background-color:#dfdfdf;
            }.button:active {
                position:relative;
                top:1px;
            }
        </style>
        <script type="text/javascript">

            function validateForm() {
            <%     if (!filled) {
                    rs = st.executeQuery("SELECT id, type, mandatory, show_other IS NOT NULL FROM cal_poll_question WHERE model_id = " + modelId + " ORDER BY place ASC");
                    for (int i = 0; rs.next(); i++) {
                        int qId = rs.getInt(1);
                        String qType = rs.getString(2);
                        boolean mandatory = rs.getBoolean(3);
                        boolean showOther = rs.getBoolean(4);
                        if (mandatory) {
                            if (qType.equals("opt_mat") || qType.equals("eval")) {
                                Question[] rows = Question.getQuestions(con, "row", qId);
                                for (int j = 0; j < rows.length; j++) {
                                    out.write("if(getRadioValue('" + (i + "_" + j) + "') === null){alert('Por favor, conteste la pregunta " + (i + 1) + ".'); return false;};");
                                }
                            } else if (qType.equals("chk_mat")) {
                                Question[] rows = Question.getQuestions(con, "row", qId);
                                Question[] cols = Question.getQuestions(con, "col", qId);
                                out.write("if(");
                                for (int j = 0; j < rows.length; j++) {
                                    for (int k = 0; k < cols.length; k++) {
                                        out.write("!getByName('" + (i + "_" + j + "_" + k) + "').checked && ");
                                    }
                                }
                                out.write("true){alert('Por favor, conteste la pregunta " + (i + 1) + ".'); return false;}\n\r");
                            } else if (qType.equals("chk_list")) {
                                Question[] rows = Question.getQuestions(con, "row", qId);
                                out.write("if(");
                                for (int j = 0; j < rows.length; j++) {
                                    out.write("!getByName('" + (i + "_" + j) + "').checked && ");
                                }
                                if (showOther) {
                                    out.write("!getByName('" + (i + "_other") + "').checked && ");
                                }
                                out.write("true){alert('Por favor, conteste la pregunta " + (i + 1) + ".'); return false;}\n\r");
                                if (showOther) {
                                    out.write("if(getByName('" + i + "_other').checked && getByName('" + i + "_txt_other').value == ''){alert('Pregunta " + (i + 1) + ", escriba el otro.'); return false;}");
                                }

                            } else if (qType.equals("opt_list")) {
                                out.write("if(getRadioValue(" + i + ") === null){alert('Por favor, conteste la pregunta " + (i + 1) + ".'); return false;}");
                                if (showOther) {
                                    out.write("if(getById('" + i + "_other').checked && getByName('" + i + "_txt_other').value == ''){alert('Pregunta " + (i + 1) + ", escriba el otro.'); return false;}");
                                }
                            } else if (qType.equals("num_list")) {
                                out.write("if(");
                                Question[] rows = Question.getQuestions(con, "row", qId);
                                for (int j = 0; j < rows.length; j++) {
                                    out.write("getByName('" + (i + "_" + j) + "').value == '' && ");
                                }
                                out.write("true){alert('Por favor, conteste la pregunta " + (i + 1) + ".'); return false;}\n\r");
                            } else if (qType.equals("txt_list")) {
                                Question[] rows = Question.getQuestions(con, "row", qId);
                                out.write("if(");
                                for (int j = 0; j < rows.length; j++) {
                                    out.write("getByName('" + (i + "_" + j) + "').value == '' && ");
                                }
                                out.write("true){alert('Por favor, conteste la pregunta " + (i + 1) + ".'); return false;}\n\r");
                            } else if (qType.equals("obs")) {
                                out.write("if(getById('" + i + "').value==''){alert('Por favor, conteste la pregunta " + (i + 1) + ".'); return false;}");
                            } else {
                                out.write(qType);
                            }
                        }
                    }
                }
            %>
                return true;
            }

            function getById(id) {
                return document.getElementById(id);
            }

            function getByName(name) {
                return document.getElementsByName(name)[0];
            }
            function getRadioValue(id) {
                var btns = document.getElementsByName(id);
                for (i = 0; i < btns.length; i++) {
                    if (btns[i].checked) {
                        return btns[i].value;
                    }
                }
                return null;
            }

            function justNum(evt) {
                var theEvent = evt || window.event;
                var key = theEvent.keyCode || theEvent.which;
                key = String.fromCharCode(key);
                var regex = /[0-9]|\./;
                if (!regex.test(key)) {
                    theEvent.returnValue = false;
                    if (theEvent.preventDefault)
                        theEvent.preventDefault();
                }
            }

        </script>
    </head>
    <style>
        .questionTittle{
            font-size: 16px;
            font-weight: bold;
            margin-top: 30px;
            margin-left: 20px;
        }
        .gray{
            background-color: #EFEFEE;
        }

        td{
            padding: 8px;
            text-align: center;
        }
        table{
            border-collapse:collapse;
            margin-left: 20px;
            margin-right: 20px;
        }
        .left{
            text-align: left;
        }
        body{
            padding: 0px;
            margin: 0px;
            font-family: Arial, sans-serif;
            font-size: 13px;
        }

    </style>
    <body class="arial">
        <table style="margin:0px; width: 100%">
            <tr style="background-color: #183D68; color:white; font-weight: bold">
                <td><%=name.toUpperCase()%></td>
            </tr>
            <tr style="background-color: #B3CDF3">
                <td></td>
            </tr>
        </table>
        <%
            if (!filled) {
        %>
        <form method="POST" action="savePoll" onsubmit="return validateForm()">
            <input type="hidden" id="q" name="q" value="<%=q%>" />
            <%
                rs = st.executeQuery("SELECT id, type, txt, mandatory, show_other FROM cal_poll_question WHERE model_id = " + modelId + " ORDER BY place ASC");
                for (int i = 0; rs.next(); i++) {
                    int qId = rs.getInt(1);
                    String qType = rs.getString(2);
                    String qTxt = rs.getString(3);
                    boolean mandatory = rs.getBoolean(4);
                    boolean showOther = rs.getBoolean(5);
                    out.write("<div class=\"questionTittle\">" + (i + 1) + ". " + (mandatory ? "*" : "") + qTxt + "</div><br>");

                    if (qType.equals("opt_mat") || qType.equals("eval")) {
                        Question[] rows = Question.getQuestions(con, "row", qId);
                        Question[] cols = Question.getQuestions(con, "col", qId);
                        int colWidth = (50 / cols.length);
                        out.write("<table width = \"70%\">");
                        out.write("<tr>");
                        out.write("<td width=\"50%\"></td>");
                        for (int j = 0; j < cols.length; j++) {
                            out.write("<td width=\"" + colWidth + "%\" >" + cols[j].txt + "</td>");
                        }
                        out.write("</tr>");
                        for (int j = 0; j < rows.length; j++) {
                            out.write("<tr class=\"" + (j % 2 == 0 ? "gray" : "") + "\">");
                            out.write("<td class=\"left\">" + rows[j].txt + "</td>");
                            for (int k = 0; k < cols.length; k++) {
                                out.write("<td><input value=\"" + k + "\" name=\"" + (i + "_" + j) + "\" type = \"radio\"/></td>");
                            }
                            out.write("</tr>");
                        }
                        out.write("</table>");
                    } else if (qType.equals("chk_mat")) {
                        Question[] rows = Question.getQuestions(con, "row", qId);
                        Question[] cols = Question.getQuestions(con, "col", qId);
                        int colWidth = (50 / cols.length);
                        out.write("<table width = \"70%\">");
                        out.write("<tr>");
                        out.write("<td width=\"50%\"></td>");
                        for (int j = 0; j < cols.length; j++) {
                            out.write("<td width=\"" + colWidth + "%\" >" + cols[j].txt + "</td>");
                        }
                        out.write("</tr>");
                        for (int j = 0; j < rows.length; j++) {
                            out.write("<tr class=\"" + (j % 2 == 0 ? "gray" : "") + "\">");
                            out.write("<td class=\"left\">" + rows[j].txt + "</td>");
                            for (int k = 0; k < cols.length; k++) {
                                out.write("<td><input name=\"" + (i + "_" + j + "_" + k) + "\" type = \"checkbox\"/></td>");
                            }
                            out.write("</tr>");
                        }
                        out.write("</table>");
                    } else if (qType.equals("chk_list")) {
                        Question[] rows = Question.getQuestions(con, "row", qId);
                        out.write("<table width = \"70%\">");
                        for (int j = 0; j < rows.length; j++) {
                            out.write("<tr " + (j % 2 == 0 ? "class='gray'" : "") + ">");
                            out.write("<td width=\"30px\" ><input name=\"" + (i + "_" + j) + "\" type = \"checkbox\"/></td>");
                            out.write("<td class=\"left\">" + rows[j].txt + "</td>");
                            out.write("</tr>");
                        }
                        if (showOther) {
                            out.write("<tr " + (rows.length % 2 == 0 ? "class='gray'" : "") + ">");
                            out.write("<td width=\"30px\"><input name=\"" + i + "_other\" type = \"checkbox\" onclick=\"getByName('" + i + "_txt_other').disabled = !getByName('" + i + "_other').checked\"/></td>");
                            out.write("<td class=\"left\">Otro</td>");
                            out.write("</tr>");
                            out.write("<tr " + (rows.length % 2 != 0 ? "class='gray'" : "") + ">");
                            out.write("<td class=\"left\" colspan=\"2\"><input disabled name=\"" + i + "_txt_other\" type = \"text\"/></td>");
                            out.write("</tr>");
                        }
                        out.write("</table>");
                    } else if (qType.equals("opt_list")) {
                        Question[] rows = Question.getQuestions(con, "row", qId);
                        out.write("<table width = \"70%\">");
                        for (int j = 0; j < rows.length; j++) {
                            out.write("<tr " + (j % 2 == 0 ? "class='gray'" : "") + ">");
                            out.write("<td width=\"30px\" ><input value =\"" + j + "\" name=\"" + i + "\" type = \"radio\"/ onclick=\"getByName('" + i + "_txt_other').disabled = true\"></td>");
                            out.write("<td class=\"left\">" + rows[j].txt + "</td>");
                            out.write("</tr>");
                        }
                        if (showOther) {
                            out.write("<tr " + (rows.length % 2 == 0 ? "class='gray'" : "") + ">");
                            out.write("<td width=\"30px\"><input value =\"" + rows.length + "\" name=\"" + i + "\" id=\"" + i + "_other\"type = \"radio\"/ onclick=\"getByName('" + i + "_txt_other').disabled = !getById('" + i + "_other').checked\"/></td>");
                            out.write("<td class=\"left\">Otro</td>");
                            out.write("</tr>");
                            out.write("<tr " + (rows.length % 2 != 0 ? "class='gray'" : "") + ">");
                            out.write("<td class=\"left\" colspan=\"2\"><input disabled name=\"" + i + "_txt_other\" type = \"text\"/></td>");
                            out.write("</tr>");
                        }
                        out.write("</table>");
                    } else if (qType.equals("num_list")) {
                        Question[] rows = Question.getQuestions(con, "row", qId);
                        out.write("<table width = \"70%\">");
                        for (int j = 0; j < rows.length; j++) {
                            out.write("<tr>");
                            out.write("<td class=\"left\" width=\"25%\">" + rows[j].txt + "</td>");
                            out.write("<td class=\"left\" width=\"75%\"><input name=\"" + i + "_" + j + "\" type = \"text\" onkeypress='justNum(event)'/></td>");
                            out.write("</tr>");
                        }
                        out.write("</table>");
                    } else if (qType.equals("txt_list")) {
                        Question[] rows = Question.getQuestions(con, "row", qId);
                        out.write("<table width = \"70%\">");
                        for (int j = 0; j < rows.length; j++) {
                            out.write("<tr>");
                            out.write("<td class=\"left\" width=\"25%\">" + rows[j].txt + "</td>");
                            out.write("<td class=\"left\" width=\"75%\"><input name=\"" + i + "_" + j + "\" type = \"text\"/></td>");
                            out.write("</tr>");
                        }
                        out.write("</table>");
                    } else if (qType.equals("obs")) {
                        out.write("<table width = \"70%\">");
                        out.write("<tr>");
                        out.write("<td class=\"left\" width=\"25%\"><textarea id = \"" + i + "\" name = \"" + i + "\" rows=\"6\" cols=\"46\"></textarea></td>");
                        out.write("</tr>");
                        out.write("</table>");
                    } else {
                        out.write(qType);
                    }
                }
            %>
            <br>
            <table border="0" width="70%" style="margin-top: 10px; margin-bottom: 50px">
                <tr>
                    <td>
                        <input class="button" type="submit" value="Enviar">
                    </td>
                </tr>
            </table>
        </form>
        <%
        } else {
            if (!result) {
        %>
        <div class="questionTittle">Gracias, la encuesta ya fue diligenciada.</div><br>
        <%
        } else {
        %>
        <div class="questionTittle">Gracias, la encuesta ya fue diligenciada.</div><br>
        <%
                }

            }
        %>
    </body>
</html>
<%
            
            
    MySQLCommon.closeConnection(con, st);
    
%>