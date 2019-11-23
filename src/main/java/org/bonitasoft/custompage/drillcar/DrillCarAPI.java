package org.bonitasoft.custompage.drillcar;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.beanutils.BeanPropertyValueChangeClosure;
import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.log.event.BEventFactory;
import org.bonitasoft.serverconfiguration.ComparaisonResult;
import org.bonitasoft.serverconfiguration.ComparaisonResult.LOGSTRATEGY;
import org.bonitasoft.serverconfiguration.ComparaisonResultDecoMap;
import org.bonitasoft.serverconfiguration.ConfigAPI;
import org.bonitasoft.serverconfiguration.ConfigAPI.ComparaisonParameter;
import org.bonitasoft.serverconfiguration.referentiel.BonitaConfigBundle;

public class DrillCarAPI {

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
            
            currentConfig = ConfigAPI.getInstance(fileBundle);
            listEvents.addAll( currentConfig.setupPull() );
        }
        else
            currentConfig = ConfigAPI.getInstance(comparaisonParameter.localFile);

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
