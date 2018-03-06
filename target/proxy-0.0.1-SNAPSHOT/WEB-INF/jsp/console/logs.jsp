<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<!doctype html>
<html class="no-js" lang="" ng-app="app">
    <head>
        <meta charset="utf-8">
        <meta http-equiv="x-ua-compatible" content="ie=edge">
        <title>Console : JSON Objects</title>
		<jsp:include page="../inc/resources.jsp"></jsp:include>
    </head>
    <body>
	<div class="container theme-showcase" role="main">
	 <jsp:include page="../inc/navbar.jsp"></jsp:include>
		<div class="jumbotron">
			<div class="row" ng-controller="LogController">
				<div class="form-group">
					<div class="col-xs-4">
						<label for="sel1">Select list (select one):</label> 
						<select class="form-control" ng-model="selectedFile" id="file">
							<option disabled selected value="">-- select file to view--</option>
							<option ng-repeat="file in files" value="{{file}}">
      							{{file}}
    						</option>
						</select>
					</div>
					<div class="col-xs-3">
						<label for="ex2">Load lines:</label>
						<input class="form-control"id="load-lines" ng-model="limitLines" type="number">
					</div>
					<div class="col-xs-3">
						<label for="ex2">Pattern:</label>
						<input class="form-control"id="load-lines" ng-model="regexp" type="text">
					</div>
					<div class="col-xs-2">
						<label for="ex2">&nbsp;</label><br/>
						<button id="search-btn" ng-click="onSearchClick()" type="button" class="btn btn-sm btn-default btn-success">
							<span class="glyphicon glyphicon-search" aria-hidden="true"></span>
								Search
						</button>
					</div>
				</div>



				<table class="table table-striped">
					<thead>
						<tr>
							<th>Log line</th>
						</tr>
					</thead>
					<tbody>
						<tr ng-repeat="line in logs" style="cursor: pointer;" ng-dblclick="showLine(line)">
							<td>{{line}}</td>
						</tr>
					</tbody>
				</table>
				<!-- Modal -->
				<div class="modal fade" id="log-content" role="dialog">
					<div class="modal-dialog modal-lg" >
						<!-- Modal content-->
						<div class="modal-content">
							<div class="modal-header">
								<h4>Log line</h4>
							</div>
							<div class="modal-body">
								<div class="form-group">
									<textarea class="form-control" rows="10" ng-model="currentLog" required="required"></textarea>
								</div>

							</div>
							<div class="modal-footer">
								<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

	</div>
	<script type="text/javascript">
		var app = angular.module('app', []);
		
		function LogController($scope, $http, $filter) {
			$scope.files = [];
			$scope.logs = [];
			
			$scope.limitLines = 200;
			
			$http.get('/admin/log').success(function(response){
	            $scope.files = response;
	        });
			
			$scope.onSearchClick = function() {
				if($scope.selectedFile) {
					disableShowSearch($('#search-btn'));
					
					$http.get('/admin/log/' + $scope.selectedFile, { params : {'limit' : $scope.limitLines, 'regexp' : $scope.regexp}}).success(function(response){
			            $scope.logs = response;
			            enableHideSearch($('#search-btn'));
			        });
				}
		    };
		    
		    $scope.showLine = function(line) {
		    	$scope.currentLog = line;
	    		$('#log-content').modal();
		    }
		    
			
		}
		
		app.controller('LogController', ['$scope', '$http', '$filter', LogController]);
		
		
		
	</script>	
	
    </body>
</html>
