package com.appsubaruod.comicviewer.utils.messages;

import android.content.Intent;

/**
 * Created by s-yamada on 2017/03/04.
 */

public class RequestActivityIntentEvent {

    private Intent mIntent;
    private int mCode;

    public RequestActivityIntentEvent(Intent intent, int code) {
        mIntent = intent;
        mCode = code;
    }

    public Intent getIntent() {
        return mIntent;
    }

    public int getCode() {
        return mCode;
    }

}
