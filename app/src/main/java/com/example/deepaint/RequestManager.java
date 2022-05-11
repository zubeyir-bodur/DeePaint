package com.example.deepaint;

import android.graphics.Bitmap;
import android.os.Environment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
    public static void sendDrawingRequest(Bitmap bitmapToConvert, String fileName) {
        try {
            new Thread(() -> {
                try {
                    String requestUrl = "https://f375-34-91-214-233.ngrok.io/drawings";
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
                                File outFile;
                                System.out.println("Request Success");
                                String fileNameNoExt = fileName.substring(0, fileName.lastIndexOf('.'));
                                String downloadPath = Environment.getExternalStorageDirectory().toString()
                                        + File.separator
                                        + "Download/" + fileNameNoExt + "_anime.png";
                                outFile = new File(downloadPath);
                                BufferedSink sink = Okio.buffer(Okio.sink(outFile));
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
    }

    public static void sendDeepFillRequest(Bitmap d1, String fileName1, Bitmap d2, String fileName2) {
        System.out.println("Deep Fill request...");
        try {
            new Thread(() -> {
                try {
                    String requestUrl = "https://608f-34-83-212-48.ngrok.io/deepfill";
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

    public static void sendSegmentationRequest(Bitmap bitmapToSegment, String fileName) {
        System.out.println("Segmentation clicked in java");
        try {
            new Thread(() -> {
                try {
                    String requestUrl = "https://7ab6-35-221-228-167.ngrok.io/segmentate";
                    final OkHttpClient client = new OkHttpClient.Builder()
                            .build();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmapToSegment.compress(Bitmap.CompressFormat.JPEG, 100, stream);
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
                                        + "Download/" + fileNameNoExt + "_segmentation.zip";
                                File file = new File(downloadPath);
                                BufferedSink sink = Okio.buffer(Okio.sink(file));
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

    public static void sendAutoRemoveRequest(int labelNo, Bitmap mask, Bitmap mapWithImg, String fileNameNoExt) {
        System.out.println("Auto Fill Request...");
        try {
            new Thread(() -> {
                try {
                    String requestUrl = "https://608f-34-83-212-48.ngrok.io/deepfillauto";
                    final OkHttpClient client = new OkHttpClient.Builder()
                            .build();
                    ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
                    mask.compress(Bitmap.CompressFormat.PNG, 100, stream1);
                    ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
                    mapWithImg.compress(Bitmap.CompressFormat.PNG, 100, stream2);
                    byte[] byteArray1 = stream1.toByteArray();
                    byte[] byteArray2 = stream2.toByteArray();
                    String fileName1 = fileNameNoExt + "_pred_masks.png";
                    String fileName2 = fileNameNoExt + "_pred.png";
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("img.png", fileName1,
                                    RequestBody.create(MediaType.parse("image/png"), byteArray2))
                            .addFormDataPart("mask.png", fileName2,
                                    RequestBody.create(MediaType.parse("image/png"), byteArray1))
                            .addFormDataPart("label.json", fileNameNoExt + ".json",
                                    RequestBody.create(MediaType.parse("application-type/json"), "{\n" +
                                            "  \"labelNo\": " + labelNo + " \n" +
                                            "}"))
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
                                String downloadPath = Environment.getExternalStorageDirectory().toString()
                                        + File.separator
                                        + "Download/" + fileNameNoExt + "_auto_removed.png";
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

    public static void sendStyleRequest(Bitmap bitmapTarget, Bitmap bitmapStyle, String fileNameTarget, String fileNameStyle) {
        System.out.println("Style request...");
        try {
            new Thread(() -> {
                try {
                    String requestUrl = "https://5dcc-34-122-135-47.ngrok.io/style_transfer";
                    final OkHttpClient client = new OkHttpClient.Builder()
                            .build();
                    ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
                    bitmapTarget.compress(Bitmap.CompressFormat.PNG, 100, stream1);
                    ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
                    bitmapStyle.compress(Bitmap.CompressFormat.PNG, 100, stream2);
                    byte[] byteArray1 = stream1.toByteArray();
                    byte[] byteArray2 = stream2.toByteArray();
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("img.png", fileNameTarget,
                                    RequestBody.create(MediaType.parse("image/png"), byteArray1))
                            .addFormDataPart("style.png", fileNameStyle,
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
                                String fileNameNoExt = fileNameTarget.substring(0, fileNameTarget.lastIndexOf('.'));
                                String styleNameNoExt = fileNameStyle.substring(0, fileNameStyle.lastIndexOf('.'));
                                String downloadPath = Environment.getExternalStorageDirectory().toString()
                                        + File.separator
                                        + "Download/" + fileNameNoExt + "_styled_with" + styleNameNoExt + ".png";
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
}
