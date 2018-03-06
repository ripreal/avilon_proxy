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
			<div class="row" ng-controller="UsersController">
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
							<th class="col-md-2">Name</th>
							<th class="col-md-4">Roles</th>
							<th class="col-md-4">JSON Object UUID</th>
						</tr>
					</thead>
					<tbody>
						<tr ng-repeat="user in users">
							<td>
								<a href="#" ng-click="onEditClick($event, user)"><span class="glyphicon glyphicon-pencil" aria-hidden="true"></span></a>
							  	<a href="#" ng-click="onRemoveClick($event, user)"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span></a>
							</td>
							<td>{{user.name}}</td>
							<td>{{user.roles}}</td>
							<td>{{user.json_object_uuid}}</td>
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
    								<label>User name</label>
    								<input id="user-name" type="text" class="form-control" ng-model="currentUser.name" required="required">
  								</div>
  								<div class="form-group">
    								<label>Password</label>
    								<input type="password" class="form-control" ng-model="currentUser.password" required="required">
  								</div>
  								<div class="form-group">
    								<label>JSON Object UUID: {{currentUser.json_object_uuid}}</label>
  								</div>
  								<div class="form-group">
    								<label>Roles</label>
    								<br>
    								<button ng-click="currentUser.roles.push('')" type="button" class="btn btn-sm btn-default btn-success">
										<span class="glyphicon glyphicon-plus" aria-hidden="true"></span>
										Add
									</button>
									<br>
									<div ng-repeat="role in currentUser.roles track by $index">
										<div class="input-group">
										<input type="text" class="form-control" ng-model="currentUser.roles[$index]" required="required">
										<span class="input-group-btn">
										<button ng-click="currentUser.roles.pop(role)" type="button" class="btn btn-default">
											<span class="glyphicon glyphicon-remove" aria-hidden="true"></span>
											Remove
										</button>
										</span>
										</div>
									</div>
  								</div>
							</div>
							<div class="modal-footer">
								<button type="button" class="btn btn-default btn-success" ng-click="onSaveClick($event, currentUser)">Save</button>
								<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

	</div>
	<script type="text/javascript">
		function UsersController($scope, $http, $filter, UsersFactory) {
	
			function queryUsers() {
				$scope.users = UsersFactory.query(function(data) {
				});
			}
			
			queryUsers();
	    	
			$scope.onSaveClick = function($event, user) {
	    		$event.preventDefault();
	    		var allowedRoles = [];
	    		angular.forEach(user.roles, function(role) {
	    			if(!!role) 
	    				allowedRoles.push(role);
	    		});
	    		user.roles = allowedRoles;
	    		$scope.users[$scope.users.indexOf($scope.selectedUser)] = UsersFactory.update(user, function(response) {
	    			queryUsers();
			    	$('#myModal').modal('hide');	
				}, function(response) {
					console.log(response);
					alert('Ошибка сохранения данных на сервере!');
				}); 
						
			}
	    	
	    	$scope.onEditClick = function($event, user) {
	    		$event.preventDefault();
	    		$scope.selectedUser = user;
	    		$scope.currentUser = angular.copy($scope.selectedUser);
	    		$('#myModal').modal();
	    	}
	    	
	    	$scope.onAddClick = function() {
	    		$scope.currentUser = new User();
	    		$('#myModal').modal();
	    	}
	    	
	    	
	    	$scope.onRemoveClick = function($event, user) {
	    		$event.preventDefault();
	    		if (confirm('Удалить данный объект?')) {
	    			UsersFactory.delete({'name': user.name}, function(response) {
	    				$scope.users.splice($scope.users.indexOf(user), 1);
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
		
		
		app.controller('UsersController', ['$scope', '$http', '$filter', 'UsersFactory', UsersController]);
		
		
		services.factory('UsersFactory', function ($resource) {
		    return $resource('/admin/user/:name', {}, {
		        query: { method: 'GET', isArray: true },
		        update: { method: 'POST' },
	 	        delete: { method: 'DELETE', params: {'name': '@name'}}
		    })
		});
		
		function User() {
			  this.name = null;
			  this.password = null;
			  this.roles = [];
		}
	</script>	
	
    </body>
</html>
