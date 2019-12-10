'use strict';
/**
 *
 */

(function() {


var appCommand = angular.module('drillapp', ['googlechart', 'ui.bootstrap','ngSanitize', 'ngModal', 'ngMaterial']);


/* Material : for the autocomplete
 * need
  <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular.min.js"></script>
  <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular-animate.min.js"></script>
  <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular-aria.min.js"></script>
  <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular-messages.min.js"></script>

  <!-- Angular Material Library -->
  <script src="https://ajax.googleapis.com/ajax/libs/angular_material/1.1.0/angular-material.min.js">
 */



// --------------------------------------------------------------------------
//
// Controler Ping
//
// --------------------------------------------------------------------------

// Ping the server
appCommand.controller('DrillControler',
	function ( $http, $scope,$sce,$filter ) {

	this.showhistory = function(show) {
		this.isshowhistory = show;
	}
	this.navbaractiv='properties';
	
	this.getNavClass = function( tabtodisplay )
	{
		if (this.navbaractiv === tabtodisplay)
			return 'ng-isolate-scope active';
		return 'ng-isolate-scope';
	}

	this.getNavStyle = function( tabtodisplay )
	{
		if (this.navbaractiv === tabtodisplay)
			return 'border: 1px solid #c2c2c2;border-bottom-color: transparent;';
		return '';
	}
	
	this.inprogress=false;

	// -----------------------------------------------------------------------------------------
	//  										Properties
	// -----------------------------------------------------------------------------------------
	this.props = {	'param' : { 'collectSetup':true, 'collectServer':true, 'collectAnalysis':true, 'useLocalFile':true, 'displaylevel': 'all'}, 
					'platform': [], 
					'tenants': { 
						'list':[], 
						'setup': [] }
				 };
	
	this.propsCollect = function( collectSetup, collectServer, collectAnalysis) {	
		var self=this;
		self.inprogress=true;

		self.props.param.collectSetup = collectSetup;
		self.props.param.collectServer = collectServer;
		self.props.param.collectAnalysis = collectAnalysis;
		
		
		self.props.platform = [];
		self.props.tenants = {};
		// 7.6 : the server force a cache on all URL, so to bypass the cache, then create a different URL 
		var d = new Date();
		
		
		var json = encodeURI( angular.toJson( self.props.param, false));
		
		$http.get( '?page=custompage_drill&action=propscollect&paramjson='+json+'&t='+d.getTime() )
				.success( function ( jsonResult, statusHttp, headers, config ) {
					
					// connection is lost ?
					if (statusHttp==401 || typeof jsonResult === 'string') {
						console.log("Redirected to the login page !");
						window.location.reload();
					}
					console.log("propscollect",jsonResult);
					self.props.listevents 			= jsonResult.listevents;
					self.props.setup 				= jsonResult.setup;
					self.props.tomcat 				= jsonResult.tomcat;
					self.props.analysis 			= jsonResult.analysis;
					
					self.inprogress=false;

					var listTenants =self.propsGetTenants();
					
					if (listTenants.length==1) {
						self.props.currenttenantid = listTenants[ 0 ];
					}
				})
				.error( function() {
					self.inprogress=false;
				});
			
	
	};
	
	this.propsShow = function( keypropertie)
	{
		if (keypropertie.isEnable)
			return true;
		return this.props.param.displaylevel === 'all';
	}
	this.propsStyle = function( keypropertie)
	{
		var style="border-top: 2px solid;padding: 1px 10px"
		if (keypropertie.isEnable)
			style += "font-style: italic;";
		return style;
	}
	this.propsGetTenants = function() {
		var listTenants = [];
		for (var i in this.props.tenants)
			listTenants.push( i );
		return listTenants;
	}
	
	// Expand or close all properties
	this.propsExpand = function( expand) {
		
		for (var classproperties in this.props.properties) {
			// console.log("classproperties on expand["+classproperties+"]");
			// console.log("listClassJson ="+angular.toJson( this.props.properties[ classproperties ]))
			for (var propertieindex in this.props.properties[ classproperties ]) {
				var propertiefile = this.props.properties[ classproperties ][ propertieindex ];
				// console.log("propertues file="+propertiefile);
				propertiefile.show = expand;
			}
		}
		
		// Tenant
		console.log("tenants now:");
		console.log("alltenants ="+angular.toJson( this.props.tenants ));
		for (var tenantid in this.props.tenants) {
			
			// console.log("listTenant ="+angular.toJson( this.props.tenants[ tenantid ]))
			for (var propertieindex in this.props.tenants[ tenantid ]) {
				var propertiefile = this.props.tenants[ tenantid ][ propertieindex ];
				
				propertiefile.show = expand;
			}
		}
	}
	
	this.indicatorCssClass = function (indicator ) {
		if (indicator.level ===  'DANGER')
			return "panel panel-danger";
		if (indicator.level ===  'WARNING')
			return "panel panel-warning";
		if (indicator.level === 'INFO')
			return "panel panel-info";
		if (indicator.level === 'SUCCESS')
			return "panel panel-success";
		return "panel panel-info";
	}
	// -----------------------------------------------------------------------------------------
	//  										Differentiel
	// -----------------------------------------------------------------------------------------

	this.diff = {"displaylevel": 100,
				"param": 
					{"useLocalFile": true, 
					"referentielIsABundle": true,
					"comparaisonFile": "", 
					"referenceFile":'',
					'compareContextXml': false,
					'ignoreImage': true,
	            	'ignoreBonitaTranslationFile': true,
	            	'ignoreTemp': true,
	            	'ignoreLicence':true,
	            	'ignoreSetup':false,
	            	'ignoreDeferedJs': true
					}
				};
	
	// D:/pym/Google Drive/consulting/20190429 Verizon Migration/analysis/7.8.4/BonitaSubscription-7.8.4_SIT_110119-2
	this.diffAnalysis = function()	{
		
		var self=this;
		self.inprogress=true;
		self.diff.result = [];
		// 7.6 : the server force a cache on all URL, so to bypass the cache, then create a different URL
		var d = new Date();

		var json = encodeURI( angular.toJson( self.diff.param, false));
		
		$http.get( '?page=custompage_drill&action=diffanalysis&paramjson='+json+'&t='+d.getTime() )
				.success( function ( jsonResult, statusHttp, headers, config ) {
					
					// connection is lost ?
					if (statusHttp==401 || typeof jsonResult === 'string') {
						console.log("Redirected to the login page !");
						window.location.reload();
					}
					console.log("history",jsonResult);
					self.diff.result 		= jsonResult;
					self.inprogress=false;


				})
				.error( function() {
					self.inprogress=false;
				});
	}

	this.diffDisplay = function( item ) {
		// console.log("Compare ItemLevel="+item.levelnum+" with "+this.diff.displaylevel+" : <= ? "+(item.levelnum <= this.diff.displaylevel));
		if (item.levelnum <= this.diff.displaylevel)
			return true;
		return false;
	}

	// -----------------------------------------------------------------------------------------
	//  										Autocomplete
	// -----------------------------------------------------------------------------------------
	this.autocomplete={};

	this.queryUser = function(searchText) {
		var self=this;
		console.log("QueryUser HTTP CALL["+searchText+"]");

		self.autocomplete.inprogress=true;
		self.autocomplete.search = searchText;
		self.inprogress=true;

		var paramJson={ 'userfilter' :  self.autocomplete.search};

		var json = encodeURI( angular.toJson( paramJson, false));
		// 7.6 : the server force a cache on all URL, so to bypass the cache, then create a different URL
		var d = new Date();

		return $http.get( '?page=custompage_drillcar&action=queryusers&paramjson='+json+'&t='+d.getTime() )
		.then( function ( jsonResult, statusHttp, headers, config ) {
			console.log("QueryUser HTTP SUCCESS.1 - result= "+angular.toJson(jsonResult, false));
				self.autocomplete.inprogress=false;
			 	self.autocomplete.listUsers =  jsonResult.data.listUsers;
				console.log("QueryUser HTTP SUCCESS length="+self.autocomplete.listUsers.length);
				self.inprogress=false;

				return self.autocomplete.listUsers;
				},  function ( jsonResult ) {
				console.log("QueryUser HTTP THEN");
		});

	  };

	// -----------------------------------------------------------------------------------------
	//  										Excel
	// -----------------------------------------------------------------------------------------

	this.exportData = function ( nameData )
	{
		//Start*To Export SearchTable data in excel
	// create XLS template with your field.
        var trackingJson = [];
    	var mystyle = {};
    	if (nameData=== "comparison") {
    		var trackingJson = this.diff.result

        	var mystyle = {
    				headers:true,
    				columns: [
    					{ columnid: 'name', title: 'Name'},
    					{ columnid: 'version', title: 'Version'},
    					{ columnid: 'state', title: 'State'},
    					{ columnid: 'deployeddate', title: 'Deployed date'},
    					],
    		};
    	}
    	
    	
        //get current system date.
        var date = new Date();
        $scope.CurrentDateTime = $filter('date')(new Date().getTime(), 'MM/dd/yyyy HH:mm:ss');
        //Create XLS format using alasql.js file.
        alasql('SELECT * INTO XLS("Process_' + $scope.CurrentDateTime + '.xls",?) FROM ?', [mystyle, trackingJson]);
    };


	// -----------------------------------------------------------------------------------------
	//  										Properties
	// -----------------------------------------------------------------------------------------
	this.propsFirstName='';
	this.saveProps = function() {
		var self=this;
		self.inprogress=true;

		var paramJson={ 'firstname': this.propsFirstName };
		var json = encodeURI( angular.toJson( paramJson, false));

		// 7.6 : the server force a cache on all URL, so to bypass the cache, then create a different URL
		var d = new Date();

		$http.get( '?page=custompage_drillcar&action=saveprops&paramjson='+json +'&t='+d.getTime())
				.success( function ( jsonResult, statusHttp, headers, config ) {
						console.log("history",jsonResult);
						self.listevents		= jsonResult.listevents;
						self.inprogress=false;

						alert('Properties saved');
				})
				.error( function() {
					alert('an error occure');
					});
	}

	this.loadProps =function() {
		var self=this;
		self.inprogress=true;

		// 7.6 : the server force a cache on all URL, so to bypass the cache, then create a different URL
		var d = new Date();

		$http.get( '?page=custompage_drillcar&action=loadprops&t='+d.getTime() )
				.success( function ( jsonResult, statusHttp, headers, config ) {
					
						console.log("history",jsonResult);
						self.propsFirstName = jsonResult.firstname;
						self.listevents		= jsonResult.listevents;
						self.inprogress		= false;

				})
				.error( function() {
					alert('an error occure');
					});
	}



	<!-- Manage the event -->
	this.getListEvents = function ( listevents ) {
		return $sce.trustAsHtml(  listevents );
	}
	<!-- Manage the Modal -->
	this.isshowDialog=false;
	this.openDialog = function()
	{
		this.isshowDialog=true;
	};
	this.closeDialog = function()
	{
		this.isshowDialog=false;
	}

	// -----------------------------------------------------------------------------------------
	// tool
	// -----------------------------------------------------------------------------------------

	this.getHtml = function( listevents ) {
		// console.log("getHtml:Start (r/o) source="+sourceContext);
		return $sce.trustAsHtml(listevents);
	}


});



})();