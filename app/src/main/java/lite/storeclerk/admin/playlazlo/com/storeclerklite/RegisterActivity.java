package lite.storeclerk.admin.playlazlo.com.storeclerklite;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.refactor.lib.colordialog.PromptDialog;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.helper.APIInterface;

import lite.storeclerk.admin.playlazlo.com.storeclerklite.helper.AndroidUtilities;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.helper.Constants;

/**
 * Created by mymac on 6/20/17.
 */

public class RegisterActivity extends AppCompatActivity {


    private ProgressDialog mProgressDialog;
    private JSONObject checkoutObj;
    private JSONObject receivedObj;
    private double amount;
    private String userAccessToken = "";

    private static int i_wasBorn = 0;
    private static String str_wasBorn = "";
    private static TextView lblBirthDate;

    private EditText txtFirstName;
    private EditText txtLastName;
    private EditText txtEmail;
    private EditText txtConfirmEmail;
    private EditText txtPhone;
    private EditText txtConfirmPhone;
    private EditText txtPhotoIdCode;

    private Spinner storesDropDown;
    private List<String> storeNameArr = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(RegisterActivity.this.getApplicationContext());
        userAccessToken = sharedPref.getString("fbAccessToken","");

        lblBirthDate = (TextView) findViewById(R.id.register_birth_date_txt);
        txtFirstName = (EditText) findViewById(R.id.register_first_name_txt);
        txtLastName = (EditText) findViewById(R.id.register_last_name_txt);
        txtEmail = (EditText) findViewById(R.id.register_email_txt);
        txtConfirmEmail = (EditText) findViewById(R.id.register_confirm_email_txt);
        txtPhone = (EditText) findViewById(R.id.register_phone_txt);
        txtConfirmPhone = (EditText) findViewById(R.id.register_confirm_phone_txt);
        txtPhotoIdCode = (EditText) findViewById(R.id.register_photo_id_code_txt);

        storesDropDown = (Spinner) findViewById(R.id.register_stores_dropdown);

