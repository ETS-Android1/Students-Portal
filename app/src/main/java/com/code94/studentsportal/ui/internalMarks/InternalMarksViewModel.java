package com.code94.studentsportal.ui.internalMarks;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class InternalMarksViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public InternalMarksViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is marks fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}