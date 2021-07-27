package com.krant.daniil.pet.gpxrallyparser;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import com.krant.daniil.pet.gpxrallyparser.databinding.ActivityMainBinding;
import com.krant.daniil.pet.gpxrallyparser.ui.main.fragment.SectionsPagerAdapter;
import com.krant.daniil.pet.gpxrallyparser.ui.main.fragment.list.ListItemClicked;
import com.krant.daniil.pet.gpxrallyparser.ui.main.fragment.list.ListViewItemHolder;
import com.krant.daniil.pet.gpxrallyparser.ui.main.fragment.map.ZoomToMarker;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mBinding;
    private ViewPager mViewPager;
    private TabLayout mTabs;
    private FrameLayout mOpenFileHintLayout;
    private FloatingActionButton mFab;
    private ShowMap mShowMapGoToMarker;
    private static ZoomToMarker mZoomToMarker;
    private boolean mIsRedrawActivity = true;

    private static final int PICKFILE_RESULT_CODE = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mShowMapGoToMarker = new ShowMap();
        GPXDataRoutine.setContext(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIsRedrawActivity) {
            mBinding = ActivityMainBinding.inflate(getLayoutInflater());
            setContentView(mBinding.getRoot());

            mFab = mBinding.fab;
            mViewPager = mBinding.viewPager;
            mTabs = mBinding.tabs;
            mOpenFileHintLayout = mBinding.openHintLayout;
            Button chooseFileButton = mBinding.chooseFileButton;
            TextView authorText = mBinding.author;

            chooseFileButton.setOnClickListener(new OpenFileClickListener());
            mFab.setOnClickListener(new OpenFileClickListener());
            authorText.setOnClickListener(view -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.linkedin.com/in/daniilkrant/"));
                startActivity(browserIntent);
            });
            mIsRedrawActivity = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mOpenFileHintLayout.setVisibility(View.GONE);
        if (requestCode == PICKFILE_RESULT_CODE && resultCode == RESULT_OK) {
            Uri filePath = data.getData();
            new ParseTask(filePath).execute();
        } else {
            mIsRedrawActivity = true;
        }
    }

    private boolean parseFile(Uri fileUri) {
        InputStream fileInputStream;
        try {
            fileInputStream = getApplicationContext().getContentResolver().openInputStream(fileUri);
            if (GPXDataRoutine.getInstance().parseGpx(fileInputStream)) {
                return true;
            }
        } catch (FileNotFoundException e) {
            showError(getApplicationContext().getString(R.string.file_not_found));
            e.printStackTrace();
        }
        return false;
    }

    private void showError(String error) {
        Snackbar.make(mBinding.viewPager, error,
                Snackbar.LENGTH_LONG).show();
    }

    private void showUI() {
        mOpenFileHintLayout.setVisibility(View.GONE);
        mFab.setVisibility(View.VISIBLE);
    }

    public static void setZoomToMarker(ZoomToMarker mZoomToMarker) {
        MainActivity.mZoomToMarker = mZoomToMarker;
    }


    class OpenFileClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("*/*");
            chooseFile = Intent.createChooser(chooseFile, "Choose a file");
            startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
        }
    }

    class ParseTask extends AsyncTask<Void, Void, Boolean> {
        Uri mFilePath;
        ProgressDialog mProgress;
        SectionsPagerAdapter mSectionsPagerAdapter;

        public ParseTask(Uri filePath) {
            mFilePath = filePath;
            mProgress = new ProgressDialog(MainActivity.this, R.style.AppCompatAlertDialogStyle);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgress.setTitle(getApplicationContext().getString(R.string.loading_title));
            mProgress.setMessage(getApplicationContext().getString(R.string.loading_text));
            mProgress.setCancelable(false);
            mProgress.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean res = parseFile(mFilePath);
            if (res) {
                mSectionsPagerAdapter = new SectionsPagerAdapter(
                        MainActivity.this, getSupportFragmentManager());
            }
            return res;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                mViewPager.setAdapter(mSectionsPagerAdapter);
                mTabs.setupWithViewPager(mViewPager);
                ListViewItemHolder.setListItemClicked(mShowMapGoToMarker);
                showUI();
            } else {
                showError(getApplicationContext().getString(R.string.file_not_parsed));
            }
            mProgress.cancel();
        }
    }

    class ShowMap implements ListItemClicked {

        @Override
        public void itemClicked(int position) {
            TabLayout.Tab mapTab = mTabs.getTabAt(1);
            mTabs.selectTab(mapTab);
            if (mZoomToMarker != null) {
                mZoomToMarker.zoomToMarker(position);
            }
        }
    }


}