        if (Constants.globalRetailerArr.size() > 0) {
            for (JSONObject retailerItem : Constants.globalRetailerArr) {
                try {
                    storeNameArr.add(" Store > " + retailerItem.getString("retailerName"));
                } catch (Exception e) {
                    Log.d("json_e-->", e.getMessage());
                }
            }
        }
        ArrayAdapter<String> storeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, storeNameArr);
        storesDropDown.setAdapter(storeAdapter);

        storesDropDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = (TextView)view.findViewById(android.R.id.text1);
                tv.setText(storeNameArr.get(position));
                JSONObject selectedRetailerItem = Constants.globalRetailerArr.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void onBackAction(View view) {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("backView", "back");
        startActivity(i);
        finish();
    }

    public void createDatePicker(View view) {
        DialogFragment dFragment = new DatePickerFragment();
        // Show the date picker dialog fragment
        dFragment.show(RegisterActivity.this.getSupportFragmentManager(), "please choose birth date.");
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener{

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR) - 18;
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dpd = new DatePickerDialog(getActivity(),this,year,month,day);
            return  dpd;
        }

        public void onDateSet(DatePicker view, int year, int month, int day){
            // Do something with the chosen date

            // Create a Date variable/object with user chosen date
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(0);
            cal.set(year, month, day, 0, 0, 0);
            Date chosenDate = cal.getTime();

            // Format the date using style and locale
            str_wasBorn = new SimpleDateFormat("MM-dd-yyyy", Locale.US).format(chosenDate);

            // check user age and the birthdate insert field border color update
            i_wasBorn = Calendar.getInstance().get(Calendar.YEAR) - year;
            updateInsertBirthdateStroke();
            // Display the chosen date to app interface

            lblBirthDate.setText(str_wasBorn);

        }
    }
    public void registerBtnAction(View view) {

        if (txtFirstName.getText().toString().matches("")) {
            new PromptDialog(RegisterActivity.this)
                    .setDialogType(PromptDialog.DIALOG_TYPE_WARNING)
                    .setAnimationEnable(true)
                    .setTitleText("Warning!")
                    .setContentText("Oops first name fields is empty. \n Please enter first name.")
                    .setPositiveListener("Ok", new PromptDialog.OnPositiveListener() {
                        @Override
                        public void onClick(PromptDialog dialog) {
                            dialog.dismiss();
                        }
                    }).show();
        } else if (txtLastName.getText().toString().matches("")) {
            new PromptDialog(RegisterActivity.this)
                    .setDialogType(PromptDialog.DIALOG_TYPE_WARNING)
                    .setAnimationEnable(true)
                    .setTitleText("Warning!")
                    .setContentText("Oops last name fields is empty. \n Please enter last name.")
                    .setPositiveListener("Ok", new PromptDialog.OnPositiveListener() {
                        @Override
                        public void onClick(PromptDialog dialog) {
                            dialog.dismiss();
                        }
                    }).show();
        } else if (txtPhone.getText().toString().matches("")) {
            new PromptDialog(RegisterActivity.this)
                    .setDialogType(PromptDialog.DIALOG_TYPE_WARNING)
                    .setAnimationEnable(true)
                    .setTitleText("Warning!")
                    .setContentText("Oops phone fields is empty. \n Please enter phone number.")
                    .setPositiveListener("Ok", new PromptDialog.OnPositiveListener() {
                        @Override
                        public void onClick(PromptDialog dialog) {
                            dialog.dismiss();
                        }
                    }).show();
        } else if (txtEmail.getText().toString().matches("")) {
            new PromptDialog(RegisterActivity.this)
                    .setDialogType(PromptDialog.DIALOG_TYPE_WARNING)
                    .setAnimationEnable(true)
                    .setTitleText("Warning!")
                    .setContentText("Oops email fields is empty. \n Please enter email address.")
                    .setPositiveListener("Ok", new PromptDialog.OnPositiveListener() {
                        @Override
                        public void onClick(PromptDialog dialog) {
                            dialog.dismiss();
                        }
                    }).show();
        } else if (txtPhotoIdCode.getText().toString().matches("")) {
            new PromptDialog(RegisterActivity.this)
                    .setDialogType(PromptDialog.DIALOG_TYPE_WARNING)
                    .setAnimationEnable(true)
                    .setTitleText("Warning!")
                    .setContentText("Oops photo id code fields is empty. \n Please enter photo id code.")
                    .setPositiveListener("Ok", new PromptDialog.OnPositiveListener() {
                        @Override
                        public void onClick(PromptDialog dialog) {
                            dialog.dismiss();
                        }
                    }).show();
        } else if (!txtPhone.getText().toString().matches(txtConfirmPhone.getText().toString())) {
            new PromptDialog(RegisterActivity.this)
                    .setDialogType(PromptDialog.DIALOG_TYPE_WARNING)
                    .setAnimationEnable(true)
                    .setTitleText("Warning!")
                    .setContentText("Oops don't match phone number. \n Please confirm phone number.")
                    .setPositiveListener("Ok", new PromptDialog.OnPositiveListener() {
                        @Override
                        public void onClick(PromptDialog dialog) {
                            dialog.dismiss();
                        }
                    }).show();
        } else if (!txtEmail.getText().toString().matches(txtConfirmEmail.getText().toString())) {
            new PromptDialog(RegisterActivity.this)
                    .setDialogType(PromptDialog.DIALOG_TYPE_WARNING)
                    .setAnimationEnable(true)
                    .setTitleText("Warning!")
                    .setContentText("Oops don't match email address. \n Please confirm email address.")
                    .setPositiveListener("Ok", new PromptDialog.OnPositiveListener() {
                        @Override
                        public void onClick(PromptDialog dialog) {
                            dialog.dismiss();
                        }
                    }).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(txtEmail.getText().toString()).matches()) {
            new PromptDialog(RegisterActivity.this)
                    .setDialogType(PromptDialog.DIALOG_TYPE_WARNING)
                    .setAnimationEnable(true)
                    .setTitleText("Warning!")
                    .setContentText("Please input valid email address.")
                    .setPositiveListener("Ok", new PromptDialog.OnPositiveListener() {
                        @Override
                        public void onClick(PromptDialog dialog) {
                            dialog.dismiss();
                        }
                    }).show();
        } else if (!Patterns.PHONE.matcher(txtPhone.getText().toString()).matches()) {
            new PromptDialog(RegisterActivity.this)
                    .setDialogType(PromptDialog.DIALOG_TYPE_WARNING)
                    .setAnimationEnable(true)
                    .setTitleText("Warning!")
                    .setContentText("Please input valid phone number.")
                    .setPositiveListener("Ok", new PromptDialog.OnPositiveListener() {
                        @Override
                        public void onClick(PromptDialog dialog) {
                            dialog.dismiss();
                        }
                    }).show();
        } else {
            registerUser();
        }
    }

    private void registerUser() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.requestWindowFeature(Window.FEATURE_NO_TITLE);
        pd.setMessage("Registering...");
        pd.show();
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        new Thread(new Runnable() {
            public void run() {
                try {
                    String photoIdCode = txtPhotoIdCode.getText().toString().trim();
                    String firstName = txtFirstName.getText().toString().trim();
                    String lastName = txtLastName.getText().toString().trim();
                    String email = txtEmail.getText().toString().trim();
                    String phone = txtPhone.getText().toString().trim();
                    String uuid = AndroidUtilities.getUUID(RegisterActivity.this);
                    receivedObj = APIInterface.registerUser(photoIdCode,firstName,lastName,email,phone,userAccessToken,uuid);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (receivedObj != null) {

                                try {
                                    JSONObject jsonData = receivedObj.getJSONObject("data");
                                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(RegisterActivity.this.getApplicationContext());
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putBoolean("registerState", true);
                                    editor.apply();
                                    Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                                    startActivity(i);
                                    finish();

                                } catch (Exception e) {
                                    Log.d("json_e-->", e.getMessage());
                                }

                            } else {

                                if (pd.isShowing()) {
                                    pd.dismiss();
                                }
                                Vibrator v = (Vibrator) RegisterActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
                                v.vibrate(1000);
                                new PromptDialog(RegisterActivity.this)
                                        .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                        .setAnimationEnable(true)
                                        .setTitleText("Register error")
                                        .setContentText("Oops Register failed. \n Please restart after exit.")
                                        .setPositiveListener("Ok", new PromptDialog.OnPositiveListener() {
                                            @Override
                                            public void onClick(PromptDialog dialog) {
                                                dialog.dismiss();
                                            }
                                        }).show();
                            }

                        }
                    });
                } catch (final Exception e) {
                    Log.e("register error--->", e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (pd.isShowing()) {
                                pd.dismiss();
                            }
                            Vibrator v = (Vibrator) RegisterActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
                            v.vibrate(1000);
                            new PromptDialog(RegisterActivity.this)
                                    .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                    .setAnimationEnable(true)
                                    .setTitleText("Register error")
                                    .setContentText("Oops Register failed. \n Please restart after exit.")
                                    .setPositiveListener("Ok", new PromptDialog.OnPositiveListener() {
                                        @Override
                                        public void onClick(PromptDialog dialog) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                        }
                    });

                }
            }
        }).start();
    }

    private static void updateInsertBirthdateStroke() {
        lblBirthDate.setBackgroundResource(R.drawable.background_blue_stroke);
        GradientDrawable drawable = (GradientDrawable) lblBirthDate.getBackground();
        if (i_wasBorn < 18) {
            drawable.setStroke(2, Color.RED);
        } else {
            drawable.setStroke(2, Color.BLUE);
        }
    }
}
