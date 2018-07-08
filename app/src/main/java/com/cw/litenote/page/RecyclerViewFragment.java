/*
* Copyright (C) 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.cw.litenote.page;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cw.litenote.R;
import com.cw.litenote.db.DB_page;

/**
 * Demonstrates the use of {@link RecyclerView} with a {@link LinearLayoutManager} and a
 * {@link GridLayoutManager}.
 */
public class RecyclerViewFragment extends Fragment {

    public static DB_page mDb_page;
    public RecyclerView mRecyclerView;
    protected RecyclerView.LayoutManager mLayoutManager;
    public int page_tableId;
    int page_pos;

    Cursor mCursor_note;
    public PageRecyclerViewAdapter mItemAdapter;

    public RecyclerViewFragment(){
    }

    @SuppressLint("ValidFragment")
    public RecyclerViewFragment(int pos,int id){
        page_pos = pos;
        page_tableId = id;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.recycler_view_frag, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new LinearLayoutManager(getActivity());

        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);

        mDb_page = new DB_page(getActivity(), page_tableId);
        mDb_page.open();
        mCursor_note = mDb_page.mCursor_note;

        mItemAdapter = new PageRecyclerViewAdapter(mCursor_note,page_pos);

        mDb_page.close();// set close here, if cursor is used in mTabsPagerAdapter

        // Set PageRecyclerViewAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mItemAdapter);

        return rootView;
    }

}
