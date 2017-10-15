package space.greenraven.android.nabalny;

import android.accounts.Account;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.Scopes;

import java.io.IOException;

import static android.content.ContentValues.TAG;

/**
 * Created by istepnikov on 13.10.2017.
 */
public class AccessTokenHelper extends AsyncTask<Account,Void,String> {

    private String token = null;

    public String getToken() {
        return token;
    }

    public MainActivity getActivity() {
        return activity;
    }

    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }

    MainActivity activity;

    String scopes = "oauth2:"
            + Scopes.PLUS_LOGIN
            + " "
            + Scopes.PROFILE;

    public AccessTokenHelper(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(Account... params) {
        Account account = params[0];
//        String scopes = "oauth2:profile email";
        String token = null;
        try {
            token = GoogleAuthUtil.getToken(getActivity().getApplicationContext(), account, scopes);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } catch (UserRecoverableAuthException e) {
            //startActivityForResult(e.getIntent(), REQ_SIGN_IN_REQUIRED);
        } catch (GoogleAuthException e) {
            Log.e(TAG, e.getMessage());
        }
        //token can be checked on
        //https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=
        return token;
    }

    @Override
    protected void onPostExecute(String token) {
        super.onPostExecute(token);
        Log.i("Token Value: ", token);
    }
}
