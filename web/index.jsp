<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="api.sys.model.SystemApp"%>
<%@page import="utilities.Base64"%>
<%@page import="utilities.MySQLQuery"%>
<%@page import="service.MySQL.MySQLCommon"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.Statement"%>
<%@page import="java.sql.Connection"%>

<%

    Connection con = MySQLCommon.getConnection("sigmads", null);

    Object[][] sysCfg = new MySQLQuery("SELECT app_name, "                        
            + "url_new_launcher, "
            + "web_password "
            + "FROM sys_cfg").getRecords(con);
    
    String appName = MySQLQuery.getAsString(sysCfg[0][0]);
    String url_new_launcher = MySQLQuery.getAsString(sysCfg[0][1]);
    String webPassword = MySQLQuery.getAsString(sysCfg[0][2]);
    
    if (webPassword != null && !webPassword.isEmpty()) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Basic ")) {
            auth = auth.substring(6);
            if (!new String(Base64.decode(auth), "UTF-8").equals("admin:" + webPassword)) {
                response.setHeader("WWW-Authenticate", "Basic realm=\"Acceso Restringido\"");
                response.setStatus(401);
                return;
            }
        } else {
            response.setHeader("WWW-Authenticate", "Basic realm=\"Acceso Restringido\"");
            response.setStatus(401);
            return;
        }
    }

    boolean userAgentIsMobile;
    if (request.getParameter("type") != null) {
        userAgentIsMobile = request.getParameter("type").equals("mobile");
    } else {
        userAgentIsMobile = isMobile(request.getHeader("User-Agent").toLowerCase());
    }
