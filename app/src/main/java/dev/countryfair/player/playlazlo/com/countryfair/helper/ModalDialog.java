package dev.countryfair.player.playlazlo.com.countryfair.helper;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype;
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;

import dev.countryfair.player.playlazlo.com.countryfair.R;

/**
 * Created by mymac on 3/9/17.
 */

public class ModalDialog{

    Context context;
    String titleStr;
    Drawable icon;
    String contentStr;

    public ModalDialog() {

    }

    public ModalDialog(Context context, String title, String content, Drawable icon) {
        this.context = context;
        this.titleStr = title;
        this.icon = icon;
        this.contentStr = content;
    }

    public NiftyDialogBuilder createNewMoadl() {
        NiftyDialogBuilder dialogBuilder= NiftyDialogBuilder.getInstance(this.context);
        dialogBuilder
                .withTitle(this.titleStr)
                .withTitleColor("#FFFFFF")                                  //def
                .withDividerColor("#11000000")                              //def
                .withMessage(this.contentStr)                                  //.withMessage(null)  no Msg
                .withMessageColor("#FFFFFFFF")                              //def  | withMessageColor(int resid)
                .withDialogColor("#FFE74C3C")                               //def  | withDialogColor(int resid)
                .withIcon(this.icon)
                .withDuration(700)                                          //def
                .withEffect(Effectstype.Slidetop)                           //def Effectstype.Slidetop
                .withButton1Text("OK")                                      //def gone
                .withButton2Text("Cancel")                                  //def gone
                .isCancelableOnTouchOutside(true)                           //def    | isCancelable(true)
                .setCustomView(R.layout.modal_birthday_layout, this.context)         //.setCustomView(View or ResId,context)
                .setButton1Click(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(v.getContext(), "i'm btn1", Toast.LENGTH_SHORT).show();
                    }
                })
                .setButton2Click(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(v.getContext(),"i'm btn2", Toast.LENGTH_SHORT).show();
                    }
                });
        return dialogBuilder;
    }

    public Dialog createCustomDialogUsingLayout() {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.modal_birthday_layout);
        dialog.setTitle(this.titleStr);

        // set the custom dialog components - text, image and button
        TextView textBody = (TextView) dialog.findViewById(R.id.modal_main_text);
        textBody.setText(this.contentStr);

        TextView textTitle = (TextView) dialog.findViewById(R.id.modal_title_text);
        textTitle.setText(this.titleStr);

        ImageView imageView = (ImageView) dialog.findViewById(R.id.modal_top_image);
        imageView.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.circleselfiesample));

        Button certifyBtn = (Button) dialog.findViewById(R.id.birthdate_certify);
        certifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button cancelBtn = (Button) dialog.findViewById(R.id.birthdate_cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        return dialog;
    }

}
