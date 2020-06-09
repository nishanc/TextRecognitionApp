package com.nishan.textrecognitionapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.fonts.FontFamily;
import android.os.Build;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private Button captureImageButton, detectTextButton, convertToSpeech;
    private ImageView imageView;
    private TextView textView;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap imageBitmap;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        captureImageButton = findViewById(R.id.capture_image);
        detectTextButton = findViewById(R.id.detect_image);
        convertToSpeech = findViewById(R.id.speak);
        imageView = findViewById(R.id.image_view);
        textView = findViewById(R.id.text_display);

        captureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
                textView.setText("");
            }
        });

        detectTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectTextFromImage();
            }
        });

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    //Select language
                int lang = textToSpeech.setLanguage(Locale.ENGLISH);
                }
            }
        });

        convertToSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textToConvert = textView.getText().toString();
                int speech;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    speech = textToSpeech.speak(textToConvert,TextToSpeech.QUEUE_FLUSH,null,null);
                } else {
                    speech = textToSpeech.speak(textToConvert, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });
    }


    // Take a photo with a camera app
    // https://developer.android.com/training/camera/photobasics#TaskCaptureIntent
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    // https://developer.android.com/training/camera/photobasics#TaskPhotoView
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
        }
    }

    private void detectTextFromImage() {
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextDetector firebaseVisionTextDetector = FirebaseVision.getInstance().getVisionTextDetector();
        firebaseVisionTextDetector.detectInImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                displayTextFromImage(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Error occurred: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayTextFromImage(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.Block> blockList = firebaseVisionText.getBlocks();
        if (blockList.size() == 0){
            Toast.makeText(this, "There is no test to detect", Toast.LENGTH_SHORT).show();
        }
        else {
            String text = "";
            for(FirebaseVisionText.Block block : firebaseVisionText.getBlocks()){
                text += block.getText() + "\n";
            }
            textView.setText(text);
        }
    }
}