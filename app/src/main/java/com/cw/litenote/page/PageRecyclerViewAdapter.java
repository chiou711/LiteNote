package com.cw.litenote.page;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cw.litenote.R;
import com.cw.litenote.db.DB_drawer;
import com.cw.litenote.db.DB_page;
import com.cw.litenote.folder.FolderUi;
import com.cw.litenote.main.MainAct;
import com.cw.litenote.note.Note;
import com.cw.litenote.note.Note_edit;
import com.cw.litenote.operation.audio.AudioManager;
import com.cw.litenote.operation.audio.AudioPlayer_page;
import com.cw.litenote.tabs.TabsHost;
import com.cw.litenote.util.ColorSet;
import com.cw.litenote.util.CustomWebView;
import com.cw.litenote.util.Util;
import com.cw.litenote.util.audio.UtilAudio;
import com.cw.litenote.util.image.AsyncTaskAudioBitmap;
import com.cw.litenote.util.image.UtilImage;
import com.cw.litenote.util.image.UtilImage_bitmapLoader;
import com.cw.litenote.util.uil.UilCommon;
import com.cw.litenote.util.video.UtilVideo;
import com.mobeta.android.dslv.DragSortListView;

import static android.content.ContentValues.TAG;
import static com.cw.litenote.db.DB_page.KEY_NOTE_AUDIO_URI;
import static com.cw.litenote.db.DB_page.KEY_NOTE_BODY;
import static com.cw.litenote.db.DB_page.KEY_NOTE_CREATED;
import static com.cw.litenote.db.DB_page.KEY_NOTE_LINK_URI;
import static com.cw.litenote.db.DB_page.KEY_NOTE_MARKING;
import static com.cw.litenote.db.DB_page.KEY_NOTE_PICTURE_URI;
import static com.cw.litenote.db.DB_page.KEY_NOTE_TITLE;
import static com.cw.litenote.page.RecyclerViewFragment.mDb_page;

// Pager adapter
public class PageRecyclerViewAdapter extends RecyclerView.Adapter<PageRecyclerViewAdapter.ViewHolder>
{
	private AppCompatActivity mAct;
	private Cursor cursor;
	private int count;
	private String linkUri;
	private int style;
	private int page_pos;

    PageRecyclerViewAdapter(Cursor _cursor,int _page_pos) {
        cursor = _cursor;
        page_pos = _page_pos;
        style = Util.getCurrentPageStyle(_page_pos);

        if(_cursor != null)
            count = _cursor.getCount();
        else
            count = 0;

        System.out.println("PageRecyclerViewAdapter / _constructor / count = " + count);
        System.out.println("PageRecyclerViewAdapter / _constructor / page_pos = " + page_pos);

        // add this for fixing java.lang.IllegalStateException: attempt to re-open an already-closed object
        mDb_page.open();
        mDb_page.close();

        mAct = MainAct.mAct;
    }

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView btnMarking;
        ImageView btnViewNote;
        ImageView btnEditNote;
        ImageView btnPlayAudio;
        ImageView btnPlayYouTube;
        ImageView btnPlayWeb;
		TextView rowId;
		View audioBlock;
		ImageView iconAudio;
		TextView audioName;
		TextView textTitle;
		TextView textBody;
		TextView textTime;
		ImageView btnDrag;
		View thumbBlock;
		ImageView thumbPicture;
		ImageView thumbAudio;
		CustomWebView thumbWeb;
		ProgressBar progressBar;

