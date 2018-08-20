/*
 * Copyright (C) 2018 CW Chiu
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

package com.cw.litenote.util.audio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cw.litenote.R;
import com.cw.litenote.note.AudioUi_note;
import com.cw.litenote.operation.audio.Audio_manager;
import com.cw.litenote.operation.audio.BackgroundAudioService;
import com.cw.litenote.tabs.TabsHost;

// for earphone jack connection on/off
public class NoisyAudioStreamReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent)
	{
        if (android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction()))
		{
			if((BackgroundAudioService.mMediaPlayer != null) && BackgroundAudioService.mMediaPlayer.isPlaying() )
			{
				System.out.println("NoisyAudioStreamReceiver / play -> pause");
                BackgroundAudioService.mMediaPlayer.pause();

                Audio_manager.setPlayerState(Audio_manager.PLAYER_AT_PAUSE);

                //update audio panel button in Page view
                if(Audio_manager.getAudioPlayMode() == Audio_manager.PAGE_PLAY_MODE)
                {
                    UtilAudio.updateAudioPanel(TabsHost.audioUi_page.audioPanel_play_button,
                                               TabsHost.audioUi_page.audio_panel_title_textView);
                }

				//update audio play button in Note view
				if( (AudioUi_note.mPager_audio_play_button != null) &&
					AudioUi_note.mPager_audio_play_button.isShown()    )
				{
					AudioUi_note.mPager_audio_play_button.setImageResource(R.drawable.ic_media_play);
				}
			}
        }
    }
}