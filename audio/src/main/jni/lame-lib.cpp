#include <jni.h>
#include "libmp3lame/lame.h"

static lame_global_flags *glf = NULL;

extern "C"
JNIEXPORT jstring

JNICALL
Java_com_example_audio_util_LameUtil_version(JNIEnv *env, jclass type) {
    return env->NewStringUTF(get_lame_version());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_audio_util_LameUtil_init(JNIEnv *env, jclass type, jint inSampleRate,
              jint outChannel, jint outSampleRate, jint outBitrate, jint quality) {
    if (glf != NULL) {
        lame_close(glf);
        glf = NULL;
    }
    glf = lame_init();
    lame_set_in_samplerate(glf, inSampleRate);
    lame_set_num_channels(glf, outChannel);
    lame_set_out_samplerate(glf, outSampleRate);
    lame_set_brate(glf, outBitrate);
    lame_set_quality(glf, quality);
    lame_init_params(glf);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_audio_util_LameUtil_encode(JNIEnv *env, jclass type, jshortArray buffer_l_,
               jshortArray buffer_r_, jint samples, jbyteArray mp3buf_) {
    jshort *buffer_l = env->GetShortArrayElements(buffer_l_, NULL);
    jshort *buffer_r = env->GetShortArrayElements(buffer_r_, NULL);
    jbyte *mp3buf = env->GetByteArrayElements(mp3buf_, NULL);

    const jsize mp3buf_size = env->GetArrayLength(mp3buf_);

    int result = lame_encode_buffer(glf, buffer_l, buffer_r, samples, (u_char*)mp3buf, mp3buf_size);

    env->ReleaseShortArrayElements(buffer_l_, buffer_l, 0);
    env->ReleaseShortArrayElements(buffer_r_, buffer_r, 0);
    env->ReleaseByteArrayElements(mp3buf_, mp3buf, 0);

    return result;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_audio_util_LameUtil_flush(JNIEnv *env, jclass type, jbyteArray mp3buf_) {
    jbyte *mp3buf = env->GetByteArrayElements(mp3buf_, NULL);

    const jsize  mp3buf_size = env->GetArrayLength(mp3buf_);

    int result = lame_encode_flush(glf, (u_char*)mp3buf, mp3buf_size);

    env->ReleaseByteArrayElements(mp3buf_, mp3buf, 0);

    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_audio_util_LameUtil_close(JNIEnv *env, jclass type) {
    lame_close(glf);
    glf = NULL;
}


