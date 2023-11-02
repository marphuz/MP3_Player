module com.example.mp3_p {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;


    opens com.example.mp3_p to javafx.fxml;
    exports com.example.mp3_p;
}