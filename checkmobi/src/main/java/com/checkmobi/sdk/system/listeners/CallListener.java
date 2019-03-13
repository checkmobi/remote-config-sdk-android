package com.checkmobi.sdk.system.listeners;

import com.checkmobi.sdk.model.LastValidation;
import com.checkmobi.sdk.network.response.CheckNumberResponse;
import com.checkmobi.sdk.storage.StorageController;
import com.checkmobi.sdk.validation.ValidationController;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

public class CallListener extends BroadcastReceiver {
    
    public static final String CALL_TO_VERIFY = "CallListener.CALL_TO_VERIFY";
    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static boolean isIncoming;
    private static String dialedNumber;
    
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            dialedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
        } else {
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            
            int state = 0;
            
            if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE))
                state = TelephonyManager.CALL_STATE_IDLE;
            else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))
                state = TelephonyManager.CALL_STATE_OFFHOOK;
            else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING))
                state = TelephonyManager.CALL_STATE_RINGING;
            
            onCallStateChanged(context, state, number);
        }
    }
    
    private void onCallStateChanged(final Context context, int state, String number) {
        if (number == null || lastState == state)
            return;
        
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                dialedNumber = number;
                System.out.println("ringing: " + dialedNumber);
                verifyIncomingCall(context);
                break;
            
            case TelephonyManager.CALL_STATE_OFFHOOK:
                
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false;
                    System.out.println("offhook: " + dialedNumber);
                }
                break;
            
            case TelephonyManager.CALL_STATE_IDLE:
                System.out.println("idle: " + dialedNumber);
                break;
        }
        
        lastState = state;
    }
    
    private void verifyIncomingCall(final Context context) {
        LastValidation lastValidation = StorageController.getInstance().getLatestLastValidation();
        if (lastValidation != null && lastValidation.getValidationType().equals(CheckNumberResponse.ValidationMethod.REVERSE_CLI) && dialedNumber != null) {
            Intent intent = new Intent(CALL_TO_VERIFY);
            intent.putExtra(ValidationController.PIN_EXTRA, dialedNumber.substring(dialedNumber.length() - 4));
            intent.putExtra(ValidationController.ID_EXTRA, lastValidation.getValidationResponse().getId());
            context.sendBroadcast(intent);
        }
    }
    
}
