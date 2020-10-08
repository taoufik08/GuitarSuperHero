package gsh.main;

import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import gsh.processing.AudioDispatcher;
import gsh.processing.AudioEvent;
import gsh.processing.EncapsAudioInputStream;
import gsh.processing.PitchDetectionHandler;
import gsh.processing.PitchDetectionResult;
import gsh.processing.PitchProcessor;
import gsh.processing.PitchProcessor.PitchEstimationAlgorithm;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class GuitarApp extends Application implements PitchDetectionHandler{
	
	
	private AudioDispatcher dispatcher;
	private Mixer currentMixer, newMixer;
	private PitchEstimationAlgorithm algo;
	private TargetDataLine line;
	
	private VBox vbox;
	private HBox hbox;
	private VBox vbox1;
	private VBox vbox2;
	private StackPane root;
	
	private Label title;
	private Label statusLabel;
	private TextArea notesLabel;
	private Label freq;
	Button start;
	Button stop;
	
	private String message = new String();
	private String message2 = "";
	private String E ="";
	private String A ="";
	private String D ="";
	private String G ="";
	private String F ="";
	private String E6 ="";
	

	public void start(Stage primaryStage) throws Exception {
		
		vbox = new VBox(35);
		vbox1 = new VBox(5);
		vbox2 = new VBox(5);
		hbox = new HBox(20);
		root = new StackPane();
		vbox.setAlignment(Pos.TOP_CENTER);
		vbox1.setAlignment(Pos.CENTER);
		vbox2.setAlignment(Pos.TOP_RIGHT);
		hbox.setAlignment(Pos.CENTER);
		ImageView btnStartIcon = new ImageView(getClass().getResource("/resources/images/start.png").toString());
		ImageView btnStopIcon = new ImageView(getClass().getResource("/resources/images/stop.png").toString());
		start = new Button("Start");
		stop = new Button("Stop");
		
		primaryStage.setTitle("Guitar Super Hero");
		primaryStage.setWidth(1100);
		primaryStage.setHeight(700);
		primaryStage.setResizable(true);
		
		ToggleGroup group = new ToggleGroup();
		ArrayList<RadioButton> btn = new ArrayList<RadioButton>();
		int i =0;
		
		title = new Label("Guitar Super Hero \n");
		vbox1.getChildren().add(title);
		
		for(Mixer.Info info : Shared.getMixerInfo(false, true)){
			
			RadioButton radioButton = new RadioButton(Shared.toLocalString(info));
			radioButton.setUserData(Shared.toLocalString(info));
			radioButton.setToggleGroup(group);
			radioButton.setTextFill(Color.WHITE);
			radioButton.setAlignment(Pos.TOP_LEFT);
			btn.add(radioButton);
			vbox1.getChildren().add(btn.get(i));
			
			group.selectedToggleProperty().addListener((obserableValue, old_toggle, new_toggle) -> {
			    
				if (group.getSelectedToggle() != null) 
			    {
			    	if (group.getSelectedToggle().getUserData().toString().equals(info.toString())) {
			    		Mixer newValue = AudioSystem.getMixer(info);
			    		System.out.println(newValue);
			    		newMixer = newValue;
					}
			    }
			});
			
			i++;
			
		}
		  
		start.setGraphic(btnStartIcon);
		start.setStyle("-fx-background-color: #fbfbfb; ");
		start.setOnAction(e-> {
			try {
				setNewMixer(newMixer);
				start.setStyle("-fx-background-color: #65fbd2; ");
				stop.setStyle("-fx-background-color: #fbfbfb; ");
			} catch (LineUnavailableException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (UnsupportedAudioFileException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		
		stop.setGraphic(btnStopIcon);
		stop.setStyle("-fx-background-color: #fbfbfb; ");
		stop.setOnAction(e-> {
			
				line.stop();
				stop.setStyle("-fx-background-color: #f97070; ");
				start.setStyle("-fx-background-color: #fbfbfb; ");
			
		});
		
		notesLabel = new TextArea("Partition");
		notesLabel.setId("notesLabel");
		notesLabel.setOpacity(0.5);
		statusLabel = new Label("Mic : ????");
		freq = new Label("Hz");
		hbox.getChildren().addAll(start,stop);
		vbox2.getChildren().addAll(statusLabel,freq,notesLabel);
		vbox.getChildren().addAll(vbox1,hbox,vbox2);
		root.getChildren().add(vbox);
		
		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("stylesheet.css").toString());
		
		primaryStage.setScene(scene);
		primaryStage.show();
		primaryStage.centerOnScreen();
	}


	
	private void setNewMixer(Mixer mixer) throws LineUnavailableException,
			UnsupportedAudioFileException {
		
		if(dispatcher!= null)
		{
			dispatcher.stop();
		}
		currentMixer = mixer;
		algo = PitchEstimationAlgorithm.YIN;
		
		float sampleRate = 44100;
		int bufferSize = 1024;
		int overlap = 0;
		//"Started listening with " + 
		statusLabel.setText("Mic: "+Shared.toLocalString(mixer.getMixerInfo().getName()) + "\n");

		final AudioFormat format = new AudioFormat(sampleRate, 16, 1, true,
				true);
		final DataLine.Info dataLineInfo = new DataLine.Info(
				TargetDataLine.class, format);
		
		line = (TargetDataLine) mixer.getLine(dataLineInfo);
		final int numberOfSamples = bufferSize;
		line.open(format, numberOfSamples);
		line.start();
		final AudioInputStream stream = new AudioInputStream(line);

		EncapsAudioInputStream audioStream = new EncapsAudioInputStream(stream);
		// create a new dispatcher
		dispatcher = new AudioDispatcher(audioStream, bufferSize,
				overlap);

		// add a processor
		dispatcher.addAudioProcessor(new PitchProcessor(algo, sampleRate, bufferSize, this));
		
		new Thread(dispatcher,"Audio dispatching").start();
		System.out.println(Thread.currentThread().getName());
	}
	
	public static void main(String[] args) {
		launch(args);
	}


	@Override
	public void handlePitch(PitchDetectionResult pitchDetectionResult,AudioEvent audioEvent) {
		if(pitchDetectionResult.getPitch() != -1){
			double timeStamp = audioEvent.getTimeStamp();
			float pitch = pitchDetectionResult.getPitch();
			float probability = pitchDetectionResult.getProbability();
			double rms = audioEvent.getRMS() * 100;
			//String message = String.format("Pitch detected at %.2fs: %.2fHz ( %.2f probability, RMS: %.5f )\n", timeStamp,pitch,probability,rms);
			
			if(149 <= pitch & pitch <= 155) {

            	E += "---E";
            	A += "----";
            	D += "----";
            	G += "----";
            	F += "----";
            	E6 += "----";

            	message = String.format(E +"\n" + A +"\n" + D +"\n" + G +"\n" + F +"\n" + E6) ;
            	message2 = String.format("Note E detected at %.2fs: %.2fHz ( %.2f probability)\n", timeStamp,pitch,probability);

			}else if(101 <= pitch & pitch <= 108) {

            	E += "----";
            	A += "---A";
            	D += "----";
            	G += "----";
            	F += "----";
            	E6 += "----";

            	message = String.format(E +"\n" + A +"\n" + D +"\n" + G +"\n" + F +"\n" + E6) ;
            	message2 = String.format("Note A detected at %.2fs: %.2fHz ( %.2f probability)\n", timeStamp,pitch,probability);

			}else if(133 <= pitch & pitch <= 137) {
				E += "----";
            	A += "----";
            	D += "---D";
            	G += "----";
            	F += "----";
            	E6 += "----";
         
            	message = String.format(E +"\n" + A +"\n" + D +"\n" + G +"\n" + F +"\n" + E6) ;
            	message2 = String.format("Note D detected at %.2fs: %.2fHz ( %.2f probability)\n", timeStamp,pitch,probability);
	 
			}else if(182 <= pitch & pitch <= 188) {

            	E += "----";
            	A += "----";
            	D += "----";
            	G += "---G";
            	F += "----";
            	E6 += "----";
      
            	message = String.format(E +"\n" + A +"\n" + D +"\n" + G +"\n" + F +"\n" + E6) ;
            	message2 = String.format("Note G detected at %.2fs: %.2fHz ( %.2f probability)\n", timeStamp,pitch,probability);

			}else if(232 <= pitch & pitch <= 238) {

            	E += "----";
            	A += "----";
            	D += "----";
            	G += "----";
            	F += "---F";
            	E6 += "----";
            	
            	message = String.format(E +"\n" + A +"\n" + D +"\n" + G +"\n" + F +"\n" + E6) ;
            	message2 = String.format("Note F detected at %.2fs: %.2fHz ( %.2f probability)\n", timeStamp,pitch,probability);
	
			}else if(310 <= pitch & pitch <= 315) {

            	E += "----";
            	A += "----";
            	D += "----";
            	G += "----";
            	F += "----";
            	E6 += "---E";
	
            	message = String.format(E +"\n" + A +"\n" + D +"\n" + G +"\n" + F +"\n" + E6) ;
            	message2 = String.format("Note E6 detected at %.2fs: %.2fHz ( %.2f probability)\n", timeStamp,pitch,probability);

			}
			
			Platform.runLater(new Runnable() {
				
				@Override
				public void run() {
					notesLabel.setText("");
					notesLabel.appendText(message);
					notesLabel.setScrollLeft(Double.MAX_VALUE);
					freq.setText(message2);
				}
			});
			
			
		}
	}
	
}