        public ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Element " + getAdapterPosition() + " clicked.");
                }
            });

            textTitle = (TextView) v.findViewById(R.id.row_title);//todo temp
            rowId= (TextView) v.findViewById(R.id.row_id);
            audioBlock = v.findViewById(R.id.audio_block);
            iconAudio = (ImageView) v.findViewById(R.id.img_audio);
            audioName = (TextView) v.findViewById(R.id.row_audio_name);
            btnMarking = (ImageView) v.findViewById(R.id.btn_marking);
            btnViewNote = (ImageView) v.findViewById(R.id.btn_view_note);
            btnEditNote = (ImageView) v.findViewById(R.id.btn_edit_note);
            btnPlayAudio = (ImageView) v.findViewById(R.id.btn_play_audio);
            btnPlayYouTube = (ImageView) v.findViewById(R.id.btn_play_youtube);
            btnPlayWeb = (ImageView) v.findViewById(R.id.btn_play_web);
            thumbBlock = v.findViewById(R.id.row_thumb_nail);
            thumbPicture = (ImageView) v.findViewById(R.id.thumb_picture);
            thumbAudio = (ImageView) v.findViewById(R.id.thumb_audio);
            thumbWeb = (CustomWebView) v.findViewById(R.id.thumb_web);
            btnDrag = (ImageView) v.findViewById(R.id.img_dragger);
            progressBar = (ProgressBar) v.findViewById(R.id.thumb_progress);
            textTitle = (TextView) v.findViewById(R.id.row_title);
            textBody = (TextView) v.findViewById(R.id.row_body);
            textTime = (TextView) v.findViewById(R.id.row_time);
        }

        public TextView getTextView() {
            return textTitle;
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.page_view_card, viewGroup, false);//todo temp

        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        System.out.println("PageRecyclerViewAdapter / _onBindViewHolder / page_pos = " + page_pos);

        // get DB data
        String strTitle = null;
        String strBody = null;
        String pictureUri = null;
        String audioUri = null;
        Long timeCreated = null;
        linkUri = null;
        int marking = 0;

        if(cursor.moveToPosition(position)) {
            strTitle = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE_TITLE));
            strBody = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE_BODY));
            pictureUri = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE_PICTURE_URI));
            audioUri = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE_AUDIO_URI));
            linkUri = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE_LINK_URI));
            marking = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NOTE_MARKING));
            timeCreated = cursor.getLong(cursor.getColumnIndex(KEY_NOTE_CREATED));
        }

		SharedPreferences pref_show_note_attribute = MainAct.mAct.getSharedPreferences("show_note_attribute", 0);

        if(cursor.moveToPosition(position)) {
            strTitle = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE_TITLE));
            strBody = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE_BODY));
            pictureUri = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE_PICTURE_URI));
            audioUri = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE_AUDIO_URI));
            linkUri = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE_LINK_URI));
            marking = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NOTE_MARKING));
            timeCreated = cursor.getLong(cursor.getColumnIndex(KEY_NOTE_CREATED));
        }

        /**
         *  control block
         */
        // show row Id
        holder.rowId.setText(String.valueOf(position+1));
        holder.rowId.setTextColor(ColorSet.mText_ColorArray[style]);


        // show marking check box
        if(marking == 1)
        {
            holder.btnMarking.setBackgroundResource(style % 2 == 1 ?
                    R.drawable.btn_check_on_holo_light :
                    R.drawable.btn_check_on_holo_dark);
        }
        else
        {
            holder.btnMarking.setBackgroundResource(style % 2 == 1 ?
                    R.drawable.btn_check_off_holo_light :
                    R.drawable.btn_check_off_holo_dark);
        }

        // on mark note
        holder.btnMarking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println("PageAdapter / _getView / btnMarking / _onClick");
                // toggle marking
                toggleNoteMarking(mAct,position);

                // Stop if unmarked item is at playing state
                if(AudioManager.mAudioPos == position) {
                    UtilAudio.stopAudioIfNeeded();
                }

                //Toggle marking will resume page, so do Store v scroll
