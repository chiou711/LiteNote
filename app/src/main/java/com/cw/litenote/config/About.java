package com.cw.litenote.config;


import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.cw.litenote.R;
import com.cw.litenote.main.MainAct;
import com.cw.litenote.util.BaseBackPressedListener;

import de.psdev.licensesdialog.LicensesDialogFragment;

public class About extends Fragment
{

	public About(){}
	static View mRootView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		System.out.println("================ About / onCreateView ==================");

		mRootView = inflater.inflate(R.layout.about, container, false);

		// show licenses
		showLicensesDlg();
		
		// show About
		showAboutDlg();

		// set Back pressed listener
		((MainAct)getActivity()).setOnBackPressedListener(new BaseBackPressedListener(MainAct.mAct));

		return mRootView;
	}   	

    // Show Licenses
    void showLicensesDlg()
    {
		View showLicenses = mRootView.findViewById(R.id.showLicenses);
		showLicenses.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				aboutLicenses();
			}
		});
    }
    
    // About licenses
	void aboutLicenses()
	{
	        final LicensesDialogFragment fragment = LicensesDialogFragment.newInstance(R.raw.notices, false);
	        fragment.show(getActivity().getSupportFragmentManager(), null);
	}
    
    // Show About
    void showAboutDlg()
    {
		View showAbout = mRootView.findViewById(R.id.showAbout);
		showAbout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				aboutDialog();
			}
		});
    }

	
    // About dialog
	void aboutDialog()
	{
		   AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		   PackageInfo pInfo = null;
		   String version_name = "NA";
		   int version_code = 0;
           try 
           {
        	   Context context = getActivity();
        	   pInfo = context.getPackageManager()
        			   		  .getPackageInfo(context.getPackageName(),PackageManager.GET_META_DATA);
           } catch (NameNotFoundException e) {
        	   e.printStackTrace();
           }

           if(pInfo != null)
           {
        	   version_name = pInfo.versionName;
        	   version_code = pInfo.versionCode;
           }
           String msgStr = getActivity().getResources().getString(R.string.about_version_name) +
        		   			" : " + version_name + "\n" + 
        		   		   getActivity().getResources().getString(R.string.about_version_code) +
           					" : " + version_code + "\n\n" + 
   		   				   getActivity().getResources().getString(R.string.EULA_string);
           
		   builder.setTitle(R.string.about_version)
		   		  .setMessage(msgStr)
				  .setNegativeButton(R.string.notices_close, null)
				  .show();
	}
}