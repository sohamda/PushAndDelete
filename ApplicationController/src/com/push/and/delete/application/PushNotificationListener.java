package com.push.and.delete.application;


import java.sql.Connection;

import java.sql.ResultSet;
import java.sql.Statement;

import java.util.logging.Level;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.framework.event.Event;
import oracle.adfmf.framework.event.EventListener;
import oracle.adfmf.framework.exception.AdfException;
import oracle.adfmf.util.Utility;

public class PushNotificationListener implements EventListener {
    
    public final static String PUSH_MESSAGE             =   "#{applicationScope.push_message}";
    public final static String MCS_REGISTRATION_STRING  =   "#{applicationScope.mcsregistrationString}";
    public final static String PUSH_ERROR               =   "#{applicationScope.push_errorMessage}";
    public final static String PUSH_DeviceTOKEN         =   "#{applicationScope.deviceToken}";   
    
    public PushNotificationListener() {
        super();
    }

    @Override
    public void onOpen(String token) {
        
        // Clear error in app scope
        AdfmfJavaUtilities.setELValue(PUSH_ERROR, null);
        
        // Write the token into app scope        
        AdfmfJavaUtilities.setELValue(PUSH_DeviceTOKEN,token);
    }

    @Override
    public void onMessage(Event event) {
        
        deleteAllData();
        
        String msg = event.getPayload();
                
        //set push message to application scope memory attribute for 
        //display in the view. This shows the raw mwwssage format.
        AdfmfJavaUtilities.setELValue(PUSH_MESSAGE, msg);  
        
        /* *** NOTIFICATION HANDLING HINT ***
         * 
         * The message payload structure differs between Apple and Google notifications. If the message was received in 
         * an iOS device, you can extract the notification alert with the code shown below:
         * 
         * HashMap payload      = (HashMap) JSONBeanSerializationHelper.fromJSON(HashMap.class, msg);        
         * String alertMessage  = (String) payload.get("alert");  
         * ... parse alertMessage content. E.g. if alert is a JSON string, you can do
         * 
         * JSONObject alertJson = new JSONObject(alertMessage); 
         * String jsonProperty  = alertJson.get...("property_name");
         * 
         * The same on Google would look like
         * 
         * JSONObject jsonObject  = new JSONObject(msg);
         * JSONObject alertJson   = jsonObject.getJSONObject("alert"); 
         * String jsonProperty  = alertJson.get...("property_name");
         * 
         */
    }

    @Override
    public void onError(AdfException adfException) {
        
        Utility.ApplicationLogger.logp(Level.WARNING, this.getClass().getSimpleName(), "PushEventHandler::onError",
                                       "Message = " + adfException.getMessage() + "\nSeverity = " +
                                       adfException.getSeverity() + "\nType = " + adfException.getType());

        // Write the error into app scope
        // ValueExpression ve = AdfmfJavaUtilities.getValueExpression(PushConstants.PUSH_ERROR, String.class);
        // ve.setValue(AdfmfJavaUtilities.getAdfELContext(), adfException.toString());
        
        AdfmfJavaUtilities.setELValue(PUSH_ERROR, adfException.toString());
    }
    
    private void deleteAllData() {
        
        try {
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            stmt.executeQuery("DELETE FROM PUSH_AND_DEL");
            conn.commit();
        } catch (Exception e) {
            // TODO: Add catch code
            e.printStackTrace();
        }
    }
}
