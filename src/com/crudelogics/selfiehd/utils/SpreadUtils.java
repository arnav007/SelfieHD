package com.crudelogics.selfiehd.utils;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.widget.Toast;

public class SpreadUtils {

	private static final String REVIEW_PREFS = "Review_pref_data";

	private static final String KEY_REVIEW_DONE = "ReviewDone";

	private static final String KEY_COUNT = "count";

	public interface ReviewDialogFinishListener {

		void onReviewDialogFinish();
	}

	public static boolean isReviewNeeded(Context ctx, int count,
			boolean incrementCount) {
		if (incrementCount && getCount(ctx) <= count + 1) {
			incrementCount(ctx);
		}
		if (isReviewDone(ctx) || !isConnectionAvailable(ctx)
				|| getCount(ctx) < count) {
			return false;
		}
		return true;

	}

	public static Dialog getReviewDialog(final Context ctx,
			final ReviewDialogFinishListener listener, String title,
			int drawableId, String msg) {

		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle(title);
		if (drawableId != -1) {
			builder.setIcon(drawableId);
		}
		builder.setMessage(msg)
				.setCancelable(false)
				.setPositiveButton("Rate it",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int id) {
								startReview(ctx);
								if (listener != null) {
									listener.onReviewDialogFinish();
									setReviewDone(ctx, true);
								}

							}
						})
				.setNeutralButton("Later",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int id) {
								if (listener != null) {
									listener.onReviewDialogFinish();
								}
							}
						});
		AlertDialog alert = builder.create();
		return alert;
	}

	public static void setReviewDone(Context context, boolean isDone) {

		SharedPreferences sharedPreferences = context.getSharedPreferences(
				REVIEW_PREFS, Context.MODE_PRIVATE);
		Editor edit = sharedPreferences.edit();
		edit.putBoolean(KEY_REVIEW_DONE, isDone);
		edit.commit();

	}

	public static boolean isReviewDone(Context context) {

		SharedPreferences sharedPreferences = context.getSharedPreferences(
				REVIEW_PREFS, Context.MODE_PRIVATE);
		return sharedPreferences.getBoolean(KEY_REVIEW_DONE, false);
	}

	public static void incrementCount(Context context) {

		SharedPreferences sharedPreferences = context.getSharedPreferences(
				REVIEW_PREFS, Context.MODE_PRIVATE);
		Editor edit = sharedPreferences.edit();
		edit.putInt(KEY_COUNT, sharedPreferences.getInt(KEY_COUNT, 0) + 1);
		edit.commit();

	}

	public static int getCount(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				REVIEW_PREFS, Context.MODE_PRIVATE);
		return sharedPreferences.getInt(KEY_COUNT, 0);
	}

	public static void getMoreApps(Context context, String publisherName) {
		Intent intent = new Intent(Intent.ACTION_VIEW,
				getMoreAppsUri(publisherName));

		if (isIntentAvailable(context, intent)) {
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		} else {
			Toast.makeText(context, "Network Error", Toast.LENGTH_LONG).show();
		}
	}

	public static void feedback(Context context, String feedBackEmailId,
			String emailSubject, String msg) {

		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.putExtra(Intent.EXTRA_EMAIL,
				new String[] { feedBackEmailId });
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
		emailIntent.putExtra(Intent.EXTRA_TEXT, msg);
		emailIntent.setType("message/rfc822");
		emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		if (isIntentAvailable(context, emailIntent)) {
			context.startActivity(emailIntent);
		} else {
			Toast.makeText(context, "No Email Application Found",
					Toast.LENGTH_LONG).show();
		}
	}

	public static void shareApp(Context context, String chooserTitle,
			String message, String messageSubject) {
		message = message + getShareUrl(context);

		Intent mainIntent = new Intent();
		mainIntent.setAction(Intent.ACTION_SEND);
		mainIntent.setType("text/plain");
		mainIntent.putExtra(Intent.EXTRA_TEXT, message);
		mainIntent.putExtra(Intent.EXTRA_SUBJECT, messageSubject);
		mainIntent.setPackage("com.android.bluetooth");

		PackageManager pm = context.getPackageManager();

		Intent sentIntent = new Intent(Intent.ACTION_SEND);
		sentIntent.setType("text/plain");
		sentIntent.putExtra(Intent.EXTRA_SUBJECT, messageSubject);
		sentIntent.putExtra(Intent.EXTRA_TEXT, message);

		Intent openInchooser = Intent.createChooser(mainIntent, chooserTitle);

		List<ResolveInfo> resinfo = pm.queryIntentActivities(sentIntent, 0);
		List<LabeledIntent> intentList2 = new ArrayList<LabeledIntent>();

		for (int i = 0; i < resinfo.size(); i++) {
			ResolveInfo ri = resinfo.get(i);
			String packageName = ri.activityInfo.packageName;
			if (packageName.contains("bluetooth")) {
				continue;
			}

			Intent intent = new Intent();
			intent.setComponent(new ComponentName(packageName,
					ri.activityInfo.name));
			intent.setAction(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_SUBJECT, messageSubject);
			intent.putExtra(Intent.EXTRA_TEXT, message);

			intentList2.add(new LabeledIntent(intent, packageName, ri
					.loadLabel(pm), ri.icon));
		}

		String[] shortOrder = { "mms", "whatsapp", "tencent.mm", "hike",
				"line", "nimbuzz", "chaton", "android.email", "android.gm" };
		for (int i = shortOrder.length - 1; i >= 0; i--) {
			String pkgpart = shortOrder[i];
			for (int j = 0; j < intentList2.size(); j++) {
				LabeledIntent labeledIntent = intentList2.get(j);
				if (labeledIntent.getSourcePackage().contains(pkgpart)) {
					LabeledIntent remove = intentList2.remove(j);
					intentList2.add(0, remove);
					break;
				}
			}
		}

		LabeledIntent[] extIntents = new LabeledIntent[intentList2.size()];
		for (int i = 0; i < intentList2.size(); i++) {
			extIntents[i] = intentList2.get(i);
		}

		openInchooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extIntents);
		context.startActivity(openInchooser);

	}

	private static String getShareUrl(Context context) {

		return "\n" + "http://play.google.com/store/apps/details?id="
				+ context.getPackageName();
	}

	public static void startReview(Context context) {

		Intent intent = new Intent(Intent.ACTION_VIEW, getReviewUri(context));
		if (isIntentAvailable(context, intent)) {
			context.startActivity(intent);
			setReviewDone(context, true);
		} else {
			setReviewDone(context, false);
			Toast.makeText(context, "Network Error", Toast.LENGTH_LONG).show();
		}

	}

	private static Uri getReviewUri(Context context) {
		return Uri.parse("market://details?id=" + context.getPackageName());
	}

	private static Uri getMoreAppsUri(String publisherName) {
		return Uri.parse("market://search?q=pub:" + publisherName);
	}

	private static boolean isConnectionAvailable(Context context) {

		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();

		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	public static boolean isIntentAvailable(Context ctx, Intent in) {
		PackageManager packageManager = ctx.getPackageManager();
		List<ResolveInfo> activities = packageManager.queryIntentActivities(in,
				0);
		return (activities == null) ? false : (activities.size() > 0);
	}

	public static String getVersion(Context ctx) {
		PackageInfo pInfo;
		try {
			pInfo = ctx.getPackageManager().getPackageInfo(
					ctx.getPackageName(), 0);
			return pInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return "";
	}

}