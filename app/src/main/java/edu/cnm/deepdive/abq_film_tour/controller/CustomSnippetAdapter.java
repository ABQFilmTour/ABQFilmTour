package edu.cnm.deepdive.abq_film_tour.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import edu.cnm.deepdive.abq_film_tour.R;
import org.w3c.dom.Text;

public class CustomSnippetAdapter implements GoogleMap.InfoWindowAdapter {

  private final View mSnippet;
  private Context context;

  public CustomSnippetAdapter(Context context) {
    context = context;
    mSnippet = LayoutInflater.from(context).inflate(R.layout.custom_info_snippet, null);
  }

  private void renderSnippetText(Marker marker, View view){
    String title = marker.getTitle();
    TextView snippetTitle = (TextView) view.findViewById(R.id.snippet_title);

    if (!title.equals("")){
      snippetTitle.setText(title);
    }
    String snippet = marker.getTitle();
    TextView snippetDescription = (TextView) view.findViewById(R.id.snippet_description);

    if (!snippet.equals("")) {
      snippetDescription.setText(snippet);
    }
  }


  @Override
  public View getInfoWindow(Marker marker) {
    renderSnippetText(marker, mSnippet);
    return null;
  }

  @Override
  public View getInfoContents(Marker marker) {
    renderSnippetText(marker, mSnippet);
    return null;
  }
}
