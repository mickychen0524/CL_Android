package dev.countryfair.player.playlazlo.com.countryfair.helper;

/**
 * Created by Mycom on 9/19/16.
 */

/**
 * Callback to use with token request. User implements this callback to use
 * result in their context.
 *
 * @param <T>
 */
public interface AuthenticationCallBackMy<T> {

    /**
     * This will have the token info.
     *
     * @param result returns <T>
     */
    void onSuccess(T result);

    /**
     * Sends error information. This can be user related error or server error.
     * Cancellation error is AuthenticationCancelError.
     *
     * @param exc   return {@link Exception}
     */
    void onError(Exception exc);
}
