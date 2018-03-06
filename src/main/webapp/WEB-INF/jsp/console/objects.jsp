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
		<div class="jumbotron" ng-controller="JsonObjectsController">
			<div class="row">
			
				<div class="form-group">
					<div class="col-xs-2">
						<label for="sel1">Client app:</label> 
						<input class="form-control" ng-model="filter.f_clientapp" type="text">
					</div>
					<div class="col-xs-2">
						<label for="ex2">Object type:</label>
						<input class="form-control" ng-model="filter.f_objecttype" type="text">
					</div>
					<div class="col-xs-3">
						<label>Time from:</label>
						<input class="form-control time-control" ng-model="filter.time_from" type="text">
					</div>
					<div class="col-xs-3">
						<label>Time to:</label>
						<input class="form-control time-control" ng-model="filter.time_to" type="text">
					</div>
				</div>
			</div>
			<div class="row">
				<div class="form-group">
					<div class="col-xs-8">
						<label for="sel1">Pattern:</label> 
						<input class="form-control" ng-model="filter.pattern" type="text">
					</div>
					<div class="col-xs-2">
						<label>Limit:</label>
						<input class="form-control" ng-model="filter.limit" type="text">
					</div>
					<div class="col-xs-2">
						<label for="ex2">&nbsp;</label><br/>
						<button id="search-btn" ng-click="onSearchClick()" type="button" class="btn btn-sm btn-default btn-success">
							<span class="glyphicon glyphicon-search" aria-hidden="true"></span>
								Search
						</button>
					</div>
				</div>
			</div>
			<div class="row" style="padding-top: 7px;">
				<div class="btn-group">
					<button ng-click="onAddClick()" type="button" class="btn btn-sm btn-default btn-success">
						<span class="glyphicon glyphicon-plus" aria-hidden="true"></span>
						Add
					</button>
				</div>
				<div class="btn-group">
					<button ng-click="onRemoveAllClick($event)" type="button" class="btn btn-sm btn-default btn-danger">
						<span class="glyphicon glyphicon-remove" aria-hidden="true"></span>
						Remove visible
					</button>
				</div>
			</div>

			<div class="row">
				<table class="table table-striped">
					<thead>
						<tr>
							<th class="col-md-1" style="padding: 1px;">
								
							</th>
							<th class="col-md-5">UUID</th>
							<th class="col-md-3">Time</th>
							<th class="col-md-2">Object type</th>
							<th class="col-md-2">Client app</th>
							<th class="col-md-5">Object data</th>
						</tr>
					</thead>
					<tbody>
						<tr ng-repeat="jsonObject in jsonObjects">
							<td>
								<a href="#" ng-click="onEditClick($event, jsonObject)"><span class="glyphicon glyphicon-pencil" aria-hidden="true"></span></a>
							  	<a href="#" ng-click="onRemoveClick($event, jsonObject)"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span></a>
							</td>
							<td>{{jsonObject.uuid}}</td>
							<td>{{jsonObject.timestamp | date:'yyyy-MM-dd HH:mm:ss' }}</td>
							<td>{{jsonObject.objecttype}}</td>
							<td>{{jsonObject.clientapp}}</td>
							<td>{{jsonObject.data}}</td>
						</tr>
					</tbody>
				</table>
				<!-- Modal -->
				<div class="modal fade" id="myModal" role="dialog">
					<div class="modal-dialog">
						<!-- Modal content-->
						<div class="modal-content">
							<div class="modal-header">
								<h4>{{currentJsonObject.uuid}}</h4>
							</div>
							<div class="modal-body">
								<div class="form-group">
    								<label for="exampleInputEmail1">Time</label>
    								<input type="text" class="form-control time-control" ng-model="currentJsonObject.dateAsString" required="required">
  								</div>
  								<div class="form-group">
    								<label for="exampleInputEmail1">Object type</label>
    								<input type="text" class="form-control" ng-model="currentJsonObject.objecttype" required="required">
  								</div>
  								<div class="form-group">
    								<label for="exampleInputEmail1">Object data</label>
    								<textarea class="form-control" rows="3" ng-model="currentJsonObject.data" required="required"></textarea>
  								</div>
								
							</div>
							<div class="modal-footer">
								<button type="button" class="btn btn-default btn-success" ng-click="onSaveClick($event, currentJsonObject)">Save</button>
								<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

	</div>
	<script type="text/javascript">
		$(function() {
			$('.time-control').inputmask("9999-99-99 99:99:99").change();;
		});
		
		
	
		function JsonObjectsController($scope, $http, $filter, JsonObjectsFactory) {
	
			function queryObjects() {
				
				disableShowSearch($('#search-btn'));
				
				$scope.jsonObjects = JsonObjectsFactory.query($scope.filter, function(data) {
					for(var i in  data) {
						var element = data[i];
						if("m" == element.constructor.name)
							element.timestamp = new Date(element.timestamp);
					}
					enableHideSearch($('#search-btn'));
				});
			}
			
			$scope.filter = {};
			
			$scope.filter.limit = 200;
			
			$scope.onSearchClick = function() {
	    		var parsedDateFrom = moment($scope.filter.time_from);
	    		if(!parsedDateFrom.valueOf()) {
// 	    			console.log('Ошибка обработки даты ' + $scope.filter.time_from + '!');
	    			$scope.filter.timestamp_from = null;
	    		} else {
	    			$scope.filter.timestamp_from = parsedDateFrom.valueOf();
	    		}
	    		
	    		var parsedDateTo = moment($scope.filter.time_to);
	    		if(!parsedDateTo.valueOf()) {
// 	    			console.log('Ошибка обработки даты ' + $scope.filter.time_to + '!');
	    			$scope.filter.timestamp_to = null;
	    		} else {
	    			$scope.filter.timestamp_to = parsedDateTo.valueOf();
	    		}
	    		
	    		queryObjects();
		    };
		    
		    queryObjects();
			
	    	
			$scope.onSaveClick = function($event, jsonObject) {
	    		$event.preventDefault();
	    		
	    		var parsedDate = moment(jsonObject.dateAsString);
	    		if(!parsedDate.valueOf()) {
	    			alert('Ошибка обработки даты ' + jsonObject.dateAsString + '!');
	    			return;
	    		}
				jsonObject.timestamp = parsedDate.valueOf();
				
				$scope.jsonObjects[$scope.jsonObjects.indexOf($scope.selectedJsonObject)] = JsonObjectsFactory.update(jsonObject, function(response) {
					queryObjects();
			    	$('#myModal').modal('hide');	
				}, function(response) {
					console.log(response);
					alert('Ошибка сохранения данных на сервере!');
				}); 
						
			}
	    	
	    	$scope.onEditClick = function($event, jsonObject) {
	    		$event.preventDefault();
	    		$scope.selectedJsonObject = jsonObject;
	    		$scope.currentJsonObject = angular.copy($scope.selectedJsonObject);
	    		$scope.currentJsonObject.dateAsString = $filter('date')($scope.currentJsonObject.timestamp, "yyyy-MM-dd HH:mm:ss"); 
	    		$('#myModal').modal();
	    	}
	    	
	    	$scope.onAddClick = function() {
	    		$scope.currentJsonObject = new JsonObject();
	    		$scope.currentJsonObject.dateAsString = $filter('date')($scope.currentJsonObject.timestamp, "yyyy-MM-dd HH:mm:ss");
	    		$('#myModal').modal();
	    	}
	    	
	    	$scope.onRemoveAllClick = function($event) {
	    		$event.preventDefault();
	    		if (confirm('Удалить все видимые объекты?')) {
	    			angular.forEach($scope.jsonObjects, function(jsonObject) {
						JsonObjectsFactory.delete({'uuid': jsonObject.uuid, 'clientapp': jsonObject.clientapp, 'objecttype': jsonObject.objecttype}, function(response) {
	  	    				$scope.jsonObjects.splice($scope.jsonObjects.indexOf(jsonObject), 1);
	  					}, function(response) {
	  						console.log(response);
	  						alert('Ошибка сохранения данных на сервере!');
	  						}); 
	    				});
	    		} else {
	    		}
	    	}
	    	
	    	
	    	$scope.onRemoveClick = function($event, jsonObject) {
	    		$event.preventDefault();
	    		if (confirm('Удалить данный объект?')) {
	    			JsonObjectsFactory.delete({'uuid': jsonObject.uuid, 'clientapp': jsonObject.clientapp, 'objecttype': jsonObject.objecttype}, function(response) {
	    				$scope.jsonObjects.splice($scope.jsonObjects.indexOf(jsonObject), 1);
					}, function(response) {
						console.log(response);
						alert('Ошибка сохранения данных на сервере!');
					}); 
	    		} else {
	    		}
	    	}
	    	
	    	$scope.onConfirmRemove = function(trackToRemove) {
	    		$('.bs-example-modal-sm').modal('hide');
	    		TracksFactory.delete(trackToRemove);
	    		$scope.tracks.splice($scope.tracks.indexOf(trackToRemove), 1);
	    	}
		}
		var controllers = angular.module('app.controllers', []);
		var services = angular.module('app.services', ['ngResource']);
		var app = angular.module('app', ['app.services','app.controllers']);
		
		
		app.controller('JsonObjectsController', ['$scope', '$http', '$filter', 'JsonObjectsFactory', JsonObjectsController]);
		
		
		services.factory('JsonObjectsFactory', function ($resource) {
		    return $resource('/admin/object/:uuid/:clientapp/:objecttype', {}, {
				query: { method: 'GET', isArray: true, params: {} },
				update: { method: 'POST' },
	 	        delete: { method: 'DELETE', params: {'uuid': '@uuid', 'clientapp': '@clientapp', 'objecttype': '@objecttype'} }
		    })
		});
		
		function JsonObject() {
			  this.uuid = null;
			  this.objecttype = null;
			  this.timeUUID = null;
			  this.timestamp = (new Date()).getTime();
			  this.data = null;
		}
	</script>	
	
    </body>
</html>