//                DragSortListView listView = TabsHost.mTabsPagerAdapter.fragmentList.get(TabsHost.getFocus_tabPos()).drag_listView;
                DragSortListView listView = null;//todo temp
                TabsHost.store_listView_vScroll(listView);
                TabsHost.isDoingMarking = true;

                TabsHost.reloadCurrentPage();
                TabsHost.showFooter(MainAct.mAct);

                // update audio info
                if(PageUi.isAudioPlayingPage()) {
                    System.out.println("PageAdapter / _getView / btnMarking / is AudioPlayingPage");
                    AudioPlayer_page.prepareAudioInfo();
                }
            }
        });

        // show drag button
        if(pref_show_note_attribute.getString("KEY_ENABLE_DRAGGABLE", "no").equalsIgnoreCase("yes"))
            holder.btnDrag.setVisibility(View.VISIBLE);
        else
            holder.btnDrag.setVisibility(View.GONE);

        // on view note
        holder.btnViewNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                TabsHost.getCurrentPage().currPlayPosition = position;//todo temp
                DB_page db_page = new DB_page(mAct,TabsHost.getCurrentPageTableId());
                int count = db_page.getNotesCount(true);
                if(position < count)
                {
                    // apply Note class
                    Intent intent;
                    intent = new Intent(mAct, Note.class);
                    intent.putExtra("POSITION", position);
                    mAct.startActivity(intent);
                }
            }
        });

        // on edit note
        holder.btnEditNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mAct, Note_edit.class);
                DB_page db_page = new DB_page(mAct, TabsHost.getCurrentPageTableId());
                Long rowId = db_page.getNoteId(position,true);
                i.putExtra("list_view_position", position);
                i.putExtra(DB_page.KEY_NOTE_ID, rowId);
                i.putExtra(DB_page.KEY_NOTE_TITLE, db_page.getNoteTitle_byId(rowId));
                i.putExtra(DB_page.KEY_NOTE_PICTURE_URI , db_page.getNotePictureUri_byId(rowId));
                i.putExtra(DB_page.KEY_NOTE_AUDIO_URI , db_page.getNoteAudioUri_byId(rowId));
                i.putExtra(DB_page.KEY_NOTE_LINK_URI , db_page.getNoteLinkUri_byId(rowId));
                i.putExtra(DB_page.KEY_NOTE_BODY, db_page.getNoteBody_byId(rowId));
                i.putExtra(DB_page.KEY_NOTE_CREATED, db_page.getNoteCreatedTime_byId(rowId));
                mAct.startActivity(i);            }
        });

        // show audio button
        if( !Util.isEmptyString(audioUri) && (marking == 1))
            holder.btnPlayAudio.setVisibility(View.VISIBLE);
        else
            holder.btnPlayAudio.setVisibility(View.GONE);

		// on play audio
        holder.btnPlayAudio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                AudioManager.setAudioPlayMode(AudioManager.PAGE_PLAY_MODE);
                DB_page db_page = new DB_page(mAct, TabsHost.getCurrentPageTableId());
                int notesCount = db_page.getNotesCount(true);
                if(position >= notesCount) //end of list
                    return ;

                int marking = db_page.getNoteMarking(position,true);
                String uriString = db_page.getNoteAudioUri(position,true);

                boolean isAudioUri = false;
                if( !Util.isEmptyString(uriString) && (marking == 1))
                    isAudioUri = true;

                if(position < notesCount) // avoid footer error
                {
                    if(isAudioUri)
                    {
                        // cancel playing
                        if(AudioManager.mMediaPlayer != null)
                        {
                            if(AudioManager.mMediaPlayer.isPlaying())
                                AudioManager.mMediaPlayer.pause();

                            if(TabsHost.audioPlayer_page != null) {
                                AudioPlayer_page.mAudioHandler.removeCallbacks(TabsHost.audioPlayer_page.page_runnable);
                            }
                            AudioManager.mMediaPlayer.release();
                            AudioManager.mMediaPlayer = null;
                        }

                        AudioManager.setPlayerState(AudioManager.PLAYER_AT_PLAY);

                        // create new Intent to play audio
                        AudioManager.mAudioPos = position;
                        AudioManager.setAudioPlayMode(AudioManager.PAGE_PLAY_MODE);

                        //todo temp
//                        TabsHost.audioUi_page = new AudioUi_page(mAct, TabsHost.getCurrentPage().drag_listView);
                        TabsHost.audioUi_page.initAudioBlock(MainAct.mAct);

                        TabsHost.audioPlayer_page = new AudioPlayer_page(mAct,TabsHost.audioUi_page);
                        AudioPlayer_page.prepareAudioInfo();
                        TabsHost.audioPlayer_page.runAudioState();

                        // update audio play position
                        TabsHost.audioPlayTabPos = page_pos;
                        TabsHost.mTabsPagerAdapter.notifyDataSetChanged();

                        UtilAudio.updateAudioPanel(TabsHost.audioUi_page.audioPanel_play_button,
                                TabsHost.audioUi_page.audio_panel_title_textView);

                        // update playing page position
                        MainAct.mPlaying_pagePos = TabsHost.getFocus_tabPos();

                        // update playing page table Id
                        MainAct.mPlaying_pageTableId = TabsHost.getCurrentPageTableId();

                        // update playing folder position
                        MainAct.mPlaying_folderPos = FolderUi.getFocus_folderPos();

                        // update playing folder table Id
                        DB_drawer dB_drawer = new DB_drawer(mAct);
                        MainAct.mPlaying_folderTableId = dB_drawer.getFolderTableId(MainAct.mPlaying_folderPos,true);
                    }
                }

                TabsHost.getPage_rowItemView(position);

            }
		});

        // show/hide play YouTube button, on play Web button
        if(!Util.isEmptyString(linkUri) &&
           linkUri.startsWith("http")      )
        {
            if(Util.isYouTubeLink(linkUri))
            {
                // YouTube
                holder.btnPlayYouTube.setVisibility(View.VISIBLE);
                holder.btnPlayWeb.setVisibility(View.GONE);
            }
            else
            {
                // Web
                holder.btnPlayYouTube.setVisibility(View.GONE);
                holder.btnPlayWeb.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            holder.btnPlayYouTube.setVisibility(View.GONE);
            holder.btnPlayWeb.setVisibility(View.GONE);
        }

        // on play YouTube
        holder.btnPlayYouTube.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                //todo temp
//                TabsHost.getCurrentPage().currPlayPosition = position;
                DB_page db_page = new DB_page(mAct, TabsHost.getCurrentPageTableId());
                db_page.open();
                int count = db_page.getNotesCount(false);
                String linkStr = db_page.getNoteLinkUri(position, false);
                db_page.close();

                if (position < count) {
                    if (Util.isYouTubeLink(linkStr)) {
                        AudioManager.stopAudioPlayer();

                        // apply native YouTube
                        Util.openLink_YouTube(mAct, linkStr);
                    }
                }
            }
        });

        // on play Web
        holder.btnPlayWeb.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                DB_page db_page = new DB_page(mAct, TabsHost.getCurrentPageTableId());
                linkUri = db_page.getNoteLinkUri(position, true);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkUri));
                MainAct.mAct.startActivity(intent);
            }
        });

        // set audio name
        String audio_name = null;
        if(!Util.isEmptyString(audioUri))
            audio_name = Util.getDisplayNameByUriString(audioUri, mAct);

        // show audio name
        if(Util.isUriExisted(audioUri, mAct))
            holder.audioName.setText(audio_name);
        else
            holder.audioName.setText(R.string.file_not_found);

