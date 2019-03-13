package com.checkmobi.sdk.system.listeners;

import com.checkmobi.sdk.model.LastValidation;
import com.checkmobi.sdk.network.response.CheckNumberResponse;
import com.checkmobi.sdk.storage.StorageController;
import com.checkmobi.sdk.validation.ValidationController;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SmsListener extends BroadcastReceiver {
    
    public static final String CODE_IN_TEMPLATE = "%code%";
    public static final String SMS_TO_VERIFY = "SmsListener.SMS_TO_VERIFY";
    
    @Override
    public void onReceive(final Context context, Intent intent) {
        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Bundle bundle = intent.getExtras();           //---get the SMS message passed in---
            if (bundle != null){
                //---retrieve the SMS message received---
                try{
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    SmsMessage[] msgs = new SmsMessage[pdus.length];
                    for(int i=0; i<msgs.length; i++){
                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                        final String msgBody = msgs[i].getMessageBody();
                        System.out.println(msgBody);
                        verifyIncomingSms(context, msgBody);
                    }
                }catch(Exception e){
                            System.out.println(e.getMessage());
                }
            }
        }
    }
    
    private void verifyIncomingSms(final Context context, final String msgBody) {
        final LastValidation lastValidation = StorageController.getInstance().getLatestLastValidation();
        String smsTemplate = StorageController.getInstance().getSmsTemplateFromLastUsedValidationMethods();
        if (lastValidation.getValidationType().equals(CheckNumberResponse.ValidationMethod.SMS) &&
                isCorrectPattern(smsTemplate, msgBody)) {
            Intent intent = new Intent(SMS_TO_VERIFY);
            intent.putExtra(ValidationController.PIN_EXTRA, getCodeFromMessage(smsTemplate, msgBody));
            intent.putExtra(ValidationController.ID_EXTRA, lastValidation.getValidationResponse().getId());
            context.sendBroadcast(intent);
        }
    }
    
    private boolean isCorrectPattern(String smsTemplate, String msgBody) {
        if (smsTemplate != null && smsTemplate.contains(CODE_IN_TEMPLATE)) {
            String[] splitTemplate = smsTemplate.split(CODE_IN_TEMPLATE);
            String start = splitTemplate[0];
            if (!msgBody.startsWith(start)) {
                return false;
            }
            if (splitTemplate.length > 1) {
                String stop = splitTemplate[1];
                if (!msgBody.endsWith(stop)) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }
    
    private String getCodeFromMessage(String smsTemplate, String msgBody) {
        int beginIndex = smsTemplate.indexOf(CODE_IN_TEMPLATE);
        return msgBody.substring(beginIndex, beginIndex + 4);
    }
}
