package com.hola360.backgroundvideorecoder.ui.record.audio.utils;

import android.media.AudioFormat;
import android.os.Environment;

import java.io.Serializable;
import java.util.Locale;


public class RecordConfig implements Serializable {


    private RecordFormat format = RecordFormat.WAV;
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int encodingConfig = AudioFormat.ENCODING_PCM_16BIT;
    private int sampleRate = 16000;

    private String recordDir = String.format(Locale.getDefault(),
            "%s/Record/",
            Environment.getExternalStorageDirectory().getAbsolutePath());

    public RecordConfig() {
    }

    public RecordConfig(RecordFormat format) {
        this.format = format;
    }

    public RecordConfig(RecordFormat format, int channelConfig, int encodingConfig, int sampleRate) {
        this.format = format;
        this.channelConfig = channelConfig;
        this.encodingConfig = encodingConfig;
        this.sampleRate = sampleRate;
    }


    public String getRecordDir() {
        return recordDir;
    }

    public void setRecordDir(String recordDir) {
        this.recordDir = recordDir;
    }

    public int getEncoding() {
        if(format == RecordFormat.MP3){//mp3后期转换
            return 16;
        }

        if (encodingConfig == AudioFormat.ENCODING_PCM_8BIT) {
            return 8;
        } else if (encodingConfig == AudioFormat.ENCODING_PCM_16BIT) {
            return 16;
        } else {
            return 0;
        }
    }

    public int getRealEncoding() {
        if (encodingConfig == AudioFormat.ENCODING_PCM_8BIT) {
            return 8;
        } else if (encodingConfig == AudioFormat.ENCODING_PCM_16BIT) {
            return 16;
        } else {
            return 0;
        }
    }

    public int getChannelCount() {
        if (channelConfig == AudioFormat.CHANNEL_IN_MONO) {
            return 1;
        } else if (channelConfig == AudioFormat.CHANNEL_IN_STEREO) {
            return 2;
        } else {
            return 0;
        }
    }

    //get&set

    public RecordFormat getFormat() {
        return format;
    }

    public RecordConfig setFormat(RecordFormat format) {
        this.format = format;
        return this;
    }

    public int getChannelConfig() {
        return channelConfig;
    }

    public RecordConfig setChannelConfig(int channelConfig) {
        this.channelConfig = channelConfig;
        return this;
    }

    public int getEncodingConfig() {
        if(format == RecordFormat.MP3){
            return AudioFormat.ENCODING_PCM_16BIT;
        }
        return encodingConfig;
    }

    public RecordConfig setEncodingConfig(int encodingConfig) {
        this.encodingConfig = encodingConfig;
        return this;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public RecordConfig setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
        return this;
    }


    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "录制格式： %s,采样率：%sHz,位宽：%s bit,声道数：%s", format, sampleRate, getEncoding(), getChannelCount());
    }

    public enum RecordFormat {
        MP3(".mp3"),
        WAV(".wav"),
        PCM(".pcm");

        private String extension;

        public String getExtension() {
            return extension;
        }

        RecordFormat(String extension) {
            this.extension = extension;
        }
    }
}
