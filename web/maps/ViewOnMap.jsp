<%
    String server = request.getParameter("server");
    String fileName = request.getParameter("fileName");
    String ctLat = request.getParameter("ctLat");
    String ctLon = request.getParameter("ctLon");
%>

<html>
    <head>
        <meta name="viewport" content="initial-scale=1.0, user-scalable=no">
        <meta charset="utf-8">
        <title>Puntos y Rutas</title>
        <style>
            html, body {
                height: 100%;
                padding: 0;
                margin: 0;
            }
            #map {
                height: 100%;
                width: 100%;
                overflow: hidden;
                float: left;
                border: thin solid #333;
                object-fit: cover;
            }
            #capture {
                height: 560px;
                width: 680px;
                overflow: hidden;
                float: left;
                background-color: #ECECFB;
                border: thin solid #333;
                border-left: none;
            }
        </style>
    </head>
    <body>
        <div id="map"></div>
        <script>
            var map;
            var src = '<%=server%>GetRouteKml?fileName=<%=fileName%>';
                //console.log(src);
                function initMap() {
                    console.log("Initializing map");
                    map = new google.maps.Map(document.getElementById('map'), {
                        center: {lat: <%=ctLat%>, lng: <%=ctLon%>},
                        zoom: 16,
                        streetViewControl: true,
                        mapTypeId: 'satellite'
                    });

                    console.log("Layer init");
                    var kmlLayer = new google.maps.KmlLayer(src, {
                        suppressInfoWindows: false,
                        preserveViewport: true,
                        map: map
                    });
                }
        </script>
        <script async defer  src="https://maps.googleapis.com/maps/api/js?key=AIzaSyAnT3K3stMVDQ5rKnKGHkSwlPrR0nUj4vQ&callback=initMap"></script>
    </body>
</html>