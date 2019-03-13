package com.checkmobi.sdk.ui;

import com.checkmobi.sdk.R;
import com.checkmobi.sdk.formatting.PhoneNumberFormatter;
import com.checkmobi.sdk.model.CountryCode;
import com.checkmobi.sdk.network.response.CheckNumberResponse;
import com.checkmobi.sdk.network.response.ValidationResponse;
import com.checkmobi.sdk.storage.StorageController;
import com.checkmobi.sdk.system.listeners.CallListener;
import com.checkmobi.sdk.system.listeners.SmsListener;
import com.checkmobi.sdk.validation.ValidationController;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NumberInputActivity extends VerificationBaseActivity {
    
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int PIN_VALIDATION_REQUEST_CODE = 2;
    
    private EditText etCountryCode;
    private EditText etNumber;
    private Button btContinue;
    
    private CheckNumberResponse lastFullNumberUsed;
    
    private CountDownTimer countDownTimer;
    
    private SmsListener mSmsListener;
    private CallListener mCallListener;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number_input);
        registerReceivers();
        setViews();
        setActions();
    }
    
    private void registerReceivers() {
        mSmsListener= new SmsListener();
        registerReceiver(mSmsListener, new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
        mCallListener = new CallListener();
        registerReceiver(mCallListener, new IntentFilter("android.intent.action.PHONE_STATE"));
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        setCountryCode();
        setLastUsedNumber();
    }
    
    private void setViews() {
        etCountryCode = EditText.class.cast(findViewById(R.id.et_country_code));
        etNumber = EditText.class.cast(findViewById(R.id.et_number));
        btContinue = Button.class.cast(findViewById(R.id.bt_continue));
    }
    
    private void setActions() {
        etCountryCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent countryIntent = new Intent(NumberInputActivity.this, CountryCodeActivity.class);
                countryIntent.putExtra(CountryCodeActivity.SET_COUNTRY_CODE, true);
                startActivityWithExtras(countryIntent);
            }
        });
        btContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAndFormatNumber();
            }
        });
    }
    
    private void requestFirstValidation(CheckNumberResponse fullNumberUsed) {
        final CheckNumberResponse.ValidationMethod validationMethod = ValidationController.getFirstAvailableValidationMethod(NumberInputActivity.this, fullNumberUsed);
        if (validationMethod != null) {
            if (validationMethod.getType().equals(CheckNumberResponse.ValidationMethod.REVERSE_CLI) && (getGrantForPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED || getGrantForPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED)) {
                handleReverseCliFailed();
            } else {
                requestValidation(validationMethod.getType(), false);
            }
        } else {
            Toast.makeText(this, "No more validation methods", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void requestValidation(final String validationType, final boolean silentFail) {
        ValidationController.validate(NumberInputActivity.this, validationType, lastFullNumberUsed.getE164Format(), new Callback<ValidationResponse>() {
            @Override
            public void onResponse(Call<ValidationResponse> call, Response<ValidationResponse> response) {
                System.out.println(response);
                if (response.isSuccessful()) {
                    if (validationType.equals(CheckNumberResponse.ValidationMethod.REVERSE_CLI)) {
                        setCallCountDownTimer();
                    } else {
                        goToValidationScreen();
                    }
                } else {
                    if (silentFail) {
                        //DO NOTHING
                    } else {
                        showErrorDialog(R.string.server_error);
                    }
                }
            }
    
            @Override
            public void onFailure(Call<ValidationResponse> call, Throwable t) {
                System.out.println(t.toString());
                showErrorDialog(R.string.server_error);
            }
        });
    }
    
    private void goToValidationScreen() {
        startActivityForResultWithExtras(new Intent(NumberInputActivity.this, PinValidationActivity.class), PIN_VALIDATION_REQUEST_CODE);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PIN_VALIDATION_REQUEST_CODE) {
            setResult(resultCode, data);
            finish();
        }
    }
    
    private int getGrantForPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission);
    }
    
    private void requestSmsAndCallPermission(CheckNumberResponse fullNumberUsed) {
        String smsPermission = Manifest.permission.RECEIVE_SMS;
        String callPermission = Manifest.permission.READ_PHONE_STATE;
        String callLogPermission = Manifest.permission.READ_CALL_LOG;
        int smsGrant = getGrantForPermission(smsPermission);
        int callGrant = getGrantForPermission(callPermission);
        int callLogGrant = getGrantForPermission(callLogPermission);
        List<String> permissionList = new ArrayList<>();
        if (callFirst(fullNumberUsed)) {
            if (callGrant != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(callPermission);
            }
            if (callLogGrant != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(callLogPermission);
            }
        }
        if (smsGrant != PackageManager.PERMISSION_GRANTED &&
                (smsFirst(fullNumberUsed) ||
                        (callFirst(fullNumberUsed) && smsAvailable(fullNumberUsed)))) {
            permissionList.add(smsPermission);
        }
        if (permissionList.size() > 0) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), PERMISSION_REQUEST_CODE);
        } else {
            requestFirstValidation(fullNumberUsed);
        }
    }
    
    private boolean callFirst(CheckNumberResponse fullNumberUsed) {
        CheckNumberResponse.ValidationMethod firstAvailableValidationMethod = ValidationController.getFirstAvailableValidationMethod(NumberInputActivity.this, fullNumberUsed);
        if (firstAvailableValidationMethod != null) {
            return firstAvailableValidationMethod.getType().equals(CheckNumberResponse.ValidationMethod.REVERSE_CLI);
        } else {
            return false;
        }
    }
    
    private boolean smsFirst(CheckNumberResponse fullNumberUsed) {
        CheckNumberResponse.ValidationMethod firstAvailableValidationMethod = ValidationController.getFirstAvailableValidationMethod(NumberInputActivity.this, fullNumberUsed);
        if (firstAvailableValidationMethod != null) {
            return firstAvailableValidationMethod.getType().equals(CheckNumberResponse.ValidationMethod.SMS);
        } else {
            return false;
        }
    }
    
    private boolean smsAvailable(CheckNumberResponse fullNumberUsed) {
        return ValidationController.isValidationMethodValid(NumberInputActivity.this, CheckNumberResponse.ValidationMethod.SMS, fullNumberUsed);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            requestFirstValidation(StorageController.getInstance().getLastUsedFullNumber());
        }
        
    }
    
    
    private void setCountryCode() {
        CountryCode countryCode = StorageController.getInstance().readCountryCode(this);
        if (countryCode != null) {
            etCountryCode.setText(PhoneNumberFormatter.getFormattedCountryCode(countryCode));
        }
    }
    
    private void setLastUsedNumber() {
        String lastUsedNumber = StorageController.getInstance().getLastUsedNumber(this);
        if (lastUsedNumber != null) {
            etNumber.setText(lastUsedNumber);
        }
    }
    
    private void setCallCountDownTimer() {
        showLoading();
        countDownTimer = new CountDownTimer(20000, 1000) {
        
            public void onTick(long millisUntilFinished) {
                //Do Nothing
            }
        
            public void onFinish() {
                hideLoading();
                handleReverseCliFailed();
            }
        };
        countDownTimer.start();
    }
    
    private void checkAndFormatNumber() {
        final CountryCode savedCountryCode = StorageController.getInstance().readCountryCode(this);
        final String number = etNumber.getText().toString();
        if (savedCountryCode == null) {
            showErrorDialog(R.string.country_code_empty_error);
        } else if (TextUtils.isEmpty(number)) {
            showErrorDialog(R.string.number_empty_error);
        } else {
            showLoading();
            StorageController.getInstance().updateLastUsedNumber(NumberInputActivity.this, number);
            PhoneNumberFormatter.checkAndFormatNumber(savedCountryCode, number, new Callback<CheckNumberResponse>() {
                @Override
                public void onResponse(Call<CheckNumberResponse> call, Response<CheckNumberResponse> response) {
                    hideLoading();
                    if(response != null && response.isSuccessful()) {
                        showValidNumberVerificationDialog(response.body());
                    } else {
                        String locallyFormattedPhoneNumber = PhoneNumberFormatter.getLocallyFormattedPhoneNumber(savedCountryCode, number);
                        showInvalidNumberVerificationDialog(CheckNumberResponse.createResponseFromE164Number(locallyFormattedPhoneNumber));
                    }
                }
    
                @Override
                public void onFailure(Call<CheckNumberResponse> call, Throwable t) {
                    hideLoading();
                    String locallyFormattedPhoneNumber = PhoneNumberFormatter.getLocallyFormattedPhoneNumber(savedCountryCode, number);
                    showInvalidNumberVerificationDialog(CheckNumberResponse.createResponseFromE164Number(locallyFormattedPhoneNumber));
                }
            });
        }
    }
    
    private void showValidNumberVerificationDialog(final CheckNumberResponse checkNumberResponse) {
        showNumberVerificationDialog(R.string.valid_number_verification_message, checkNumberResponse);
    }
    
    private void showInvalidNumberVerificationDialog(final CheckNumberResponse checkNumberResponse) {
        showNumberVerificationDialog(R.string.invalid_number_verification_message, checkNumberResponse);
    }
    
    private void showNumberVerificationDialog(final int messageId, final CheckNumberResponse checkNumberResponse) {
        String formattedNumber = checkNumberResponse.getFormatting();
        if (formattedNumber == null) {
            formattedNumber = checkNumberResponse.getE164Format();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        builder.setMessage(getResources().getString(messageId, formattedNumber));
        builder.setPositiveButton(getResources().getString(R.string.button_continue), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                lastFullNumberUsed = checkNumberResponse;
                StorageController.getInstance().updateLastUsedFullNumber(checkNumberResponse);
                showMissedCallTutorialIfNecessary(checkNumberResponse);
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.button_edit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }
    
    private void showMissedCallTutorialIfNecessary(final CheckNumberResponse checkNumberResponse) {
        if (callFirst(checkNumberResponse)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
    
            builder.setMessage(getResources().getString(R.string.missed_call_tutorial));
            builder.setPositiveButton(getResources().getString(R.string.button_continue), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    requestSmsAndCallPermission(checkNumberResponse);
                }
            });
            builder.show();
        } else {
            requestSmsAndCallPermission(checkNumberResponse);
        }
    }
    
    @Override
    protected void onDestroy() {
        unregisterReceiver(mSmsListener);
        unregisterReceiver(mCallListener);
        super.onDestroy();
    }
    
    private void handleReverseCliFailed() {
        CheckNumberResponse lastUsedFullNumber = StorageController.getInstance().getLastUsedFullNumber();
        if (ValidationController.isValidationMethodValid(this, CheckNumberResponse.ValidationMethod.SMS, lastUsedFullNumber)) {
            requestValidation(CheckNumberResponse.ValidationMethod.SMS, true);
        }
    }
    
    @Override
    protected boolean listenForSms() {
        return false;
    }
    
    @Override
    protected boolean listenForCall() {
        return true;
    }
    
    @Override
    protected void handleSuccessfulVerification() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.handleSuccessfulVerification();
    }
}
