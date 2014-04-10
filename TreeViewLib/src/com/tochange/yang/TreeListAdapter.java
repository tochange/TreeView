package com.tochange.yang;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PathEffect;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class TreeListAdapter extends BaseAdapter
{

    private Context mContext;

    private int mJumpToPosition;

    private TreeNode mRoot;

    private List<TreeNode> mAllNodeList = new ArrayList<TreeNode>();

    private List<TreeNode> mShowNodeList = new ArrayList<TreeNode>();

    private List<Integer> mShowNodeIDList = new LinkedList<Integer>();

    private TreeNode mSelectedNode;

    private OnTreeCallBack mOnTreeCallBack;

    private LayoutInflater mInflater;

    // expand node icon
    private int mExpandOnIcon = R.drawable.tree_expand_on_nr;

    private int mExpandOffIcon = R.drawable.tree_expand_off_nr;

    private boolean mShowRoot = true;
    
    private log log = new log() ;

    public TreeListAdapter(Context con, TreeNode root, boolean bShowRoot)
    {
        mContext = con;
        mInflater = (LayoutInflater) con
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRoot = root;
        mShowRoot = bShowRoot;
        establishNodeList(mRoot);
        setNodeListToShow();

        log.intLog("TreeListAdapter", true);
    }

    public boolean getShowRoot()
    {
        return mShowRoot;
    }

    public List<TreeNode> getAllLoadNodeList()
    {
        return mAllNodeList;
    }

    public void setShowRoot(boolean show)
    {
        if (mShowRoot != show)
        {
            mShowRoot = show;
            setNodeListToShow();
            notifyDataSetChanged();
        }
    }

    public void setOnTreeCallBack(OnTreeCallBack onTreeCallBack)
    {
        mOnTreeCallBack = onTreeCallBack;
    }

    // all nodes
    public void establishNodeList(TreeNode node)
    {
        if (node == null)
            return;
        mAllNodeList.add(node);
        if (node.isLeaf())
            return;
        List<TreeNode> children = node.getChildren();
        int size = children.size();
        for (int i = 0; i < size; i++)
        {
            establishNodeList(children.get(i));
        }
    }

    // all prepared to be shown
    private void setNodeListToShow()
    {
        this.mShowNodeList.clear();
        mShowNodeIDList.clear();
        establishNodeListToShow(this.mRoot);
    }

    private void establishNodeListToShow(TreeNode node)
    {
        if (mShowRoot || mRoot != node)
        {
            mShowNodeList.add(node);
            mShowNodeIDList.add(Integer.valueOf((String) node.getValueMap()
                    .get(DbToAdapter.KEY_ID)));
        }
        if (node != null && node.getExpanded() && !node.isLeaf()
                && node.getChildren() != null)
        {
            List<TreeNode> children = node.getChildren();
            int size = children.size();
            for (int i = 0; i < size; i++)
            {
                establishNodeListToShow(children.get(i));
            }
        }
    }

    private void changeNodeExpandOrFold(TreeNode node)
    {
        boolean flag = node.getExpanded();
        node.setExpanded(!flag);
    }

    public TreeNode getSelectorNode()
    {
        return mSelectedNode;
    }

    public int getCount()
    {
        return mShowNodeList.size();
    }

    public Object getItem(int arg0)
    {
        return mShowNodeList.get(arg0);
    }

    public long getItemId(int arg0)
    {
        return arg0;
    }

    public View getView(final int position, View view, ViewGroup parent)
    {
        Holder holder = null;
        if (view != null)
        {
            holder = (Holder) view.getTag();
        }
        else
        {
            holder = new Holder();
            view = this.mInflater.inflate(R.layout.tree_item, null);
            holder.layBlank = (LinearLayout) view.findViewById(R.id.lay_blank);
            // holder.nodeIcon = (ImageView) view.findViewById(R.id.iv_node);
            holder.expandOrNot = (ImageView) view
                    .findViewById(R.id.iv_expanded);
            holder.layTextContent = (LinearLayout) view
                    .findViewById(R.id.lay_tv_content);
            holder.description = (TextView) view
                    .findViewById(R.id.tv_description);
            // holder.secondTitle = (TextView) view
            // .findViewById(R.id.tv_second_title);
            view.setTag(holder);
        }

        TreeNode node = this.mShowNodeList.get(position);
        if (node == null)
            return null;
        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {
                TreeNode node = mShowNodeList.get(position);
                onClickNodeExpandOrFoldIcon(node);
            }
        });
        // set icon present expand or not expand
        if (!node.isLeaf())
        {
            holder.expandOrNot
                    .setImageResource(node.getExpanded() ? mExpandOnIcon
                            : mExpandOffIcon);
            holder.expandOrNot.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.expandOrNot.setVisibility(View.GONE);
        }
        holder.expandOrNot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                TreeNode node = mShowNodeList.get(position);
                onClickNodeExpandOrFoldIcon(node);
            }
        });

        // holder.nodeIcon.setOnClickListener(new View.OnClickListener() {
        // @Override
        // public void onClick(View v)
        // {
        // // TODO Auto-generated method stub
        // TreeNode node = mShowNodeList.get(position);
        // onClickNodeIcon(node);
        // }
        // });

        holder.description.setText(node.getDescription());
        // String title = node.getSecondTitle();
        // if (title == null || title.equals(""))
        // {
        // holder.secondTitle.setVisibility(View.GONE);
        // }
        // else
        // {
        // holder.secondTitle.setVisibility(View.VISIBLE);
        // holder.secondTitle.setText(node.getSecondTitle());
        // }

        holder.layTextContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                TreeNode node = mShowNodeList.get(position);
                doOnClick(node);
            }
        });

        // horizon offset of dotted line
        LayoutParams lp = (LayoutParams) holder.layBlank.getLayoutParams();

        int n = node.getLevel();
        int nf = n + (node.isLeaf() ? 1 : 0);
        Resources res = mContext.getResources();
        int nw = res.getDimensionPixelSize(R.dimen.treenode_icon_width);
        if (!mShowRoot)
        {
            if (nf > 0)
            {
                nf--;
            }
            n--;
        }
        nw = nf * nw + res.getDimensionPixelSize(R.dimen.treenode_offset);
        lp.width = nw;
        holder.layBlank.setLayoutParams(lp);
        Bitmap bitmap = createBitmap(node, n, nw, mContext.getResources()
                .getDimensionPixelSize(R.dimen.treenode_height));
        if (bitmap != null)
        {
            holder.layBlank.setBackgroundDrawable(new BitmapDrawable(bitmap));
        }
        else
        {
            holder.layBlank.setBackgroundColor(Color.TRANSPARENT);
        }
        if (node == mSelectedNode)
        {
            view.findViewById(R.id.lay_tv_content).setBackgroundColor(
                    mContext.getResources().getColor(R.color.tree_node_bg));
        }
        else
        {
            view.findViewById(R.id.lay_tv_content).setBackgroundResource(
                    R.drawable.treelist_selector_bg);
        }
        return view;
    }

    public Bitmap createBitmap(TreeNode node, int lev, int w, int h)
    {
        Bitmap bitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
        if (lev <= 0 || w <= 0 || h <= 0)
        {
            return null;
        }
        Resources res = mContext.getResources();
        int offW = res.getDimensionPixelSize(R.dimen.treenode_offset);
        int iconW = res.getDimensionPixelSize(R.dimen.treenode_icon_width);
        int color = res.getColor(R.color.black);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setStyle(Style.STROKE);
        PathEffect effect = new DashPathEffect(new float[] { 2, 2 }, 1);
        paint.setPathEffect(effect);
        paint.setStrokeWidth(1);
        paint.setColor(color);
        paint.setAntiAlias(true);
        int start = offW + iconW / 2;

        int y = h;
        int x = start;
        for (int i = 0; i < lev; ++i)
        {
            y = h;
            x = start + i * iconW;
            if (i == lev - 1)
            {// leaf： draw dotted line half in vertical orientation
                y = isLastChildNode(node, 1) ? (h / 2) : h;
                canvas.drawLine(x, 0, x, y, paint);
            }
            else
            {
                if (!isLastChildNode(node, lev - i))
                {
                    canvas.drawLine(x, 0, x, y, paint);
                }
            }
        }
        int line = (node.isLeaf() && node.getIcon() == -1) ? res
                .getDimensionPixelSize(R.dimen.treenode_line_offset) : 0;
        canvas.drawLine(x, h / 2, w - line, h / 2, paint);
        return bitmap;
    }

    private boolean isLastChildNode(TreeNode node, int lev)
    {
        boolean last = true;
        TreeNode child = node;
        TreeNode parent = child;
        int i = 0;
        for (i = 0; i < lev; ++i)
        {
            parent = child.getParent();
            if (parent == null)
            {
                break;
            }
            if (i == lev - 1)
            {
                List<TreeNode> childList = parent.getChildren();
                if (childList != null && childList.size() > 0)
                {
                    last = (childList.get(childList.size() - 1) == child);
                }
            }
            else
            {
                child = parent;
            }
        }

        return last;
    }

    public void doOnClick(TreeNode node)
    {
        setSelectorNode(node);
        if (!node.isLeaf())
        {
            setNodeExpandOrNot(node);
        }
    }

    public void imitateDoOnClick(TreeNode node)
    {

        if (!node.getExpanded())
        {
            if (mOnTreeCallBack != null)
            {
                mOnTreeCallBack.onNodeExpand(node);
            }
            setNodeListToShow();
            notifyDataSetChanged();
        }
    }

    public void setSelectorNode(TreeNode node)
    {
        if (mSelectedNode != node)
        {
            mSelectedNode = node;
            // notifyDataSetChanged();

            if (mOnTreeCallBack != null)
            {
                if (!mOnTreeCallBack.onSelectNode(node))
                {
                    return;
                }
                log.e("last node:" + node.getDescription());
                if (!mShowNodeIDList.contains(Integer.valueOf((String) node
                        .getValueMap().get(DbToAdapter.KEY_ID))))
                {
                    establishNodeListToShow(node);// put this node into show
                                                  // list
                }
                notifyDataSetChanged();
            }
        }
    }

    public boolean calulateListViewPosition(String lastid)
    {
        ArrayList<String> parentList = mOnTreeCallBack.getParentIDList(lastid);
        if (!parentList.isEmpty())
        {
            log.e("..." + parentList.get(0));
            int size = parentList.size();
            mJumpToPosition = -1;

            for (int j = (size - 1) - 1; j >= 0; j--)
            {
                String id = parentList.get(j);
                int nodeSize = mShowNodeList.size();
                for (int i = 0; i < nodeSize; i++)
                {
                    Map<String, Object> mp = mShowNodeList.get(i).getValueMap();

                    log.e("foreach=" + j + " " + id + " "
                            + mp.get(DbToAdapter.KEY_ID) + " "
                            + mShowNodeList.get(i).getDescription());

                    if (mp != null
                            && ((String) mp.get(DbToAdapter.KEY_ID)).equals(id))
                    {
                        mJumpToPosition += (Integer) mp
                                .get(DbToAdapter.KEY_CHILD_ORDER) + 1;
                        log.e("found mJumpToPosition= " + mJumpToPosition);
                        break;
                    }
                }
            }

            log.e("position=" + mJumpToPosition);
        }
        else
            mJumpToPosition = 0;
        return true;
    }

    public int getTreeListPosition(String id)

    {
        if (calulateListViewPosition(id))
            return mJumpToPosition;
        else
            return -1;
    }

    public void setNodeExpandOrNot(TreeNode node)
    {
        if (!node.isLeaf())
        {
            changeNodeExpandOrFold(node);
            if (mOnTreeCallBack != null)
            {
                mOnTreeCallBack.onNodeExpandOrNot(node);
            }
            setNodeListToShow();
            notifyDataSetChanged();
        }
    }

    public int setNodeShow(TreeNode node)
    {
        TreeNode parent = node.getParent();
        while (parent != null)
        {
            parent.setExpanded(true);
            parent = parent.getParent();
        }
        setNodeListToShow();
        int i = 0;
        int size =  mShowNodeList.size();
        for (i = 0; i < size; ++i)
        {
            if (node == mShowNodeList.get(i))
            {
                break;
            }
        }
        if (i >= mShowNodeList.size())
        {
            i = -1;
        }
        notifyDataSetChanged();

        return i;
    }

    public boolean onClickNodeIcon(TreeNode node)
    {

        return onClickNodeExpandOrFoldIcon(node);
    }

    public boolean onClickNodeExpandOrFoldIcon(TreeNode node)
    {
        if (!node.isLeaf())
        {
            this.changeNodeExpandOrFold(node);
            this.setNodeListToShow();
            this.notifyDataSetChanged();
        }
        return true;
    }

    public class Holder
    {
        LinearLayout layBlank;

        ImageView nodeIcon;

        ImageView expandOrNot;

        TextView description;

        TextView secondTitle;

        LinearLayout layTextContent;

    }

    public interface OnTreeCallBack
    {
        public boolean onSelectNode(TreeNode node);

        // manual click node to expand or not
        public void onNodeExpandOrNot(TreeNode node);

        // imitate expand
        public void onNodeExpand(TreeNode node);

        public ArrayList<String> getParentIDList(String id);
    }
}
