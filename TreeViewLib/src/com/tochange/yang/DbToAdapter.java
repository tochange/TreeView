package com.tochange.yang;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DbToAdapter
{
    public final static String KEY_ID = "id";

    public final static String KEY_PARENT_ID = "parentid";

    public final static String KEY_PCSCODE = "pcscode";

    public final static String KEY_LOAD = "loaded";

    public final static String KEY_CHILD_ORDER = "childrenorder";

    public static String ROOT_PCSID;

    private String FIELD_NAME;

    private String FIELD_REMARK;

    private String FIELD_ID;

    private String FIELD_ID_PARENT;

    private final String TABLE_NAME;

    private final String DBPATH;

    private int ROOT_PCSID_INT;

    private final int mIcon = -1;

    private String mLastPcsCodeInDb;

    private TreeListAdapter mTreeListAdapter;

    private boolean mAlreadyNotify = false;

    private ArrayList<String> mParentIDList = new ArrayList<String>();

    private String mFirstIDInParentList;

    private SQLiteDatabase mDb;

    private log log = new log();

    public void colseCursorAndSql(Cursor cur, SQLiteDatabase db)
    {
        if (cur == null && db == null)
            if (mDb.isOpen())
                mDb.close();
        if (cur != null && !cur.isClosed())
            cur.close();
        if (db != null && db.isOpen())
            db.close();
    }

    public ArrayList<String> getParentIDList(String id)
    {
        // if (!mParentIDList.isEmpty())
        // log.e("1 mParentIDList[0]=" + mParentIDList.get(0) + " id=" + id);
        if (!mParentIDList.isEmpty() && !id.equals(mParentIDList.get(0)))
        {
            mParentIDList.clear();
            // mLastPcsCodeInDb = id;// dangerous
            if (!isSameParentWithLastNode(id))
            {
                String parentID = getparentID(mFirstIDInParentList);
                TreeNode node = findNodeByID(
                        mTreeListAdapter.getAllLoadNodeList(), parentID);

                mTreeListAdapter.setNodeExpandOrNot(node);
            }
            mFirstIDInParentList = id;

            putParentPcsCodeList(id);
        }
        return mParentIDList;
    }

    public DbToAdapter(String pcsCode, DatabaseStruct dbStructure)
    {
        mFirstIDInParentList = mLastPcsCodeInDb = pcsCode;
        FIELD_NAME = dbStructure.fieldName;
        FIELD_REMARK = dbStructure.fieldRemark;
        FIELD_ID = dbStructure.fieldID;
        FIELD_ID_PARENT = dbStructure.fieldIDParent;
        TABLE_NAME = dbStructure.tableName;
        DBPATH = dbStructure.dbPath;
        ROOT_PCSID_INT = dbStructure.rootFieldID;
        ROOT_PCSID = String.valueOf(ROOT_PCSID_INT - 1);

        if (mDb == null)
            mDb = SQLiteDatabase.openDatabase(DBPATH, null,
                    SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        log.intLog("DbToAdapter", true);
    }

    private String getparentID(String id)
    {
        String parentID = null;

        String sql = "select " + FIELD_ID + ", " + FIELD_ID_PARENT + "  from "
                + TABLE_NAME + " where " + FIELD_ID + " = '" + id + "'";
        Cursor cur = mDb.rawQuery(sql, null);
        if (cur != null && cur.moveToFirst())
            parentID = cur.getString(cur.getColumnIndex(FIELD_ID_PARENT));
        colseCursorAndSql(cur, null);
        return parentID;
    }

    private TreeNode findNodeByID(List<TreeNode> mShowNodeList, String id)
    {
        TreeNode tmp = null;
        int nodeSize = mShowNodeList.size();
        for (int i = 0; i < nodeSize; i++)
        {
            tmp = mShowNodeList.get(i);
            if (tmp.getValueMap().get(KEY_ID).equals(id))
            {
                break;
            }
        }
        return tmp;
    }

    private boolean isSameParentWithLastNode(String id)
    {
        String parentID = null;
        String lastParentID = null;

        String sql = "select " + FIELD_ID + ", " + FIELD_ID_PARENT + "  from "
                + TABLE_NAME + " where " + FIELD_ID + " = '" + id + "'";
        Cursor cur = mDb.rawQuery(sql, null);
        if (cur != null && cur.moveToFirst())
            parentID = cur.getString(cur.getColumnIndex(FIELD_ID_PARENT));
        if (cur != null && !cur.isClosed())
            cur.close();

        sql = "select " + FIELD_ID + ", " + FIELD_ID_PARENT + "  from "
                + TABLE_NAME + " where " + FIELD_ID + " = '"
                + mFirstIDInParentList + "'";
        cur = mDb.rawQuery(sql, null);
        if (cur != null && cur.moveToFirst())
            lastParentID = cur.getString(cur.getColumnIndex(FIELD_ID_PARENT));
        if (cur != null && !cur.isClosed())
            cur.close();

        colseCursorAndSql(cur, null);
        return parentID.equals(lastParentID);
    }

    private String getRootName(SQLiteDatabase db)
    {
        String pcsName = "default pcsname";
        String sql = "select " + FIELD_NAME + ", " + FIELD_ID_PARENT
                + "  from " + TABLE_NAME + " where " + FIELD_ID_PARENT + " = '"
                + ROOT_PCSID + "'";
        Cursor cur = db.rawQuery(sql, null);

        if (cur != null && cur.moveToFirst())
            pcsName = cur.getString(cur.getColumnIndex(FIELD_NAME));
        if (cur != null && !cur.isClosed())
            cur.close();
        return pcsName;
    }

    // load 3 levels
    public void loadDefaultTree(TreeNode parent)
    {
        log.e("mParentIDList=" + mParentIDList.toString());
        if (!mDb.isOpen())
            mDb = SQLiteDatabase.openDatabase(DBPATH, null,
                    SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        mDb.beginTransaction();

        Map<String, Object> mp1 = new HashMap<String, Object>();
        mp1.put(KEY_ID, String.valueOf(ROOT_PCSID_INT));
        mp1.put(KEY_PCSCODE, String.valueOf(ROOT_PCSID_INT));
        mp1.put(KEY_LOAD, true);
        mp1.put(KEY_CHILD_ORDER, ROOT_PCSID_INT - 1);
        mp1.put(KEY_PARENT_ID, ROOT_PCSID_INT - 1);
        parent.setDescription(getRootName(mDb));
        parent.setValueMap(mp1);
        parent.setExpanded(true);

        String sql = "select " + FIELD_NAME + ", " + FIELD_REMARK + ", "
                + FIELD_ID_PARENT + ", " + FIELD_ID + " from " + TABLE_NAME
                + " where " + FIELD_ID_PARENT + " = '"
                + String.valueOf(ROOT_PCSID_INT) + "'";
        Cursor cur = mDb.rawQuery(sql, null);
        if (cur != null && cur.moveToFirst())
        {
            int childOrder = 0;
            do
            {
                String pcsName = cur.getString(cur.getColumnIndex(FIELD_NAME));
                String pcsCode = cur
                        .getString(cur.getColumnIndex(FIELD_REMARK));
                String pcsID = cur.getString(cur.getColumnIndex(FIELD_ID));

                TreeNode node = new TreeNode(parent, pcsName, mIcon);
                Map<String, Object> mp = new HashMap<String, Object>();
                mp.put(KEY_PCSCODE, pcsCode);
                mp.put(KEY_ID, pcsID);
                mp.put(KEY_LOAD, false);
                mp.put(KEY_CHILD_ORDER, childOrder);
                mp.put(KEY_PARENT_ID, ROOT_PCSID_INT);
                node.setValueMap(mp);
                if (!mLastPcsCodeInDb.equals(pcsID)
                        && mParentIDList.contains(pcsID))
                    node.setExpanded(true);
                parent.addChildNode(node);
                childOrder++;

                // log.e(TAG, "add to=" + pcsName);
                addChildNode(mDb, node, pcsID);
            }
            while (cur.moveToNext());
        }
        if (parent.getChildren() != null)
        {
            parent.setDescription(parent.getDescription() + "("
                    + parent.getChildren().size() + ")");
        }
        mDb.setTransactionSuccessful();
        mDb.endTransaction();
        colseCursorAndSql(cur, mDb);
    }

    public void loadTreeNode(String pcsCode, TreeListAdapter adapter)
    {
        mTreeListAdapter = adapter;
        List<TreeNode> allNodeList = adapter.getAllLoadNodeList();

        for (int i = mParentIDList.size() - 1; i >= 0; i--)
        {
            String tempId = mParentIDList.get(i);
            TreeNode n = null;
            int size = allNodeList.size();
            for (int j = 0; j < size; j++)
            {
                if (((String) allNodeList.get(j).getValueMap().get(KEY_ID))
                        .equals(tempId))
                {
                    n = allNodeList.get(j);
                    if (!mAlreadyNotify && mTreeListAdapter != null
                            && tempId.equals(mLastPcsCodeInDb))
                    {
                        mAlreadyNotify = true;
                        mTreeListAdapter.setSelectorNode(n);
                    }
                    break;
                }
            }
            if (n != null)
            {
                Map<String, Object> mp1 = n.getValueMap();
                if (!(Boolean) mp1.get(KEY_LOAD))
                    adapter.imitateDoOnClick(n);
            }
        }
    }

    public void putParentPcsCodeList(String pcsID)
    {
        // log.e("putparentlist id=" + pcsID);
        String sql = "select " + FIELD_ID_PARENT + ", " + FIELD_ID + " from "
                + TABLE_NAME + " where " + FIELD_ID + " = '" + pcsID + "'";
        Cursor cur = mDb.rawQuery(sql, null);
        if (mParentIDList.contains(ROOT_PCSID) || pcsID.equals(ROOT_PCSID))
        {
            mParentIDList.add(0, mFirstIDInParentList);
            colseCursorAndSql(cur, null);
            return;
        }
        File f = new File(DBPATH);
        if (!f.exists())
        {
            log.e("db does not exist");
            colseCursorAndSql(cur, mDb);
            return;
        }
        String parentCode = null;
        if (cur != null && cur.moveToFirst())
        {
            parentCode = cur.getString(cur.getColumnIndex(FIELD_ID_PARENT));
            mParentIDList.add(parentCode);
        }
        else
        {
            log.e("last pcsCode does not exist");
            colseCursorAndSql(cur, mDb);
            return;
        }
        colseCursorAndSql(cur, null);
        putParentPcsCodeList(parentCode);
    }

    public void addChildNode(SQLiteDatabase db, TreeNode parent, String id)
    {
        addChildNode(db, parent, id, false);
    }

    public void addChildNode(SQLiteDatabase db, TreeNode parent, String id,
            boolean haveToNotify)
    {
        if (!mDb.isOpen())
        {
            mDb = SQLiteDatabase.openDatabase(DBPATH, null,
                    SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        }
        String sql = "select " + FIELD_NAME + ", " + FIELD_REMARK + ", "
                + FIELD_ID + ", " + FIELD_ID_PARENT + " from " + TABLE_NAME
                + " where " + FIELD_ID_PARENT + " = '" + id + "'";
        Cursor cur = mDb.rawQuery(sql, null);
        if (cur != null && cur.moveToFirst())
        {
            int childOrder = 0;
            int parentID = (Integer) parent.getValueMap().get(KEY_PARENT_ID);
            do
            {
                String pcsName = cur.getString(cur.getColumnIndex(FIELD_NAME));
                String pcsCode = cur
                        .getString(cur.getColumnIndex(FIELD_REMARK));
                String pcsID = cur.getString(cur.getColumnIndex(FIELD_ID));

                TreeNode node = new TreeNode(parent, pcsName, mIcon);
                Map<String, Object> mp = new HashMap<String, Object>();
                mp.put(KEY_ID, pcsID);
                mp.put(KEY_PCSCODE, pcsCode);
                mp.put(KEY_LOAD, false);
                mp.put(KEY_CHILD_ORDER, childOrder);
                mp.put(KEY_PARENT_ID, parentID);
                node.setValueMap(mp);

                parent.addChildNode(node);
                childOrder++;
                if (!mAlreadyNotify && haveToNotify && mTreeListAdapter != null
                        && pcsID.equals(mLastPcsCodeInDb))
                {
                    mAlreadyNotify = true;
                    mTreeListAdapter.setSelectorNode(node);
                    // log.e(TAG, "done leaf set=" + pcsName);
                }
                log.e("add=" + pcsName + " " + pcsCode + " " + pcsID);
            }
            while (cur.moveToNext());

            if (parent.getChildren() != null)
            {
                parent.setDescription(parent.getDescription().replaceAll(
                        "\\([0-9]+\\)", "")
                        + "(" + parent.getChildren().size() + ")");
            }

            // default load 3 levels, when haveToNotify==false,regard as haven't
            // load
            if (parent.getLevel() > 1 && id.equals(mLastPcsCodeInDb))
            {
                Map<String, Object> mp = parent.getValueMap();
                mp.put(KEY_LOAD, true);
                parent.setValueMap(mp);
            }

            // last chosen id in parent list
            if (haveToNotify && mTreeListAdapter != null
                    && id.equals(mLastPcsCodeInDb))
            {
                parent.setExpanded(false);
                mTreeListAdapter.setSelectorNode(parent);
            }

            if (haveToNotify)
            {
                if (!id.equals(mLastPcsCodeInDb) && mParentIDList.contains(id))
                    parent.setExpanded(true);
                else
                    parent.setExpanded(false);

            }
        }
        if (haveToNotify)
            colseCursorAndSql(cur, null);
        else if (cur != null && !cur.isClosed())
            cur.close();
    }
}