%>
<html dir="ltr">
    <head>
        <!-- Basic -->
        <meta charset="utf-8">
        <title>Descargue <%= appName%> <%= userAgentIsMobile ? "Móvil" : "Desktop"%></title>
        <meta name="keywords" content="">
        <meta name="description" content="Descargas">
        <meta name="author" content="Qualisys">
        <!-- Mobile Metas -->
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <!-- Web Fonts  -->
        <link href="http://fonts.googleapis.com/css?family=Open+Sans:300,400,600,700,800%7CShadows+Into+Light" rel="stylesheet" type="text/css">

        <!-- Vendor CSS -->
        <link rel="stylesheet" href="vendor/bootstrap/bootstrap.css">
        <link rel="stylesheet" href="vendor/fontawesome/css/font-awesome.css">
        <link rel="stylesheet" href="vendor/owlcarousel/owl.carousel.css" media="screen">
        <link rel="stylesheet" href="vendor/owlcarousel/owl.theme.css" media="screen">
        <link rel="stylesheet" href="vendor/magnific-popup/magnific-popup.css" media="screen">

        <!-- Theme CSS -->
        <link rel="stylesheet" href="css/theme.css">
        <link rel="stylesheet" href="css/theme-elements.css">
        <link rel="stylesheet" href="css/theme-blog.css">
        <link rel="stylesheet" href="css/theme-shop.css">
        <link rel="stylesheet" href="css/theme-animate.css">

        <!-- Skin CSS -->
        <link rel="stylesheet" href="css/skins/default.css">

        <!-- Theme Custom CSS -->
        <link rel="stylesheet" href="css/custom.css">

        <!-- Head Libs -->
        <script src="vendor/modernizr/modernizr.js"></script>
        <style type="text/css">
            @media (max-width: 767px) {
                .img_resp {
                    float: none;
                    margin: auto;
                    width: 50%;
                }
            }
        </style>
    </head>
    <body >
        <div class="body">
            <header id="header" class="flat-menu single-menu">
                <div class="container">
                    <h1 class="logo">
                        <img alt="Porto" width="208" height="70" data-sticky-width="149" data-sticky-height="50" src="img/logo.png">
                    </h1>
                    <button class="btn btn-responsive-nav" data-toggle="collapse" data-target=".nav-main-collapse">
                        <i class="fa fa-bars"></i>
                    </button>
                </div>
                <div class="navbar-collapse nav-main-collapse collapse">
                    <div class="container">
                        <nav class="nav-main mega-menu">
                            <ul class="nav nav-pills nav-main" id="mainMenu">
                                <li data-key="contact" <%=!userAgentIsMobile ? "class=\"active\"" : ""%>>
                                    <a href="?type=desktop">
                                        Descargas Desktop
                                    </a>
                                </li>
                                <li data-key="contact"  <%=userAgentIsMobile ? "class=\"active\"" : ""%>>
                                    <a href="?type=mobile">
                                        Descargas Móvil
                                    </a>
                                </li>
                            </ul>
                        </nav>
                    </div>
                </div>
            </header>

            <div role="main">
                <section class="page-top">
                    <div class="container">
                        <div class="row">
                            <div class="col-md-12">
                                <h2><strong>Descargue</strong> <%= appName%> <%= userAgentIsMobile ? "Móvil" : "Desktop"%></h2>
                            </div>
                        </div>
                    </div>
                </section>
                <div class="container">
                    <div class="col-sm-12" style="text-align: center;">
                        <%
                            if (!userAgentIsMobile) {
                                writeDescktop(url_new_launcher, out);
                            } else {
                                writesMobile(con, out);
                            }
                        %>
                    </div>
                    <div class="clearfix"></div>
                </div>

                <!-- Vendor --> 
                <script src="vendor/jquery/jquery.js"></script> 
                <script src="vendor/bootstrap/bootstrap.js"></script>
                <script src="vendor/common/common.js"></script> 
                <!-- Theme Base, Components and Settings --> 
                <script src="js/theme.js"></script> 
                <!-- Specific Page Vendor and Views --> 
                <script src="js/views/view.contact.js"></script> 
                <!-- Theme Custom --> 
                <script src="js/custom.js"></script> 
                <!-- Theme Initialization Files --> 
                <script src="js/theme.init.js"></script> 
                <script src="http://maps.google.com/maps/api/js?sensor=false"></script> 

                </body>
                </html>
                <%    MySQLCommon.closeConnection(con);%>
                <%!
                    private Boolean isMobile(String userAgent) {
                        return (userAgent).matches(".*(android|avantgo|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\\/|plucker|pocket|psp|symbian|treo|up\\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino).*") || userAgent.substring(0, 4).matches("1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\\-(n|u)|c55\\/|capi|ccwa|cdm\\-|cell|chtm|cldc|cmd\\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\\-s|devi|dica|dmob|do(c|p)o|ds(12|\\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\\-|_)|g1 u|g560|gene|gf\\-5|g\\-mo|go(\\.w|od)|gr(ad|un)|haie|hcit|hd\\-(m|p|t)|hei\\-|hi(pt|ta)|hp( i|ip)|hs\\-c|ht(c(\\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\\-(20|go|ma)|i230|iac( |\\-|\\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\\/)|klon|kpt |kwc\\-|kyo(c|k)|le(no|xi)|lg( g|\\/(k|l|u)|50|54|e\\-|e\\/|\\-[a-w])|libw|lynx|m1\\-w|m3ga|m50\\/|ma(te|ui|xo)|mc(01|21|ca)|m\\-cr|me(di|rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\\-2|po(ck|rt|se)|prox|psio|pt\\-g|qa\\-a|qc(07|12|21|32|60|\\-[2-7]|i\\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\\-|oo|p\\-)|sdk\\/|se(c(\\-|0|1)|47|mc|nd|ri)|sgh\\-|shar|sie(\\-|m)|sk\\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\\-|v\\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\\-|tdg\\-|tel(i|m)|tim\\-|t\\-mo|to(pl|sh)|ts(70|m\\-|m3|m5)|tx\\-9|up(\\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|xda(\\-|2|g)|yas\\-|your|zeto|zte\\-");
                    }

                    private void writeLinkDesktop(String href, String name, String description, String img, JspWriter mOut) throws Exception {
                        mOut.println(""
                                + "<div class=\"col-sm-3\"> "
                                + "<div class=\"col-ms-12\">"
                                + "<div class=\"col-sm-12\">"
                                + "<div class=\"col-sm-8\" style=\"float:none !important; margin:auto;\">"
                                + "<a  href=\"" + href + "\"> <img alt=\"Descargar\"  class=\"img-responsive\" src=\"img/" + img + "\" > </a>"
                                + "</div>"
                                + "</div>"
                                + "<h4 style=\"font-size:14px !important; font-weight: bold;\">" + escapeHtml(name) + "</h4>"
                                + "<p style=\"font-size:14px !important;\">"
                                + description
                                + "</p>"
                                + "<p>&nbsp;</p>"
                                + "</div>"
                                + "</div>"
                        );
                    }

                    private void writesMobile(Connection con, JspWriter mOut) throws Exception {
                        List<SystemApp> apps = SystemApp.getAll(con);
                        for (int i = 0; i < apps.size(); i++) {
                            if (i % 4 == 0) {
                                mOut.println("<div class=\"row\">");
                            }
                            writeLinkMobile(apps.get(i), mOut);
                            if (((i + 1) % 4 == 0 || (i + 1) == apps.size())) {
                                mOut.println("</div>");
                            }
                        }
                    }

                    public void writeLinkMobile(SystemApp app, JspWriter mOut) throws Exception {
                        mOut.println(""
                                + "<div class=\"col-sm-3\"> "
                                + "<div class=\"col-sm-12\">"
                                + "<div class=\"col-sm-12\">"
                                + "<div class=\"col-sm-8\" style=\"float:none !important; margin:auto;\">"
                                + "<a href=\"" + (app.appDownloadUrl == null ? "DownloadApk?appName=" + app.packageName : app.appDownloadUrl) + "\"> <img alt=\"Descargar\" class=\"img-responsive img_resp\" src=\"img/" + app.appDownloadImg + "\" > </a>"
                                + "</div>"
                                + "</div>"
                                + "<h4 style=\"font-size:14px !important; font-weight: bold;\">" + escapeHtml(app.showName) + "</h4>"
                                + "<p style=\"font-size:14px !important;\">"
                                + (app.description != null ? app.description : "")
                                + "</p>"
                                + "</div>"
                                + "<p>&nbsp;</p>"
                                + "</div>");
                    }

                    private void writeDescktop(String url_new_launcher, JspWriter mOut) throws Exception {
                        
                        writeLinkDesktop("GetInstallerExe?poolName=sigmads", "Iniciar Aplicación Windows", "La opción recomendada para usuarios windows.", "aplicacion_windows.png", mOut);

                        if (url_new_launcher != null && !url_new_launcher.isEmpty()) {
                            writeLinkDesktop(url_new_launcher, "Iniciar la Aplicación Estándar", "La opción recomendada para la mayoría de usuarios.", "aplicacion_java.png", mOut);
                        } else {
                            writeLinkDesktop("launcher/generate?repoPath=", "Iniciar la Aplicación Estándar", "La opción recomendada para la mayoría de usuarios.", "aplicacion_java.png", mOut);
                        }

                    }

                    private String escapeHtml(String txt) {
                        txt.replaceAll("á", "&aacute;");
                        txt.replaceAll("é", "&eacute;");
                        txt.replaceAll("í", "&iacute;");
                        txt.replaceAll("ó", "&oacute;");
                        txt.replaceAll("ú", "&uacute;");

                        txt.replaceAll("Á", "&Aacute;");
                        txt.replaceAll("É", "&Eacute;");
                        txt.replaceAll("Í", "&Iacute;");
                        txt.replaceAll("Ó", "&Oacute;");
                        txt.replaceAll("Ú", "&Uacute;");
                        return txt;
                    }
                %>
