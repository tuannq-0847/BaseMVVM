package com.karleinstein.basemvvm.base

import android.annotation.SuppressLint
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.Px
import androidx.recyclerview.widget.*
import androidx.viewbinding.ViewBinding
import java.util.concurrent.Executors

abstract class BaseRecyclerAdapter<Item : Any>(
    callBack: DiffUtil.ItemCallback<Item> = BaseDiffUtil(),
    val onClickItem: (item: Item) -> Unit = {}
) : ListAdapter<Item, BaseViewHolder>(
    AsyncDifferConfig.Builder(callBack)
        .setBackgroundThreadExecutor(Executors.newSingleThreadExecutor())
        .build()
) {
    protected var viewBinding: ViewBinding? = null

    private val states = mutableMapOf<Item, Boolean>()

    abstract fun provideViewBinding(@LayoutRes layout: Int, parent: ViewGroup): ViewBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        viewBinding = provideViewBinding(viewType, parent)
        return BaseViewHolder(
            viewBinding?.root ?: LayoutInflater.from(parent.context)
                .inflate(viewType, parent, false)
        ).apply {
            bindFirstTime(this)
            currentList.forEach { states[it] = false }
        }
    }

    protected open fun bindFirstTime(baseViewHolder: BaseViewHolder) {}

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = getItem(position)
        onBind(holder, item, position)
    }

    override fun getItemViewType(position: Int): Int {
        return buildLayoutRes(position)
    }

    abstract fun onBind(holder: BaseViewHolder, item: Item, position: Int)

    @LayoutRes
    protected open fun buildLayoutRes(position: Int): Int = -1

    protected fun stateClickedHandler(isStateChanged: Boolean, position: Int): Boolean {
        val key = states.keys.toList()[position]
        states[key] = isStateChanged
        return isStateChanged
    }
}

class BaseDiffUtil<Item : Any> : DiffUtil.ItemCallback<Item>() {
    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem === newItem
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem == newItem
    }

}

open class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

/**
 * This is an [RecyclerView.OnScrollListener] which will scrolls
 * the [RecyclerView] to the next closes position if the
 * position of the snapped View (calculated by the given [snapHelper] in [SnapHelper.findSnapView])
 * is odd. Because each odd position is a space/placeholder where we want to jump over.
 *
 * See also [this SO question](https://stackoverflow.com/q/51747104).
 */
class SpacePagerSnapHelper(
    private val snapHelper: SnapHelper
) : RecyclerView.OnScrollListener() {

    private enum class ScrollDirection {
        UNKNOWN, LEFT, RIGHT
    }

    private var scrollDirection: ScrollDirection = ScrollDirection.UNKNOWN

    fun attachToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.addOnScrollListener(this)
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        scrollDirection = if (dx > 0) ScrollDirection.RIGHT else ScrollDirection.LEFT
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        when (newState) {
            RecyclerView.SCROLL_STATE_IDLE -> onPageChanged(recyclerView)
            RecyclerView.SCROLL_STATE_SETTLING -> onPageChanged(recyclerView)
        }
    }

    private fun onPageChanged(recyclerView: RecyclerView) {
        val layoutManager = recyclerView.layoutManager
        val viewToSnap = snapHelper.findSnapView(layoutManager)
        viewToSnap?.let {
            val position = layoutManager?.getPosition(it) ?: return
            // Only "jump over" each second item because it is a space/placeholder
            // see also MediaContentAdapter.
            if (position % 2 != 0) {
                when (scrollDirection) {
                    ScrollDirection.LEFT -> {
                        recyclerView.smoothScrollToPosition(position - 1)
                    }
                    ScrollDirection.RIGHT -> {
                        recyclerView.smoothScrollToPosition(position + 1)
                    }
                    ScrollDirection.UNKNOWN -> {
                    }
                }
            }
        }
    }
}

class LinearHorizontalSpacingDecoration(@Px private val innerSpacing: Int) :
    RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val itemPosition = parent.getChildAdapterPosition(view)

        outRect.left = if (itemPosition == 0) 0 else innerSpacing / 2
        outRect.right = if (itemPosition == state.itemCount - 1) 0 else innerSpacing / 2
    }
}
