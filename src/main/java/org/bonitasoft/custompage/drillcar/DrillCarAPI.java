package org.bonitasoft.custompage.drillcar;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.log.event.BEventFactory;
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
        if (collectParameter.collectSetup)
            result.put("setup", decoMap.getMap( TYPECOLLECT.SETUP));
        if (collectParameter.collectServer)
            result.put("tomcat", decoMap.getMap( TYPECOLLECT.TOMCAT));
        
        if (collectParameter.collectAnalysis)
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
        ComparaisonParameter comparaisonParameter = ComparaisonParameter.getInstanceFromMap(parameters);

        ConfigAPI currentConfig;
        // value is D:\bonita\BPM-SP-7.9.0\workspace\tomcat\server\temp\bonita_portal_10028@Dragon-Pierre-Yves\tenants\1\pages\custompage_drillcar
        File fileBundle = null;
        fileBundle = new File(pageDirectory.getAbsoluteFile() + "/../../../../../../../");
        ArrayList<BEvent> listEvents = new ArrayList<BEvent>();
        
        try {
            fileBundle = new File(fileBundle.getCanonicalPath());
        } catch (Exception e) {
        }

        if (comparaisonParameter.useLocalFile) {
            
            currentConfig = ConfigAPI.getInstance( BonitaConfigPath.getInstance( fileBundle)) ;
            listEvents.addAll( currentConfig.setupPull() );
        }
        else
            currentConfig = ConfigAPI.getInstance( BonitaConfigPath.getInstance( comparaisonParameter.localFile));

        BonitaConfigBundle bonitaReferentiel = BonitaConfigBundle.getInstance(comparaisonParameter.referenceFile);

        ComparaisonResult comparaison = currentConfig.compareWithReferentiel(bonitaReferentiel, comparaisonParameter, LOGSTRATEGY.NOLOG);

        ComparaisonResultDecoMap decoMap = new ComparaisonResultDecoMap(comparaison, "", comparaisonParameter.localFile, comparaisonParameter.referenceFile);
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
