package space.greenraven.android.nabalny.remoting;

import android.os.AsyncTask;
import space.greenraven.android.nabalny.MainActivity;
import space.greenraven.android.nabalny.PropertyHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class VoteProcessingService extends AsyncTask<String, Void, String> {
    public static final String OK_RESULT = "OK";
    private final MainActivity mainActivity;

    public VoteProcessingService(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    /**
     @param strings  - accessToken, productId, payload (yes, no, abstain)
     @return productId in the case of success, otherwise error description
     */
    @Override
    protected String doInBackground(String... strings) {
        String ret = strings[1];//productId
        try {
            String urlStr = PropertyHelper.get(PropertyHelper.VOTE_URL)+"?token="+strings[0]+"&product="+
                    strings[1]+"&payload="+strings[2];
            URL url = new URL(urlStr);
            URLConnection connection = url.openConnection();
            InputStreamReader r = new InputStreamReader(connection.getInputStream());
            BufferedReader br = new BufferedReader(r);
            String line = br.readLine();
            if(!OK_RESULT.equals(line)){
                ret = "Error"+line;
            }
        } catch (Exception e) {
            ret = "Error:"+e.getMessage();
            e.printStackTrace();
        }

        return ret;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        mainActivity.onVoteSubmissionResult(s);
    }
}
