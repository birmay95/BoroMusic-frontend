#include <stdio.h>
#include <jni.h>

JNIEXPORT jstring JNICALL
Java_com_example_musicplatform_Utils_00024Companion_formatTime(JNIEnv *env, jobject thiz, jlong seconds) {
    if(seconds == 1000000)
        return (*env)->NewStringUTF(env, "00:00");

    int minutes = seconds / 60;
    int remainingSeconds = seconds % 60;

    char formattedTime[6];
    snprintf(formattedTime, sizeof(formattedTime), "%02d:%02d", minutes, remainingSeconds);
    return (*env)->NewStringUTF(env, formattedTime);
}