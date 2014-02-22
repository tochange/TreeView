package com.tochange.yang;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TreeNode
{
    private TreeNode mParent;

    private List<TreeNode> mChildList;

    private String mOid;

    private String mName;

    private String mSecondTitle;

    private boolean mIsExpanded = false;

    private int mIcon = -1;

    private Map<String, Object> mValueMap;

    public TreeNode(TreeNode parent, String description, int icon)
    {
        this.mParent = parent;
        this.mOid = UUID.randomUUID().toString();
        this.mName = description;
        this.mIcon = icon;
    }

    public void setValueMap(Map<String, Object> valueMap)
    {
        mValueMap = valueMap;
    }

    public Map<String, Object> getValueMap()
    {
        return mValueMap;
    }

    public void setSecondTitle(String value)
    {
        this.mSecondTitle = value;
    }

    public String getSecondTitle()
    {
        return this.mSecondTitle;
    }

    public void setIcon(int icon)
    {
        this.mIcon = icon;
    }

    public int getIcon()
    {
        return this.mIcon;
    }

    public String getDescription()
    {
        return this.mName;
    }

    public void setDescription(String name)
    {
        this.mName = name;
    }

    public String getOid()
    {
        return this.mOid;
    }

    public boolean isLeaf()
    {
        return mChildList == null || mChildList.size() == 0;
    }

    // root level is 0
    public int getLevel()
    {
        return mParent == null ? 0 : mParent.getLevel() + 1;
    }

    public void setExpanded(boolean isExpanded)
    {
        this.mIsExpanded = isExpanded;
    }

    public boolean getExpanded()
    {
        return this.mIsExpanded;
    }

    public void addChildNode(TreeNode child)
    {
        if (mChildList == null)
        {
            mChildList = new ArrayList<TreeNode>();
        }
        mChildList.add(child);
    }

    public void clearChildren()
    {
        if (!mChildList.equals(null))
        {
            mChildList.clear();
        }
    }

    public boolean isRoot()
    {
        return mParent.equals(null) ? true : false;
    }

    public final List<TreeNode> getChildren()
    {
        return mChildList;
    }

    public TreeNode getParent()
    {
        return mParent;
    }
}
