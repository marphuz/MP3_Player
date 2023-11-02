package com.example.mp3_p;

import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.io.File;
import java.net.URL;
import java.util.*;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class MusicPlayerController implements Initializable {
    // Dichiarazioni dei componenti dell'interfaccia utente
    @FXML
    private Label MP3PLAYER;
    @FXML
    private Button Play, Pause, Reset, Previous, Next;
    @FXML
    private Slider VolumeSlider;
    @FXML
    AnchorPane Pane;
    @FXML
    ComboBox<String> Speed;

    // Dichiarazioni per la gestione dei file musicali
    private Media media;
    private MediaPlayer mediaPlayer;
    @FXML
    ProgressBar MusicBar;
    private int speeds[] = {0, 25, 50, 75, 100, 125, 150, 200};
    private boolean running;
    ArrayList<File> song;
    int SongIndex;
    Timer timer;
    TimerTask timerstask;
    Random shuffle;
    @FXML
    CheckBox AutoPlay, Shuffle;
    double end, current;

    // Metodo per abilitare la modalità di riproduzione casuale (Shuffle)
    public int Shuffle() {
        if (Shuffle.isSelected()) {
            shuffle = new Random();
            SongIndex = shuffle.nextInt(0, song.size());
        }
        return SongIndex;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inizializzazione della lista delle canzoni dalla directory specificata
        song = new ArrayList<>();
        File directory = new File("C:\\Users\\crews\\OneDrive\\Desktop\\Musica");
        File[] files = directory.listFiles();
        for (File file : files) { //file è una variabile temporanea che rappresenta ciascun oggetto all'interno di files
            song.add(file);
            SongIndex = Shuffle();
        }

        // Aggiunta delle velocità di riproduzione possibili alla ComboBox
        for (int x : speeds) {
            Speed.getItems().add((x) + " %");
            Speed.setOnAction(this::Speed); // imposta il gestore di eventi per l'oggetto Speed, in modo che quando si verifica un'azione specifica su Speed, il metodo Speed verrà chiamato per gestire quell'azione
            VolumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                    mediaPlayer.setVolume(VolumeSlider.getValue() * 0.01);
                }
            });
            while (true) {
                try {
                    media = new Media(song.get(SongIndex).toURI().toString());
                    mediaPlayer = new MediaPlayer(media);
                    MP3PLAYER.setText(song.get(SongIndex).getName());
                    break;
                } catch (Exception e) {
                    SongIndex++;
                }
            }
        }
    }

    // Metodo per avviare la riproduzione della canzone
    public void Play() {
        Speed(null);
        mediaPlayer.setVolume(VolumeSlider.getValue() * 0.01);
        mediaPlayer.play();
        startMusic();
    }

    // Metodo per mettere in pausa la riproduzione della canzone
    public void Pause() {
        mediaPlayer.pause();
        stopMusic();
    }

    // Metodo per ripartire dalla fine della canzone corrente
    public void Reset() {
        startMusic();
        mediaPlayer.seek(Duration.seconds(0));
        mediaPlayer.play();
    }

    // Metodo per passare alla canzone precedente
    public void Previous() {
        Shuffle();
        if (SongIndex > 0) {
            SongIndex--;
            mediaPlayer.stop();
            if (running) {
                stopMusic();
            }
            while (true) {
                try {
                    media = new Media(song.get(SongIndex).toURI().toString());
                    mediaPlayer = new MediaPlayer(media);
                    MP3PLAYER.setText(song.get(SongIndex).getName());
                    this.Play();
                    break;
                } catch (Exception e) {
                    SongIndex--;
                }
            }
        } else {
            SongIndex = 0;
            mediaPlayer.stop();
            while (true) {
                try {
                    if (running) {
                        stopMusic();
                    }
                    media = new Media(song.get(SongIndex).toURI().toString());
                    mediaPlayer = new MediaPlayer(media);
                    MP3PLAYER.setText(song.get(SongIndex).getName());
                    this.Play();
                    break;
                } catch (Exception e) {
                    SongIndex++;
                }
            }
        }
    }

    // Metodo per passare alla canzone successiva
    public void Next() {
        Shuffle();
        if (SongIndex < song.size() - 1) {
            SongIndex++;
            mediaPlayer.stop();
            if (running) {
                stopMusic();
            }
            while (true) {
                try {
                    media =  new Media(song.get(SongIndex).toURI().toString());
                    mediaPlayer = new MediaPlayer(media);
                    MP3PLAYER.setText(song.get(SongIndex).getName());
                    this.Play();
                    break;
                } catch (Exception e) {
                    SongIndex++;
                }
            }
        } else {
            SongIndex = 0;
            mediaPlayer.stop();
            if (running) {
                stopMusic();
            }
            while (true) {
                try {
                    media = new Media(song.get(SongIndex).toURI().toString());
                    mediaPlayer = new MediaPlayer(media);
                    MP3PLAYER.setText(song.get(SongIndex).getName());
                    this.Play();
                    break;
                } catch (Exception e) {
                    SongIndex++;
                }
            }
        }
    }

    // Metodo per impostare la velocità di riproduzione
    public void Speed(ActionEvent event) {
        if (Speed.getValue() == null) {
            mediaPlayer.setRate(1);
        } else {
            mediaPlayer.setRate(0.01 * Integer.parseInt(Speed.getValue().substring(0, Speed.getValue().length() - 2)));
        }
    }

    // Metodo per avviare la sincronizzazione della barra di avanzamento
    public void startMusic() {
        running = true;
        timer = new Timer();
        timerstask = new TimerTask() {
            @Override
            public void run() {
                current = mediaPlayer.getCurrentTime().toSeconds();
                end = media.getDuration().toSeconds();
                MusicBar.setProgress(current / end);
                if (current == end) {
                    AutoPlay();
                }
            }
        };
        timer.scheduleAtFixedRate(timerstask, 0, 1000);
    }

    // Metodo per fermare la sincronizzazione della barra di avanzamento
    public void stopMusic() {
        running = false;
        timer.cancel();
    }

    // Metodo per passare automaticamente alla prossima canzone
    public synchronized void AutoPlay() {
        if (AutoPlay.isSelected()) {
            mediaPlayer.stop();
            SongIndex++;
            while (true) {
                try {
                    media = new Media(song.get(SongIndex).toURI().toString());
                    mediaPlayer = new MediaPlayer(media);
                    MP3PLAYER.setText(song.get(SongIndex).getName());
                    break;
                } catch (Exception e) {
                    SongIndex++;
                }
            }
            Play();
        }
    }

    // Metodo per gestire il click sulla barra di avanzamento
    public void setProgress(MouseEvent e) {
        System.out.println(e.getX());
    }
}
