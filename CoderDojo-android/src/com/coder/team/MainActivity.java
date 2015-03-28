package com.coder.team;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.google.gson.JsonObject;
import com.syncano.android.lib.Syncano;
import com.syncano.android.lib.SyncanoBase;
import com.syncano.android.lib.modules.Response;
import com.syncano.android.lib.modules.data.ParamsDataGet;
import com.syncano.android.lib.modules.data.ParamsDataNew;
import com.syncano.android.lib.modules.data.ResponseDataGet;
import com.syncano.android.lib.modules.data.ResponseDataNew;
import com.syncano.android.lib.objects.Channel;
import com.syncano.android.lib.objects.Data;
import com.syncano.android.lib.syncserver.DataChanges;
import com.syncano.android.lib.syncserver.SyncServerConnection;
import com.syncano.android.lib.utils.Baser64;

public class MainActivity extends Activity {
	ImageView iv;

	private Syncano syncano;
	private SyncServerConnection syncServer;
	private final String projectId = "6263";
	private final String collectionId = "18809";
	private final String folderName = "img";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		startSyncano();
		//sendData();
		//getData();

		Button btn = (Button) findViewById(R.id.takePhoto);
		iv = (ImageView) findViewById(R.id.imageView);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent inent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(inent, 0);

			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent Data)
	{
		if(requestCode == 0);
		{
			Bitmap theImage =(Bitmap) Data.getExtras().get("data");
			iv.setImageBitmap(theImage);

			Uri imageUri = getImageUri(getApplicationContext(), theImage);
			String imagePath = getRealPathFromURI(imageUri);
			sentImageToSyncano(imagePath);
		}

	}
	
	private void sentImageToSyncano(String imagePath) {
		String imageBaser64 = Baser64.getBaseImageJpg(imagePath);
		
		final ParamsDataNew paramsDataNew = new ParamsDataNew(projectId, collectionId, null, Data.MODERATED);
		paramsDataNew.setFolder(folderName);
		paramsDataNew.setImage(imageBaser64);
		// Sending parameters to Syncano backend
		System.out.println("sendImage");
		syncano.sendAsyncRequest(paramsDataNew, new SyncanoBase.Callback() {
			// Handling response from the server
			@Override
			public void finished(Response response) {
				Data data = ((ResponseDataNew) response).getData();
				if (response.getResultCode() != Response.CODE_SUCCESS) {
					System.out.println("sendImage error");
				} else {
					System.out.println("sendImage success");
				}
			}
		});
		
	}

	private Uri getImageUri(Context inContext, Bitmap inImage) {
	    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
	    inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
	    String path = Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
	    return Uri.parse(path);
	}

	private String getRealPathFromURI(Uri uri) {
	    Cursor cursor = getContentResolver().query(uri, null, null, null, null); 
	    cursor.moveToFirst(); 
	    int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA); 
	    return cursor.getString(idx); 
	}


	private void startSyncano() {
		// TODO Auto-generated method stub
		syncano = new Syncano(this, "old-breeze-627079", "bad7c6180bfac538597487a45f3e001a1e43c481");

		syncServer = new SyncServerConnection(this, "old-breeze-627079", "bad7c6180bfac538597487a45f3e001a1e43c481",
				// This listener will be notified when connection state with Sync Server will be
				// changed or when notification message was received
				new SyncServerConnection.SyncServerListener() {




			@Override
			public void onMessage(String object, JsonObject message) {
				Log.d("SyncServerTag", "Received notification message: " + message);
			}

			@Override
			public void onError(String why) {
				Log.d("Error",why);
			}

			@Override
			public void onDisconnected() {
				Log.d("SyncServer","Disconnected");
			}

			@Override
			public void onConnected() {
				Log.d("SyncServer","Connected");
			}
			// This listener will be notified when new subscription event will be received.
			// It can be either newly created object, info about the change in existing one,
			// or deletion info
		}, new SyncServerConnection.SubscriptionListener() {
			@Override
			public void onDeleted(String[] ids, Channel channel) {
			}

			@Override
			public void onChanged(ArrayList<DataChanges> changes, Channel channel) {
			}

			@Override
			public void onAdded(Data data, Channel channel) {
			}
		});


		syncServer.start();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_about) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void getData() {
		// Create data.get() params object, passing project and collection id
		final ParamsDataGet paramsDataGet = new ParamsDataGet("PROJECT_ID"," COLLECTION_ID", null);
		// Get only one data object, since we want only the newest one
		paramsDataGet.setLimit(1);
		// Set order to descending to download the newest one (default is ASC, which would download oldest one)
		paramsDataGet.setOrder("DESC");
		// Send params to Syncano server
		syncano.sendAsyncRequest(paramsDataGet, new SyncanoBase.Callback() {
			// Response handling
			@Override
			public void finished(Response response) {
				Data[] data = ((ResponseDataGet) response).getData();
				if (response.getResultCode() != Response.CODE_SUCCESS) {
					// Error handling code
				} else if (data.length == 0) {
					// There was no data to be downloaded from server
				} else {
					// Received response, you can use 'data' object now
				}
			}
		});
	}


	public class IOUtil {

		public byte[] readFile(String file) throws IOException {
			return readFile(new File(file));
		}

		public byte[] readFile(File file) throws IOException {
			// Open file
			RandomAccessFile f = new RandomAccessFile(file, "r");
			try {
				// Get and check length
				long longlength = f.length();
				int length = (int) longlength;
				if (length != longlength)
					throw new IOException("File size <= 2 GB");
				// Read file and return data
				byte[] data = new byte[length];
				f.readFully(data);
				return data;
			} finally {
				f.close();
			}
		}
	}


}
