package com.tochange.yang;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.tochange.yang.TreeListAdapter.OnTreeCallBack;

public class LoadTreeTask extends AsyncTask<String, Integer, String>
{
    private TreeListAdapter mTreeListAdapter;

    private DbToAdapter mDbToAdapter;

    private TreeNode mRoot;

    private long mStartTime, t1, t2, t3;

    private String mPcsName, mPcsCode, mPcsID, mLastPcsIDInDb;

    private ProgressDialog mDataGetProgressDialog;

    private Context mContext;

    private LinearLayout mContainTreeLayout;

    private DatabaseStruct mDbStruct;

    private ListView mTreeListView;

    private log log = new log();

    public String getPcsName()
    {
        return mPcsName;
    }

    public String getPcsCode()
    {
        return mPcsCode;
    }

    public String getPcsID()
    {
        return mPcsID;
    }

    public void exit()
    {
        mDbToAdapter.colseCursorAndSql(null, null);
    }

    public void setTreeListViewPosition(String id)
    {
        mTreeListView.setSelection(mTreeListAdapter.getTreeListPosition(id));
        // mTreeListAdapter.notifyDataSetChanged();//no need?
    }

    public LoadTreeTask(Context c, DatabaseStruct dbStruct, LinearLayout l,
            String id, String pcsName)
    {
        mStartTime = System.currentTimeMillis();
        mContext = c;
        mDbStruct = dbStruct;
        mContainTreeLayout = l;
        mTreeListView = (ListView) mContainTreeLayout
                .findViewById(R.id.lv_tree);

        if (id.equals("") || id.equals(""))
        {
            mLastPcsIDInDb = DbToAdapter.ROOT_PCSID;
        }
        else
        {
            mLastPcsIDInDb = id;
        }

        log.intLog("LoadTreeTask", true);
    }

    @Override
    protected void onPreExecute()
    {
        if (null == mDataGetProgressDialog
                || !mDataGetProgressDialog.isShowing())
        {
            mDataGetProgressDialog = ProgressDialog.show(mContext, null,
                    "loading data..", false, false);
        }
    }

    private TreeNode getRoot(String LastPcsIDInDb)
    {
        TreeNode root = new TreeNode(null, "default pcsname", -1);
        log.e("t1.1 =" + (System.currentTimeMillis() - mStartTime));
        mDbToAdapter.putParentPcsCodeList(LastPcsIDInDb);
        log.e("t1.2 =" + (System.currentTimeMillis() - mStartTime));
        mDbToAdapter.loadDefaultTree(root);
        log.e("t1.3 =" + (System.currentTimeMillis() - mStartTime));
        return root;
    }

