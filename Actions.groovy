import org.bonitasoft.serverconfiguration.ComparaisonResult
import org.bonitasoft.serverconfiguration.ComparaisonResultDecoLog
import org.bonitasoft.serverconfiguration.ConfigAPI
import org.bonitasoft.serverconfiguration.ComparaisonResult.LOGSTRATEGY
import org.bonitasoft.serverconfiguration.ConfigAPI.ComparaisonParameter
import org.bonitasoft.serverconfiguration.referentiel.BonitaConfigBundle

import java.lang.management.RuntimeMXBean;
import java.lang.management.ManagementFactory;

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.logging.Logger;
import java.io.File
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.Runtime;

import org.json.simple.JSONObject;
import org.codehaus.groovy.tools.shell.CommandAlias;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;



import javax.naming.Context;
import javax.naming.InitialContext;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.Clob;
import java.util.Date;

import org.apache.commons.lang3.StringEscapeUtils


import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.service.TenantServiceSingleton

import org.bonitasoft.web.extension.page.PageContext;
import org.bonitasoft.web.extension.page.PageController;
import org.bonitasoft.web.extension.page.PageResourceProvider;

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;

import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstancesSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor;
import org.bonitasoft.engine.business.data.BusinessDataRepository
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.command.CommandDescriptor;
import org.bonitasoft.engine.command.CommandCriterion;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;

import org.bonitasoft.engine.identity.UserSearchDescriptor;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;



import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.log.event.BEvent.Level;
import org.bonitasoft.log.event.BEventFactory;

import org.bonitasoft.properties.BonitaProperties;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;


import org.bonitasoft.custompage.drillcar.DrillCarAPI;


public class Actions {

    private static Logger logger= Logger.getLogger("org.bonitasoft.custompage.longboard.groovy");
    
    
    private static EVENT_USERS_FOUND = new BEvent("org.bonitasoft.custompage.drillcar", 1, Level.INFO, "Number of users found in the system", "", "", "");
    private static EVENT_FAKE_ERROR  = new BEvent("com.bonitasoft.custompage.drillcar", 1, Level.APPLICATIONERROR, "Fake error", "This is not a real error", "No consequence", "don't call anybody");

    
      // 2018-03-08T00:19:15.04Z
    public final static SimpleDateFormat sdfJson = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    public final static SimpleDateFormat sdfHuman = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /* -------------------------------------------------------------------- */
    /*                                                                      */
    /* doAction */
    /*                                                                      */
    /* -------------------------------------------------------------------- */

