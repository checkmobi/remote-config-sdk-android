package com.checkmobi.checkmobisample.ui;

import com.checkmobi.checkmobisample.R;
import com.checkmobi.sdk.CheckmobiSdk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SuccessActivity extends AppCompatActivity {
    
    private TextView tvVerifiedNumber;
    private Button btResetVerifiedNumber;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);
        setViews();
        setActions();
        loadVerifiedNumber();
    }
    
    private void setViews() {
        tvVerifiedNumber = (TextView) findViewById(R.id.tv_verified_number);
        btResetVerifiedNumber = (Button) findViewById(R.id.bt_reset_verified_number);
    }
    
    private void setActions() {
        btResetVerifiedNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckmobiSdk.getInstance().resetVerifiedNumber(SuccessActivity.this);
                startActivity(new Intent(SuccessActivity.this, StartActivity.class));
                finish();
            }
        });
    }
    
    private void loadVerifiedNumber() {
        String verifiedNumber = CheckmobiSdk.getInstance().getVerifiedNumber(this);
        if (verifiedNumber != null) {
            tvVerifiedNumber.setText(verifiedNumber);
        }
    }
}
