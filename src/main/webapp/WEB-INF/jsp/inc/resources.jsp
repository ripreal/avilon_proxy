<%@page import="ru.avilon.proxy.rest.configuration.Version"%>
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%
request.setAttribute("page_revision", Version.getRevision(request));
%>
        <meta name="description" content="">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <link rel="apple-touch-icon" href="apple-touch-icon.png">
        <!-- Place favicon.ico in the root directory -->

		<script src="/assets/js/vendor/jquery-1.12.0.min.js"></script>
		<script src="/assets/js/vendor/jquery.inputmask.min.js"></script>
		<script src="/assets/js/vendor/moment.min.js"></script>
        <link rel="stylesheet" href="/assets/css/bootstrap-3.3.6/css/bootstrap.min.css">
        <script src="/assets/css/bootstrap-3.3.6/js/bootstrap.min.js"></script>
        
        <script src="/assets/js/vendor/angular/angular.min.js"></script>
        <script src="/assets/js/vendor/angular/angular-resource.min.js"></script>
       
        
<!--         <link rel="stylesheet" href="/assets/css/normalize.css"> -->
<!--         <link rel="stylesheet" href="/assets/css/main.css"> -->
        <script src="/assets/js/vendor/modernizr-2.8.3.min.js"></script>
        
        <script src="/assets/js/plugins.js?ver=${page_revision}"></script>
        <script src="/assets/js/main.js?ver=${page_revision}"></script>
		<style>
			table {
				table-layout: fixed;
				/*   width: 100px; */
			}
			
			td {
				white-space: nowrap;
				overflow: hidden; /* <- this does seem to be required */
				text-overflow: ellipsis;
			}
			.glyphicon-refresh-animate {
			    -animation: spin .7s infinite linear;
			    -webkit-animation: spin2 .7s infinite linear;
			}
			
			@-webkit-keyframes spin2 {
			    from { -webkit-transform: rotate(0deg);}
			    to { -webkit-transform: rotate(360deg);}
			}
			
			@keyframes spin {
			    from { transform: scale(1) rotate(0deg);}
			    to { transform: scale(1) rotate(360deg);}
			}
		</style>