package com.tochange.yang;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TestTreeViewActivity extends Activity
{
    @Override
    protected void onStop()
    {
        super.onStop();
        mLoadTreeTask.exit();
    }

    private SharedPreferences mSharedPreferences;

    private LoadTreeTask mLoadTreeTask;

    private TextView mResultTextView;

    private ProgressDialog mDataGetProgressDialog;

    private String mDirToBeCopyInAsset = "datadir";

    private log log = new log();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        log.intLog("TestActivity", true);

        final LinearLayout treeLayout = (LinearLayout) findViewById(R.id.tree);
        final Button closeBtn = (Button) findViewById(R.id.cancel_btn);
        final Button openBtn = (Button) findViewById(R.id.confirm_btn);
        mResultTextView = (TextView) findViewById(R.id.t2);

        mSharedPreferences = getSharedPreferences("treenode",
                Context.MODE_PRIVATE);
        mResultTextView.setText(mSharedPreferences.getString("pcs_name",
                "default treenode's name"));

        final DatabaseStruct dbStruct = new DatabaseStruct();
        if (initialData(dbStruct))
        {
            File f = new File(dbStruct.dbPath);
            if (!f.exists())
                new LoadDataToAppSpaceTask(this).execute(mDirToBeCopyInAsset);

            closeBtn.setEnabled(false);
            closeBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v)
                {
                    treeLayout.setVisibility(View.GONE);
                    mSharedPreferences.edit()
                            .putString("pcs_name", mLoadTreeTask.getPcsName())
                            .commit();
                    mSharedPreferences.edit()
                            .putString("pcs_id", mLoadTreeTask.getPcsID())
                            .commit();
                    mSharedPreferences.edit()
                            .putString("pcs_code", mLoadTreeTask.getPcsCode())
                            .commit();
                    closeBtn.setEnabled(false);
                    openBtn.setEnabled(true);
                    mResultTextView.setText(mLoadTreeTask.getPcsName());
                }
            });

            openBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v)
                {
                    long t1 = System.currentTimeMillis();
                    treeLayout.setVisibility(View.VISIBLE);
                    if (mLoadTreeTask == null)
                    {
                        mLoadTreeTask = new LoadTreeTask(TestTreeViewActivity.this,
                                dbStruct, treeLayout, mSharedPreferences
                                        .getString("pcs_id", "unknow id"),
                                mSharedPreferences.getString("pcs_name",
                                        "unknow name"));
                        mLoadTreeTask.execute();
                    }
                    else
                        mLoadTreeTask
                                .setTreeListViewPosition(mSharedPreferences
                                        .getString("pcs_id", "unknow id"));
                    closeBtn.setEnabled(true);
                    openBtn.setEnabled(false);
                    log.e("all time =" + (System.currentTimeMillis() - t1));
                }
            });
        }
    }

    private boolean initialData(DatabaseStruct dbStruct)
    {
        dbStruct.fieldName = "name";
        dbStruct.fieldRemark = "remark";
        dbStruct.fieldID = "ID";
        dbStruct.fieldIDParent = "parentID";
        dbStruct.tableName = "TreeViewData";
        dbStruct.rootFieldID = 1;
        dbStruct.dbPath = getFilesDir().getAbsolutePath().replace("files",
                mDirToBeCopyInAsset)
                + "/" + dbStruct.tableName + ".db";// maybe problem
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.activity_test, menu);
        return true;
    }

    private class LoadDataToAppSpaceTask extends
            AsyncTask<String, Integer, String>
    {
        Context mContext;

        public LoadDataToAppSpaceTask(Context contex)
        {
            mContext = contex;
        }

        @Override
        protected void onPreExecute()
        {
            if (null == mDataGetProgressDialog
                    || !mDataGetProgressDialog.isShowing())
            {
                mDataGetProgressDialog = ProgressDialog.show(mContext, null,
                        "loading data to app space..", false, false);
            }
        }

        @Override
        protected String doInBackground(String... params)
        {
            copyDbPic(mContext, params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            if (mDataGetProgressDialog != null)
            {
                mDataGetProgressDialog.dismiss();
            }
        }

        public void copyDbPic(Context contex, String path)
        {
            try
            {
                String str[] = contex.getAssets().list(path);
                if (str.length > 0)
                {// dir
                    File file = new File(contex.getFilesDir().getAbsolutePath()
                            .replace("files", path));
                    file.mkdirs();
                    for (String string : str)
                    {
                        path = path + "/" + string;
                        copyDbPic(contex, path);
                        path = path.substring(0, path.lastIndexOf('/'));
                    }
                }
                else
                {// files
                    InputStream is = contex.getAssets().open(path);
                    FileOutputStream fos = new FileOutputStream(new File(contex
                            .getFilesDir().getAbsolutePath()
                            .replace("files", path)));
                    byte[] buffer = new byte[1024];
                    int count = 0;
                    while (true)
                    {
                        count++;
                        int len = is.read(buffer);
                        if (len == -1)
                        {
                            break;
                        }
                        fos.write(buffer, 0, len);
                    }
                    is.close();
                    fos.close();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