    public static Index.ActionAnswer doAction(HttpServletRequest request, String paramJsonSt, HttpServletResponse response, PageResourceProvider pageResourceProvider, PageContext pageContext) {
                
        // logger.info("#### PingActions:Actions start");
        Index.ActionAnswer actionAnswer = new Index.ActionAnswer(); 
        List<BEvent> listEvents=new ArrayList<BEvent>();
        Object jsonParam = (paramJsonSt==null ? null : JSONValue.parse(paramJsonSt));
          
        try {
            String action=request.getParameter("action");
            logger.info("#### log:Actions  action is["+action+"] !");
            if (action==null || action.length()==0 )
            {
                actionAnswer.isManaged=false;
                logger.info("#### log:Actions END No Actions");
                return actionAnswer;
            }
            
            //Make sure no action is executed if the CSRF protection is active and the request header is invalid
            if (! TokenValidator.checkCSRFToken(request, response)) {
                actionAnswer.isResponseMap=false;
                return actionAnswer;
            }
            actionAnswer.isManaged=true;
            
            // Hello
            APISession apiSession = pageContext.getApiSession();
            HttpSession httpSession = request.getSession();            
            ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(apiSession);
            IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(apiSession);
			
            long tenantId = apiSession.getTenantId();          
            TenantServiceAccessor tenantServiceAccessor = TenantServiceSingleton.getInstance(tenantId);             

			if ("diffanalysis".equals(action))
			{
                File pageDirectory = pageResourceProvider.getPageDirectory();
                actionAnswer.responseMap.putAll( DrillCarAPI.diffAnalysis( jsonParam , pageDirectory ));
			}
            else if ("propscollect".equals(action))
            {
                File pageDirectory = pageResourceProvider.getPageDirectory();
                actionAnswer.responseMap.putAll( DrillCarAPI.propsCollects( jsonParam , pageDirectory, processAPI, identityAPI,tenantId ));
            }
			else if ("saveprops".equals(action))	{
				logger.info("Save properties paramJsonSt="+paramJsonSt);
				if (jsonParam!=null)
				{
					try
					{
						BonitaProperties bonitaProperties = new BonitaProperties( pageResourceProvider );

						listEvents.addAll( bonitaProperties.load() );
						bonitaProperties.setProperty( apiSession.getUserId()+"_firstname", jsonParam.get("firstname") );
						logger.info("Save properties -["+apiSession.getUserId()+"_firstname] <- ["+jsonParam.get("firstname") +"]");
			
						listEvents.addAll(  bonitaProperties.store());
					}
					catch( Exception e )
					{
						logger.severe("Exception "+e.toString());
						listEvents.add( new BEvent("com.bonitasoft.drillcar", 10, Level.APPLICATIONERROR, "Error using BonitaProperties", "Error :"+e.toString(), "Properties is not saved", "Check exception"));
					}
				}
				else
					listEvents.add( new BEvent("com.bonitasoft.drillcar", 11, Level.APPLICATIONERROR, "JsonHash can't be decode", "the parameters in Json can't be decode", "Properties is not saved", "Check page"));

			}
			else if ("loadprops".equals(action)) {
				try
				{
					logger.info("Load properties");

					BonitaProperties bonitaProperties = new BonitaProperties( pageResourceProvider );
					listEvents.addAll( bonitaProperties.load() );
					logger.info("Load done, events = "+listEvents.size() );

					String firstName = bonitaProperties.getProperty( apiSession.getUserId()+"_firstname" );
					logger.info("Load done, firstName["+firstName+"]" );
					actionAnswer.responseMap.put("firstname", (firstName==null ? "" : firstName) );
		
				}
				catch( Exception e )
				{
					logger.severe("Exception "+e.toString());
					listEvents.add( new BEvent("com.bonitasoft.drillcar", 10, Level.APPLICATIONERROR, "Error using BonitaProperties", "ErrorLoadprops :"+e.toString(), "Properties is not saved", "Check exception"));

				}

			}
             
               
            
            logger.info("#### log:Actions END responseMap ="+actionAnswer.responseMap.size());
            return actionAnswer;
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionDetails = sw.toString();
            logger.severe("#### log:Groovy Exception ["+e.toString()+"] at "+exceptionDetails);
            actionAnswer.isResponseMap=true;
            actionAnswer.responseMap.put("Error", "log:Groovy Exception ["+e.toString()+"] at "+exceptionDetails);
            

            
            return actionAnswer;
        }
    }

    /**
		to create a simple chart
		*/
		public static class ActivityTimeLine
		{
				public String activityName;
				public Date dateBegin;
				public Date dateEnd;
				
