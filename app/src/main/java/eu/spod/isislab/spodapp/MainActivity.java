package eu.spod.isislab.spodapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import eu.spod.isislab.spodapp.fragments.AgoraRoomsListFragment;
import eu.spod.isislab.spodapp.fragments.CocreationRoomsListFragment;
import eu.spod.isislab.spodapp.fragments.LoginFragment;
import eu.spod.isislab.spodapp.services.SpodLocationServices;
import eu.spod.isislab.spodapp.utils.NetworkChannel;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final int INTERNET_PERMISSION = 0;
    public static final int ACCESS_COARSE_LOCATION_PERMISSION = 1;
    public static final int WRITE_EXTERNAL_STORAGE_PERMISSION = 2;
    public static final int READ_EXTERNAL_STORAGE_PERMISSION  = 3;

    public DrawerLayout drawer;
    public Toolbar toolbar;

    private boolean internetPermissionOk = false;

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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            //GPS PERMISSION
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                startService(new Intent(this, SpodLocationServices.class));
            }else{
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, ACCESS_COARSE_LOCATION_PERMISSION);
            }
            //INTERNET PERMISSION
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.INTERNET)) {
                getSupportFragmentManager().beginTransaction().add(R.id.container, new LoginFragment()).addToBackStack("login").commit();
            }else{
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.INTERNET}, INTERNET_PERMISSION);
            }
            //SDCARD WRITE PERMISSION
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_PERMISSION);
            }
            //SDCARD READ PERMISSION
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION);
            }
        }else{
            startService(new Intent(this, SpodLocationServices.class));
            getSupportFragmentManager().beginTransaction().add(R.id.container, new LoginFragment()).addToBackStack("login").commit();
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
            if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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
            case R.id.action_settings:
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case INTERNET_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission concessa
                    internetPermissionOk = true;
                } else {
                    // permission negata
                    Snackbar.make(this.findViewById(R.id.container), getString(R.string.main_activity_notwork_connection_off), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                return;
            }
            case ACCESS_COARSE_LOCATION_PERMISSION:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startService(new Intent(this, SpodLocationServices.class));

                } else {
                    Snackbar.make(this.findViewById(R.id.container), getString(R.string.main_activity_gps_off), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
            case WRITE_EXTERNAL_STORAGE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Snackbar.make(this.findViewById(R.id.container), getString(R.string.main_activity_storage_permission_off), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                break;
            case READ_EXTERNAL_STORAGE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Snackbar.make(this.findViewById(R.id.container), getString(R.string.main_activity_storage_permission_off), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(internetPermissionOk){
            getSupportFragmentManager().beginTransaction().replace(R.id.container, new LoginFragment()).addToBackStack("login").commit();
            internetPermissionOk = false;
        }
    }
}
