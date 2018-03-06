<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<!doctype html>
<html class="no-js" lang="" ng-app="app">
    <head>
        <meta charset="utf-8">
        <meta http-equiv="x-ua-compatible" content="ie=edge">
        <title>Console : Convesion scripts</title>
		<jsp:include page="../inc/resources.jsp"></jsp:include>
    </head>
    <body>
	<div class="container theme-showcase" role="main">
	 <jsp:include page="../inc/navbar.jsp"></jsp:include>
		<div class="jumbotron">
			<div class="row" ng-controller="ScriptsController">
				<div class="btn-group" role="group" aria-label="Panel">
					<button ng-click="onAddClick()" type="button" class="btn btn-sm btn-default btn-success">
						<span class="glyphicon glyphicon-plus" aria-hidden="true"></span>
						Add
					</button>
				</div>
				

				<table class="table table-striped">
					<thead>
						<tr>
							<th class="col-md-1"></th>
							<th class="col-md-2">Client app</th>
							<th class="col-md-2">Direction</th>
							<th class="col-md-8">Script</th>
						</tr>
					</thead>
					<tbody>
						<tr ng-repeat="script in scripts">
							<td>
								<a href="#" ng-click="onEditClick($event, script)"><span class="glyphicon glyphicon-pencil" aria-hidden="true"></span></a>
							  	<a href="#" ng-click="onRemoveClick($event, script)"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span></a>
							</td>
							<td>{{script.client_app}}</td>
							<td>{{script.from_to}}</td>
							<td>{{script.script}}</td>
						</tr>
					</tbody>
				</table>
				<!-- Modal -->
				<div class="modal fade" id="myModal" role="dialog">
					<div class="modal-dialog">
						<!-- Modal content-->
						<div class="modal-content">
							<div class="modal-body">
								<div class="form-group">
    								<label for="exampleInputEmail1">Client app</label>
    								<input id="item-time" type="text" class="form-control" ng-model="currentScript.client_app" required="required">
  								</div>
  								<div class="form-group">
    								<label for="exampleInputEmail1">Direction</label>
    								<input type="text" class="form-control" ng-model="currentScript.from_to" required="required">
  								</div>
  								<div class="form-group">
    								<label for="exampleInputEmail1">Script</label>
    								<textarea class="form-control" rows="5" ng-model="currentScript.script" required="required"></textarea>
  								</div>
								
							</div>
							<div class="modal-footer">
								<button type="button" class="btn btn-default btn-success" ng-click="onSaveClick($event, currentScript)">Save</button>
								<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

	</div>
	<script type="text/javascript">
		function ScriptsController($scope, $http, $filter, ScriptsFactory) {
	
			function queryScripts() {
				$scope.scripts = ScriptsFactory.query(function(data) {
				});
			}
			
			queryScripts();
	    	
			$scope.onSaveClick = function($event, script) {
	    		$event.preventDefault();
				
	    		$scope.scripts[$scope.scripts.indexOf($scope.selectedScript)] = ScriptsFactory.update(script, function(response) {
	    			queryScripts();
			    	$('#myModal').modal('hide');	
				}, function(response) {
					console.log(response);
					alert('Ошибка сохранения данных на сервере!');
				}); 
						
			}
	    	
	    	$scope.onEditClick = function($event, script) {
	    		$event.preventDefault();
	    		$scope.selectedScript = script;
	    		$scope.currentScript = angular.copy($scope.selectedScript);
	    		$('#myModal').modal();
	    	}
	    	
	    	$scope.onAddClick = function() {
	    		$scope.currentScript = new Script();
	    		$('#myModal').modal();
	    	}
	    	
	    	
	    	$scope.onRemoveClick = function($event, script) {
	    		$event.preventDefault();
	    		if (confirm('Удалить данный объект?')) {
	    			ScriptsFactory.delete({'client_app': script.client_app, 'from_to': script.from_to}, function(response) {
	    				$scope.scripts.splice($scope.scripts.indexOf(script), 1);
					}, function(response) {
						console.log(response);
						alert('Ошибка сохранения данных на сервере!');
					}); 
	    		} else {
	    		}
	    	}

		}
		var controllers = angular.module('app.controllers', []);
		var services = angular.module('app.services', ['ngResource']);
		var app = angular.module('app', ['app.services','app.controllers']);
		
		
		app.controller('ScriptsController', ['$scope', '$http', '$filter', 'ScriptsFactory', ScriptsController]);
		
		
		services.factory('ScriptsFactory', function ($resource) {
		    return $resource('/admin/script/:client_app/:from_to', {}, {
		        query: { method: 'GET', isArray: true },
		        update: { method: 'POST' },
	 	        delete: { method: 'DELETE', params: {'client_app': '@client_app', 'from_to': '@from_to'}}
		    })
		});
		
		function Script() {
			  this.client_app = null;
			  this.from_to = null;
			  this.script = null;
		}
	</script>	
	
    </body>
</html>
