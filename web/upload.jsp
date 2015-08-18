<%-- 
    Document   : test
    Created on : Mar 31, 2014, 2:12:56 PM
    Author     : connie
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory"%>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>
<%!private static BlobstoreService blobstoreService=BlobstoreServiceFactory.getBlobstoreService();%>
<!DOCTYPE html>
  
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Upload Images</title>
    </head>
    <body>
       <form action="<%= blobstoreService.createUploadUrl("/upload") %>" method="post"  enctype="multipart/form-data">
         <input type="file" name="file" multiple="multiple">
         <input type="submit" value="Submit">
     </form>   
     </body>
</html>