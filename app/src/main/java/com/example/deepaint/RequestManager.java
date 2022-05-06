package com.example.deepaint;

import android.graphics.Bitmap;
import android.os.Environment;
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


    public static File sendDrawingRequest(Bitmap bitmapToConvert, String fileName) {
        File[] outFile = new File[1];
        try {
            new Thread(() -> {
                try {
                    String requestUrl = "https://2327-34-78-61-205.ngrok.io/drawings";
                    final OkHttpClient client = new OkHttpClient.Builder()
                            .build();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmapToConvert.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("img.png", fileName,
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
                            if (response.code() == 200) {
                                System.out.println("Request Success");
                                String fileNameNoExt = fileName.substring(0, fileName.lastIndexOf('.'));
                                String downloadPath = Environment.getExternalStorageDirectory().toString()
                                        + File.separator
                                        + "Download/" + fileNameNoExt + "_anime.png";
                                outFile[0] = new File(downloadPath);
                                BufferedSink sink = Okio.buffer(Okio.sink(outFile[0]));
                                sink.writeAll(response.body().source());
                                sink.close();
                            } else {
                                System.out.println("Error " + response.code());
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return outFile[0];
    }

    public static void sendDeepFillRequest(Bitmap d1, String fileName1, Bitmap d2, String fileName2) {
        System.out.println("Deep Fill request...");
        try {
            new Thread(() -> {
                try {
                    String requestUrl = "https://6803-34-123-211-158.ngrok.io//deepfill";
                    final OkHttpClient client = new OkHttpClient.Builder()
                            .build();
                    ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
                    d1.compress(Bitmap.CompressFormat.PNG, 100, stream1);
                    ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
                    d2.compress(Bitmap.CompressFormat.PNG, 100, stream2);
                    byte[] byteArray1 = stream1.toByteArray();
                    byte[] byteArray2 = stream2.toByteArray();
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("img.png", fileName1,
                                    RequestBody.create(MediaType.parse("image/png"), byteArray1))
                            .addFormDataPart("mask.png", fileName2,
                                    RequestBody.create(MediaType.parse("image/png"), byteArray2))
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
                            if (response.code() == 200) {
                                System.out.println("Request Success");
                                String fileNameNoExt = fileName1.substring(0, fileName1.lastIndexOf('.'));
                                String downloadPath = Environment.getExternalStorageDirectory().toString()
                                        + File.separator
                                        + "Download/" + fileNameNoExt + "_removed.png";
                                File downloadedFile = new File(downloadPath);
                                BufferedSink sink = Okio.buffer(Okio.sink(downloadedFile));
                                sink.writeAll(response.body().source());
                                sink.close();
                            } else {
                                System.out.println("Error " + response.code() + " " + response.message());
                            }
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
