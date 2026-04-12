package tn.esprit.util;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioRecorder {
    private TargetDataLine targetDataLine;
    private AudioFormat audioFormat;
    private File audioFile;
    private boolean isRecording = false;

    public AudioRecorder() {
        audioFormat = getAudioFormat();
    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 16000;
        int sampleSizeInBits = 16;
        int channels = 1; // Mono
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    public void startRecording(String filePath) {
        try {
            audioFile = new File(filePath);
            File parent = audioFile.getParentFile();
            if(parent != null && !parent.exists()){ 
                parent.mkdirs(); 
            }
            
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
            if (!AudioSystem.isLineSupported(dataLineInfo)) {
                System.out.println("Audio Line not supported.");
                return;
            }
            
            targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
            targetDataLine.open(audioFormat);
            targetDataLine.start();
            isRecording = true;

            Thread captureThread = new Thread(() -> {
                try {
                    AudioInputStream audioInputStream = new AudioInputStream(targetDataLine);
                    AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, audioFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            captureThread.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        if (targetDataLine != null && isRecording) {
            targetDataLine.stop();
            targetDataLine.close();
            isRecording = false;
        }
    }
    
    public boolean isRecording() {
        return isRecording;
    }
}
