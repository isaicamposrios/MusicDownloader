package net.ddns.paolo7297.musicdownloader;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.webkit.WebView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import static net.ddns.paolo7297.musicdownloader.Constants.SERVER_ADDRESS;

/**
 * Created by paolo on 21/02/17.
 */

public class UpdateServer {



    public static void checkUpdate(final Activity activity) {
        final String versionUrl = "http://"+SERVER_ADDRESS+"/projects/musicdownloader/version.php";
        RequestQueue queue = Volley.newRequestQueue(activity);
        StringRequest request = new StringRequest(
                Request.Method.GET,
                versionUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            final JSONObject obj = new JSONObject(response);
                            final String lastestUrl = "http://"+SERVER_ADDRESS+"/projects/musicdownloader/apks/"+obj.getString("name");
                            if (Float.parseFloat(obj.getString("version")) > Float.parseFloat(activity.getPackageManager().getPackageInfo(activity.getPackageName(),0).versionName)) {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(new android.view.ContextThemeWrapper(activity,R.style.FooterPopupStyle));
                                View view = View.inflate(new android.view.ContextThemeWrapper(activity,R.style.FooterPopupStyle), R.layout.dialog_changelog,null);
                                WebView webView = (WebView) view.findViewById(R.id.website);
                                webView.loadUrl("http://"+SERVER_ADDRESS+"/projects/musicdownloader/changelog.php?minver="+activity.getPackageManager().getPackageInfo(activity.getPackageName(),0).versionName);
                                builder.setView(view);
                                builder.setTitle("Aggiornamento disponibile!");
                                builder.setPositiveButton("Aggiorna ora", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        try {
                                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(lastestUrl));
                                            request.setTitle("Aggiornamento di Music Downloader");
                                            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                                            request.setAllowedOverRoaming(false);
                                            request.setDestinationUri(Uri.fromFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+Constants.FOLDER_HOME,obj.getString("name"))));
                                            request.setMimeType("application/vnd.android.package-archive");
                                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                            request.setVisibleInDownloadsUi(true);
                                            DownloadManager dm = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
                                            dm.enqueue(request);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                builder.setNegativeButton("Più tardi", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                                builder.show();
                            } else {
                                //Toast.makeText(activity.getApplicationContext(),"L'applicazione è aggiornata!",Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException | PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(activity.getApplicationContext(),"Non riesco a collegarmi al server!",Toast.LENGTH_LONG).show();
                    }
                }
        );
        queue.add(request);
        queue.start();
    }
}
