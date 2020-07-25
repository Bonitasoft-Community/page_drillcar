package org.bonitasoft.custompage.drillcar;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.log.event.BEventFactory;
import org.bonitasoft.log.event.BEvent.Level;
import org.bonitasoft.serverconfiguration.CollectOperation.TYPECOLLECT;
import org.bonitasoft.serverconfiguration.CollectResult;
import org.bonitasoft.serverconfiguration.CollectResult.COLLECTLOGSTRATEGY;
import org.bonitasoft.serverconfiguration.CollectResultDecoMap;
import org.bonitasoft.serverconfiguration.ComparaisonResult;
import org.bonitasoft.serverconfiguration.ComparaisonResult.LOGSTRATEGY;
import org.bonitasoft.serverconfiguration.ComparaisonResultDecoMap;
import org.bonitasoft.serverconfiguration.ConfigAPI;
import org.bonitasoft.serverconfiguration.ConfigAPI.CollectParameter;
import org.bonitasoft.serverconfiguration.ConfigAPI.ComparaisonParameter;
import org.bonitasoft.serverconfiguration.referentiel.BonitaConfigBundle;
import org.bonitasoft.serverconfiguration.referentiel.BonitaConfigPath;

public class DrillCarAPI {


    public static BEvent eventParameterError = new BEvent(DrillCarAPI.class.getName(), 1, Level.APPLICATIONERROR, "Parameter error", "Something is wrong with parameters", "Analysis can't be done", "Check error");
/**
     * collect all properties information
     * @param parameters
     * @param pageDirectory
     * @return
     */
    public static Map<String, Object> propsCollects(Map<String, Object> parameters, File pageDirectory) {
        
        File fileBundle = null;
        fileBundle = new File(pageDirectory.getAbsoluteFile() + "/../../../../../../../");
        ArrayList<BEvent> listEvents = new ArrayList<BEvent>();
        
        try {
            fileBundle = new File(fileBundle.getCanonicalPath());
        } catch (Exception e) {
        }
        
        CollectParameter collectParameter = CollectParameter.getInstanceFromMap(parameters);
        BonitaConfigPath localBonitaConfig;
        ConfigAPI currentConfig;
        if (collectParameter.useLocalFile) {
            localBonitaConfig = BonitaConfigPath.getInstance(fileBundle);
            currentConfig = ConfigAPI.getInstance( localBonitaConfig );
            collectParameter.localFile = fileBundle;
            if (collectParameter.doSetupPull)
                listEvents.addAll( currentConfig.setupPull() );
        }
        else {
            localBonitaConfig = BonitaConfigPath.getInstance(collectParameter.localFile);
            currentConfig = ConfigAPI.getInstance( localBonitaConfig );
        }

        // now, collect result
        CollectResult collectResult = currentConfig.collectParameters( collectParameter, COLLECTLOGSTRATEGY.LOGALL);
        
        // I want the result in JSON, so use a ResultDecoMap
        CollectResultDecoMap decoMap = new CollectResultDecoMap(collectResult, "", localBonitaConfig.getRootPath() );
        decoMap.setLineFeedToHtml( true );
        // ok, get the value of decoration
        
        Map<String,Object> result=new HashMap<String,Object>();
        if (collectParameter.listTypeCollect.contains(TYPECOLLECT.SETUP))
            result.put("setup", decoMap.getMap( TYPECOLLECT.SETUP));
        if (collectParameter.listTypeCollect.contains(TYPECOLLECT.SERVER))
            result.put("tomcat", decoMap.getMap( TYPECOLLECT.SERVER));
        
        if (collectParameter.listTypeCollect.contains(TYPECOLLECT.ANALYSIS))
            result.put("analysis", decoMap.getMap( TYPECOLLECT.ANALYSIS));
        // collect errors
        listEvents.addAll( collectResult.getErrors());
        result.put("listevents", BEventFactory.getSyntheticHtml(listEvents));
        
        return result;

    }
    
    /**
     * @param parameters
     * @param pageDirectory
     * @return
     */
    public static Map<String, Object> diffAnalysis(Map<String, Object> parameters, File pageDirectory) {
        
        ArrayList<BEvent> listEvents = new ArrayList<BEvent>();
        ComparaisonParameter comparaisonParameter;
        try {
            comparaisonParameter = ComparaisonParameter.getInstanceFromMap(parameters);
        } catch(Exception e) 
        {
            listEvents.add( new BEvent(eventParameterError, e, ""));
            Map<String,Object> result = new HashMap<>();
            result.put("listevents", BEventFactory.getSyntheticHtml(listEvents));
            return result;
        }

        ConfigAPI currentConfig;
        // value is D:\bonita\BPM-SP-7.9.0\workspace\tomcat\server\temp\bonita_portal_10028@Dragon-Pierre-Yves\tenants\1\pages\custompage_drillcar
        File localServerFile = null;
        localServerFile = new File(pageDirectory.getAbsoluteFile() + "/../../../../../../../");
        
        try {
            localServerFile = new File(localServerFile.getCanonicalPath());
        } catch (Exception e) {
        }

        File applicationFile=null;
        if (comparaisonParameter.useLocalServer) {
            
            currentConfig = ConfigAPI.getInstance( BonitaConfigPath.getInstance( localServerFile)) ;
            applicationFile = localServerFile;
            if (comparaisonParameter.doSetupPull)
                listEvents.addAll( currentConfig.setupPull() );
        }
        else {
            currentConfig = ConfigAPI.getInstance( BonitaConfigPath.getInstance( comparaisonParameter.applicationFile));
            applicationFile = comparaisonParameter.applicationFile;
        }
        BonitaConfigBundle bonitaReferentiel = BonitaConfigBundle.getInstance(comparaisonParameter.referenceFile);

        ComparaisonResult comparaison = currentConfig.compareWithReferentiel(bonitaReferentiel, comparaisonParameter, LOGSTRATEGY.NOLOG);

        ComparaisonResultDecoMap decoMap = new ComparaisonResultDecoMap(comparaison, "", applicationFile, comparaisonParameter.referenceFile);
        listEvents.addAll( comparaison.getErrors());
        Map<String,Object> result = decoMap.getMap();
        result.put("listevents", BEventFactory.getSyntheticHtml(listEvents));
        if (comparaison.getListComparaisonsItems().size() == 0)
            result.put("finalstatus", "IDENTICAL");
        else
            result.put("finalstatus", comparaison.getListComparaisonsItems()+" differences");
        return result;

    }
}
