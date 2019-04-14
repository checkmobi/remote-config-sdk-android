package com.checkmobi.sdk.ui;

import com.checkmobi.sdk.R;
import com.checkmobi.sdk.model.LastValidation;
import com.checkmobi.sdk.network.response.CheckNumberResponse;
import com.checkmobi.sdk.network.response.ValidationResponse;
import com.checkmobi.sdk.storage.StorageController;
import com.checkmobi.sdk.validation.ValidationController;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.checkmobi.sdk.network.response.CheckNumberResponse.ValidationMethod.IVR;
import static com.checkmobi.sdk.network.response.CheckNumberResponse.ValidationMethod.REVERSE_CLI;
import static com.checkmobi.sdk.network.response.CheckNumberResponse.ValidationMethod.SMS;

public class PinValidationActivity extends VerificationBaseActivity {
    
    private static final int PERMISSION_REQUEST_CODE = 1;
    
    private TextView receiveSoonInfo;
    private TextView didNotReceiveCodeInfo;
    
    private Button smsButton;
    private Button callButton;
    private Button missedCallButton;
    
    private TextView smsTimeLeft;
    private TextView callTimeLeft;
    private TextView missedCallTimeLeft;
    
    private EditText etPin;
    private Button btValidatePin;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_validation);
        setViews();
        setActions();
        updateActionBar();
        hideValidationMethodsIfNotAvailable();
        updateStatus();
    }
    
    private void setViews() {
        receiveSoonInfo = TextView.class.cast(findViewById(R.id.tv_receive_code_soon));
        didNotReceiveCodeInfo = TextView.class.cast(findViewById(R.id.tv_did_not_receive_code));
        
        smsButton = Button.class.cast(findViewById(R.id.bt_sms_label));
        callButton = Button.class.cast(findViewById(R.id.bt_call_label));
        missedCallButton = Button.class.cast(findViewById(R.id.bt_missed_call_label));
        
        smsTimeLeft = TextView.class.cast(findViewById(R.id.tv_sms_time_left));
        callTimeLeft = TextView.class.cast(findViewById(R.id.tv_call_left_time));
        missedCallTimeLeft = TextView.class.cast(findViewById(R.id.tv_missed_call_time_left));
        
        etPin = EditText.class.cast(findViewById(R.id.et_pin));
        btValidatePin = Button.class.cast(findViewById(R.id.bt_validate_pin));
    }
    
    private void setActions() {
        btValidatePin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final LastValidation lastValidation = StorageController.getInstance().getLatestLastValidation();
                verifyPin(etPin.getText().toString(), lastValidation.getValidationResponse().getId());
            }
        });
        
        smsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestValidationFor(CheckNumberResponse.ValidationMethod.SMS);
            }
        });
        
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestValidationFor(CheckNumberResponse.ValidationMethod.IVR);
            }
        });
        
        missedCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMissedCallTutorial();
            }
        });
    }
    
    private void requestValidationFor(final String validationType) {
        ValidationController.validate(PinValidationActivity.this, validationType, StorageController.getInstance().getLastUsedFullNumber().getE164Format(), new Callback<ValidationResponse>() {
            @Override
            public void onResponse(Call<ValidationResponse> call, Response<ValidationResponse> response) {
                if (response.isSuccessful()) {
                    if (validationType.equals(CheckNumberResponse.ValidationMethod.REVERSE_CLI)) {
                        setCallCountDownTimer();
                    }
                    if (validationType.equals(CheckNumberResponse.ValidationMethod.SMS)) {
                        registerForSMSRetrieverApi();
                    }
                    updateStatus();
                } else {
                    showErrorDialog(R.string.server_error);
                }
                System.out.println(response);
            }
            
            @Override
            public void onFailure(Call<ValidationResponse> call, Throwable t) {
                showErrorDialog(R.string.server_error);
                System.out.println(t.getMessage());
            }
        });
    }
    
    private void updateActionBar() {
        CheckNumberResponse lastUsedFullNumber = StorageController.getInstance().getLastUsedFullNumber();
        if (lastUsedFullNumber != null) {
            String formattedNumber = lastUsedFullNumber.getFormatting();
            if (formattedNumber == null) {
                formattedNumber = lastUsedFullNumber.getE164Format();
            }
            getSupportActionBar().setTitle(formattedNumber);
        }
    }
    
    private void updateStatus() {
        String lastMethodReadable = "Call";
        LastValidation lastValidation = StorageController.getInstance().getLatestLastValidation();
        if (lastValidation != null && lastValidation.getValidationType().equals(CheckNumberResponse.ValidationMethod.SMS)) {
            lastMethodReadable = "SMS";
        }
        receiveSoonInfo.setText(getString(R.string.receive_code_soon, lastMethodReadable));
        didNotReceiveCodeInfo.setText(getString(R.string.did_not_receive_code, lastMethodReadable));
        List<CheckNumberResponse.ValidationMethod> validationMethods = StorageController.getInstance().getLastUsedValidationMethods();
        for (CheckNumberResponse.ValidationMethod validationMethod : validationMethods) {
            switch (validationMethod.getType()) {
                case SMS: {
                    setDataForValidationType(validationMethod, smsButton, smsTimeLeft);
                    break;
                }
                case IVR: {
                    setDataForValidationType(validationMethod, callButton, callTimeLeft);
                    break;
                }
                case REVERSE_CLI: {
                    setDataForValidationType(validationMethod, missedCallButton, missedCallTimeLeft);
                }
            }
        }
    }
    
    private void hideValidationMethodsIfNotAvailable() {
        hideValidationMethodIfNotAvailable(SMS, smsButton, smsTimeLeft);
        hideValidationMethodIfNotAvailable(IVR, callButton, callTimeLeft);
        hideValidationMethodIfNotAvailable(REVERSE_CLI, missedCallButton, missedCallTimeLeft);
    }
    
    private void hideValidationMethodIfNotAvailable(String validationType, Button button, TextView textView) {
        if (!ValidationController.isValidationMethodTurnedOn(validationType)) {
            button.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
        }
    }
    
    private void setDataForValidationType(CheckNumberResponse.ValidationMethod validationMethod, Button button, TextView textView) {
        if (ValidationController.isValidationMethodTurnedOn(validationMethod.getType())) {
            long retriesLeft = ValidationController.getRetriesLeftForValidationMethod(this, validationMethod.getType());
            if (retriesLeft > 0) {
                long timeLeft = ValidationController.getTimeUntilValidationMethodsAreAvailable();
                if (timeLeft > 0) {
                    textView.setVisibility(View.VISIBLE);
                    button.setEnabled(false);
                    setCountDownTimer(button, textView, timeLeft);
                } else {
                    textView.setVisibility(View.GONE);
                    button.setEnabled(true);
                }
            } else {
                textView.setVisibility(View.VISIBLE);
                textView.setText(R.string.no_retries_left);
                button.setEnabled(false);
            }
        } else {
            button.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
        }
    }
    
    private void setCountDownTimer(final Button button, final TextView textView, long millisInFuture) {
        new CountDownTimer(millisInFuture, 1000) {
            
            public void onTick(long millisUntilFinished) {
                textView.setText("seconds remaining: " + millisUntilFinished / 1000);
            }
            
            public void onFinish() {
                textView.setVisibility(View.GONE);
                button.setEnabled(true);
            }
        }.start();
    }
    
    private void showMissedCallTutorial() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        builder.setMessage(getResources().getString(R.string.missed_call_tutorial));
        builder.setPositiveButton(getResources().getString(R.string.button_continue), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                requestCallPermission();
            }
        });
        builder.show();
    }
    
    private void requestCallPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);
        } else {
            requestValidationFor(CheckNumberResponse.ValidationMethod.REVERSE_CLI);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestValidationFor(CheckNumberResponse.ValidationMethod.REVERSE_CLI);
            } else {
                showErrorDialog(R.string.method_will_not_work_without_permission);
            }
            
        }
    }
    
    private void setCallCountDownTimer() {
        showLoading();
        new CountDownTimer(20000, 1000) {
            
            public void onTick(long millisUntilFinished) {
                //Do Nothing
            }
            
            public void onFinish() {
                hideLoading();
            }
        }.start();
    }
    
    @Override
    protected boolean listenForSms() {
        return true;
    }
    
    @Override
    protected boolean listenForCall() {
        return true;
    }
}
