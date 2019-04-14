package com.checkmobi.sdk.system.listeners;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

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
    
    public static final String START_FOR_TEMPLATE_WITH_HASH = "<#> ";
    public static final String CODE_IN_TEMPLATE = "%code%";
    public static final String SMS_TO_VERIFY = "SmsListener.SMS_TO_VERIFY";
    
    @Override
    public void onReceive(final Context context, Intent intent) {
        System.out.println("SMS Listener received action: " + intent.getAction());
        if(SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())){
            Bundle extras = intent.getExtras();
            Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);
    
            switch(status.getStatusCode()) {
                case CommonStatusCodes.SUCCESS:
                    // Get SMS message contents
                    String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                    System.out.println(message);
                    verifyIncomingSms(context, message);
                    break;
                case CommonStatusCodes.TIMEOUT:
                     System.out.println("Waiting for SMS timed out (5 minutes)");
                    break;
            }
        }
    }
    
    private void verifyIncomingSms(final Context context, final String msgBody) {
        final LastValidation lastValidation = StorageController.getInstance().getLatestLastValidation();
        String smsTemplate = START_FOR_TEMPLATE_WITH_HASH + StorageController.getInstance().getSmsTemplateFromLastUsedValidationMethods();
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
