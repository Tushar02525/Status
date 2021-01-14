package com.tushar.statussaverandcleaner.cleaner.tabs;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.tushar.statussaverandcleaner.cleaner.CheckRecentRun;
import com.tushar.statussaverandcleaner.cleaner.DataHolder;
import com.tushar.statussaverandcleaner.R;
import com.tushar.statussaverandcleaner.cleaner.MainActivityCleaner;
import com.tushar.statussaverandcleaner.cleaner.adapters.innerAdapeters.InnerDetailsAdapter;
import com.tushar.statussaverandcleaner.cleaner.datas.FileDetails;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;


public class FilesFragment extends Fragment implements InnerDetailsAdapter.OnCheckboxListener {

    private Button button, date, name, size;
    private ImageView no_files;
    private InnerDetailsAdapter innerDetailsAdapter;
    private ArrayList<FileDetails> innerDataList = new ArrayList<>();
    private ArrayList<FileDetails> filesToDelete = new ArrayList<>();
    private ProgressDialog progressDialog;
    private CheckBox selectall;
    private boolean flag_d = true, flag_n = true, flag_s = true;
    @SuppressWarnings("FieldCanBeLocal")
    private SharedPreferences settings = null;
    @SuppressWarnings("FieldCanBeLocal")
    private SharedPreferences.Editor editor = null;
    private final static String TAG = "MainActivity";
    public final static String PREFS = "PrefsFile";

    @SuppressWarnings("FieldCanBeLocal")


