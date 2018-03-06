<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
	 <nav class="navbar navbar-default">
        <div class="container-fluid">
          <div class="navbar-header">
            <a class="navbar-brand">Admin console</a>
          </div>
          <div id="navbar" class="navbar-collapse collapse">
            <ul class="nav navbar-nav">
              <li class="${console_navbar_json_active }"><a href="/console">JSON Objects</a></li>
              <li class="${console_navbar_scripts_active }"><a href="/console/scripts">Scripts</a></li>
              <li class="${console_navbar_filterscripts_active}"><a href="/console/filterscripts">Filter Scripts</a></li>
              <li class="${console_navbar_users_active }"><a href="/console/users">Users</a></li>
              <li class="${console_navbar_logs_active }"><a href="/console/logs">Logs</a></li>
            </ul>
          </div><!--/.nav-collapse -->
        </div><!--/.container-fluid -->
      </nav>