package com.tushar.statussaverandcleaner;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.tushar.statussaverandcleaner.cleaner.MainActivityCleaner;
import com.tushar.statussaverandcleaner.fragments.WAFragment;

import java.io.File;


public class MainActivity extends AppCompatActivity {
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });


        adView = findViewById(R.id.banner_main);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);


        adView.setAdListener(new AdListener() {


            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {

                AdRequest adRequest = new AdRequest.Builder().build();
                adView.loadAd(adRequest);

                super.onAdFailedToLoad(loadAdError);
            }
        });


        Fragment fragment = new WAFragment();
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.framelayout, fragment).commit();


        stash();


    }

    @Override
    public void onBackPressed() {


        super.onBackPressed();
    }


    public void stash() {
        File file = new File(new StringBuffer().append(new StringBuffer().append(Environment.getExternalStorageDirectory()).append(File.separator).toString()).append("WhatsApp/Media/.Statuses").toString());
        if (!file.isDirectory()) {
            file.mkdirs();
        }

        new File(new StringBuffer().append(new StringBuffer().append(Environment.getExternalStorageDirectory()).append(File.separator).toString()).append("StorySaver/").toString()).mkdirs();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {


            case R.id.cleaner:

                startActivity(new Intent(MainActivity.this, MainActivityCleaner.class));
                break;

            case R.id.privacy_policy:

                Uri uri=Uri.parse("https://statussaverandcleaner.blogspot.com/p/privacy-policy.html");
                Intent intent=new
                        Intent(Intent.ACTION_VIEW,uri);
                startActivity(intent);

                break;

            case R.id.feedback:

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));

                break;

            case R.id.share:
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_TEXT, "Try this new *" + getString(R.string.app_name) + "* App : https://play.google.com/store/apps/details?id=" + getPackageName());
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                startActivity(Intent.createChooser(sharingIntent, "Share using"));

                break;
        }

        return super.onOptionsItemSelected(item);
    }
}

