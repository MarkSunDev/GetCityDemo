package com.home.mark.getcitydemo;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.home.mark.getcitydemo.Util.GetCityNameByLocation;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private String mCurrentPhotoPath;
    private static final int CODE_REQUEST_CAMERA_PHOTO = 200;
    private static final int CODE_REQUEST_CROP_PHOTO = 201;
    private static final int CODE_REQUEST_PICTURE = 202;
    private static final int CODE_REQUEST_CROP_PICTURE = 203;
    private ImageView mPhotoImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPromiss();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final TextView cityNameTv = (TextView) findViewById(R.id.tv_city_name);

        Button button = (Button) findViewById(R.id.btn_get_city);
        assert button != null;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetCityNameByLocation.startLocation(MainActivity.this, true, new GetCityNameByLocation.CallBack() {
                    @Override
                    public void onGetLocaltionSuccess(String cityName) {
                        assert cityNameTv != null;
                        cityNameTv.setText(cityName);
                    }

                    @Override
                    public void onGetLocaltionFail(GetCityNameByLocation.LocErrorType type) {
                        cityNameTv.setText("定位失败");
                    }
                });
            }

    });
        Button cameraBtn = (Button) findViewById(R.id.btn_camera_photo);
        assert cameraBtn != null;
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                mCurrentPhotoPath = System.currentTimeMillis() + ".jpg";
//                intent.putExtra(MediaStore.EXTRA_OUTPUT,
//                        Uri.fromFile(new File(Environment.getExternalStorageDirectory(), mCurrentPhotoPath)));
                startActivityForResult(intent, CODE_REQUEST_CAMERA_PHOTO);
            }
        });
        Button pictureBtn = (Button) findViewById(R.id.btn_picture);
        assert pictureBtn != null;
        pictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentFromGallery = new Intent();
                intentFromGallery.setType("image/*");//选择图片
                intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
                mCurrentPhotoPath = System.currentTimeMillis() + ".jpg";
                intentFromGallery.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(new File(Environment.getExternalStorageDirectory(), mCurrentPhotoPath)));
                startActivityForResult(intentFromGallery, CODE_REQUEST_PICTURE);
            }
        });

        mPhotoImg = (ImageView) findViewById(R.id.img_photo);


    }

    private void checkPromiss(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String permission1 = Manifest.permission.CAMERA;
            String permission2 = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            String permission3 = Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS;
            requestPermissions(new String[]{permission1,permission2,permission3}, 123);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CODE_REQUEST_CAMERA_PHOTO && resultCode == Activity.RESULT_OK){
            if(data == null) return;
//            File tempFile = new File(
//                    Environment.getExternalStorageDirectory(),
//                    mCurrentPhotoPath);
//            cropRawPhoto(Uri.fromFile(tempFile), 100, 100);
            cropRawPhoto(data.getData(), 200, 200);
        }else if(requestCode == CODE_REQUEST_CROP_PHOTO && resultCode == Activity.RESULT_OK){
            if(data != null ){
                Bitmap photo = data.getExtras().getParcelable("data");
                if(photo != null){
                    Log.e("", photo.getWidth() + "" + photo.getHeight());
                    mPhotoImg.setImageBitmap(photo);
                }
            }
            String path =  Environment.getExternalStorageDirectory().getPath() + File.separator + mCurrentPhotoPath;

        }else if(requestCode == CODE_REQUEST_PICTURE && resultCode == Activity.RESULT_OK){
            if(data == null) return;
            cropRawPhoto(data.getData(), 200, 200);
        }
    }

    /**
     * 裁剪原始的图片
     * @param uri
     * @param output_X 宽
     * @param output_Y 高
     */
    public void cropRawPhoto(Uri uri, int output_X, int output_Y) {
        if (uri == null) return;
        Intent intent = new Intent("com.android.camera.action.CROP");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            String url = getPath( this, uri);
            intent.setDataAndType(Uri.fromFile(new File(url)), "image/*");
        }else{
            intent.setDataAndType(uri, "image/*");
        }
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", output_X);
        intent.putExtra("outputY", output_Y);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", true);
//        mCurrentPhotoPath = "pz_" + mCurrentPhotoPath;
//        Uri uritempFile = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), mCurrentPhotoPath));
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, uritempFile);
        startActivityForResult(intent, CODE_REQUEST_CROP_PHOTO);
    }

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }


}
