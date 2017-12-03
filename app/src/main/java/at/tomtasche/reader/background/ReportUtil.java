package at.tomtasche.reader.background;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ApplicationErrorReport;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import at.tomtasche.reader.R;

public class ReportUtil {

	@TargetApi(14)
	public static Intent createFeedbackIntent(Context context, Throwable error) {
		ApplicationErrorReport report = new ApplicationErrorReport();
		report.packageName = report.processName = context.getPackageName();
		report.time = System.currentTimeMillis();
		report.type = ApplicationErrorReport.TYPE_CRASH;
		report.systemApp = false;

		ApplicationErrorReport.CrashInfo crash = new ApplicationErrorReport.CrashInfo();
		crash.exceptionClassName = error.getClass().getSimpleName();
		crash.exceptionMessage = error.getMessage();

		StringWriter writer = new StringWriter();
		PrintWriter printer = new PrintWriter(writer);
		error.printStackTrace(printer);

		crash.stackTrace = writer.toString();

		StackTraceElement stack = error.getStackTrace()[0];
		crash.throwClassName = stack.getClassName();
		crash.throwFileName = stack.getFileName();
		crash.throwLineNumber = stack.getLineNumber();
		crash.throwMethodName = stack.getMethodName();

		report.crashInfo = crash;

		Intent intent = new Intent(Intent.ACTION_APP_ERROR);
		intent.putExtra(Intent.EXTRA_BUG_REPORT, report);

		return intent;
	}

	public static void submitFile(Context context, Throwable error,
			Uri originalCacheUri, Uri originalUri, Uri htmlCacheUri,
			int errorDescription) {
		submitFile(context, error, originalCacheUri, originalUri, htmlCacheUri,
				context.getString(errorDescription));
	}

	public static void submitFile(final Context context, final Throwable error,
			final Uri originalCacheUri, final Uri originalUri,
			final Uri htmlCacheUri, final String errorDescription) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.toast_error_generic);
		builder.setMessage(errorDescription
				+ System.getProperty("line.separator")
				+ System.getProperty("line.separator")
				+ context.getString(R.string.dialog_submit_file));
		builder.setNegativeButton(android.R.string.no, null);
		builder.setNeutralButton(R.string.dialog_error_send_error_only,
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						context.startActivity(ReportUtil.createFeedbackIntent(
								context, error));
					}
				});
		builder.setPositiveButton(android.R.string.yes, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Bundle bundle = new Bundle();
				bundle.putStringArray(Intent.EXTRA_EMAIL,
						new String[] { "tickets@opendocument.uservoice.com" });

				ArrayList<Uri> uris = new ArrayList<Uri>();
				uris.add(originalCacheUri);
				if (htmlCacheUri != null) {
					uris.add(htmlCacheUri);
				}
				bundle.putParcelableArrayList(Intent.EXTRA_STREAM, uris);

				String version;
				try {
					version = context.getPackageManager().getPackageInfo(
							context.getPackageName(), 0).versionName;
				} catch (NameNotFoundException e1) {
					version = "unknown";
				}
				bundle.putString(Intent.EXTRA_SUBJECT, "OpenDocument Reader ("
						+ version + "): Error occurred");

				StringWriter writer = new StringWriter();
				PrintWriter printer = new PrintWriter(writer);
				printer.println("-----------------");
				printer.println("Information for the developer:");
				printer.println("- " + Build.MODEL + " running Android "
						+ Build.VERSION.SDK_INT);
				printer.println("- The following error occured while opening the file located at: "
						+ originalUri.toString());
				printer.println(errorDescription);
				printer.println();
				error.printStackTrace(printer);
				printer.println();
				printer.println("-----------------");

				try {
					printer.close();
					writer.close();
				} catch (IOException e) {
				}

				bundle.putString(Intent.EXTRA_TEXT, writer.toString());

				Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
				intent.setType("plain/text");
				intent.putExtras(bundle);

				context.startActivity(Intent.createChooser(intent,
						context.getString(R.string.dialog_submit_file_title)));

				dialog.dismiss();
			}
		});

		builder.show();
	}
}