    @Override
    protected String doInBackground(String... params)
    {
        if (mDbToAdapter == null)
            mDbToAdapter = new DbToAdapter(mLastPcsIDInDb, mDbStruct);
        mRoot = getRoot(mLastPcsIDInDb);

        mTreeListAdapter = new TreeListAdapter(mContext, mRoot, true);

        mTreeListAdapter.setOnTreeCallBack(new OnTreeCallBack() {
            @Override
            public boolean onSelectNode(TreeNode node)
            {
                if (node != null)
                {
                    mPcsName = node.getDescription().replaceAll("\\([0-9]+\\)",
                            "");
                    mPcsCode = (String) node.getValueMap().get(
                            DbToAdapter.KEY_PCSCODE);
                    mPcsID = (String) node.getValueMap()
                            .get(DbToAdapter.KEY_ID);
                    // log.e( "setting:" + mPcsCode);
                }
                return true;
            }

            @Override
            public void onNodeExpand(TreeNode node)
            {

                Map<String, Object> mp = node.getValueMap();
                boolean haveLoad = (Boolean) mp.get(DbToAdapter.KEY_LOAD);
                // level > 1 means haven't preload
                if (!haveLoad && node.getLevel() > 1)
                {
//                    mp.put(DbToAdapter.KEY_LOAD, true);
//                    node.setValueMap(mp);

                    // load direct line only
                    // String id = (String) mp.get(CobwebDeal.KEY_ID);
                    // ((CobwebDeal) mDbToAdapter)
                    // .addChildNode(node, id, true);

                    // load collateral also thus it can be clicked
                    TreeNode parent = node.getParent();
                    List<TreeNode> childs = parent.getChildren();
                    if (childs != null)
                    {
                        int size = childs.size();
                        for (int i = 0; i < size; ++i)
                        {
                            TreeNode tn = childs.get(i);
                            mp = tn.getValueMap();
                            String idd = (String) mp.get(DbToAdapter.KEY_ID);
                            if (!(Boolean) mp.get(DbToAdapter.KEY_LOAD))
                            {
                                mDbToAdapter.addChildNode(null, tn, idd, true);
                                // mp.put("load", true);
                                tn.setValueMap(mp);
                            }
                        }
                        mp = parent.getValueMap();
                        mp.put(DbToAdapter.KEY_LOAD, true);
//                        log.e("set true=----1----------------------------" + parent.getDescription());
                        parent.setValueMap(mp);
                    }
                }
            }

            @Override
            public void onNodeExpandOrNot(TreeNode node)
            {
                if (node.getExpanded())
                {
                    Map<String, Object> mp = node.getValueMap();
                    boolean hasLoaded = (Boolean) mp.get(DbToAdapter.KEY_LOAD);

//                    log.e("!hasLoaded =" +  node.getDescription()  + " " + !hasLoaded);

                    if (!hasLoaded)
                    {

                        List<TreeNode> childList = node.getChildren();

//                        log.e("!null" + (childList == null));

                        if (childList != null)
                        {
                            int size = childList.size();
                            for (int i = 0; i < size; ++i)
                            {
                                TreeNode childNode = childList.get(i);
                                mp = childNode.getValueMap();
                                String id = (String) mp.get(DbToAdapter.KEY_ID);

                                log.e("load grandchild id = " + id);

                                if (!(Boolean) mp.get(DbToAdapter.KEY_LOAD))
                                {
                                    mDbToAdapter.addChildNode(null, childNode,
                                            id, true);
//                                    mp.put(DbToAdapter.KEY_LOAD, true);
//                                    childNode.setValueMap(mp);

                                    if (childNode.isLeaf())
                                        continue;
                                    List<TreeNode> grandchildren = childNode
                                            .getChildren();
                                    int size1 = grandchildren.size();
                                    for (int j = 0; j < size1; j++)
                                    {
                                        mTreeListAdapter
                                                .establishNodeList(grandchildren
                                                        .get(j));
                                    }
                                }
                            }
                            mp.put(DbToAdapter.KEY_LOAD, true);
//                            log.e("set true=-------2-------------------------" + node.getDescription());
                            node.setValueMap(mp);
                        }
                    }
                }
            }

            @Override
            public ArrayList<String> getParentIDList(String id)
            {
                return mDbToAdapter.getParentIDList(id);
            }

        });

        log.e("t2 =" + (System.currentTimeMillis() - mStartTime));
        mDbToAdapter.loadTreeNode(mLastPcsIDInDb, mTreeListAdapter);
        return null;
    }

    @Override
    protected void onPostExecute(String result)
    {
        if (mDataGetProgressDialog != null)
        {
            mDataGetProgressDialog.dismiss();
        }

        mTreeListView.setAdapter(mTreeListAdapter);
        mTreeListView.setSelection(mTreeListAdapter
                .getTreeListPosition(mLastPcsIDInDb));
        // mTreeListView.requestFocusFromTouch();//no need?

        log.e("t3 =" + (System.currentTimeMillis() - mStartTime));

        long l2 = System.currentTimeMillis();
        Toast toast = Toast.makeText(mContext, "loading tree takes:"
                + ((l2 - mStartTime) / 1000) + "." + (l2 - mStartTime) % 1000
                + "s", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();

    }

}
