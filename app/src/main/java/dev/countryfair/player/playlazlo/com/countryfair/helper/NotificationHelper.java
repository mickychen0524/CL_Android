package dev.countryfair.player.playlazlo.com.countryfair.helper;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import dev.countryfair.player.playlazlo.com.countryfair.R;


/**
 * Show notifications
 */

public class NotificationHelper {


    /**
	 * Interface to handle dialog buttons click
	 */
	public interface OnButtonClick{
		/**
		 * Button clicked
		 * @param id string title id of the clicked button
         */
		void onButtonClick(@StringRes int id);
	}

	/**
	 * Ask a question with two buttons
	 * @param context base context
	 * @param titleId dialog title id
	 * @param messageId dialog message
	 * @param negativeButton negative button string id
	 * @param positiveButton positive button string id
     * @param listener result listener
     */
    public static void ask(Context context, int titleId, int messageId, final int negativeButton, final int positiveButton, final OnButtonClick listener) {
		ask(context,titleId,messageId,negativeButton,positiveButton,0,listener);
    }

	/**
	 * Ask a question with two buttons
	 * @param context base context
	 * @param title dialog title
	 * @param message dialog message
	 * @param negativeButton negative button string id
	 * @param positiveButton positive button string id
	 * @param listener result listener
	 */
	public static void ask(Context context, String title, String message, final int negativeButton, final int positiveButton, final OnButtonClick listener) {
		ask(context,title,message,negativeButton,positiveButton,0,listener);
	}

	public static void ask(Context context, int titleId, String message, final int negativeButton, final int positiveButton, final OnButtonClick listener) {
		if (context==null)
			return;
		ask(context,context.getString(titleId),message,negativeButton,positiveButton,0,listener);
	}

	/**
	 * Ask a question with two buttons
	 * @param context base context
	 * @param titleId dialog title id
	 * @param messageId dialog message
	 * @param negativeButton negative button string id
	 * @param positiveButton positive button string id
	 * @param neutralButton positive button string id
	 * @param listener result listener
	 */
	public static void ask(Context context, int titleId, int messageId, final int negativeButton, final int positiveButton, final int neutralButton, final OnButtonClick listener) {
		if (context==null||titleId==0||messageId==0)
			return;
		ask(context,context.getString(titleId),context.getString(messageId),negativeButton,positiveButton,neutralButton,listener);
	}
	/**
	 * Ask a question with two buttons
	 * @param context base context
	 * @param title dialog title
	 * @param message dialog message
	 * @param negativeButton negative button string id
	 * @param positiveButton positive button string id
	 * @param neutralButton positive button string id
	 * @param listener result listener
	 */
	public static void ask(Context context, String title, String message, final int negativeButton, final int positiveButton, final int neutralButton, final OnButtonClick listener) {
		if (context==null||title==null||message==null)
			return;
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setMessage(message);
		if (negativeButton!=0) {
			builder.setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (listener != null)
						listener.onButtonClick(negativeButton);
				}
			});
		}
		if (positiveButton!=0) {
			builder.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (listener != null)
						listener.onButtonClick(positiveButton);
				}
			});
		}
		if (neutralButton!=0) {
			builder.setNeutralButton(neutralButton, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (listener != null)
						listener.onButtonClick(neutralButton);
				}
			});
		}
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if (listener!=null)
					listener.onButtonClick(negativeButton);
			}
		});
		builder.show();

	}

	/**
	 * Show dialog and waits for button click
	 * @param context base context
	 * @param titleId dialog title id
	 * @param messageId dialog message
	 * @param positiveButton positive button string id
	 * @param listener result listener
     */
	public static void ask(Context context, int titleId, int messageId, final int positiveButton, final OnButtonClick listener) {
		ask(context,titleId,messageId,0,positiveButton,0,listener);
	}

	public static void ask(Context context, String title, String message, final int positiveButton, final OnButtonClick listener) {
		ask(context,title,message,0,positiveButton,0,listener);
	}

	/**
	 * Show alert dialog
	 * @param context base context
	 * @param title dialog title
	 * @param msg dialog message
     */
    public static void alert(Context context, @StringRes int title, @StringRes int msg){
		ask(context,title,msg,android.R.string.ok,null);
    }
	/**
	 * Show alert dialog
	 * @param context base context
	 * @param title dialog title
	 * @param msg dialog message
	 */
	public static void alert(Context context, String title, String msg){
		ask(context,title,msg,0,android.R.string.ok,null);
	}
	/**
	 * Show alert dialog
	 * @param context base context
	 * @param titleId dialog title
	 * @param msg dialog message
	 */
	public static void alert(Context context, int titleId, String msg){
		ask(context,context!=null?context.getString(titleId):"Alert",msg,0,android.R.string.ok,null);
	}

	/**
	 * Show toast
	 * @param context base context
	 * @param stringId string resource id with message
     */
    public static void toast(Context context, int stringId) {
        if (context!=null)
            toast(context,context.getString(stringId));
    }

	/**
	 * Show toast
	 * @param context base context
	 * @param msg message to show
     */
	public static void toast(Context context, String msg) {
		if (context!=null)
			Toast.makeText(context, msg,Toast.LENGTH_LONG).show();
	}
	public static void toastS(Context context, String msg) {
		if (context!=null)
			Toast.makeText(context, msg,Toast.LENGTH_SHORT).show();
	}

	/**
	 * Show not yet implemented toast message
	 * @param context base context
	 */
	public static void notYetImplemented(Context context) {
		toast(context, R.string.not_yet_implemented);
	}
	public static void noConnection(Context context) {
		alert(context,R.string.title_no_connection,R.string.msg_no_connection);
	}
}
