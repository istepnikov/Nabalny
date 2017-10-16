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
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
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
import space.greenraven.android.nabalny.util.IabHelper;
import space.greenraven.android.nabalny.util.Inventory;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final int RC_SIGN_IN = 9001;

    private GoogleApiClient mGoogleApiClient;

    AccessTokenHelper tokenHelper = new AccessTokenHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        initBilling();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }

        mHelper.handleActivityResult(requestCode, resultCode, data);
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
                try {
                    mHelper.launchPurchaseFlow(this, ITEM_SKU,
                            10001, mPurchaseFinishedListener, tokenHelper.getToken());
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                }
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

    IabHelper mHelper;
    static final String ITEM_SKU = "space.green-raven.nabalny.vote";

    void initBilling(){

        //get the resource from the google console
        String base64EncodedPublicKey = getString(R.string.nabalny_public_key);

        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup((result) -> {
            if(!result.isSuccess()){
                Log.d("Billing:", "setup failed:"+result);
            } else {
                Log.d( "Billing", "setup successful");
            }
        });
    }



    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener =
            (result, purchase) -> {
                if(result.isFailure()){
                    Log.e("Billing:", "Purchase Error" + result.getMessage());
                    return;
                } else if (purchase.getSku().equals(ITEM_SKU)){
                    //TODO here is a real purchase happens
                    ((TextView)findViewById(R.id.textView)).setText("Bought Successfully");
                    //consume item
                    try {
                        //this is where consumptions starts
                        consumeItem();
                    } catch (IabHelper.IabAsyncInProgressException e) {
                        e.printStackTrace();
                    }
                }
            };
    //this shitty code is required due to google's design flaw
    public void consumeItem() throws IabHelper.IabAsyncInProgressException {
        mHelper.queryInventoryAsync(mReceiverInventoryListener);
    }


    IabHelper.QueryInventoryFinishedListener mReceiverInventoryListener =
            (result, inventory) ->{
            if(result.isFailure()){
                //Handle failures
            } else {
                consumeFinishedItem(inventory);
            }
        };
    //this code is required due to the google's design flaw
    public  void consumeFinishedItem(Inventory inventory){
        try {
            mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU), mConsumeFinishedListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            (purchase, result) -> {
                if(result.isSuccess()){
                    ((TextView)findViewById(R.id.textView)).setText("Bought successfully");
                } else {
                    //handle error
                }
            };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mHelper != null) try {
            mHelper.dispose();
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
        mHelper = null;
    }
}
