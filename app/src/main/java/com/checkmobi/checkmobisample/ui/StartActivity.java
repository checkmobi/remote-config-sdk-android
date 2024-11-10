package com.checkmobi.checkmobisample.ui;

import com.checkmobi.checkmobisample.R;
import com.checkmobi.sdk.CheckmobiSdk;

import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class StartActivity extends AppCompatActivity {

    static {
        System.loadLibrary("native-lib");
    }

    private static final String CHECKMOBI_SECRET_KEY = "";
    
    private static final String SHARED_PREFS_FILE = "android_checkmobi_sample_prefs";
    private static final String LAST_USED_API_KEY = "last_used_api_key";
    
    private static final int VERIFICATION_RC = 1;
    
    private Button btStartVerification;
    private Button btStartVerificationWithTheme;
    private EditText mApiKey;

    public native String stringFromJNI();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        setViews();
        retrieveLastUsedApiKey();
        setActions();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        checkIfNumberAlreadyVerified();
    }
    
    private void checkIfNumberAlreadyVerified() {
        String verifiedNumber = CheckmobiSdk.getInstance().getVerifiedNumber(this);
        if (verifiedNumber != null) {
            startActivity(new Intent(this, SuccessActivity.class));
            finish();
        }
    }
    
    private void setupCheckmobi() {
        CheckmobiSdk.getInstance().setApiKey(mApiKey.getText().toString());
    }
    
    private void retrieveLastUsedApiKey() {
        String lastApiKeyUsed = getSharedPreferences(SHARED_PREFS_FILE, MODE_PRIVATE).getString(LAST_USED_API_KEY, null);
        if (!TextUtils.isEmpty(lastApiKeyUsed)) {
            mApiKey.setText(lastApiKeyUsed);
        } else {
            String key_jni = stringFromJNI();
            if(!TextUtils.isEmpty(key_jni))
                mApiKey.setText(key_jni);
            else
                mApiKey.setText(CHECKMOBI_SECRET_KEY);
        }
    }
    
    private void saveLastUsedApiKey() {
        getSharedPreferences(SHARED_PREFS_FILE, MODE_PRIVATE)
                .edit()
                .putString(LAST_USED_API_KEY, mApiKey.getText().toString())
                .commit();
    }
    
    private void setViews() {
        btStartVerification = Button.class.cast(findViewById(R.id.bt_start_verification));
        btStartVerificationWithTheme = Button.class.cast(findViewById(R.id.bt_start_verification_with_theme));
        mApiKey = EditText.class.cast(findViewById(R.id.et_api_key));
    }
    
    private void setActions() {
        btStartVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(mApiKey.getText())) {
                    showNoApiKeyErrorDialog();
                } else {
                    saveLastUsedApiKey();
                    setupCheckmobi();
                    startActivityForResult(
                            CheckmobiSdk.getInstance()
                                    .createVerificationIntentBuilder()
                                    .build(StartActivity.this),
                            VERIFICATION_RC);
                }
            }
        });
        btStartVerificationWithTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(mApiKey.getText())) {
                    showNoApiKeyErrorDialog();
                } else {
                    saveLastUsedApiKey();
                    setupCheckmobi();
                    startActivityForResult(
                            CheckmobiSdk.getInstance()
                                    .createVerificationIntentBuilder()
                                    .setTheme(R.style.AppTheme)
                                    .build(StartActivity.this),
                            VERIFICATION_RC);
                }
            }
        });
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VERIFICATION_RC) {
            if (resultCode == RESULT_OK) {
                startActivity(new Intent(this, SuccessActivity.class));
                finish();
            } else {
                Toast.makeText(this, R.string.user_canceled_verification_message, Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    protected void showNoApiKeyErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please set your Api Key");
        builder.setPositiveButton(getResources().getString(com.checkmobi.sdk.R.string.button_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }
}
