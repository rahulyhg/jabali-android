package ke.co.mauko.jabali.adapter;

/**
 * Created by Kevin Barassa on 05-Nov-16.
 */

/*
 * Copyright (c) 2015 52inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class BetterRecyclerAdapter<M, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    /***********************************************************************************************
     *
     * Constants
     *
     */


    /***********************************************************************************************
     *
     * Variables
     *
     */

    protected ArrayList<M> items = new ArrayList<>();
    protected ArrayList<M> filteredItems = new ArrayList<>();

    private OnItemClickListener<M> itemClickListener;
    private OnItemLongClickListener<M> itemLongClickListener;
    private View emptyView;

    private CharSequence query;

    /**
     * Default Constructor
     */
    public BetterRecyclerAdapter() {
        setHasStableIds(true);
    }

    /***********************************************************************************************
     *
     * Protected Methods
     *
     */

    /**
     * Call this to trigger the user set item click listener
     *
     * @param view          the view that was clicked
     * @param position      the position that was clicked
     */
    protected void onItemClick(View view, int position){
        if(itemClickListener != null) itemClickListener.onItemClick(view, getItem(position), position);
    }

    /**
     * Call this to trigger the user set item long click lisetner
     * @param view          the view that was clicked
     * @param position      the position that was clicked
     */
    protected void onItemLongClick(View view, int position){
        if(itemLongClickListener != null) itemLongClickListener.onItemLongClick(view, getItem(position), position);
    }

    /**
     * Override so you can sort the items in the array according
     * to your specification. Do nothing if you choose not to sort, or
     * plan to on your own accord.
     *
     * @param items     the list of items needing sorting
     */
    protected void onSort(List<M> items){}

    /**
     * Override to listen to when the adapter finishes filtering the dataset
     */
    protected void onFiltered(){}

    /**
     * Override to observe reorder changes in the underlying true data set
     *
     * @param start     the real start index, non-filtered
     * @param end       the real end index, non-filtered
     */
    protected void onItemMoved(M item, int start, int end){}

    /**
     * Override to return the applicable filter for this adapter to
     * filter with given specified constraint
     *
     * @return      the adapter filter
     */
    protected Filter<M> getFilter(){
        return null;
    }

    /***********************************************************************************************
     *
     * Helper Methods
     *
     */

    /**
     * Set the empty view to be used so that
     * @param emptyView
     */
    public void setEmptyView(View emptyView){
        if(this.emptyView != null){
            unregisterAdapterDataObserver(mEmptyObserver);
        }
        this.emptyView = emptyView;
        registerAdapterDataObserver(mEmptyObserver);
    }

    /**
     * Check if we should show the empty view
     */
    private void checkIfEmpty(){
        if(emptyView != null){
            emptyView.setVisibility(getItemCount() > 0 ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Data change observer
     */
    private RecyclerView.AdapterDataObserver mEmptyObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            checkIfEmpty();
        }
    };

    /**
     * Set the item click listener for this adapter
     */
    public void setOnItemClickListener(OnItemClickListener<M> itemClickListener){
        this.itemClickListener = itemClickListener;
    }

    /**
     * Set the item long click listener for this adapter
     */
    public void setOnItemLongClickListener(OnItemLongClickListener<M> itemLongClickListener){
        this.itemLongClickListener = itemLongClickListener;
    }

    /***********************************************************************************************
     *
     * Array Methods
     *
     */

    /**
     * Add a single object to this adapter
     * @param object    the object to add
     */
    public void add(M object) {
        items.add(object);
        applyFilter();
    }

    /**
     * Add a single object at the given index
     *
     * @param index     the position to add the object at
     * @param object    the object to add
     */
    public void add(int index, M object) {
        items.add(index, object);
        applyFilter();
    }

    /**
     * Add a collection of objects to this adapter
     *
     * @param collection        the collection of objects to add
     */
    public void addAll(Collection<? extends M> collection) {
        if (collection != null) {
            items.addAll(collection);
            applyFilter();
        }
    }

    /**
     * Update an item in the collection of objects with a new object
     *
     * @param index      the index of the object to update
     * @param object     the new object to replace at index position
     */
    public void set(int index, M object){
        items.set(index, object);
        applyFilter();
    }

    /**
     * Clear this adapter of all items
     */
    public void clear() {
        items.clear();
        filteredItems.clear();
        query = null;
    }

    /**
     * Remove a specific object from this adapter
     *
     * @param object        the object to remove
     */
    public void remove(M object) {
        items.remove(object);
        applyFilter();
    }

    /**
     * Remove an item at the given index
     *
     * @param index     the index of the item to remove
     * @return          the removed item
     */
    public M remove(int index){
        M item = items.remove(index);
        applyFilter();
        return item;
    }

    /**
     * Move an item around in the underlying array
     *
     * @param start     the item to move
     * @param end       the position to move to
     */
    public void moveItem(int start, int end){
        M startItem = filteredItems.get(start);
        M endItem = filteredItems.get(end);

        int realStart = items.indexOf(startItem);
        int realEnd = items.indexOf(endItem);

        Collections.swap(items, realStart, realEnd);
        applyFilter();

        onItemMoved(startItem, realStart, realEnd);
        notifyItemMoved(realStart, realEnd);
    }

    /**
     * Sort the items in this adapter by a given
     * {@link java.util.Comparator}
     *
     * @param comparator        the comparator to sort with
     */
    public void sort(Comparator<M> comparator){
        Collections.sort(items, comparator);
        applyFilter();
    }

    /**
     * Get an item from this adapter at a specific index
     *
     * @param position      the position of the item to retrieve
     * @return              the item at that position, or null
     */
    public M getItem(int position) {
        return filteredItems.get(position);
    }

    /**
     * Get all the items in this adapter
     *
     * @return      the all the 'filtered' items in the adapter
     */
    public List<M> getItems(){
        return filteredItems;
    }

    /**
     * Get a list of the raw underlying dataset of items
     *
     * @return      the unflitered items in the adapter
     */
    public List<M> getUnfilteredItems(){
        return items;
    }

    /***********************************************************************************************
     *
     * Filter Methods
     *
     */

    /**
     * Return whether or not this adapter is filtered
     *
     * @return      true if there is a query inplace that constitutes filtering
     */
    protected boolean isFiltered(){
        return !TextUtils.isEmpty(query);
    }

    /**
     * Filter this adapter with the specified constraints
     *
     * This endpoints with notify the adapter of content change
     *
     * @param constraints       the constraints to filter with
     */
    public void filter(CharSequence constraints){
        this.query = constraints;
        applyFilter();
        notifyDataSetChanged();
    }

    /**
     * Clear this adapter's filter and re-apply
     *
     * This endpoints with notify the adapter of content change
     */
    public void clearFilter(){
        query = null;
        applyFilter();
        notifyDataSetChanged();
    }

    /**
     * Apply the filter, if possible, to the adapter to update content
     */
    private void applyFilter(){
        filteredItems.clear();

        Filter<M> filter = getFilter();
        if(filter == null){
            filteredItems.addAll(items);
        }else{
            for (int i = 0; i < items.size(); i++) {
                M item = items.get(i);
                if(filter.filter(item, query)){
                    filteredItems.add(item);
                }
            }
        }

        onFiltered();
    }

    /***********************************************************************************************
     *
     * Adapter Methods
     *
     */

    /**
     * Get the active number of items in this adapter, i.e. the number of
     * filtered items
     *
     * @return      the number of filtered items (i.e. the displayable) items in this adapter
     */
    @Override
    public int getItemCount() {
        return filteredItems.size();
    }

    /**
     * Intercept the bind View holder method to wire up the item click listener only if
     * the listener is set by the user
     *
     * CAVEAT: Be sure that you still override this method and call it's super (or don't if you want
     * to override this functionality and use the {@link #onItemClick(android.view.View, int)} method)
     *
     * @param vh        the view holder
     * @param i         the position being bound
     */
    @Override
    public void onBindViewHolder(final VH vh, final int i) {
        if(itemClickListener != null){
            vh.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = vh.getAdapterPosition();
                    itemClickListener.onItemClick(v, getItem(position), position);
                }
            });
        }

        if(itemLongClickListener != null){
            vh.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = vh.getAdapterPosition();
                    return itemLongClickListener.onItemLongClick(v, getItem(position), position);
                }
            });
        }
    }

    /**
     * Get the item Id for a given position
     * @param position
     * @return
     */
    @Override
    public long getItemId(int position) {
        if(position > -1 && position < getItemCount()) {
            M item = getItem(position);
            if (item != null) return item.hashCode();
            return position;
        }
        return RecyclerView.NO_ID;
    }

    /***********************************************************************************************
     *
     * Inner Classes & Interfaces
     *
     */

    public interface Filter<T>{
        boolean filter(T item, @Nullable CharSequence query);
    }

    /**
     * The interface for detecting item click events from within the adapter, this listener
     * is triggered by {@link #onItemClick(android.view.View, int)}
     */
    public interface OnItemClickListener<T>{
        void onItemClick(View v, T item, int position);
    }

    /**
     * The interface for detecting item long click events from within the adapter
     */
    public interface OnItemLongClickListener<T>{
        boolean onItemLongClick(View v, T item, int position);
    }


}
