package edu.cnm.deepdive.abq_film_tour.controller;


import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.cloudinary.android.MediaManager;
import edu.cnm.deepdive.abq_film_tour.FilmTourApplication;
import edu.cnm.deepdive.abq_film_tour.R;
import edu.cnm.deepdive.abq_film_tour.controller.LocationActivity;
import edu.cnm.deepdive.abq_film_tour.model.entity.FilmLocation;
import edu.cnm.deepdive.abq_film_tour.model.entity.Image;
import edu.cnm.deepdive.abq_film_tour.model.entity.Production;
import java.io.IOException;
import java.util.UUID;
import retrofit2.Call;
import retrofit2.Response;


public class SubmitImageDialog extends DialogFragment implements View.OnClickListener {

  private static final int RESULT_LOAD_IMAGE = 1;

  ImageView userUploadImage;

  Button userImageButton, sendImageButton;

  private FilmTourApplication filmTourApplication;
  private LocationActivity parentActivity;
  FilmLocation location;
  Production production;

  private String token;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    filmTourApplication = (FilmTourApplication) getActivity().getApplication();
    parentActivity = (LocationActivity) getActivity();
    location = parentActivity.getLocation();
    production = parentActivity.getProduction();

    token = getString(R.string.oauth2_header, filmTourApplication.getAccount().getIdToken());
    View view = inflater.inflate(R.layout.submit_image_fragment, null, false);

    location = parentActivity.getLocation();

    userUploadImage = view.findViewById(R.id.user_upload_image);
    userImageButton = view.findViewById(R.id.user_image_button);
    sendImageButton = view.findViewById(R.id.send_image_button);

    userImageButton.setOnClickListener(this);
    sendImageButton.setOnClickListener(this);

    return view;
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null){
      Uri selectedImage = data.getData();
      userUploadImage.setImageURI(selectedImage);
    }
  }

  @Override
  public void onResume() {
    super.onResume();

    ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
    params.width = LayoutParams.MATCH_PARENT;
    getDialog().getWindow().setAttributes((WindowManager.LayoutParams) params);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.user_image_button:
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
        break;
      case R.id.send_image_button:
        Bitmap image = ((BitmapDrawable) userUploadImage.getDrawable()).getBitmap();
        Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        new ImageUploadTask().execute(imageUri);
    }
  }



  private class ImageUploadTask extends AsyncTask<Uri, Void, Boolean> {

    //Note that this is only used to store a reference to an image in the database, it is not an
    //actual image.
    Image newImage;
    String url;
    String uniqueID;
    Uri localImage;

    @Override
    protected void onPreExecute() {
      newImage = new Image();
      newImage.setFilmLocation(location);
      newImage.setGoogleId(filmTourApplication.getAccount().getId());
    }

    @Override
    protected Boolean doInBackground(Uri... uris) {
      boolean successfulQuery = false;
      localImage = uris[0];
      Call<Image> call = filmTourApplication.getService().postImage(token, newImage, location.getId());
      try {
        Response<Image> response = call.execute();
        if (response.isSuccessful()) {
          successfulQuery = true;
        } else {
          System.out.println(response.code());
        }
      } catch (IOException e) {
        //TODO Improve exception handling
      }
      return successfulQuery;
    }

    @Override
    protected void onPostExecute(Boolean successfulQuery) {
      if (successfulQuery) {
        //TODO change to submitted image
        String requestId = MediaManager.get().upload(localImage)
            .unsigned("jgg1ktxp") //Does this need to be hidden?
            .option("tags", new String[] {location.getSiteName(), production.getTitle()})
            .dispatch();
//            .option("tags", production.getTitle())
//            .option("tags", newImage.getId()) //Only storing the ID here for easy reference
        //TODO Get image URL
        //TODO set up listener service and callback interface to check for progress of uploads
        Toast.makeText(parentActivity, "image uploaded", Toast.LENGTH_LONG).show();
        dismiss();
      } else {
        Toast.makeText(parentActivity, "Failed", Toast.LENGTH_LONG).show();
      }
    }
  }
}