//			holder.audioName.setTextSize(12.0f);

        if(!Util.isEmptyString(audioUri))
            holder.audioName.setTextColor(ColorSet.mText_ColorArray[style]);

        // show audio highlight if audio is not at Stop
        if( PageUi.isAudioPlayingPage() &&
            (position == AudioManager.mAudioPos)  &&
            (AudioManager.getPlayerState() != AudioManager.PLAYER_AT_STOP) &&
            (AudioManager.getAudioPlayMode() == AudioManager.PAGE_PLAY_MODE) 	)
        {
//            System.out.println("PageAdapter / _getView / show highlight / position = " + position);
            //todo temp
//            TabsHost.getCurrentPage().mHighlightPosition = position;
            holder.audioBlock.setBackgroundResource(R.drawable.bg_highlight_border);
            holder.audioBlock.setVisibility(View.VISIBLE);

            // set type face
//			holder.audioName.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            holder.audioName.setTextColor(ColorSet.getHighlightColor(mAct));

            // set icon
            holder.iconAudio.setVisibility(View.VISIBLE);
            holder.iconAudio.setImageResource(R.drawable.ic_audio);

            // set animation
//			Animation animation = AnimationUtils.loadAnimation(mContext , R.anim.right_in);
//			holder.audioBlock.startAnimation(animation);
        }
        else
        {

//			System.out.println("PageAdapter / _getView / not show highlight ");
            holder.audioBlock.setBackgroundResource(R.drawable.bg_gray_border);
            holder.audioBlock.setVisibility(View.VISIBLE);

            // set type face
//			holder.audioName.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);

            // set icon
            holder.iconAudio.setVisibility(View.VISIBLE);
            if(style % 2 == 0)
                holder.iconAudio.setImageResource(R.drawable.ic_audio_off_white);
            else
                holder.iconAudio.setImageResource(R.drawable.ic_audio_off_black);
        }

        // show audio icon and block
        if(Util.isEmptyString(audioUri))
        {
            holder.iconAudio.setVisibility(View.GONE);
            holder.audioBlock.setVisibility(View.GONE);
        }

		// show text title
		if( Util.isEmptyString(strTitle) )
		{
			if(Util.isYouTubeLink(linkUri)) {
				strTitle = Util.getYouTubeTitle(linkUri);
				holder.textTitle.setVisibility(View.VISIBLE);
				holder.textTitle.setText(strTitle);
				holder.textTitle.setTextColor(Color.GRAY);
			}
			else if( (linkUri != null) && (linkUri.startsWith("http")))
			{
				holder.textTitle.setVisibility(View.VISIBLE);
				Util.setHttpTitle(linkUri, mAct,holder.textTitle);
			}
			else
			{
				// make sure empty title is empty after scrolling
				holder.textTitle.setVisibility(View.VISIBLE);
				holder.textTitle.setText("");
			}
		}
		else
		{
			holder.textTitle.setVisibility(View.VISIBLE);
			holder.textTitle.setText(strTitle);
			holder.textTitle.setTextColor(ColorSet.mText_ColorArray[style]);
		}

		// set YouTube thumb nail if picture Uri is none and YouTube link exists
		if(Util.isEmptyString(pictureUri) &&
		   Util.isYouTubeLink(linkUri)      )
		{
			pictureUri = "http://img.youtube.com/vi/"+Util.getYoutubeId(linkUri)+"/0.jpg";
		}

        System.out.println("PageRecyclerViewAdapter / _onBindViewHolder / position = " + position);
        System.out.println("PageRecyclerViewAdapter / _onBindViewHolder / strTitle = " + strTitle);
        System.out.println("PageRecyclerViewAdapter / _onBindViewHolder / pictureUri = " + pictureUri);

		// case 1: show thumb nail if picture Uri exists
		if(UtilImage.hasImageExtension(pictureUri, mAct ) ||
		   UtilVideo.hasVideoExtension(pictureUri, mAct )   )
		{
			holder.thumbBlock.setVisibility(View.VISIBLE);
			holder.thumbPicture.setVisibility(View.VISIBLE);
			holder.thumbAudio.setVisibility(View.GONE);
			holder.thumbWeb.setVisibility(View.GONE);
			// load bitmap to image view
			try
			{
                System.out.println("PageRecyclerViewAdapter / _onBindViewHolder / case 1");
				new UtilImage_bitmapLoader(holder.thumbPicture,
										   pictureUri,
										   holder.progressBar,
                                           UilCommon.optionsForFadeIn,
										   mAct);
			}
			catch(Exception e)
			{
				Log.e("PageRecyclerViewAdapter", "UtilImage_bitmapLoader error");
				holder.thumbBlock.setVisibility(View.GONE);
				holder.thumbPicture.setVisibility(View.GONE);
				holder.thumbAudio.setVisibility(View.GONE);
				holder.thumbWeb.setVisibility(View.GONE);
			}
		}
		// case 2: show audio thumb nail if picture Uri is none and audio Uri exists
		else if((Util.isEmptyString(pictureUri) && UtilAudio.hasAudioExtension(audioUri) ) )
		{
			holder.thumbBlock.setVisibility(View.VISIBLE);
			holder.thumbPicture.setVisibility(View.GONE);
			holder.thumbAudio.setVisibility(View.VISIBLE);
			holder.thumbWeb.setVisibility(View.GONE);

            try {
                AsyncTaskAudioBitmap audioAsyncTask;
                audioAsyncTask = new AsyncTaskAudioBitmap(mAct,
                        audioUri,
                        holder.thumbAudio,
                        holder.progressBar,
                        false);
                audioAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "Searching media ...");
            } catch (Exception e) {
                Log.e("PageAdapter", "AsyncTaskAudioBitmap error");
                holder.thumbBlock.setVisibility(View.GONE);
                holder.thumbPicture.setVisibility(View.GONE);
                holder.thumbAudio.setVisibility(View.GONE);
                holder.thumbWeb.setVisibility(View.GONE);
            }
		}
		// case 3: set web title and web view thumb nail of link if no title content
		else if(!Util.isEmptyString(linkUri) &&
                linkUri.startsWith("http")   &&
				!Util.isYouTubeLink(linkUri)   )
		{
			// reset web view
			CustomWebView.pauseWebView(holder.thumbWeb);
			CustomWebView.blankWebView(holder.thumbWeb);

			holder.thumbBlock.setVisibility(View.VISIBLE);
			holder.thumbWeb.setInitialScale(50);
			holder.thumbWeb.getSettings().setJavaScriptEnabled(true);//Using setJavaScriptEnabled can introduce XSS vulnerabilities
			holder.thumbWeb.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT );
			holder.thumbWeb.loadUrl(linkUri);
			holder.thumbWeb.setVisibility(View.VISIBLE);

			holder.thumbPicture.setVisibility(View.GONE);
			holder.thumbAudio.setVisibility(View.GONE);

			//Add for non-stop showing of full screen web view
			holder.thumbWeb.setWebViewClient(new WebViewClient() {
				@Override
			    public boolean shouldOverrideUrlLoading(WebView view, String url)
			    {
			        view.loadUrl(url);
			        return true;
			    }
			});


			if (Util.isEmptyString(strTitle)) {

				holder.thumbWeb.setWebChromeClient(new WebChromeClient() {
					@Override
					public void onReceivedTitle(WebView view, String title) {
						super.onReceivedTitle(view, title);
						if (!TextUtils.isEmpty(title) &&
								!title.equalsIgnoreCase("about:blank")) {
							holder.textTitle.setVisibility(View.VISIBLE);
							holder.rowId.setText(String.valueOf(position + 1));
							holder.rowId.setTextColor(ColorSet.mText_ColorArray[style]);

						}
					}
				});
			}
		}
		else
		{
			holder.thumbBlock.setVisibility(View.GONE);
			holder.thumbPicture.setVisibility(View.GONE);
			holder.thumbAudio.setVisibility(View.GONE);
			holder.thumbWeb.setVisibility(View.GONE);
		}

		// Show text body
	  	if(pref_show_note_attribute.getString("KEY_SHOW_BODY", "yes").equalsIgnoreCase("yes"))
	  	{
	  		// test only: enabled for showing picture path
//            String strBody = cursor.getString(cursor.getColumnIndex(KEY_NOTE_BODY));
	  		if(!Util.isEmptyString(strBody)){
				//normal: do nothing
			}
	  		else if(!Util.isEmptyString(pictureUri)) {
//				strBody = pictureUri;//show picture Uri
			}
	  		else if(!Util.isEmptyString(linkUri)) {
//				strBody = linkUri; //show link Uri
			}

			holder.textBody.setText(strBody);
//			holder.textBody.setTextSize(12);

//			holder.rowDivider.setVisibility(View.VISIBLE);
			holder.textBody.setTextColor(ColorSet.mText_ColorArray[style]);
			// time stamp
            holder.textTime.setText(Util.getTimeString(timeCreated));
			holder.textTime.setTextColor(ColorSet.mText_ColorArray[style]);
	  	}
	  	else
	  	{
            holder.textBody.setVisibility(View.GONE);
            holder.textTime.setVisibility(View.GONE);
	  	}

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return cursor.getCount();//mDataSet.length;
    }

    // toggle mark of note
    public static int toggleNoteMarking(AppCompatActivity mAct, int position)
    {
        int marking = 0;
		DB_page db_page = new DB_page(mAct,TabsHost.getCurrentPageTableId());
        db_page.open();
        int count = db_page.getNotesCount(false);
        if(position >= count) //end of list
        {
            db_page.close();
            return marking;
        }

        String strNote = db_page.getNoteTitle(position,false);
        String strPictureUri = db_page.getNotePictureUri(position,false);
        String strAudioUri = db_page.getNoteAudioUri(position,false);
        String strLinkUri = db_page.getNoteLinkUri(position,false);
        String strNoteBody = db_page.getNoteBody(position,false);
        Long idNote =  db_page.getNoteId(position,false);

        // toggle the marking
        if(db_page.getNoteMarking(position,false) == 0)
        {
            db_page.updateNote(idNote, strNote, strPictureUri, strAudioUri, "", strLinkUri, strNoteBody, 1, 0, false);
            marking = 1;
        }
        else
        {
            db_page.updateNote(idNote, strNote, strPictureUri, strAudioUri, "", strLinkUri, strNoteBody, 0, 0, false);
            marking = 0;
        }
        db_page.close();

        System.out.println("PageAdapter / _toggleNoteMarking / position = " + position + ", marking = " + db_page.getNoteMarking(position,true));
        return  marking;
    }

}