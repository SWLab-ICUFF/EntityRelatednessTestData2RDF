<%@page import="java.net.URL"%>
<%@page import="java.util.Scanner"%>
<%@page import="java.io.Reader"%>
<%@page import="jdk.nashorn.api.scripting.URLReader"%>
<%
    String domain = request.getRequestURL().toString().replaceFirst("/\\z","");
    String viewer = "<embed width=\"100%\" height=\"100%\" style=\"position:absolute; border:none; width:100%; height:100%;\" src=\"http://visualdataweb.de/webvowl/#iri="+domain+".rdf\"/>";
    String ontology="";
    try(Reader reader = new URLReader(new URL("http://rdf-translator.appspot.com/convert/xml/rdfa/"+domain+".rdf"))){
        ontology = new Scanner(reader).useDelimiter("\\Z").next();
    } catch (Exception e){
    }
%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <link href="swlab.css" rel="stylesheet" type="text/css">
        <title>Entity Relatedness Test Data Ontology (v1)</title>
    </head>
    <body>
        <%=viewer%>
        <%=ontology%>
    </body>
</html>