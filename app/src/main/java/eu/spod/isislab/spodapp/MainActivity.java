package eu.spod.isislab.spodapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import eu.spod.isislab.spodapp.fragments.settings.SettingsFragment;
import eu.spod.isislab.spodapp.services.AuthorizationService;
import eu.spod.isislab.spodapp.fragments.agora.AgoraRoomsListFragment;
import eu.spod.isislab.spodapp.fragments.cocreation.CocreationRoomsListFragment;
import eu.spod.isislab.spodapp.services.SpodLocationService;
import eu.spod.isislab.spodapp.utils.Consts;
import eu.spod.isislab.spodapp.utils.CustomDialog;
import eu.spod.isislab.spodapp.utils.NetworkChannel;
import eu.spod.isislab.spodapp.utils.SpodNotificationManager;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public DrawerLayout drawer;
    public Toolbar toolbar;

    //private boolean internetPermissionOk = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        this.checkPermissions();

    }

    public void handleNotificationIntent(){

        String fragment_tag = "agora_room_list";
        Fragment fragment   = new AgoraRoomsListFragment();

        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                if(key.equals(SpodNotificationManager.NOTIFICATION_INTENT_EXTRA_BODY)){
                    try {
                        JSONObject notificationBody = new JSONObject(value.toString());
                        switch(notificationBody.getString("plugin")){
                            case Consts.AGORA_PLUGIN:
                                switch(notificationBody.getString("action")){
                                    case Consts.AGORA_ACTION_NEW_ROOM:
                                        break;
                                    case Consts.AGORA_ACTION_COMMENT:
                                        break;
                                    case Consts.AGORA_ACTION_MENTION:
                                        break;
                                    case Consts.AGORA_ACTION_REPLAY:
                                        break;
                                }
                                break;
                            case Consts.COCREATION_PLUGIN:
                                fragment = new CocreationRoomsListFragment();
                                switch(notificationBody.getString("action")){
                                    case Consts.COCREATION_ACTION_NEW_ROOM:
                                        break;
                                    case Consts.COCREATION_ACTION_COMMENT:
                                        break;
                                    case Consts.COCREATION_ACTION_INVITE:
                                        break;
                                    case Consts.COCREATION_ACTION_DATASET_PUBLISHED:
                                        break;
                                }
                                break;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.d("MAIN-NOTIFICATION", "Key: " + key + " Value: " + value);
            }
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(fragment_tag)
                .commit();
    }

    public void checkPermissions(){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)               != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)  != PackageManager.PERMISSION_GRANTED)
        {
            boolean requestPermissions = false;

            //GPS PERMISSION
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                startService(new Intent(this, SpodLocationService.class));
            }else{
                requestPermissions = true;
            }
            //INTERNET PERMISSION
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.INTERNET)) {
                NetworkChannel.getInstance().init(this);
            }else{
                requestPermissions = true;
            }
            //SDCARD WRITE PERMISSION
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                requestPermissions = true;
            }
            //SDCARD READ PERMISSION
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                requestPermissions = true;
            }

            if(requestPermissions){
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.INTERNET,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                        },
                        Consts.ASK_APPLICAITON_PERMISSIONS);
            }
        }else{
            NetworkChannel.getInstance().init(this);
            startService(new Intent(this, SpodLocationService.class));
            AuthorizationService.getInstance().init(this);
            AuthorizationService.getInstance().enablePostAuthorizationFlows();
        }
    }

    public void setToolbarTitle(String title){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            int index = getSupportFragmentManager().getBackStackEntryCount() - 1;
            if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                if(getSupportFragmentManager().getBackStackEntryAt(index).getName().contains("cocreation_data_room")      ||
                   getSupportFragmentManager().getBackStackEntryAt(index).getName().contains("cocreation_knowledge_room") ||
                   getSupportFragmentManager().getBackStackEntryAt(index).getName().contains("cocreation_members_fragment"))
                    getSupportFragmentManager().popBackStack();
                getSupportFragmentManager().popBackStack();
            } else {
                this.finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case android.R.id.home:
                if(drawer.isDrawerOpen(Gravity.LEFT)) {
                    drawer.closeDrawer(Gravity.LEFT);
                }else{
                    drawer.openDrawer(Gravity.LEFT);
                }
                break;
            case R.id.action_info:

                CustomDialog dialog = new CustomDialog();
                dialog.setTitle("Version");
                dialog.setBody("Current version " + BuildConfig.VERSION_NAME);
                dialog.show(getSupportFragmentManager(), "custom_dialog_fragment");

                break;
            case R.id.action_settings:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, new SettingsFragment())
                        .addToBackStack("settings_fragment")
                        .commit();

                break;
            case R.id.sign_out:
                AuthorizationService.getInstance().signOut();
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch(id){
            case R.id.nav_whatsnew:
                break;
            case R.id.nav_agora:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, new AgoraRoomsListFragment())
                        .addToBackStack("agora_room_list")
                        .commit();
                break;
            case R.id.nav_cocreation:
                getSupportFragmentManager()
                   .beginTransaction()
                   .replace(R.id.container, new CocreationRoomsListFragment())
                   .addToBackStack("cocoreation_room_list")
                   .commit();
                break;
            case R.id.nav_share:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void openPermissions(){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode){
            case Consts.ASK_APPLICAITON_PERMISSIONS:
                if(grantResults.length > 0){
                    if(grantResults[Consts.INTERNET_PERMISSION]               == PackageManager.PERMISSION_GRANTED  &&
                       grantResults[Consts.ACCESS_COARSE_LOCATION_PERMISSION] == PackageManager.PERMISSION_GRANTED  &&
                       grantResults[Consts.WRITE_EXTERNAL_STORAGE_PERMISSION] == PackageManager.PERMISSION_GRANTED  &&
                       grantResults[Consts.READ_EXTERNAL_STORAGE_PERMISSION]  == PackageManager.PERMISSION_GRANTED)
                    {
                        NetworkChannel.getInstance().init(this);
                        startService(new Intent(this, SpodLocationService.class));
                        AuthorizationService.getInstance().init(this);
                        AuthorizationService.getInstance().enablePostAuthorizationFlows();
                    }else{
                        openPermissions();
                    }
                }

                break;
        }
    }

}
