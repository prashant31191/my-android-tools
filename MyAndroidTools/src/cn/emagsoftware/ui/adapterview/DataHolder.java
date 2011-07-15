package cn.emagsoftware.ui.adapterview;

import android.content.Context;
import android.view.View;

public abstract class DataHolder {
	
	protected Object mData = null;
	protected boolean mIsAsyncDataCompleted = false;
	
	public DataHolder(Object data){
		mData = data;
	}
	
	/**
	 * <p>使用当前data创建View时触发
	 * <p>创建View时可通过isAsyncDataCompleted()方法来判断异步数据是否加载完成，若加载完成，也要更新到View上
	 * <p>可以通过ViewHolder来绑定View的结构信息，从而提高更新时的效率
	 * @param context
	 * @param position
	 * @param data
	 * @return
	 */
	public abstract View onCreateView(Context context,int position,Object data);
	
	/**
	 * <p>使用当前data更新View时触发，出于节约资源的考虑，View默认会被复用，此时只需要更新View即可
	 * <p>更新View时可通过isAsyncDataCompleted()方法来判断异步数据是否加载完成，若加载完成，也要更新到View上
	 * <p>若设置了View不复用，但当前方法仍然有可能被回调，情况如下：
	 *    <br>1.在异步数据加载完成时，会判断当前data是否处于被绑定AdapterView的可见位置，若处于可见位置，会回调当前方法来更新View
	 *    <br>2.如果设置了异步数据调度器刷新可见区域(setRefreshVisibleArea)，则会回调当前方法来刷新View
	 * <p>更新View可以通过ViewHolder来提高效率
	 * @param context
	 * @param position
	 * @param view
	 * @param data
	 */
	public abstract void onUpdateView(Context context,int position,View view,Object data);
	
	public Object getData(){
		return mData;
	}
	
	public boolean isAsyncDataCompleted(){
		return mIsAsyncDataCompleted;
	}
	
	public void setAsyncDataCompleted(boolean isAsyncDataCompleted){
		mIsAsyncDataCompleted = isAsyncDataCompleted;
	}
	
}
