package com.push.and.delete.mobile;

import com.oracle.maf.sample.mcs.apis.notifications.Notifications;
import com.oracle.maf.sample.mcs.shared.authorization.auth.Authorization;
import com.oracle.maf.sample.mcs.shared.exceptions.ServiceProxyException;
import com.oracle.maf.sample.mcs.shared.mbe.MBE;
import com.oracle.maf.sample.mcs.shared.mbe.MBEConfiguration;
import com.oracle.maf.sample.mcs.shared.mbe.MBEManager;
import com.oracle.maf.sample.mcs.shared.mbe.error.OracleMobileError;
import com.oracle.maf.sample.mcs.shared.mbe.error.OracleMobileErrorHelper;

import com.push.and.delete.application.DBConnectionFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import oracle.adf.model.datacontrols.device.DeviceManagerFactory;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.java.beans.ProviderChangeListener;
import oracle.adfmf.java.beans.ProviderChangeSupport;
import oracle.adfmf.json.JSONException;
import oracle.adfmf.util.Utility;

public class ServiceDC {
    
    List<PushAndDelete> localData = new ArrayList<>();
    protected transient ProviderChangeSupport providerChangeSupport = new ProviderChangeSupport(this);
    
    public ServiceDC() {
        super();
    }

    public List<PushAndDelete> getLocalData() {
        if(localData.isEmpty()) {
            populateLocalData();
            registerWithMCS();
        }
        return localData;
    }
    
    private void populateLocalData() {
        
        try {
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM PUSH_AND_DEL");

            rs.beforeFirst();
            while (rs.next()) {
                int id = rs.getInt("PAD_ID");
                String first = rs.getString("FIRST_NAME");
                if (first.equals("null")) {
                    first = "";
                }
                String last = rs.getString("LAST_NAME");
                if (last.equals("null")) {
                    last = "";
                }

                PushAndDelete p = new PushAndDelete(id, first, last);
                localData.add(p);
            }
            providerChangeSupport.fireProviderRefresh("localData");
        } catch (SQLException sqle) {
            // TODO: Add catch code
            sqle.printStackTrace();
        } catch (Exception e) {
            // TODO: Add catch code
            e.printStackTrace();
        }
        
    }
    
    private void registerWithMCS() {
        String mobileBackendUrl = "https://mobileportalsetrial0004dev-mcsdem0004.mobileenv.us2.oraclecloud.com:443";
        //clear
        AdfmfJavaUtilities.clearSecurityConfigOverrides("PUSHREG");
        //override
        AdfmfJavaUtilities.overrideConnectionProperty("PUSHREG", "restconnection", "url", mobileBackendUrl);


        String mobileBackendId =  "95b977ca-caf7-47a4-8b0c-deb61719c8ab";
        String mbeAnonymousKey =  "TUNTREVNMDAwNF9NT0JJTEVQT1JUQUxTRVRSSUFMMDAwNERFVl9NT0JJTEVfQU5PTllNT1VTX0FQUElEOlJvNDBieXVfaG53dHZw";
        String appKey =    "c04cc028-f40d-4360-a0a5-41252d57fc99";

        /*
        *  ACCESS TO MAF MCS UTILITY TO CREATE BACKEND INSTANCE REGISTER MBE UNDER THE NAME OF THE PROVIDED MCS MBE ID
        */

        //MBE instance need to be configured to support a specific authentication mechanism. At the moment only Basic Authentication is supported. This however will change
        //soon when OAuth becomes available as a second choice
        MBEConfiguration mbeConfiguration = new MBEConfiguration("PUSHREG", mobileBackendId, mbeAnonymousKey, appKey,
                                                                 MBEConfiguration.AuthenticationType.BASIC_AUTH);

        //enable analytics for MBE
        mbeConfiguration.setEnableAnalytics(true);

        //logging can be enabled / disabled at runtime for a MBE instance. Note that logging for an MBE requires
        //a. logging to be enabled on the MVE configfuration (if set to false, no log messages are attempted to be written)
        //b. logging to be enabled for the MAF application (if enabled log level doesn't match level of log message then no message is written)
        mbeConfiguration.setLoggingEnabled(true);

        //try to identify the device so that analytics can distinguish between the devices owned by a person
        mbeConfiguration.setMobileDeviceId(DeviceManagerFactory.getDeviceManager().getName());
        MBE mobileBackend = MBEManager.getManager().createOrRenewMobileBackend(mobileBackendId, mbeConfiguration);
        
        
        mobileBackend.getMbeConfiguration().getLogger().logFine("Anonymous authentication invoked from DC",
                                                                this.getClass().getSimpleName(), "anonymousLogin");
        Authorization authorization = mobileBackend.getAuthorizationProvider();
        try {
            authorization.authenticateAsAnonymous();
        } catch (ServiceProxyException e) {
            //is exception dur to an MCS response error ?
            if (e.isApplicationError()) {
                //if this is a well formatted Oracle Mobile error, we can display a user friendly error message
                try {
                    OracleMobileError mobileError = OracleMobileErrorHelper.getMobileErrorObject(e.getMessage());
                    //print short description of error
                    Utility.ApplicationLogger.log(Level.SEVERE, mobileError.getTitle());
                } catch (JSONException f) {
                    Utility.ApplicationLogger.log(Level.SEVERE, e.getMessage());
                }
            } else {
                Utility.ApplicationLogger.log(Level.SEVERE, e.getMessage());
            }
        }
        
        //get the _token retrieved from Google or Apple
        String _token = (String) AdfmfJavaUtilities.getELValue("#{applicationScope.deviceToken}");
        
        //If we have a _token, set it to the MBE configuration for the Notifications service proxy to 
        //access when registering and de-registering the application to MCS
        mobileBackend.getMbeConfiguration().setDeviceToken(_token);

        String packageName = "com.company.PushAndDelete";
        mobileBackend.getMbeConfiguration().setGooglePackageName(packageName);

        //Showcase asynchronous invocation of MAF MCS Utility API calls. Note that the same construct used here can be used with 
        //any API exposed on the MBE instances            
        Runnable mcsJob = new Runnable(){

          public void run(){                                               
             try {
                 Notifications notification = mobileBackend.getServiceProxyNotifications();

                 String registrationInfo = notification.registerDeviceToMCS();
                   AdfmfJavaUtilities.setELValue("#{applicationScope.mcsregistrationString}", registrationInfo);
               } catch (ServiceProxyException e) {
                 if (e.isApplicationError()) {
                     //if this is a well formatted Oracle Mobile error, we can display a user friendly error message
                     try {
                         OracleMobileError mobileError = OracleMobileErrorHelper.getMobileErrorObject(e.getMessage());
                         //print short description of error
                         Utility.ApplicationLogger.log(Level.SEVERE, mobileError.getDetail());
                     } catch (JSONException f) {
                         Utility.ApplicationLogger.log(Level.SEVERE, e.getMessage());
                     }
                 } else {
                     Utility.ApplicationLogger.log(Level.SEVERE, e.getMessage());
                 }
               }
             
              //ensure main thread is synchronized with result
              AdfmfJavaUtilities.flushDataChangeEvent();
          }
        };
        
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.execute(mcsJob);
        executor.shutdown();
    }
    
    public void addProviderChangeListener(ProviderChangeListener l) {
        providerChangeSupport.addProviderChangeListener(l);
    }

    public void removeProviderChangeListener(ProviderChangeListener l) {
        providerChangeSupport.removeProviderChangeListener(l);
    }
}
