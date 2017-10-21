package space.greenraven.android.nabalny.remoting;

import android.os.AsyncTask;
import space.greenraven.android.nabalny.MainActivity;
import space.greenraven.android.nabalny.PropertyHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class VoteResultsService extends AsyncTask <String, Void, VoteStats>{

    public static final String NO_VOTE_ERROR = "novote";
    private final MainActivity mainActivity;

    public VoteResultsService(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    public VoteStats getVoteStats(String email){
        VoteStats ret = new VoteStats();

        try {
            String urlStr = PropertyHelper.get(PropertyHelper.RESULTS_URL)+"?email="+email;
            URL url = new URL(urlStr);
            URLConnection connection = url.openConnection();
            InputStreamReader r = new InputStreamReader(connection.getInputStream());
            BufferedReader br = new BufferedReader(r);
            String line = br.readLine();
            if(NO_VOTE_ERROR.equals(line)){
                ret.setErrors(NO_VOTE_ERROR);
            } else { //TODO better protection is required
                String[] pairs = line.split("\t");
                ret.setLabels(new String[pairs.length]);
                ret.setValues(new Integer[pairs.length]);
                for(int i = 0; i< pairs.length; ++i){
                    String[] kv = pairs[i].split(":");
                    if(kv.length>1){
                        ret.getLabels()[i] = kv[0];
                        ret.getValues()[i] = Integer.parseInt(kv[1]);
                    }
                }
            }
            r.close();
        } catch (Exception e) {
            ret.setErrors(e.getMessage());
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    protected VoteStats doInBackground(String... strings) {
        return getVoteStats(strings[0]);
    }

    @Override
    protected void onPostExecute(VoteStats voteStats) {
        super.onPostExecute(voteStats);
        mainActivity.refreshChart(voteStats);
    }
}
