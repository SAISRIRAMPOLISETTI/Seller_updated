package com.bizzmark.seller.sellerwithoutlogin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bizzmark.seller.sellerwithoutlogin.db.StoreBO;
import com.bizzmark.seller.sellerwithoutlogin.login.Login;
import com.bizzmark.seller.sellerwithoutlogin.wifidirect_new.DeviceDetailFragment;
import com.bizzmark.seller.sellerwithoutlogin.wifidirect_new.DeviceListFragment;
import com.bizzmark.seller.sellerwithoutlogin.wifidirect_new.broadcastreceiver.WifiBroadCastReceiver;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class WifiDirectReceive extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, WifiP2pManager.ChannelListener, DeviceListFragment.DeviceActionListener {

    final Context context = this;
    public ImageView imgmenu;
    public ImageButton buttonlogout;

    private FirebaseAuth firebaseAuth;

    public static final String TAG = "smartpointseller";
    private boolean isWifiP2pEnabled = false;
    private WifiP2pManager manager;
    private boolean retryChannel = false;
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;

    private Button btnRefresh;
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled){
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_direct_receive);

        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() == null){
            finish();
            startActivity(new Intent(this, Login.class));

        }

        // for enable wifi
        WifiManager wifi = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifi.setWifiEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        imgmenu=(ImageView)findViewById(R.id.imgmenu);
        imgmenu.setOnClickListener(this);

        buttonlogout=(ImageButton) findViewById(R.id.action_logout);
        buttonlogout.setOnClickListener(this);

        btnRefresh=(Button)findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(this);

      // add necessary intent values to be matched.

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel =  manager.initialize(this,getMainLooper(),null);
        discoverPeers();


    }


    //peers discovery

    public void discoverPeers(){
        if (!isWifiP2pEnabled){
            Toast.makeText(this, "Enable P2P from action bar button above or system settings",
                    Toast.LENGTH_SHORT).show();
        }

        DeviceListFragment deviceListFragment = (DeviceListFragment) getFragmentManager()
                .findFragmentById(R.id.frag_list);
        deviceListFragment.onInitiateDiscovery();
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(WifiDirectReceive.this, "Discovery initiated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(WifiDirectReceive.this, "Discovery failed :" + reason,
                        Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        receiver = new WifiBroadCastReceiver(manager,channel,this);
        registerReceiver(receiver,intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public void resetData(){
        DeviceListFragment fragmentList = (DeviceListFragment)
                getFragmentManager().findFragmentById(R.id.frag_list);
        DeviceDetailFragment fragmentDetails = (DeviceDetailFragment)
                getFragmentManager().findFragmentById(R.id.frag_detals);
        if (fragmentList != null){
            fragmentList.cleerPeers();
        }
        if (fragmentDetails != null){
            fragmentDetails.resetViews();
        }
    }
    @Override
    public void showDetails(WifiP2pDevice device) {

        DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detals);
        fragment.showDetails(device);

    }

    @Override
    public void connect(WifiP2pConfig config) {


        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(WifiDirectReceive.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void disconnect() {

        final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detals);
        fragment.resetViews();
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
            }

            @Override
            public void onSuccess() {
                fragment.getView().setVisibility(View.GONE);
            }

        });

    }

    @Override
    public void onChannelDisconnected() {

        // we will try once more
        if (manager != null && !retryChannel) {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            resetData();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this,
                    "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void cancelDisconnectt() {

          /*
         * A cancel abort request by user. Disconnect i.e. removeGroup if
         * already connected. Else, request WifiP2pManager to abort the ongoing
         * request
         */
        if (manager != null) {
            final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                    .findFragmentById(R.id.frag_list);
            if (fragment.getDevice() == null
                    || fragment.getDevice().status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE
                    || fragment.getDevice().status == WifiP2pDevice.INVITED) {

                manager.cancelConnect(channel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(WifiDirectReceive.this, "Aborting connection",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(WifiDirectReceive.this,
                                "Connect abort request failed. Reason Code: " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

    }

    /*clearing remembered groups */

    private void deletePersistentGroups(){

        try {
            Method[] methods = WifiP2pManager.class.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("deletePersistentGroup")) {
                    // Delete any persistent group
                    for (int netid = 0; netid < 32; netid++) {
                        methods[i].invoke(manager, channel, netid, null);
                    }
                }
            }
            Toast.makeText(getApplicationContext(),"Groups Removed Successfully ",Toast.LENGTH_LONG).show();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

@Override
public void onBackPressed() {
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
        drawer.closeDrawer(GravityCompat.START);
    } else {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        //StopConnect();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater menuInflater = getMenuInflater();

        getMenuInflater().inflate(R.menu.wifi_direct_receive, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_faq) {
            Intent i = new Intent(getApplicationContext(), FAQ.class);
            startActivity(i);
        } else if (id == R.id.nav_terms_conditions) {
            Intent i = new Intent(getApplication(), Terms.class);
            startActivity(i);
        } else if (id == R.id.nav_privacy_policy) {
            Intent i=new Intent(getApplicationContext(),PrivacyPolicy.class);
            startActivity(i);
        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {
            shareButtionFunctionality();
        } else if (id == R.id.nav_contact_us) {
            contactus();
        }else if (id == R.id.nav_exit){
            exit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {


        if (v == btnRefresh){
            Animation rotation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.buttonrotate);
            rotation.start();
            btnRefresh.startAnimation(rotation);
            deletePersistentGroups();
            discoverPeers();
        }
        if (v == imgmenu){
            slidemenu();
        }

        if(v == buttonlogout){
            firebaseAuth.signOut();
            finish();
            startActivity(new Intent(this,Login.class));
        }

    }

    public  void shareButtionFunctionality(){

        try {

            ApplicationInfo app=getApplicationContext().getApplicationInfo();
            String filepath = app.sourceDir;
            Intent intent=new Intent(Intent.ACTION_SEND);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(filepath)));
            startActivity(Intent.createChooser(intent,"Share app"));
            Toast.makeText(getApplicationContext(),"Share the Seller App . . .", Toast.LENGTH_LONG).show();

        }catch (Exception e){

            e.printStackTrace();
        }
    }
    //method for slide menu button
    public void slidemenu(){
        DrawerLayout slider=(DrawerLayout)findViewById(R.id.drawer_layout);
        slider.openDrawer(Gravity.LEFT);
    }

    // method for exit the app
    public void exit(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        //StopConnect();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
    // method for contactus
    private void contactus() {

        try {

            AlertDialog.Builder builder1 = new AlertDialog.Builder(context);

            builder1.setMessage("www.bizzmark.in\n PH:  ");
            builder1.setCancelable(true);
            builder1.setIcon(R.drawable.ic_launcher);
            builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            AlertDialog alert11 = builder1.create();
            alert11.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
