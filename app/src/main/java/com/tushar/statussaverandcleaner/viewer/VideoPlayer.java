package com.tushar.statussaverandcleaner.viewer;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.tushar.statussaverandcleaner.HelperMethods;
import com.tushar.statussaverandcleaner.R;

import java.io.File;

public class VideoPlayer extends AppCompatActivity implements EasyVideoCallback{
    HelperMethods helperMethods;
    FloatingActionMenu menu;
    private EasyVideoPlayer player;
    int position=0;
    File f;
    class SomeClass implements View.OnClickListener {
        private final VideoPlayer videoPlayer;
        private final File file;

        class SomeOtherClass implements Runnable {
            private final SomeClass context;
            private final File file;

            SomeOtherClass(SomeClass someClass, File file) {
                context = someClass;
                this.file = file;
            }



            @Override
            public void run() {
                try {
                    HelperMethods.transfer(this.file);
                    Toast.makeText(getApplicationContext(), "Video Saved to Gallery :)", Toast.LENGTH_SHORT).show();

                    if (interstitialAd.isLoaded()) {

                        interstitialAd.show();
                    } else {


                        interstitialAd.loadAd(new AdRequest.Builder().build());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("GridView", new StringBuffer().append("onClick: Error: ").append(e.getMessage()).toString());
                }

            }
        }

        SomeClass(VideoPlayer videoPlayer, File file) {
            this.videoPlayer = videoPlayer;
            this.file = file;
        }

        @Override
        public void onClick(View view) {
            new SomeOtherClass(this, this.file).run();
        }
    }

    @Override
    public void onPaused(EasyVideoPlayer easyVideoPlayer) {
    }

    @Override
    public void onStarted(EasyVideoPlayer easyVideoPlayer) {
    }

    @Override
    public void onSubmit(EasyVideoPlayer easyVideoPlayer, Uri uri) {
    }

    private InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.interstitial_ads));
        interstitialAd.loadAd(new AdRequest.Builder().build());


        helperMethods = new HelperMethods(this);
        this.helperMethods = new HelperMethods(this);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
//        getSupportActionBar().setIcon(R.drawable.business_notif);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().hide();
        Intent intent = getIntent();
        this.f = new File(intent.getExtras().getString("pos"));
        this.position = intent.getExtras().getInt("position");


        this.menu = (FloatingActionMenu) findViewById(R.id.menu);
        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.save);
        FloatingActionButton floatingActionButton2 = (FloatingActionButton) findViewById(R.id.rep);
        FloatingActionButton floatingActionButton3 = (FloatingActionButton) findViewById(R.id.dlt);
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("deleteFab", true)) {
            floatingActionButton3.setVisibility(View.VISIBLE);
        } else {
            floatingActionButton3.setVisibility(View.GONE);
        }
        floatingActionButton.setOnClickListener(downloadMediaItem(this.f));
        floatingActionButton2.setOnClickListener(new View.OnClickListener() {


            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View view) {
                Parcelable uriForFile;
                Intent intent;
                if (Build.VERSION.SDK_INT >= 24) {
                    uriForFile = FileProvider.getUriForFile(getApplicationContext(), new StringBuffer().append(getApplicationContext().getPackageName()).append(".provider").toString(), f);
                    try {
                        intent = new Intent("android.intent.action.SEND");
                        intent.setType("*/*");
                        intent.setPackage("com.whatsapp");
                        intent.putExtra("android.intent.extra.STREAM", uriForFile);
                        intent.addFlags(Intent.EXTRA_DOCK_STATE_DESK);
                        startActivity(intent);
                        startActivity(intent);
                        return;
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getApplicationContext(), "WhatsApp Not Found on this Phone :(", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                uriForFile = Uri.parse(new StringBuffer().append("file://").append(f.getAbsolutePath()).toString());
                try {
                    intent = new Intent("android.intent.action.SEND");
                    intent.setType("*/*");
                    intent.setPackage("com.whatsapp");
                    intent.putExtra("android.intent.extra.STREAM", uriForFile);
                    startActivity(intent);
                } catch (ActivityNotFoundException e2) {
                    Toast.makeText(getApplicationContext(), "WhatsApp Not Found on this Phone :(", 0).show();
                }
            }
        });
        floatingActionButton3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(this.this$0);
//                builder.setMessage("Sure to Delete this Video?").setNegativeButton("Nope", new DialogInterface.OnClickListener(this) {
//
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//                    }
//                }).setPositiveButton("Delete", new DialogInterface.OnClickListener() {
//
//
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        sendBackData();
//                        Toast.makeText(getApplicationContext(), "Video Deleted", Toast.LENGTH_SHORT).show();
//                    }
//                });
//                builder.create().show();
                sendBackData();
                Toast.makeText(getApplicationContext(), "Video Deleted", Toast.LENGTH_SHORT).show();
            }
        });
        this.player = (EasyVideoPlayer) findViewById(R.id.player);
        this.player.setCallback(this);
        this.player.setSource(Uri.fromFile(this.f));
    }

    public void sendBackData() {
        if (this.f.exists()) {
            this.f.delete();
        }
        Intent intent = new Intent();
        intent.putExtra("pos", this.position);
        setResult(-1, intent);
    }


    public View.OnClickListener downloadMediaItem(File file) {
        return new SomeClass(this, file);
    }


    @Override
    protected void onPause() {
        super.onPause();
        this.player.pause();
    }

    @Override
    public void onPreparing(EasyVideoPlayer easyVideoPlayer) {
        Log.d("EVP-Sample", "onPreparing()");
    }

    @Override
    public void onPrepared(EasyVideoPlayer easyVideoPlayer) {
        Log.d("EVP-Sample", "onPrepared()");
    }

    @Override
    public void onBuffering(int i) {
        Log.d("EVP-Sample", new StringBuffer().append(new StringBuffer().append("onBuffering(): ").append(i).toString()).append("%").toString());
    }

    @Override
    public void onError(EasyVideoPlayer easyVideoPlayer, Exception exception) {
        Log.d("EVP-Sample", new StringBuffer().append("onError(): ").append(exception.getMessage()).toString());
    }

    @Override
    public void onCompletion(EasyVideoPlayer easyVideoPlayer) {
        Log.d("EVP-Sample", "onCompletion()");
    }

    @Override
    public void onRetry(EasyVideoPlayer easyVideoPlayer, Uri uri) {
        Toast.makeText(this, "Retry", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }


}