    public static FilesFragment newInstance(String category, String path) {
        FilesFragment filesFragment = new FilesFragment();
        Bundle bundle = new Bundle();
        bundle.putString("path", path);
        bundle.putString("category", category);
        filesFragment.setArguments(bundle);
        return filesFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView;
        String category = null;
        String path = null;

        if (getArguments() != null) {
            path = getArguments().getString("path");
            category = getArguments().getString("category");
        } else {
            Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            if (getActivity() != null)
                getActivity().finish();
        }

        if (category == null) {
            if (getActivity() != null)
                getActivity().finish();
            return null;
        }

        RecyclerView recyclerView;
        int IMAGES = 1;
        int VIDEOS = 2;
        int AUDIOS = 3;
        int FILE = 4;
        int VOICE = 6;
        switch (category) {
            default:
            case DataHolder.WALLPAPER:
            case DataHolder.IMAGE:
                rootView = inflater.inflate(R.layout.image_activity_cleaner, container, false);
                recyclerView = rootView.findViewById(R.id.recycler_view);
                recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                innerDetailsAdapter = new InnerDetailsAdapter(IMAGES, getActivity(), innerDataList, this);
                break;
            case DataHolder.GIF:
            case DataHolder.VIDEO:
                rootView = inflater.inflate(R.layout.image_activity_cleaner, container, false);
                recyclerView = rootView.findViewById(R.id.recycler_view);
                recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                innerDetailsAdapter = new InnerDetailsAdapter(VIDEOS, getActivity(), innerDataList, this);
                break;
            case DataHolder.VOICE:
                rootView = inflater.inflate(R.layout.doc_activity_cleaner, container, false);
                recyclerView = rootView.findViewById(R.id.recycler_view);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                innerDetailsAdapter = new InnerDetailsAdapter(VOICE, getActivity(), innerDataList, this);
                break;
            case DataHolder.AUDIO:
                rootView = inflater.inflate(R.layout.doc_activity_cleaner, container, false);
                recyclerView = rootView.findViewById(R.id.recycler_view);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                innerDetailsAdapter = new InnerDetailsAdapter(AUDIOS, getActivity(), innerDataList, this);
                break;
            case DataHolder.NONDEFAULT:
            case DataHolder.DOCUMENT:
                rootView = inflater.inflate(R.layout.doc_activity_cleaner, container, false);
                recyclerView = rootView.findViewById(R.id.recycler_view);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                innerDetailsAdapter = new InnerDetailsAdapter(FILE, getActivity(), innerDataList, this);
                break;
        }

        //Switch toggle = rootView.findViewById(R.id.switch1);


        button = rootView.findViewById(R.id.delete);
        date = rootView.findViewById(R.id.date);
        name = rootView.findViewById(R.id.name);
        size = rootView.findViewById(R.id.size);
        no_files = rootView.findViewById(R.id.nofiles);
        selectall = rootView.findViewById(R.id.selectall);




// banner ads




        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(innerDetailsAdapter);

        settings = requireContext().getSharedPreferences(PREFS, MODE_PRIVATE);



        // First time running app?
        if (!settings.contains("lastRun"))
            enableNotification(null);
        else
            recordRunTime();

        Log.v(TAG, "Starting CheckRecentRun service...");
        requireContext().startService(new Intent(getContext(), CheckRecentRun.class));



        new FetchFiles(this, category, fileDetails -> {
            if (fileDetails != null && !fileDetails.isEmpty()) {
                innerDataList.addAll(fileDetails);
                innerDetailsAdapter.notifyDataSetChanged();
                progressDialog.dismiss();
                no_files.setVisibility(View.INVISIBLE);
            } else {
                progressDialog.dismiss();
                Log.e("Nofiles", "NO Files Found");
                no_files.setVisibility(View.VISIBLE);
                no_files.setImageResource(R.drawable.file);
            }
        }).execute(path);

        date.setOnClickListener(v -> {
            if (!flag_n || !flag_s) {
                flag_n = true;
                flag_s = true;
                name.setTextColor(Color.parseColor("#FF161616"));
                size.setTextColor(Color.parseColor("#FF161616"));
            }
            if (flag_d) {
//                    Toast.makeText(getContext(), "sorted", Toast.LENGTH_SHORT).show();
                flag_d = false;
                date.setTextColor(Color.parseColor("#C103A9F4"));
                Collections.sort(innerDataList, (o1, o2) -> -o1.getMod().compareTo(o2.getMod()));
                innerDetailsAdapter.notifyDataSetChanged();
            } else {
//                    Toast.makeText(getContext(), "Unsorted", Toast.LENGTH_SHORT).show();
                flag_d = true;
                date.setTextColor(Color.parseColor("#FF161616"));
                Collections.sort(innerDataList, (o1, o2) -> o1.getMod().compareTo(o2.getMod()));
                Log.e("State", "Disabled");
                innerDetailsAdapter.notifyDataSetChanged();
            }
        });

        name.setOnClickListener(v -> {
            if (!flag_d || !flag_s) {
                flag_d = true;
                flag_s = true;
                date.setTextColor(Color.parseColor("#FF161616"));
                size.setTextColor(Color.parseColor("#FF161616"));
            }
            if (flag_n) {
//                    Toast.makeText(getContext(), "sorted", Toast.LENGTH_SHORT).show();
                flag_n = false;
                name.setTextColor(Color.parseColor("#C103A9F4"));
                Collections.sort(innerDataList, (o1, o2) -> o1.getName().compareTo(o2.getName()));
                Log.e("State", "Toggled");
                innerDetailsAdapter.notifyDataSetChanged();
            } else {
//                    Toast.makeText(getContext(), "Unsorted", Toast.LENGTH_SHORT).show();
                flag_n = true;
                name.setTextColor(Color.parseColor("#FF161616"));
                Collections.sort(innerDataList, (o1, o2) -> -o1.getName().compareTo(o2.getName()));
                Log.e("State", "Disabled");
                innerDetailsAdapter.notifyDataSetChanged();
            }
        });
        size.setOnClickListener(v -> {
            if (!flag_d || !flag_n) {
                flag_d = true;
                flag_n = true;
                date.setTextColor(Color.parseColor("#FF161616"));
                name.setTextColor(Color.parseColor("#FF161616"));
            }
            if (flag_s) {
//                    Toast.makeText(getContext(), "sorted", Toast.LENGTH_SHORT).show();
                flag_s = false;
                size.setTextColor(Color.parseColor("#C103A9F4"));
                Collections.sort(innerDataList, (o1, o2) -> -o1.getS().compareTo(o2.getS()));
                Log.e("State", "Toggled");
                innerDetailsAdapter.notifyDataSetChanged();
            } else {
//                    Toast.makeText(getContext(), "Unsorted", Toast.LENGTH_SHORT).show();
                flag_s = true;
                size.setTextColor(Color.parseColor("#FF161616"));
                Collections.sort(innerDataList, (o1, o2) -> o1.getS().compareTo(o2.getS()));
                Log.e("State", "Disabled");
                innerDetailsAdapter.notifyDataSetChanged();
            }
        });

        selectall.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                for (int i = 0; i < innerDataList.size(); i++) {
                    innerDataList.get(i).setSelected(true);
                }
                innerDetailsAdapter.notifyDataSetChanged();
            } else {
                for (int i = 0; i < innerDataList.size(); i++) {
                    innerDataList.get(i).setSelected(false);
                }
                innerDetailsAdapter.notifyDataSetChanged();
            }
        });
        button.setOnClickListener(v -> {
            if (!filesToDelete.isEmpty()) {
                new AlertDialog.Builder(getContext())
                        .setMessage("Are you sure you want to delete selected files?")
                        .setCancelable(true)
                        .setPositiveButton("YES", (dialog, which) -> {
                            int success = -1;
                            ArrayList<FileDetails> deletedFiles = new ArrayList<>();

                            for (FileDetails details : filesToDelete) {
                                File file = new File(details.getPath());
                                if (file.exists()) {
                                    if (file.delete()) {
                                        deletedFiles.add(details);
                                        if (success == 0) {
                                            return;
                                        }
                                        success = 1;
                                    } else {
                                        Log.e("TEST", "" + file.getName() + " delete failed");
                                        success = 0;
                                    }
                                } else {
                                    Log.e("TEST", "" + file.getName() + " doesn't exists");
                                    success = 0;
                                }
                            }

                            filesToDelete.clear();

                            for (FileDetails deletedFile : deletedFiles) {
                                innerDataList.remove(deletedFile);
                            }
                            innerDetailsAdapter.notifyDataSetChanged();
                            if (selectall.isChecked()) {
                                Intent intent = new Intent(getContext(), MainActivityCleaner.class);
                                startActivity(intent);
                            }
                            if (success == 0) {
                                Toast.makeText(getContext(), "Couldn't delete some files", Toast.LENGTH_SHORT).show();
                            } else if (success == 1) {
                                Toast.makeText(getContext(), "Deleted successfully", Toast.LENGTH_SHORT).show();
                            }
                            button.setText(R.string.delete_items_blank);
                            button.setTextColor(Color.parseColor("#A9A9A9"));
                        })
                        .setNegativeButton("NO", (dialog, which) -> dialog.dismiss()).create().show();
            }
        });

        return rootView;
    }

    private static long getFileSize(File file) {
        if (file != null && file.isFile()) {
            return file.length();
        }
        return 0;
    }

    public void recordRunTime() {
        editor = settings.edit();
        editor.putLong("lastRun", System.currentTimeMillis());
        editor.apply();
    }

    public void enableNotification(View v) {
        editor = settings.edit();
        editor.putLong("lastRun", System.currentTimeMillis());
        editor.putBoolean("enabled", true);
        editor.apply();
        Log.v(TAG, "Notifications enabled");
    }
    @Override
    public void oncheckboxlistener(View view, List<FileDetails> updatedFiles) {

        filesToDelete.clear();

        for (FileDetails details : updatedFiles) {
            if (details.isSelected()) {
                filesToDelete.add(details);
            }
        }

        if (filesToDelete.size() > 0) {

            long totalFileSize = 0;

            for (FileDetails details : filesToDelete) {
                File file = new File(details.getPath());
                totalFileSize += file.length();
            }

            String size = Formatter.formatShortFileSize(getActivity(), totalFileSize);
            button.setText("Delete Selected Items (" + size + ")");
            button.setTextColor(Color.parseColor("#C103A9F4"));
        } else {
            button.setText(R.string.delete_items_blank);
            button.setTextColor(Color.parseColor("#A9A9A9"));
        }
    }

    private static class FetchFiles extends AsyncTask<String, Void, Object> {

        private String category;
        private OnFilesFetched onFilesFetched;
        private WeakReference<FilesFragment> filesFragmentWeakReference;

        FetchFiles(FilesFragment filesFragment, String category, OnFilesFetched onFilesFetched) {
            filesFragmentWeakReference = new WeakReference<>(filesFragment);
            this.category = category;
            this.onFilesFetched = onFilesFetched;
        }

        @Override
        protected void onPreExecute() {
            // display a progress dialog for good user experiance
            filesFragmentWeakReference.get().progressDialog = new ProgressDialog(filesFragmentWeakReference.get().getContext());
            filesFragmentWeakReference.get().progressDialog.setMessage("Please Wait");
            filesFragmentWeakReference.get().progressDialog.setCancelable(false);
            if (!filesFragmentWeakReference.get().progressDialog.isShowing()) {
                filesFragmentWeakReference.get().progressDialog.show();
            }
        }

        @Override
        protected Object doInBackground(String... strings) {

            String path = strings[0];
            String extension;
            ArrayList<FileDetails> files = new ArrayList<>();

            if (path != null) {
                File directory = new File(path);
                File[] results = directory.listFiles();

                if (results != null) {
                    //Still verify if the file is an image in whatsapp preferred format(jpg)
                    for (final File file : results) {
                        switch (category) {
                            case DataHolder.IMAGE:
                            case DataHolder.WALLPAPER:
                                if (file.isFile()) {
                                    if (!file.getName().endsWith(".nomedia")) {
                                        FileDetails fileDetails = new FileDetails();
                                        fileDetails.setName(file.getName());
                                        fileDetails.setPath(file.getPath());
                                        fileDetails.setMod(file.lastModified());
                                        //String mime = "*/*";
                                      //  File a = new File(file.getPath());
                                     //   Uri uri = FileProvider.getUriForFile(Objects.requireNonNull(filesFragmentWeakReference.get().getContext()),
                                             //   Objects.requireNonNull(filesFragmentWeakReference.get().getContext()).getApplicationContext().getPackageName() +
                                                //        ".my.package.name.provider", a);
                                       // MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
//                                        if (mimeTypeMap.hasExtension(
//                                                MimeTypeMap.getFileExtensionFromUrl(uri.toString()))) {
//
//                                        }

                                        fileDetails.setSize(Formatter.formatShortFileSize(filesFragmentWeakReference.get().
                                                        getContext(),
                                                getFileSize(file)));
                                        fileDetails.setS(getFileSize(file));
                                        files.add(fileDetails);
                                    }
                                } else if (file.isDirectory()) {
                                    if (!file.getName().equals("Sent")) {
                                        File[] res = file.listFiles();
                                        if (res != null) {
                                            for (File re : res) {
                                                if (!re.getName().endsWith(".nomedia")) {
                                                    FileDetails fileDetails = new FileDetails();
                                                    fileDetails.setName(re.getName());
                                                    fileDetails.setPath(re.getPath());
                                                    fileDetails.setMod(re.lastModified());
                                                    //extension = FilenameUtils.getExtension((re.getPath()));

    //                                                String mime = "*/*";
                                                   // File a = new File(re.getPath());
                                                   // Uri uri = FileProvider.getUriForFile(Objects.requireNonNull(filesFragmentWeakReference.get().getContext()),
                                                    //        Objects.requireNonNull(filesFragmentWeakReference.get().getContext()).getApplicationContext().getPackageName() +
                                                     //               ".my.package.name.provider", a);
                                                 //   MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
    //                                                if (mimeTypeMap.hasExtension(
    //                                                        MimeTypeMap.getFileExtensionFromUrl(uri.toString()))) {
    //
    //                                                    mime = Objects.requireNonNull(mimeTypeMap.getMimeTypeFromExtension(
    //                                                            MimeTypeMap.getFileExtensionFromUrl(uri.toString()))).split("/")[0];
    //                                                }
                                                    fileDetails.setSize(Formatter.formatShortFileSize(filesFragmentWeakReference.get().getContext(),
                                                            getFileSize(re)));
                                                    fileDetails.setS(getFileSize(re));
                                                    Log.e("size", String.valueOf(getFileSize(re)));
                                                    files.add(fileDetails);
                                                }
                                            }
                                        }
                                    }
                                }
                                break;
                            case DataHolder.NONDEFAULT:
                            case DataHolder.DOCUMENT:
                                //Check if it is a file or a folder
                                if (file.isFile()) {
                                    if (!file.getName().endsWith(".nomedia")) {
                                        FileDetails fileDetails = new FileDetails();
                                        fileDetails.setName(file.getName());
                                        fileDetails.setPath(file.getPath());
                                        fileDetails.setImage(R.drawable.ic_doc);
                                        extension = FilenameUtils.getExtension((file.getPath()));
                                        fileDetails.setMod(file.lastModified());
                                        String mime = "*/*";
                                        File a = new File(file.getPath());
                                        Uri uri = FileProvider.getUriForFile(filesFragmentWeakReference.get().requireContext(),
                                                filesFragmentWeakReference.get().requireContext().getApplicationContext().getPackageName() +
                                                        ".my.package.name.provider", a);
                                        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                                        if (mimeTypeMap.hasExtension(
                                                MimeTypeMap.getFileExtensionFromUrl(uri.toString()))) {

                                            mime = Objects.requireNonNull(mimeTypeMap.getMimeTypeFromExtension(
                                                    MimeTypeMap.getFileExtensionFromUrl(uri.toString()))).split("/")[0];
                                        }
                                        switch (mime) {
                                            case "image":
                                                fileDetails.setColor(R.color.green);
                                                fileDetails.setExt(extension);
                                                break;
                                            case "video":
                                                fileDetails.setColor(R.color.blue);
                                                fileDetails.setExt(extension);
                                                break;
                                            case "text":
                                                fileDetails.setColor(R.color.orange);
                                                fileDetails.setExt(extension);
                                                break;
                                            case "application":
                                                fileDetails.setColor(R.color.red);
                                                fileDetails.setExt(extension);
                                                break;
                                            case "audio":
                                                fileDetails.setColor(R.color.purple);
                                                fileDetails.setExt(extension);
                                                break;
                                            default:
                                                fileDetails.setColor(R.color.gray);
                                                fileDetails.setImage(R.drawable.ic_unkown);
                                                break;
                                        }

                                        fileDetails.setSize(Formatter.formatShortFileSize(filesFragmentWeakReference.get().getContext(), getFileSize(file)));
                                        fileDetails.setS(getFileSize(file));
                                        files.add(fileDetails);
                                    }
                                } else if (file.isDirectory()){
                                    if (!file.getName().equals("Sent")) {
                                        File[] res = file.listFiles();
                                        if (res != null) {
                                            for (File re : res) {
                                                if (!re.getName().endsWith(".nomedia")) {
                                                    FileDetails fileDetails = new FileDetails();
                                                    fileDetails.setName(re.getName());
                                                    fileDetails.setPath(re.getPath());
                                                    fileDetails.setMod(re.lastModified());
                                                    fileDetails.setImage(R.drawable.ic_doc);
                                                    extension = FilenameUtils.getExtension((re.getPath()));

                                                    String mime = "*/*";
                                                    File a = new File(re.getPath());
                                                    Uri uri = FileProvider.getUriForFile(filesFragmentWeakReference.get().requireContext(),
                                                            filesFragmentWeakReference.get().requireContext().getApplicationContext().getPackageName() +
                                                                    ".my.package.name.provider", a);
                                                    MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                                                    if (mimeTypeMap.hasExtension(
                                                            MimeTypeMap.getFileExtensionFromUrl(uri.toString()))) {

                                                        mime = Objects.requireNonNull(mimeTypeMap.getMimeTypeFromExtension(
                                                                MimeTypeMap.getFileExtensionFromUrl(uri.toString()))).split("/")[0];
                                                    }
                                                    switch (mime) {
                                                        case "image":
                                                            fileDetails.setColor(R.color.green);
                                                            fileDetails.setExt(extension);
                                                            break;
                                                        case "video":
                                                            fileDetails.setColor(R.color.blue);
                                                            fileDetails.setExt(extension);
                                                            break;
                                                        case "text":
                                                            fileDetails.setColor(R.color.orange);
                                                            fileDetails.setExt(extension);
                                                            break;
                                                        case "application":
                                                            fileDetails.setColor(R.color.red);
                                                            fileDetails.setExt(extension);
                                                            break;
                                                        case "audio":
                                                            fileDetails.setColor(R.color.purple);
                                                            fileDetails.setExt(extension);
                                                            break;
                                                        default:
                                                            fileDetails.setColor(R.color.gray);
                                                            fileDetails.setImage(R.drawable.ic_unkown);
                                                            break;
                                                    }
                                                    fileDetails.setSize(Formatter.formatShortFileSize(filesFragmentWeakReference.get().getContext(),
                                                            re.isDirectory() ?  FileUtils.sizeOfDirectory(re) : getFileSize(re)));
                                                    fileDetails.setS(re.isDirectory() ?  FileUtils.sizeOfDirectory(re) : getFileSize(re));
                                                    files.add(fileDetails);
                                                }
                                            }
                                        }
                                    }
                                }

                                break;
                            case DataHolder.VIDEO:
                                if (file.isFile()) {
                                    if (!file.getName().endsWith(".nomedia")) {
                                        FileDetails fileDetails = new FileDetails();
                                        fileDetails.setName(file.getName());
                                        fileDetails.setPath(file.getPath());
                                        fileDetails.setImage(R.drawable.video_play);
                                        fileDetails.setColor(R.color.transparent);
                                        fileDetails.setMod(file.lastModified());
                                        fileDetails.setSize(Formatter.formatShortFileSize(filesFragmentWeakReference.get()
                                                        .getContext(),
                                                getFileSize(file)));
                                        fileDetails.setS(getFileSize(file));
                                        files.add(fileDetails);
                                    }
                                } else if (file.isDirectory()) {
                                    if (!file.getName().equals("Sent")) {
                                        File[] res = file.listFiles();
                                        if (res != null) {
                                            for (File re : res) {
                                                if (!re.getName().endsWith(".nomedia")) {
                                                    FileDetails fileDetails = new FileDetails();
                                                    fileDetails.setName(re.getName());
                                                    fileDetails.setPath(re.getPath());
                                                    fileDetails.setMod(re.lastModified());
                                                    fileDetails.setImage(R.drawable.video_play);
                                                    fileDetails.setColor(R.color.transparent);
    //                                                extension = FilenameUtils.getExtension((re.getPath()));
    //
    //                                                String mime = "*/*";
    //                                                File a = new File(re.getPath());
    //                                                Uri uri = FileProvider.getUriForFile(Objects.requireNonNull(filesFragmentWeakReference.get().getContext()),
    //                                                        Objects.requireNonNull(filesFragmentWeakReference.get().getContext()).getApplicationContext().getPackageName() +
    //                                                                ".my.package.name.provider", a);
    //                                                MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
    //                                                if (mimeTypeMap.hasExtension(
    //                                                        MimeTypeMap.getFileExtensionFromUrl(uri.toString()))) {
    //
    //                                                    mime = Objects.requireNonNull(mimeTypeMap.getMimeTypeFromExtension(
    //                                                            MimeTypeMap.getFileExtensionFromUrl(uri.toString()))).split("/")[0];
    //                                                }
                                                    fileDetails.setSize(Formatter.formatShortFileSize(filesFragmentWeakReference.get().getContext(),
                                                            getFileSize(re)));
                                                    fileDetails.setS(getFileSize(re));
                                                    Log.e("size", String.valueOf(getFileSize(re)));
                                                    files.add(fileDetails);
                                                }
                                            }
                                        }
                                    }
                                }
                                break;
                            case DataHolder.AUDIO:
                                if (file.isFile()) {
                                    if (!file.getName().endsWith(".nomedia")) {
                                        FileDetails fileDetails = new FileDetails();
                                        fileDetails.setName(file.getName());
                                        fileDetails.setPath(file.getPath());
                                        fileDetails.setMod(file.lastModified());
                                        fileDetails.setImage(R.drawable.ic_audio);
                                        extension = FilenameUtils.getExtension((file.getPath()));
                                        String mime = "*/*";
                                        File a = new File(file.getPath());
                                        Uri uri = FileProvider.getUriForFile(filesFragmentWeakReference.get().requireContext(),
                                                filesFragmentWeakReference.get().requireContext().getApplicationContext().getPackageName() +
                                                        ".my.package.name.provider", a);
                                        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                                        if (mimeTypeMap.hasExtension(
                                                MimeTypeMap.getFileExtensionFromUrl(uri.toString()))) {

                                            mime = Objects.requireNonNull(mimeTypeMap.getMimeTypeFromExtension(
                                                    MimeTypeMap.getFileExtensionFromUrl(uri.toString()))).split("/")[0];
                                        }
                                        switch (mime) {
                                            case "image":
                                                fileDetails.setColor(R.color.green);
                                                fileDetails.setExt(extension);
                                                break;
                                            case "video":
                                                fileDetails.setColor(R.color.blue);
                                                fileDetails.setExt(extension);
                                                break;
                                            case "text":
                                                fileDetails.setColor(R.color.orange);
                                                fileDetails.setExt(extension);
                                                break;
                                            case "application":
                                                fileDetails.setColor(R.color.red);
                                                fileDetails.setExt(extension);
                                                break;
                                            case "audio":
                                                fileDetails.setColor(R.color.purple);
                                                fileDetails.setExt(extension);
                                                break;
                                            default:
                                                fileDetails.setColor(R.color.gray);
                                                fileDetails.setImage(R.drawable.ic_unkown);
                                                break;
                                        }
                                        fileDetails.setSize(Formatter.formatShortFileSize(filesFragmentWeakReference.get()
                                                        .getContext(),
                                                getFileSize(file)));
                                        fileDetails.setS(getFileSize(file));
                                        files.add(fileDetails);
                                    }
                                } else if (file.isDirectory()){
                                    if (!file.getName().equals("Sent")) {
                                        File[] res = file.listFiles();
                                        if (res != null) {
                                            for (File re : res) {
                                                if (!re.getName().endsWith(".nomedia")) {
                                                    FileDetails fileDetails = new FileDetails();
                                                    fileDetails.setName(re.getName());
                                                    fileDetails.setPath(re.getPath());
                                                    fileDetails.setMod(re.lastModified());
                                                    fileDetails.setImage(R.drawable.ic_audio);
                                                    extension = FilenameUtils.getExtension((re.getPath()));

                                                    String mime = "*/*";
                                                    File a = new File(re.getPath());
                                                    Uri uri = FileProvider.getUriForFile(filesFragmentWeakReference.get().requireContext(),
                                                            filesFragmentWeakReference.get().requireContext().getApplicationContext().getPackageName() +
                                                                    ".my.package.name.provider", a);
                                                    MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                                                    if (mimeTypeMap.hasExtension(
                                                            MimeTypeMap.getFileExtensionFromUrl(uri.toString()))) {

                                                        mime = Objects.requireNonNull(mimeTypeMap.getMimeTypeFromExtension(
                                                                MimeTypeMap.getFileExtensionFromUrl(uri.toString()))).split("/")[0];
                                                    }
                                                    switch (mime) {
                                                        case "image":
                                                            fileDetails.setColor(R.color.green);
                                                            fileDetails.setExt(extension);
                                                            break;
                                                        case "video":
                                                            fileDetails.setColor(R.color.blue);
                                                            fileDetails.setExt(extension);
                                                            break;
                                                        case "text":
                                                            fileDetails.setColor(R.color.orange);
                                                            fileDetails.setExt(extension);
                                                            break;
                                                        case "application":
                                                            fileDetails.setColor(R.color.red);
                                                            fileDetails.setExt(extension);
                                                            break;
                                                        case "audio":
                                                            fileDetails.setColor(R.color.purple);
                                                            fileDetails.setExt(extension);
                                                            break;
                                                        default:
                                                            fileDetails.setColor(R.color.gray);
                                                            fileDetails.setImage(R.drawable.ic_unkown);
                                                            break;
                                                    }
                                                    fileDetails.setSize(Formatter.formatShortFileSize(filesFragmentWeakReference.get().getContext(),
                                                            getFileSize(re)));
                                                    fileDetails.setS(getFileSize(re));
                                                    files.add(fileDetails);
                                                }
                                            }
                                        }
                                    }
                                }
                                break;
                            case DataHolder.GIF:
                                if (file.isFile()) {
                                    if (!file.getName().endsWith(".nomedia")) {
                                        FileDetails fileDetails = new FileDetails();
                                        fileDetails.setName(file.getName());
                                        fileDetails.setPath(file.getPath());
                                        fileDetails.setMod(file.lastModified());
                                        fileDetails.setImage(R.drawable.video_play);
                                        fileDetails.setColor(R.color.transparent);
                                        fileDetails.setSize(Formatter.formatShortFileSize(filesFragmentWeakReference.get().getContext(),
                                                getFileSize(file)));
                                        fileDetails.setS(getFileSize(file));
                                        files.add(fileDetails);
                                    }
                                } else if (file.isDirectory()) {
                                    if (!file.getName().equals("Sent")) {
                                        File[] res = file.listFiles();
                                        if (res != null) {
                                            for (File re : res) {
                                                if (!re.getName().endsWith(".nomedia")) {
                                                    FileDetails fileDetails = new FileDetails();
                                                    fileDetails.setName(re.getName());
                                                    fileDetails.setPath(re.getPath());
                                                    fileDetails.setMod(re.lastModified());
                                                    fileDetails.setImage(R.drawable.video_play);
                                                    fileDetails.setColor(R.color.transparent);

    //                                                String mime = "*/*";
    //                                                File a = new File(re.getPath());
    //                                                Uri uri = FileProvider.getUriForFile(Objects.requireNonNull(filesFragmentWeakReference.get().getContext()),
    //                                                        Objects.requireNonNull(filesFragmentWeakReference.get().getContext()).getApplicationContext().getPackageName() +
    //                                                                ".my.package.name.provider", a);
    //                                                MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
    //                                                if (mimeTypeMap.hasExtension(
    //                                                        MimeTypeMap.getFileExtensionFromUrl(uri.toString()))) {
    //
    //                                                    mime = Objects.requireNonNull(mimeTypeMap.getMimeTypeFromExtension(
    //                                                            MimeTypeMap.getFileExtensionFromUrl(uri.toString()))).split("/")[0];
    //                                                }
                                                    fileDetails.setSize(Formatter.formatShortFileSize(filesFragmentWeakReference.get().getContext(),
                                                            getFileSize(re)));
                                                    fileDetails.setS(getFileSize(re));
                                                    Log.e("size", String.valueOf(getFileSize(re)));
                                                    files.add(fileDetails);
                                                }
                                            }
                                        }
                                    }
                                }
                                break;
                            case DataHolder.VOICE:
                                if (file.isDirectory()) {
                                    if (!file.getName().equals("Sent")) {
                                        File[] res = file.listFiles();
                                        if (res != null) {
                                            for (File re : res) {
                                                if (!re.getName().endsWith(".nomedia")) {
                                                    FileDetails fileDetails = new FileDetails();
                                                    fileDetails.setName(re.getName());
                                                    fileDetails.setPath(re.getPath());
                                                    fileDetails.setMod(re.lastModified());
                                                    fileDetails.setImage(R.drawable.voice);
                                                    fileDetails.setColor(R.color.orange);
                                                    extension = FilenameUtils.getExtension((re.getPath()));

                                                    String mime = "*/*";
                                                    File a = new File(re.getPath());
                                                    Uri uri = FileProvider.getUriForFile(filesFragmentWeakReference.get().requireContext(),
                                                            filesFragmentWeakReference.get().requireContext().getApplicationContext().getPackageName() +
                                                                    ".my.package.name.provider", a);
                                                    MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                                                    if (mimeTypeMap.hasExtension(
                                                            MimeTypeMap.getFileExtensionFromUrl(uri.toString()))) {

                                                        mime = Objects.requireNonNull(mimeTypeMap.getMimeTypeFromExtension(
                                                                MimeTypeMap.getFileExtensionFromUrl(uri.toString()))).split("/")[0];
                                                    }
                                                    switch (mime) {
                                                        case "image":
                                                            fileDetails.setColor(R.color.green);
                                                            fileDetails.setExt(extension);
                                                            break;
                                                        case "video":
                                                            fileDetails.setColor(R.color.blue);
                                                            fileDetails.setExt(extension);
                                                            break;
                                                        case "text":
                                                            fileDetails.setColor(R.color.orange);
                                                            fileDetails.setExt(extension);
                                                            break;
                                                        case "application":
                                                            fileDetails.setColor(R.color.red);
                                                            fileDetails.setExt(extension);
                                                            break;
                                                        case "audio":
                                                            fileDetails.setColor(R.color.purple);
                                                            fileDetails.setExt(extension);
                                                            break;
                                                        default:
                                                            fileDetails.setColor(R.color.gray);
                                                            fileDetails.setImage(R.drawable.ic_unkown);
                                                            break;
                                                    }
                                                    fileDetails.setSize(Formatter.formatShortFileSize(filesFragmentWeakReference.get().getContext(),
                                                            getFileSize(re)));
                                                    fileDetails.setS(getFileSize(re));
                                                    files.add(fileDetails);
                                                }
                                            }
                                        }
                                    }
                                } else if (file.isFile()) {
                                    if (!file.getName().endsWith(".nomedia")) {
                                        FileDetails fileDetails = new FileDetails();
                                        fileDetails.setName(file.getName());
                                        fileDetails.setPath(file.getPath());
                                        fileDetails.setMod(file.lastModified());
                                        fileDetails.setImage(R.drawable.voice);
                                        fileDetails.setColor(R.color.orange);
                                        extension = FilenameUtils.getExtension((file.getPath()));
                                        String mime = "*/*";
                                        File a = new File(file.getPath());
                                        Uri uri = FileProvider.getUriForFile(filesFragmentWeakReference.get().requireContext(),
                                                filesFragmentWeakReference.get().requireContext().getApplicationContext().getPackageName() +
                                                        ".my.package.name.provider", a);
                                        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                                        if (mimeTypeMap.hasExtension(
                                                MimeTypeMap.getFileExtensionFromUrl(uri.toString()))) {

                                            mime = Objects.requireNonNull(mimeTypeMap.getMimeTypeFromExtension(
                                                    MimeTypeMap.getFileExtensionFromUrl(uri.toString()))).split("/")[0];
                                        }
                                        switch (mime) {
                                            case "image":
                                                fileDetails.setColor(R.color.green);
                                                fileDetails.setExt(extension);
                                                break;
                                            case "video":
                                                fileDetails.setColor(R.color.blue);
                                                fileDetails.setExt(extension);
                                                break;
                                            case "text":
                                                fileDetails.setColor(R.color.orange);
                                                fileDetails.setExt(extension);
                                                break;
                                            case "application":
                                                fileDetails.setColor(R.color.red);
                                                fileDetails.setExt(extension);
                                                break;
                                            case "audio":
                                                fileDetails.setColor(R.color.purple);
                                                fileDetails.setExt(extension);
                                                break;
                                            default:
                                                fileDetails.setColor(R.color.gray);
                                                fileDetails.setImage(R.drawable.ic_unkown);
                                                break;
                                        }
                                        fileDetails.setSize(Formatter.formatShortFileSize(filesFragmentWeakReference.get()
                                                        .getContext(),
                                                getFileSize(file)));
                                        fileDetails.setS(getFileSize(file));
                                        files.add(fileDetails);
                                    }
                                }
                                break;
                            case DataHolder.STATUS:
                                if (file.isFile()) {
                                    if (!file.getName().endsWith(".nomedia")) {
                                        FileDetails fileDetails = new FileDetails();
                                        fileDetails.setName(file.getName());
                                        fileDetails.setPath(file.getPath());
                                        fileDetails.setMod(file.lastModified());
//                                        String mime = "*/*";
//                                        File a = new File(file.getPath());
//                                        Uri uri = FileProvider.getUriForFile(Objects.requireNonNull(filesFragmentWeakReference.get().getContext()),
//                                                Objects.requireNonNull(filesFragmentWeakReference.get().getContext()).getApplicationContext().getPackageName() +
//                                                        ".my.package.name.provider", a);
//                                        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
//                                        if (mimeTypeMap.hasExtension(
//                                                MimeTypeMap.getFileExtensionFromUrl(uri.toString()))) {
//
//                                            mime = Objects.requireNonNull(mimeTypeMap.getMimeTypeFromExtension(
//                                                    MimeTypeMap.getFileExtensionFromUrl(uri.toString()))).split("/")[0];
//                                        }

                                        fileDetails.setSize(Formatter.formatShortFileSize(filesFragmentWeakReference.get().
                                                        getContext(),
                                                getFileSize(file)));
                                        fileDetails.setS(getFileSize(file));
                                        files.add(fileDetails);
                                    }
                                }

                        }
                    }
                } else {
                    Log.e("Files", "No files found in " + directory.getName());
                }
                Collections.sort(files, (o1, o2) -> {
                    filesFragmentWeakReference.get().flag_d = false;
                    return -o1.getMod().compareTo(o2.getMod());
                });
            } else {
                Log.e("Files", "Path is empty");
            }


            return files;
        }


        @Override
        protected void onPostExecute(Object o) {
            List<FileDetails> files = (List<FileDetails>) o;
            if (onFilesFetched != null) {
                onFilesFetched.onFilesFetched(files);
            }
        }

        public interface OnFilesFetched {
            void onFilesFetched(List<FileDetails> fileDetails);
        }

    }


}
