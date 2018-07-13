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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.cw.litenote.R;
import com.cw.litenote.db.DB_page;
import com.cw.litenote.main.MainAct;
import com.cw.litenote.page.helper.OnStartDragListener;
import com.cw.litenote.page.helper.SimpleItemTouchHelperCallback;
import com.cw.litenote.tabs.TabsHost;
import com.cw.litenote.util.preferences.Pref;
import com.cw.litenote.util.uil.UilCommon;

/**
 * Demonstrates the use of {@link RecyclerView} with a {@link LinearLayoutManager} and a
 * {@link GridLayoutManager}.
 */
public class Page_recycler extends Fragment implements OnStartDragListener {

    public static DB_page mDb_page;
    public RecyclerView recyclerView;
    protected RecyclerView.LayoutManager mLayoutManager;
    public int page_tableId;
    int page_pos;
    public static int currPlayPosition;
    public static int mHighlightPosition;
    public SeekBar seekBarProgress;
    public AppCompatActivity mAct;

    Cursor mCursor_note;
    public PageAdapter_recycler mItemAdapter;

    private ItemTouchHelper mItemTouchHelper;

    public Page_recycler(){
    }

    @SuppressLint("ValidFragment")
    public Page_recycler(int pos, int id){
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
		System.out.println("Page_recycler / _onCreateView / page_tableId = " + page_tableId);
        View rootView = inflater.inflate(R.layout.recycler_view_frag, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new LinearLayoutManager(getActivity());

        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (recyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) recyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.scrollToPosition(scrollPosition);


        UilCommon.init();

        mAct = MainAct.mAct;
        fillData();

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mItemAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        return rootView;
    }

    @Override
    public void onResume() {
        System.out.println("Page_recycler / _onResume / page_tableId = " + page_tableId);
        super.onResume();
        if(Pref.getPref_focusView_page_tableId(MainAct.mAct) == page_tableId) {
            System.out.println("Page_recycler / _onResume / resume_listView_vScroll");
            TabsHost.resume_listView_vScroll(recyclerView);
        }
    }

    public void fillData()
    {
        mDb_page = new DB_page(getActivity(), page_tableId);
        mDb_page.open();
        mCursor_note = mDb_page.mCursor_note;

        mItemAdapter = new PageAdapter_recycler(mCursor_note,page_pos, this);

        mDb_page.close();// set close here, if cursor is used in mTabsPagerAdapter

        // Set PageAdapter_recycler as the adapter for RecyclerView.
        recyclerView.setAdapter(mItemAdapter);

        //init
        TabsHost.showFooter(mAct);
    }

    // swap rows
    protected static void swapRows(DB_page dB_page, int startPosition, int endPosition)
    {
        Long mNoteNumber1;
        String mNoteTitle1;
        String mNotePictureUri1;
        String mNoteAudioUri1;
        String mNoteLinkUri1;
        String mNoteBodyString1;
        int mMarkingIndex1;
        Long mCreateTime1;
        Long mNoteNumber2 ;
        String mNotePictureUri2;
        String mNoteAudioUri2;
        String mNoteLinkUri2;
        String mNoteTitle2;
        String mNoteBodyString2;
        int mMarkingIndex2;
        Long mCreateTime2;

        dB_page.open();
        mNoteNumber1 = dB_page.getNoteId(startPosition,false);
        mNoteTitle1 = dB_page.getNoteTitle(startPosition,false);
        mNotePictureUri1 = dB_page.getNotePictureUri(startPosition,false);
        mNoteAudioUri1 = dB_page.getNoteAudioUri(startPosition,false);
        mNoteLinkUri1 = dB_page.getNoteLinkUri(startPosition,false);
        mNoteBodyString1 = dB_page.getNoteBody(startPosition,false);
        mMarkingIndex1 = dB_page.getNoteMarking(startPosition,false);
        mCreateTime1 = dB_page.getNoteCreatedTime(startPosition,false);

        mNoteNumber2 = dB_page.getNoteId(endPosition,false);
        mNoteTitle2 = dB_page.getNoteTitle(endPosition,false);
        mNotePictureUri2 = dB_page.getNotePictureUri(endPosition,false);
        mNoteAudioUri2 = dB_page.getNoteAudioUri(endPosition,false);
        mNoteLinkUri2 = dB_page.getNoteLinkUri(endPosition,false);
        mNoteBodyString2 = dB_page.getNoteBody(endPosition,false);
        mMarkingIndex2 = dB_page.getNoteMarking(endPosition,false);
        mCreateTime2 = dB_page.getNoteCreatedTime(endPosition,false);

        dB_page.updateNote(mNoteNumber2,
                mNoteTitle1,
                mNotePictureUri1,
                mNoteAudioUri1,
                "",
                mNoteLinkUri1,
                mNoteBodyString1,
                mMarkingIndex1,
                mCreateTime1,false);

        dB_page.updateNote(mNoteNumber1,
                mNoteTitle2,
                mNotePictureUri2,
                mNoteAudioUri2,
                "",
                mNoteLinkUri2,
                mNoteBodyString2,
                mMarkingIndex2,
                mCreateTime2,false);

        dB_page.close();
    }

    static public void swap(DB_page dB_page)
    {
        int startCursor = dB_page.getNotesCount(true)-1;
        int endCursor = 0;

        //reorder data base storage for ADD_NEW_TO_TOP option
        int loop = Math.abs(startCursor-endCursor);
        for(int i=0;i< loop;i++)
        {
            swapRows(dB_page, startCursor,endCursor);
            if((startCursor-endCursor) >0)
                endCursor++;
            else
                endCursor--;
        }
    }


    public int getNotesCountInPage(AppCompatActivity mAct)
    {
        DB_page mDb_page = new DB_page(mAct,page_tableId );
        mDb_page.open();
        int count = mDb_page.getNotesCount(false);
        mDb_page.close();
        return count;
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }
}