				public static ActivityTimeLine getActivityTimeLine(String activityName, int timeBegin, int timeEnd)
				{
					Calendar calBegin = Calendar.getInstance();
					calBegin.set(Calendar.HOUR_OF_DAY , timeBegin);
					Calendar calEnd = Calendar.getInstance();
					calEnd.set(Calendar.HOUR_OF_DAY , timeEnd);
					
						ActivityTimeLine oneSample = new ActivityTimeLine();
						oneSample.activityName = activityName;
						oneSample.dateBegin		= calBegin.getTime();
						oneSample.dateEnd 		= calEnd.getTime();
						
						return oneSample;
				}
				public long getDateLong()
				{ return dateBegin == null ? 0 : dateBegin.getTime(); }
		}
		
		
		/** create a simple chart 
		*/
		public static String getChartTimeLine(String title, List<ActivityTimeLine> listSamples){
				Logger logger = Logger.getLogger("org.bonitasoft");
				
				/** structure 
				 * "rows": [
           {
        		 c: [
        		      { "v": "January" },"
                  { "v": 19,"f": "42 items" },
                  { "v": 12,"f": "Ony 12 items" },
                ]
           },
           {
        		 c: [
        		      { "v": "January" },"
                  { "v": 19,"f": "42 items" },
                  { "v": 12,"f": "Ony 12 items" },
                ]
           },

				 */
				String resultValue="";
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss,SSS");
				
				for (int i=0;i<listSamples.size();i++)
				{
					logger.info("sample [i] : "+listSamples.get( i ).activityName+"] dateBegin["+simpleDateFormat.format( listSamples.get( i ).dateBegin)+"] dateEnd["+simpleDateFormat.format( listSamples.get( i ).dateEnd) +"]");
						if (listSamples.get( i ).dateBegin!=null &&  listSamples.get( i ).dateEnd != null)
								resultValue+= "{ \"c\": [ { \"v\": \""+listSamples.get( i ).activityName+"\" }," ;
								resultValue+= " { \"v\": \""+listSamples.get( i ).activityName +"\" }, " ;
								resultValue+= " { \"v\": \"Date("+ simpleDateFormat.format( listSamples.get( i ).dateBegin) +")\" }, " ;
								resultValue+= " { \"v\": \"Date("+ simpleDateFormat.format( listSamples.get( i ).dateEnd) +")\" } " ;
								resultValue+= "] },";
				}
				if (resultValue.length()>0)
						resultValue = resultValue.substring(0,resultValue.length()-1);
				
				String resultLabel = "{ \"type\": \"string\", \"id\": \"Role\" },{ \"type\": \"string\", \"id\": \"Name\"},{ \"type\": \"datetime\", \"id\": \"Start\"},{ \"type\": \"datetime\", \"id\": \"End\"}";
				
				String valueChart = "	{"
					   valueChart += "\"type\": \"Timeline\", ";
					  valueChart += "\"displayed\": true, ";
					  valueChart += "\"data\": {";
					  valueChart +=   "\"cols\": ["+resultLabel+"], ";
					  valueChart +=   "\"rows\": ["+resultValue+"] ";
					  /*
					  +   "\"options\": { "
					  +         "\"bars\": \"horizontal\","
					  +         "\"title\": \""+title+"\", \"fill\": 20, \"displayExactValues\": true,"
					  +         "\"vAxis\": { \"title\": \"ms\", \"gridlines\": { \"count\": 100 } }"
					  */
					  valueChart +=  "}";
					  valueChart +="}";
// 				+"\"isStacked\": \"true\","
 	          
//		    +"\"displayExactValues\": true,"
//		    
//		    +"\"hAxis\": { \"title\": \"Date\" }"
//		    +"},"
				logger.info("Value1 >"+valueChart+"<");

				
				return valueChart;		
		}
    
		/**
		 public static void testProcessStartedUserName( APISession apiSession ) {
		     SearchRelationAPI searchRelation = new SearchRelationAPI( apiSession );
                
		     -- SearchOptionsBuilderRelation sob = new SearchOptionsBuilderRelation( 0,10);
		        sob.filter(org.bonitasoft.search.UserSearchDescriptorRelation.USER_NAME, "Walter.Bates");
		        sob.relation(org.bonitasoft.search.ProcessInstanceSearchDescriptorRelation.STARTED_BY);
		       --
		        SearchOptionsBuilderRelation sob = new SearchOptionsBuilderRelation(0,10);
		        sob.filter(org.bonitasoft.search.ContactDataSearchDescriptorRelation.EMAIL, "Walter.Bates@bonitasoft.com");
		        sob.filter(org.bonitasoft.search.ContactDataSearchDescriptorRelation.PERSONAL, Boolean.FALSE);
		        
		        // we don't have the filterRelation. So, here we give this kind of relation, saying "Please link the second table by the STARTEDBY
		        sob.relation(org.bonitasoft.search.ProcessInstanceSearchDescriptorRelation.STARTED_BY);
                sob.relation(org.bonitasoft.search.ProcessInstanceSearchDescriptorRelation.PROCESS_DEFINITION_ID);
                sob.relation(org.bonitasoft.search.ContactDataSearchDescriptorRelation.USERID);
		        sob.filter(org.bonitasoft.search.ProcessDeploymentInfoSearchDescriptorRelation.NAME, "TestProcess");
		        try {
		            searchRelation.search(ProcessInstance.class, sob.done());
		        } catch (Exception e) {
		            logger.severe("testProcessStartedUserName >"+e.getMessage()+"<");

		        }
		        
		    }
		    */
}
