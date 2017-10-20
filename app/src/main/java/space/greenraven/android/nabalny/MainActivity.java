package space.greenraven.android.nabalny;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener,
        BillingProcessor.IBillingHandler{

    private static final int RC_SIGN_IN = 9001;
//    private String publicKey;
    private static final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjIhQ8vDMCxjR/phCRjktuoL7zIHsvmv7CaQ49rJvXgQuKd10qqLKRK3vtMqVIYI/bauTxtJCkUy23GUZLEviW4ql8VexzNZmuPkSuFqjU9dqsBYH+Y4QZP9jhR6qyi6S8rfueXisn96kdnvwDSD9+k+9f+1VlrsKQPeERwt3po1ehVAadTYKt/7cUmW3vLGs4igCOvVxcY01QDBtFuee/IlpbJy28TyVVCIgw1QpuKwHBJfSazHoMbOPNDHCrc9dmcdmcxhAHrELdXS5r5fs0JuXNzjcalRqy21S1Qz/RMh4R2H3opVqwttamemOaxjUUcHCz1v3AWiJx5ZqEBsc+QIDAQAB";

    private GoogleApiClient mGoogleApiClient;

    AccessTokenHelper tokenHelper = new AccessTokenHelper(this);

    private void showSnackBarMessage(String message){
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        Snackbar.make(fab, message, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        publicKey = getString(R.string.nabalny_public_key);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //add click listeners on the buttons
        findViewById(R.id.button_yes).setOnClickListener(this);
        findViewById(R.id.button_no).setOnClickListener(this);
        findViewById(R.id.button_abstain).setOnClickListener(this);

        //Initialize GSO
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(Scopes.PLUS_LOGIN), new Scope(Scopes.PROFILE))
                .requestServerAuthCode("741993329764-llhf3o5ebc9brcgumdrqsbictn8s54p4.apps.googleusercontent.com", false)
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addOnConnectionFailedListener(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //Sign In
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);

        //Init billing
        Log.i("Billing public key",publicKey);
        bp = new BillingProcessor(this, publicKey, this);
        if(!bp.isInitialized()){
            bp.initialize();
        }
//       Log("Billing", "Services available:"+BillingProcessor.isIabServiceAvailable(getBaseContext()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }

    }

    private void handleSignInResult(GoogleSignInResult result) {
        System.out.println("Hanle Result");
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();

            System.out.println("Successful");

            ((TextView)findViewById(R.id.textView)).setText(acct.getDisplayName()+", "+getString(R.string.you_vote));

            tokenHelper.execute(acct.getAccount());

        } else {
            // Signed out, show unauthenticated UI. Console.
            System.out.println("Unsuccessful");
            Snackbar.make(findViewById(R.id.button_yes), R.string.unsuccessful_auth_string, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //TODO Implement onConnectionFailed
    }

    private void refreshChart(){
        PieChart chart = (PieChart) findViewById(R.id.chart);
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(25, "Yes"));
        entries.add(new PieEntry(75, "No"));
        PieDataSet pieDataSet = new PieDataSet(entries, "");
        pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        PieData pieData = new PieData(pieDataSet);
        chart.setData(pieData);
        //chart.setCenterText("Results distribution for " + displayName);
        chart.invalidate();

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        refreshChart();
    }


    @Override
    public void onClick(View v) {
        Log.d("Click","onClick");
        switch (v.getId()){
            case R.id.button_yes:

                Log.i("Button:", "Yes");
                bp.purchase(this, ITEM_SKU, "Yes");
                break;
            case R.id.button_no:
                Log.i("Button:", "No");
                break;
            case R.id.button_abstain:
                Log.i("Button:", "Abstain");
                break;
        }
    }

    //Billing part. Should be refactored, probably
    static final String ITEM_SKU = "space.greenraven.nabalny.vote";
//    static final String ITEM_SKU = "android.test.purchased";

    BillingProcessor bp = null;


    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
        Log.i("Billing:", "Purchased");
        bp.consumePurchase(productId);
    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {
        Log.e("Billing:", "Billing Error:"+errorCode+" "+error);
        showSnackBarMessage("Error code:"+errorCode);
    }

    @Override
    public void onBillingInitialized() {

    }

    @Override
    protected void onDestroy() {

        if(bp!=null){
            bp.release();
        }
        super.onDestroy();

    }

    //TODO: clean-up all google demo crap from the project
}
