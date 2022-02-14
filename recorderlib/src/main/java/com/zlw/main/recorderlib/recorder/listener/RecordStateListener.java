package com.zlw.main.recorderlib.recorder.listener;


import com.zlw.main.recorderlib.recorder.RecordHelper;

public interface RecordStateListener {

    void onStateChange(RecordHelper.RecordState state);

    void onError(String error);

}
