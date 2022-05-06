package com.example.deepaint;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

public class RequestManager {


    public static void sendSegmentationRequest() {
        try {
            new Thread(() -> {
                try {
                    String requestUrl = "https://2327-34-78-61-205.ngrok.io//drawings";
                    final OkHttpClient client = new OkHttpClient.Builder()
                            .build();
                    File file = new File(Environment.getExternalStorageDirectory().toString()
                            + File.separator
                            + "Download/GokuBigMouth.png");
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] byteArray = stream.toByteArray();

                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("img.png", file.getName(),
                                    RequestBody.create(MediaType.parse("image/png"), byteArray))
                            .build();

                    Request request = new Request.Builder()
                            .url(requestUrl)
                            .post(requestBody)
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            System.out.println("Request Failed");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            System.out.println("Request Success");
                            String downloadPath = Environment.getExternalStorageDirectory().toString()
                                    + File.separator
                                    + "Download/download3.png";
                            File downloadedFile = new File(downloadPath);
                            BufferedSink sink = Okio.buffer(Okio.sink(downloadedFile));
                            sink.writeAll(response.body().source());
                            sink.close();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

                    /*
                    System.out.println("JAVA EXECUTION IN BACKGROUND");
                    String requestUrl = "https://f2b9-34-78-61-205.ngrok.io/drawings";
                    final OkHttpClient client = new OkHttpClient();
                    MediaType argbType = MediaType.parse("image/png");
                    // MediaType JSON = MediaType.parse("application/json;charset=utf-8");
                    File file = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "Download/GokuBigMouth.png");
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("image",
                                    file.getName(),
                                    RequestBody.create(argbType, file))
                            .build();
                    Request segmentationRequest = new Request.Builder()
                            .url(requestUrl).post(requestBody).build();
                    Response response = client.newCall(segmentationRequest).execute();
                    System.out.println("OKHTTP3: Request Success!");
                    System.out.println(response.body().string());
                    */
        // Response response = client.newCall(segmentationRequest).execute();
        /*
        System.out.println("SEND WAS EXECUTED IN JAVA");
        File img = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "Download/GokuBigMouth.png");
        //File mask = new File("C:\\Users\\alperen\\IdeaProjects\\untitled1\\src\\main\\resources\\style.jpeg");
        MultipartEntity entity = new MultipartEntity();

        entity.addPart

        HttpPost request = new HttpPost("http://f2b9-34-78-61-205.ngrok.io/drawings");
        request.setEntity(entity);
        HttpResponse response = null;
        String path;
        File downloadedFile = null;
        try{
        HttpClient client = HttpClientBuilder.create().build();
        response = client.execute(request);
        path = Environment.getExternalStorageDirectory().toString() + File.separator + "DCIM/"
                + "out.png";
        downloadedFile = new File(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            RequestManager.copyInputStreamToFile(response.getEntity().getContent(), downloadedFile);
        }
        finally {
            response.getEntity().getContent().close();
        }
        */

    /**
    * Copy an InputStream to a File.
     * @Deprecated
    */
    @Deprecated
    private static void copyInputStreamToFile(InputStream in, File file) {
        OutputStream out = null;

        try {
            out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            // Ensure that the InputStreams are closed even if there's an exception.
            try {
                if ( out != null ) {
                    out.close();
                }

                // If you want to close the "in" InputStream yourself then remove this
                // from here but ensure that you close it yourself eventually.
                in.close();
            }
            catch ( IOException e ) {
                e.printStackTrace();
            }
        }
    }

}
